package haakoleg.imt3673_podcast_manager;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import java.util.HashMap;
import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

/**
 * Adapter used for the RecyclerView in ShowEpisodesFragment
 */

public class EpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodesRecyclerAdapter.ViewHolder> {
    private Fragment fragment;
    private OnEpisodeClickListener listener;
    private HashMap<String, Podcast> podcasts;
    private List<PodcastEpisode> episodes;

    /**
     * Adapter constructor
     * @param fragment Reference to the fragment that this adapter is being used in, needed by Glide
     * @param podcasts List of podcasts that the episodes belong to
     * @param episodes List of episodes to display in the RecyclerView
     */
    public EpisodesRecyclerAdapter(Fragment fragment, OnEpisodeClickListener listener, List<Podcast> podcasts, List<PodcastEpisode> episodes) {
        this.fragment = fragment;
        this.listener = listener;
        this.podcasts = new HashMap<>();
        for (Podcast podcast : podcasts) {
            this.podcasts.put(podcast.getUrl(), podcast);
        }
        this.episodes = episodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_episode_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PodcastEpisode episode = episodes.get(position);
        Podcast podcast = podcasts.get(episode.getParentUrl());

        // Load podcast image
        Glide.with(fragment)
                .load(podcast.getImage())
                .transition(withCrossFade())
                .into(holder.img);

        holder.titleTxt.setText(episode.getTitle());
        holder.descTxt.setText(episode.getDescription());
        holder.podcastTxt.setText(podcast.getTitle());

        // TODO: Format duration
        holder.durationTxt.setText(Integer.toString(episode.getDuration()));
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    /**
     * ViewHolder for layout "view_episode_item"
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView titleTxt;
        TextView descTxt;
        TextView podcastTxt;
        TextView durationTxt;

        ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.episode_img);
            titleTxt = itemView.findViewById(R.id.episode_title_txt);
            descTxt = itemView.findViewById(R.id.episode_desc_txt);
            podcastTxt = itemView.findViewById(R.id.episode_podcast_txt);
            durationTxt = itemView.findViewById(R.id.episode_duration_txt);

            // Set onclick listener for this episode item
            itemView.setOnClickListener(v -> {
                PodcastEpisode episode = episodes.get(getAdapterPosition());
                Podcast podcast = podcasts.get(episode.getParentUrl());
                listener.onEpisodeClicked(episode, podcast);
            });
        }
    }

    public interface OnEpisodeClickListener {
        void onEpisodeClicked(PodcastEpisode episode, Podcast podcast);
    }
}
