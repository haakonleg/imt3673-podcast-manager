package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.tasks.GetEpisodesTask;

/**
 * This fragment is responsible for displaying episodes from a list of podcasts
 * It will order episodes in the list by the time it was published
 */

public class ShowEpisodesFragment extends Fragment {
    private RecyclerView episodesRecycler;
    private ArrayList<Podcast> podcasts;
    private int count;

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
    public void onResume() {
        super.onResume();

        // Retrieve episodes from SQLite and create new adapter for the RecyclerView
        GetEpisodesTask task = new GetEpisodesTask(getActivity(), podcasts, episodes -> {
            EpisodesRecyclerAdapter adapter = new EpisodesRecyclerAdapter(this, podcasts, episodes);
            episodesRecycler.setAdapter(adapter);
        }, error -> {
            // TODO: Handle error
        });
        ThreadManager.get().execute(task);
    }
}
