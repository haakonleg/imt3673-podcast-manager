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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;

public class PodcastsRecyclerAdapter extends RecyclerView.Adapter<PodcastsRecyclerAdapter.ViewHolder> {
    private final Fragment fragment;
    private final List<PodcastObject> podcasts;
    private final OnPodcastClickListener listener;

    public PodcastsRecyclerAdapter(
            Fragment fragment, List<Podcast> podcasts, List<Integer> subscriberCounts, List<Integer> ratings, OnPodcastClickListener listener) {
        this.fragment = fragment;
        this.podcasts = new ArrayList<>();
        for (int i = 0; i < podcasts.size(); i++) {
            this.podcasts.add(new PodcastObject(podcasts.get(i), subscriberCounts.get(i), ratings.get(i)));
        }
        this.listener = listener;
        sortByPopularity();
    }

    public void sortByPopularity() {
        Collections.sort(this.podcasts, (o1, o2) -> o2.subscribers - o1.subscribers);
        notifyDataSetChanged();
    }

    public void sortByRating() {
        Collections.sort(this.podcasts, (o1, o2) -> o2.rating - o1.rating);
        notifyDataSetChanged();
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
        PodcastObject obj = podcasts.get(position);

        Glide.with(fragment)
                .load(obj.podcast.getImage())
                .transition(withCrossFade())
                .apply(new RequestOptions().centerCrop())
                .into(holder.podcastImg);

        holder.podcastTitleTxt.setText(obj.podcast.getTitle());
        holder.podcastCategoryTxt.setText(obj.podcast.getCategory());
        holder.podcastRating.setRating((float) obj.rating);
        holder.podcastSubscribersTxt.setText(Integer.toString(obj.subscribers));
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView podcastImg;
        final TextView podcastTitleTxt;
        final TextView podcastCategoryTxt;
        final RatingBar podcastRating;
        final TextView podcastSubscribersTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            podcastImg = itemView.findViewById(R.id.podcast_img);
            podcastTitleTxt = itemView.findViewById(R.id.podcast_title_txt);
            podcastCategoryTxt = itemView.findViewById(R.id.podcast_category_txt);
            podcastRating = itemView.findViewById(R.id.podcast_rating);
            podcastSubscribersTxt = itemView.findViewById(R.id.podcast_subscribers_txt);

            // Set click listener for item
            itemView.setOnClickListener(v -> {
                PodcastObject obj = podcasts.get(getAdapterPosition());
                listener.onPodcastClicked(obj.podcast, obj.subscribers);
            });
        }
    }

    private class PodcastObject {
        private Podcast podcast;
        private int subscribers;
        private int rating;

        public PodcastObject(Podcast podcast, int subscribers, int rating) {
            this.podcast = podcast;
            this.subscribers = subscribers;
            this.rating = rating;
        }
    }

    public interface OnPodcastClickListener {
        void onPodcastClicked(Podcast podcast, int subscribers);
    }
}
