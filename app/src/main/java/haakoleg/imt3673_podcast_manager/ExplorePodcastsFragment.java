package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class ExplorePodcastsFragment extends Fragment implements
        ValueEventListener, PodcastsRecyclerAdapter.OnPodcastClickListener {

    private DatabaseReference dbRef;
    private RecyclerView podcastsRecycler;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_podcasts, container, false);
        podcastsRecycler = view.findViewById(R.id.explore_podcasts_recycler);
        podcastsRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        tabLayout = view.findViewById(R.id.explore_tab_layout);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        dbRef.addListenerForSingleValueEvent(this);
    }

    @Override
    public void onResume() {
        super.onResume();

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

        // Create adapter
        PodcastsRecyclerAdapter adapter =
                new PodcastsRecyclerAdapter(
                        ExplorePodcastsFragment.this,
                        podcasts, subscriberCounts, ratings, this);
        podcastsRecycler.setAdapter(adapter);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) { }

    @Override
    public void onPodcastClicked(Podcast podcast, int subscribers) {
        DisplayPodcastFragment fragment = DisplayPodcastFragment.newInstance(podcast);
        ((MainActivity)getActivity()).displayContent(fragment, "DisplayPodcast");
    }
}
