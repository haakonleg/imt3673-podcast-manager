package haakoleg.imt3673_podcast_manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.utils.Messages;

/**
 * Adapter for the RecyclerView used in ManagePodcastsFragment. Displays the users podcasts
 * along with a delete button so that the user can delete podcasts from the list.
 */

public class ManagePodcastsRecyclerAdapter extends RecyclerView.Adapter<ManagePodcastsRecyclerAdapter.PodcastHolder> {
    private final Context context;
    private final List<Podcast> podcasts;
    private final ManagePodcastsListener listener;

    public ManagePodcastsRecyclerAdapter(Context context, List<Podcast> podcasts, ManagePodcastsListener listener) {
        this.context = context;
        this.podcasts = podcasts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PodcastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_podcast_manage_item, parent, false);
        return new PodcastHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastHolder holder, int position) {
        Podcast podcast = podcasts.get(position);
        holder.podcastTitle.setText(podcast.getTitle());
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    private void deletePodcast(Podcast podcast, int position) {
        // Ask if the user is sure before deleting the podcast
        Messages.showConfirmationDialog(context, podcast.getTitle(), context.getString(R.string.are_you_sure_remove), (dialog, which) -> {
            podcasts.remove(position);
            notifyItemRemoved(position);
            listener.onPodcastRemoved(podcast);
        });
    }

    class PodcastHolder extends RecyclerView.ViewHolder {
        final TextView podcastTitle;
        final ImageView removeBtn;

        @SuppressLint("ClickableViewAccessibility")
        PodcastHolder(View itemView) {
            super(itemView);
            podcastTitle = itemView.findViewById(R.id.podcast_title_txt);
            removeBtn = itemView.findViewById(R.id.manage_remove_btn);

            removeBtn.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                deletePodcast(podcasts.get(pos), pos);
            });
        }
    }

    public interface ManagePodcastsListener {
        void onPodcastRemoved(Podcast podcast);
    }
}
