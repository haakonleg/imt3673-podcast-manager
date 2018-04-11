package haakoleg.imt3673_podcast_manager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;
import haakoleg.imt3673_podcast_manager.tasks.GetDownloadedEpisodesTask;
import haakoleg.imt3673_podcast_manager.tasks.GetEpisodesTask;
import haakoleg.imt3673_podcast_manager.tasks.Task;

/**
 * This fragment is responsible for displaying episodes from a list of podcasts
 * It will order episodes in the list by the time it was published
 */

public class ShowEpisodesFragment extends Fragment {
    private RecyclerView episodesRecycler;
    private ArrayList<Podcast> podcasts;
    private int count;
    private boolean instanceDownloaded;

    /**
     * Static factory method to create a new instance of ShowEpisodesFragment
     * @param podcasts List of podcasts to display episodes from
     * @param count The amount of episodes to display
     * @return New Fragment instance
     */
    public static ShowEpisodesFragment newInstance(ArrayList<Podcast> podcasts, int count) {
        ShowEpisodesFragment fragment = new ShowEpisodesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("podcasts", podcasts);
        bundle.putInt("count", count);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ShowEpisodesFragment newInstanceDownloaded(ArrayList<Podcast> podcasts) {
        ShowEpisodesFragment fragment = new ShowEpisodesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("podcasts", podcasts);
        bundle.putInt("count", 0);
        fragment.setArguments(bundle);
        fragment.instanceDownloaded = true;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get bundle from savedInstanceState if the fragment
        // is being recreated, or from getArguments if new fragment
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        podcasts = bundle.getParcelableArrayList("podcasts");
        count = bundle.getInt("count");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("podcasts", podcasts);
        outState.putInt("count", count);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_episodes, container, false);
        episodesRecycler = view.findViewById(R.id.episodes_recycler);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Task task;
        if (instanceDownloaded) {
            task = new GetDownloadedEpisodesTask(getActivity(), episodes -> {
                EpisodesRecyclerAdapter adapter =
                        new EpisodesRecyclerAdapter(this, new EpisodeClickListener(), podcasts, episodes);
                episodesRecycler.setAdapter(adapter);
            }, error -> {
                // TODO: Handle error
            });
        } else {
            // Retrieve episodes from SQLite and create new adapter for the RecyclerView
            task = new GetEpisodesTask(getActivity(), podcasts, episodes -> {
                EpisodesRecyclerAdapter adapter =
                        new EpisodesRecyclerAdapter(this, new EpisodeClickListener(), podcasts, episodes);
                episodesRecycler.setAdapter(adapter);
            }, error -> {
                // TODO: Handle error
            });
        }
        ThreadManager.get().execute(task);
    }

    private class EpisodeClickListener implements EpisodesRecyclerAdapter.OnEpisodeClickListener {
        @Override
        public void onEpisodeClicked(PodcastEpisode episode, Podcast podcast) {
            EpisodePlayerFragment fragment = EpisodePlayerFragment.newInstance(episode, podcast);
            ((MainActivity)getActivity()).displayContent(fragment, "PlayEpisode");
        }

        @Override
        public void onDownloadEpisodeClicked(PodcastEpisode episode) {
            // Ask for permission to write to external storage
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                // Use the DownloadManager service for downloading the audio file
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(episode.getAudioUrl()));
                request.setTitle(episode.getTitle());
                request.setDescription("Downloading Episode");
                // Destination file is set to the external download directory and filename is the hashcode of the podcast episode
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/podcastmanager/" + Integer.toHexString(episode.hashCode()));
                request.setMimeType("audio/*");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                // Enque the download request
                dm.enqueue(request);
            }
        }
    }
}
