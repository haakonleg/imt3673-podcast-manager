package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import haakoleg.imt3673_podcast_manager.models.Podcast;

/**
 * This fragment is used when the "Explore Podcasts" option is selected in the drawer menu.
 * Contains podcasts added by other users of the app.
 */

public class ExplorePodcastsFragment extends Fragment implements
        ValueEventListener, PodcastsRecyclerAdapter.OnPodcastClickListener, TabLayout.OnTabSelectedListener {

    private DatabaseReference dbRef;
    private RecyclerView podcastsRecycler;
    private PodcastsRecyclerAdapter adapter;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts");
        adapter = new PodcastsRecyclerAdapter(this, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_podcasts, container, false);
        podcastsRecycler = view.findViewById(R.id.explore_podcasts_recycler);
        tabLayout = view.findViewById(R.id.explore_tab_layout);

        podcastsRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        podcastsRecycler.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Add firebase listener to fetch podcasts
        dbRef.addListenerForSingleValueEvent(this);
        // Add tablayout listener
        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set title for actionbar and set home button, unlock drawer
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.explore_podcasts_actionbar_title);
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
     * Override for firebase ValueEventListener. Fetches podcasts from Firebase and adds
     * them to the PodcasatsRecyclerAdapter to display them in a RecyclerView.
     */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        ArrayList<Podcast> podcasts = new ArrayList<>((int)dataSnapshot.getChildrenCount());
        ArrayList<Integer> subscriberCounts = new ArrayList<>();
        ArrayList<Integer> ratings = new ArrayList<>();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Podcast podcast = snapshot.getValue(Podcast.class);

            // Add the podcast to list
            podcasts.add(podcast);
            // Get number of subscribers to this podcast
            int subscriberCount = (int) snapshot.child("subscribers").getChildrenCount();
            subscriberCounts.add(subscriberCount);

            // Get the average rating of this podcast
            DataSnapshot ratingSnap = snapshot.child("ratings");
            int numRatings = (int) ratingSnap.getChildrenCount();
            int avg = 0;
            for (DataSnapshot rating : ratingSnap.getChildren()) {
                avg += ((Long)rating.getValue()).intValue();
            }
            if (numRatings > 0) {
                avg /= numRatings;
            }
            ratings.add(avg);
        }

        // Set the data to display
        adapter.setData(podcasts, subscriberCounts, ratings);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // Unused
    }

    @Override
    public void onPodcastClicked(Podcast podcast) {
        DisplayPodcastFragment fragment = DisplayPodcastFragment.newInstance(podcast);
        ((MainActivity)getActivity()).displayContent(fragment, "DisplayPodcast");
    }

    /**
     * When the user selects "popular" or "top rated" tab, call adapter method
     * to sort by popularity or rating
     * @param tab The tab which was selected
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int pos = tab.getPosition();

        podcastsRecycler.scrollToPosition(0);
        TransitionManager.beginDelayedTransition(podcastsRecycler, new Fade());
        if (pos == 0) {
            adapter.sortByPopularity();
        } else {
            adapter.sortByRating();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // Unused
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // Unused
    }
}
