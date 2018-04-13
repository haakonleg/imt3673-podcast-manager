package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.tasks.DeletePodcastsTask;
import haakoleg.imt3673_podcast_manager.utils.Messages;

public class ManagePodcastsFragment extends Fragment implements ManagePodcastsRecyclerAdapter.ManagePodcastsListener {
    private List<Podcast> podcasts;
    private RecyclerView manageRecycler;

    public static ManagePodcastsFragment newInstance(ArrayList<Podcast> podcasts) {
        ManagePodcastsFragment fragment = new ManagePodcastsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("podcasts", podcasts);
        fragment.setArguments(bundle);
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_podcasts, container, false);
        manageRecycler = view.findViewById(R.id.manage_podcasts_recycler);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        ManagePodcastsRecyclerAdapter adapter = new ManagePodcastsRecyclerAdapter(getActivity(), podcasts, this);
        manageRecycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(getString(R.string.manage_podcasts));
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        mainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    /**
     * This callback is fired when a user has chosen to remove/unsubscribe a podcast
     * @param podcast The podcast object which is being removed
     */
    @Override
    public void onPodcastRemoved(Podcast podcast) {
        List<Podcast> toRemove = new ArrayList<>();
        toRemove.add(podcast);

        // Run background task to remove this podcast
        DeletePodcastsTask task = new DeletePodcastsTask(getActivity(), toRemove, true, result -> {
            Toast.makeText(getActivity(), getString(R.string.podcast_deleted), Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).removePodcastFromDrawer(podcast);
        }, error -> {
            // TODO: Handle error
        });
        ThreadManager.get().execute(task);
    }
}
