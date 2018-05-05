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
import java.util.Locale;

import haakoleg.imt3673_podcast_manager.models.Podcast;

/**
 *
 */

public class PodcastsRecyclerAdapter extends RecyclerView.Adapter<PodcastsRecyclerAdapter.PodcastHolder> {
    private final Fragment fragment;
    private List<PodcastObject> podcasts;
    private final OnPodcastClickListener listener;

    public PodcastsRecyclerAdapter(Fragment fragment, OnPodcastClickListener listener) {
        this.fragment = fragment;
        this.listener = listener;
    }

    /**
     * Sets the data for the constructor, can't set this in constructor, because otherwise
     * recyclerview logs an error "No adapter attached, skipping layout"
     * @param podcasts The podcasts to display in the recyclerview
     * @param subscriberCounts A list containing the number of subscribers for each podcast
     * @param ratings A list containing the rating for each podcast (0-5)
     */
    public void setData(List<Podcast> podcasts, List<Integer> subscriberCounts, List<Integer> ratings) {
        this.podcasts = new ArrayList<>();
        for (int i = 0; i < podcasts.size(); i++) {
            this.podcasts.add(new PodcastObject(podcasts.get(i), subscriberCounts.get(i), ratings.get(i)));
        }
        sortByPopularity();
        notifyDataSetChanged();
    }

    /**
     * Sorts podcasts in the list by popularity, by using a comparator
     */
    public void sortByPopularity() {
        Collections.sort(this.podcasts, (o1, o2) -> o2.subscribers - o1.subscribers);
        notifyDataSetChanged();
    }

    /**
     * Sorts podcasts in the list by rating, by using a comparator
     */
    public void sortByRating() {
        Collections.sort(this.podcasts, (o1, o2) -> o2.rating - o1.rating);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PodcastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_podcast_item, parent, false);
        return new PodcastHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastHolder holder, int position) {
        PodcastObject obj = podcasts.get(position);

        Glide.with(fragment)
                .load(obj.podcast.getImage())
                .transition(withCrossFade())
                .apply(new RequestOptions().centerCrop())
                .into(holder.podcastImg);

        holder.podcastTitleTxt.setText(obj.podcast.getTitle());
        holder.podcastCategoryTxt.setText(obj.podcast.getCategory());
        holder.podcastRating.setRating((float) obj.rating);
        holder.podcastSubscribersTxt.setText(String.format(Locale.getDefault(), "%d", obj.subscribers));
    }

    @Override
    public int getItemCount() {
        if (podcasts == null) {
            return 0;
        }
        return podcasts.size();
    }

    /**
     * ViewHolder for a podcast in the list
     */
    class PodcastHolder extends RecyclerView.ViewHolder {
        final ImageView podcastImg;
        final TextView podcastTitleTxt;
        final TextView podcastCategoryTxt;
        final RatingBar podcastRating;
        final TextView podcastSubscribersTxt;

        PodcastHolder(View itemView) {
            super(itemView);
            podcastImg = itemView.findViewById(R.id.podcast_img);
            podcastTitleTxt = itemView.findViewById(R.id.podcast_title_txt);
            podcastCategoryTxt = itemView.findViewById(R.id.podcast_category_txt);
            podcastRating = itemView.findViewById(R.id.podcast_rating);
            podcastSubscribersTxt = itemView.findViewById(R.id.podcast_subscribers_txt);

            // Set click listener for item
            itemView.setOnClickListener(v -> {
                PodcastObject obj = podcasts.get(getAdapterPosition());
                listener.onPodcastClicked(obj.podcast);
            });
        }
    }

    private class PodcastObject {
        final private Podcast podcast;
        final private int subscribers;
        final private int rating;

        PodcastObject(Podcast podcast, int subscribers, int rating) {
            this.podcast = podcast;
            this.subscribers = subscribers;
            this.rating = rating;
        }
    }

    public interface OnPodcastClickListener {
        void onPodcastClicked(Podcast podcast);
    }
}
