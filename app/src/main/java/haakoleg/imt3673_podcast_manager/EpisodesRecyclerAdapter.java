package haakoleg.imt3673_podcast_manager;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class EpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodesRecyclerAdapter.EpisodeHolder> {
    private final Fragment fragment;
    private final OnEpisodeClickListener listener;
    private HashMap<String, Podcast> podcasts;
    private List<PodcastEpisode> episodes;

    /**
     * Adapter constructor
     * @param fragment Reference to the fragment that this adapter is being used in, needed by Glide
     */
    public EpisodesRecyclerAdapter(Fragment fragment, OnEpisodeClickListener listener) {
        this.fragment = fragment;
        this.listener = listener;
    }

    /**
     * Sets the data for the constructor, can't set this in constructor, because otherwise
     * recyclerview logs an error "No adapter attached, skipping layout"
     * @param podcasts The podcasts the episodes belong to
     * @param episodes The podcast episodes to display in the recyclerview
     */
    public void setData(List<Podcast> podcasts, List<PodcastEpisode> episodes) {
        this.podcasts = new HashMap<>();
        // Add podcasts to hashmap
        for (Podcast podcast : podcasts) {
            this.podcasts.put(podcast.getUrl(), podcast);
        }
        this.episodes = episodes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_episode_item, parent, false);
        return new EpisodeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeHolder holder, int position) {
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
        holder.durationTxt.setText(episode.getFormattedDuration());

        // If the episode is downloaded, show save icon and disable the download button
        if (episode.isDownloaded()) {
            Glide.with(fragment)
                    .load(R.drawable.ic_save_24dp)
                    .into(holder.saveBtn);
            holder.saveBtn.setEnabled(false);
        } else {
            Glide.with(fragment)
                    .load(R.drawable.ic_file_download_24dp)
                    .into(holder.saveBtn);
            holder.saveBtn.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        if (this.episodes == null) {
            return 0;
        }
        return episodes.size();
    }

    /**
     * CommentHolder for layout "view_episode_item"
     */
    class EpisodeHolder extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView titleTxt;
        final TextView descTxt;
        final ImageButton saveBtn;
        final TextView podcastTxt;
        final TextView durationTxt;

        EpisodeHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.episode_img);
            titleTxt = itemView.findViewById(R.id.episode_title_txt);
            descTxt = itemView.findViewById(R.id.episode_desc_txt);
            saveBtn = itemView.findViewById(R.id.episode_save_btn);
            podcastTxt = itemView.findViewById(R.id.episode_podcast_txt);
            durationTxt = itemView.findViewById(R.id.episode_duration_txt);

            // Set onclick listener for this episode item
            itemView.setOnClickListener(v -> {
                PodcastEpisode episode = episodes.get(getAdapterPosition());
                Podcast podcast = podcasts.get(episode.getParentUrl());
                listener.onEpisodeClicked(episode, podcast);
            });

            // Set onclick listener for save episode button
            saveBtn.setOnClickListener(v -> {
                PodcastEpisode episode = episodes.get(getAdapterPosition());
                listener.onDownloadEpisodeClicked(episode);
            });
        }
    }

    /**
     * Callback interface for when a podcast episode is clicked
     */
    public interface OnEpisodeClickListener {
        void onEpisodeClicked(PodcastEpisode episode, Podcast podcast);
        void onDownloadEpisodeClicked(PodcastEpisode episode);
    }
}
