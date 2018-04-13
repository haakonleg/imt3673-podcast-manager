package haakoleg.imt3673_podcast_manager;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.tasks.DeletePodcastsTask;
import haakoleg.imt3673_podcast_manager.tasks.GetPodcastsTask;
import haakoleg.imt3673_podcast_manager.tasks.ParsePodcastTask;
import haakoleg.imt3673_podcast_manager.tasks.SyncPodcastTask;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final File DOWNLOAD_DIR = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/podcastmanager/");

    public DrawerLayout drawerLayout;
    private SubMenu subscriptionsMenu;

    // This contains all podcasts the user has saved
    private HashMap<Integer, Podcast> podcasts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);

        // Find elements
        NavigationView navView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up navigation view
        navView.setNavigationItemSelectedListener(this);
        Menu navMenu = navView.getMenu();
        subscriptionsMenu = navMenu.addSubMenu(R.id.nav_subscriptions, R.id.nav_subscriptions_submenu, Menu.NONE, R.string.my_subscriptions);

        this.podcasts = new HashMap<>();
        initialize();
    }

    /**
     * Interrupt all running threads when the activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThreadManager.get().interruptAll();
        AppDatabase.destroy();
    }

    /**
     * Retrieves all saved podcasts from the SQLite database, syncs episodes if there is an
     * internet connection, and adds them to the list of podcasts in the drawer menu
     */
    private void initialize() {
        GetPodcastsTask task = new GetPodcastsTask(this, podcasts -> {
            // Add locally stored podcasts to the drawer menu
            List<String> localUrls = new ArrayList<>();
            for (Podcast podcast : podcasts) {
                addPodcastToDrawer(podcast);
                localUrls.add(podcast.getUrl());
            }
            showHomeFragment();

            // If there is a network connection
            if (CheckNetwork.hasNetwork(this)) {
                // Parse and sync new episodes for locally stored podcasts
                parsePodcasts(localUrls, this::syncPodcast);
                syncWithFirebase(localUrls);
            }
        }, error -> {
            // TODO: Handle error
        });
        ThreadManager.get().execute(task);
    }

    private void syncWithFirebase(List<String> localUrls) {
        // Check if there are podcast urls from firebase for the user that has not been added to the local database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("subscriptions");

        // Single value event listener for podcast URLs
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<String> firebaseUrls = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String podcastUrl = (String) snapshot.getValue();
                    if (!localUrls.contains(podcastUrl)) {
                        firebaseUrls.add(podcastUrl);
                    }
                }

                // Parse and add these podcasts to the drawer menu
                parsePodcasts(firebaseUrls, parsed -> {
                    Log.d("From firebase", parsed.getUrl());
                    addPodcastToDrawer(parsed);
                    syncPodcast(parsed);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Starts the podcast parsing tasks on new threads for every podcast URL in the list
     * @param podcastUrls List of podcast URLs to parse
     * @param cb Callback for when a podcast has been parsed
     */
    private void parsePodcasts(List<String> podcastUrls, ParseCallback cb) {
        for (String url : podcastUrls) {
            ParsePodcastTask task = new ParsePodcastTask(this, url, cb::onPodcastParsed, error -> {
                // TODO: Handle error
            });
            ThreadManager.get().execute(task);
        }
    }

    private void syncPodcast(Podcast podcast) {
        SyncPodcastTask task = new SyncPodcastTask(this, podcast, updatedEpisodes -> {
            Log.d("Synced", podcast.getUrl());
        }, error -> {
            // TODO: handle error
        });
        ThreadManager.get().execute(task);
    }

    private void showHomeFragment() {
        // Show home fragment containing recent episodes from all podcasts
        ShowEpisodesFragment fragment = ShowEpisodesFragment.newInstance(new ArrayList<>(this.podcasts.values()), 50);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
    }

    /**
     * Adds a podcast to the drawer list, and the list containing all podcasts the user has
     * subscribed to. The SparseArray list containing the podcasts uses the hashcode of the
     * podcast object as the key, and this hashcode is also used for the item id of the MenuItem.
     * @param podcast The podcast object to add
     */
    private void addPodcastToDrawer(Podcast podcast) {
        // Uses hashcode as the key for the podcast in the array
        int id = podcast.hashCode();

        // Add to array and drawer if it doesn't already exist
        if (this.podcasts.get(id) == null) {
            this.podcasts.put(id, podcast);
            subscriptionsMenu.add(R.id.nav_subscriptions_submenu, id, 0, podcast.getTitle());

            // Set checkable and long click listener for podcast item
            MenuItem item = subscriptionsMenu.findItem(id);
            item.setCheckable(true);

            // Load podcast image into the item icon
            Glide.with(this).asDrawable().load(podcast.getImage()).apply(
                    new RequestOptions().centerCrop()).into(new SimpleTarget<Drawable>(50, 50) {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    item.setIcon(resource);
                }
            });

            // The icon won't show unless this is set on Android oreo for some reason
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                item.setIconTintMode(null);
            }
        }
    }

    /**
     * Removes a podcast from the drawer list
     * @param podcast The podcast object to remove
     */
    public void removePodcastFromDrawer(Podcast podcast) {
        subscriptionsMenu.removeItem(podcast.hashCode());
    }

    /**
     * Parses a new podcast, and if the podcast is of a valid format, it is added to the drawer menu
     */
    private void addPodcast(String url) {
        ParsePodcastTask task = new ParsePodcastTask(this, url, podcast ->  {
            // Add podcast to drawer and sync
            addPodcastToDrawer(podcast);
            syncPodcast(podcast);
        }, error -> {
            // TODO: Show better error
            Log.e("ERROR", Integer.toString(error));
        });
        ThreadManager.get().execute(task);
    }

    /**
     * Replace a fragment in the "main_content" FrameLayout, which
     * is the main container for fragments in the app
     * @param fragment The fragment to display
     * @param tag The tag used for identifying the fragment in the backstack
     */
    public void displayContent(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(tag).commit();
    }

    /**
     * Deletes all locally saved podcasts in the SQLite database and signs the user out from firebase
     */
    private void signOut() {
        List<Podcast> allPodcasts = new ArrayList<>(podcasts.values());

        // Delete all locally stored podcasts when the user signs out
        DeletePodcastsTask task = new DeletePodcastsTask(this, allPodcasts, false, res -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        }, error -> {
            // TODO: Handle error
        });
        ThreadManager.get().execute(task);
    }

    /**
     * Fired when an item is selected in the navigation drawer
     * @param item Reference to the selected MenuItem
     * @return True if a valid item was clicked
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_home:
                getSupportFragmentManager().popBackStack();
                break;
            case R.id.nav_userfeeds:
                getSupportFragmentManager().popBackStack();
                displayContent(new ExplorePodcastsFragment(), "ExplorePodcasts");
                break;
            case R.id.nav_add_feed:
                item.setCheckable(false);
                AddPodcastDialogFragment dialog = new AddPodcastDialogFragment();
                dialog.setListener(MainActivity.this::addPodcast);
                dialog.show(getSupportFragmentManager(), "AddPodcastDialog");
                break;
            case R.id.nav_saved_episodes:
                getSupportFragmentManager().popBackStack();
                displayContent(ShowEpisodesFragment.newInstanceDownloaded(new ArrayList<>(podcasts.values())), "DownloadedEpisodes");
                break;
            case R.id.nav_manage:
                getSupportFragmentManager().popBackStack();
                displayContent(ManagePodcastsFragment.newInstance(new ArrayList<>(podcasts.values())), "ManagePodcasts");
                break;
            case R.id.nav_logout:
                signOut();
                break;
            default:
                // Selected a podcast
                Podcast podcast = podcasts.get(id);
                ArrayList<Podcast> singlePodcast = new ArrayList<>();
                singlePodcast.add(podcast);
                ShowEpisodesFragment fragment = ShowEpisodesFragment.newInstance(singlePodcast, 50);
                getSupportFragmentManager().popBackStack();
                displayContent(fragment, "ShowEpisodes");
                break;
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private interface ParseCallback {
        void onPodcastParsed(Podcast podcast);
    }
}
