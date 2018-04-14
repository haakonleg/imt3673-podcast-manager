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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

public class ShowEpisodesFragment extends Fragment
        implements EpisodesRecyclerAdapter.OnEpisodeClickListener {
    private RecyclerView episodesRecycler;
    private ArrayList<Podcast> podcasts;
    private EpisodesRecyclerAdapter adapter;
    private boolean instanceDownloaded;

    /**
     * Static factory method to create a new instance of ShowEpisodesFragment
     * @param podcasts List of podcasts to display episodes from
     * @return New Fragment instance
     */
    public static ShowEpisodesFragment newInstance(ArrayList<Podcast> podcasts) {
        ShowEpisodesFragment fragment = new ShowEpisodesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("podcasts", podcasts);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ShowEpisodesFragment newInstanceDownloaded(ArrayList<Podcast> podcasts) {
        ShowEpisodesFragment fragment = new ShowEpisodesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("podcasts", podcasts);
        fragment.setArguments(bundle);
        fragment.instanceDownloaded = true;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get bundle from savedInstanceState if the fragment
        // is being recreated, or from getArguments if new fragment
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        podcasts = bundle.getParcelableArrayList("podcasts");
        adapter = new EpisodesRecyclerAdapter(this, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("podcasts", podcasts);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_episodes, container, false);
        episodesRecycler = view.findViewById(R.id.episodes_recycler);
        episodesRecycler.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchEpisodes();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (instanceDownloaded) {
            mainActivity.getSupportActionBar().setTitle(R.string.saved_episodes);
        } else {
            if (podcasts.size() == 1) {
                mainActivity.getSupportActionBar().setTitle(podcasts.get(0).getTitle());
            } else {
                mainActivity.getSupportActionBar().setTitle(R.string.home);
            }
        }
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        mainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ((MainActivity)getActivity()).drawerLayout.openDrawer(Gravity.START);
            return true;
        }
        return false;
    }

    /**
     * Fetches podcast episodes from the database and adds them to the recyclerview
     */
    private void fetchEpisodes() {
        Task task;
        if (instanceDownloaded) {
            task = new GetDownloadedEpisodesTask(getActivity(), episodes -> {
                adapter.setData(podcasts, episodes);
            }, error -> {
                Task.errorHandler(getActivity(), error);
            });
        } else {
            // Retrieve episodes from SQLite and create new adapter for the RecyclerView
            task = new GetEpisodesTask(getActivity(), podcasts, episodes -> {
                adapter.setData(podcasts, episodes);
            }, error -> {
                Task.errorHandler(getActivity(), error);
            });
        }
        ThreadManager.get().execute(task);
    }

    private void downloadEpisode(PodcastEpisode episode) {
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

    @Override
    public void onEpisodeClicked(PodcastEpisode episode, Podcast podcast) {
        EpisodePlayerFragment fragment = EpisodePlayerFragment.newInstance(episode, podcast);
        ((MainActivity)getActivity()).displayContent(fragment, "PlayEpisode");
    }

    @Override
    public void onDownloadEpisodeClicked(PodcastEpisode episode) {
        downloadEpisode(episode);
    }
}
