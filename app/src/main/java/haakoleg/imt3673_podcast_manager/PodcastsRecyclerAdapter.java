package haakoleg.imt3673_podcast_manager;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;

public class PodcastsRecyclerAdapter extends RecyclerView.Adapter<PodcastsRecyclerAdapter.ViewHolder> {
    private Fragment fragment;
    private List<Podcast> podcasts;
    private List<Integer> subscriberCounts;
    private OnPodcastClickListener listener;

    public PodcastsRecyclerAdapter(
            Fragment fragment, List<Podcast> podcasts, List<Integer> subscriberCounts, OnPodcastClickListener listener) {
        this.fragment = fragment;
        this.podcasts = podcasts;
        this.subscriberCounts = subscriberCounts;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_podcast_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Podcast podcast = podcasts.get(position);

        Glide.with(fragment)
                .load(podcast.getImage())
                .transition(withCrossFade())
                .apply(new RequestOptions().centerCrop())
                .into(holder.podcastImg);

        holder.podcastTitleTxt.setText(podcast.getTitle());
        holder.podcastCategoryTxt.setText(podcast.getCategory());
        holder.podcastSubscribersTxt.setText(Integer.toString(subscriberCounts.get(position)));
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView podcastImg;
        TextView podcastTitleTxt;
        TextView podcastCategoryTxt;
        RatingBar podcastRating;
        TextView podcastSubscribersTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            podcastImg = itemView.findViewById(R.id.podcast_img);
            podcastTitleTxt = itemView.findViewById(R.id.podcast_title_txt);
            podcastCategoryTxt = itemView.findViewById(R.id.podcast_category_txt);
            podcastRating = itemView.findViewById(R.id.podcast_rating);
            podcastSubscribersTxt = itemView.findViewById(R.id.podcast_subscribers_txt);

            // Set click listener for item
            itemView.setOnClickListener(v -> {
                Podcast podcast = podcasts.get(getAdapterPosition());
                int subscribers = subscriberCounts.get(getAdapterPosition());
                listener.onPodcastClicked(podcast, subscribers);
            });
        }
    }

    public interface OnPodcastClickListener {
        void onPodcastClicked(Podcast podcast, int subscribers);
    }
}
