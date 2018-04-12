package haakoleg.imt3673_podcast_manager;

import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

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
import java.util.ListIterator;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.tasks.DeletePodcastsTask;
import haakoleg.imt3673_podcast_manager.tasks.GetPodcastsTask;
import haakoleg.imt3673_podcast_manager.tasks.ParsePodcastTask;
import haakoleg.imt3673_podcast_manager.tasks.SyncPodcastsTask;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final File DOWNLOAD_DIR = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/podcastmanager/");

    private NavigationView navView;
    private DrawerLayout drawerLayout;
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
        navView = findViewById(R.id.nav_view);
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

            // If there is a network connection
            if (CheckNetwork.hasNetwork(this)) {
                // Parse and sync new episodes for locally stored podcasts
                parsePodcasts(localUrls, this::syncPodcasts);
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
                parsePodcasts(firebaseUrls, parsedPodcasts -> {
                    for (Podcast podcast : parsedPodcasts) {
                        addPodcastToDrawer(podcast);
                    }
                    syncPodcasts(parsedPodcasts);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void parsePodcasts(List<String> podcastUrls, ParseCallback cb) {
        List<Podcast> parsedPodcasts = new ArrayList<>();

        // Have to use iterator, otherwise it is impossible to access current index in a lambda
        for (ListIterator<String> iter = podcastUrls.listIterator(); iter.hasNext();) {
            int i = iter.nextIndex();
            String url = iter.next();

            ParsePodcastTask task = new ParsePodcastTask(this, url, parsedPodcast -> {
                parsedPodcasts.add(parsedPodcast);
                if (i == podcastUrls.size() - 1) {
                    // Calls the callback after all podcasts have been parsed
                    cb.onPodcastsParsed(parsedPodcasts);
                }
            }, error -> {
                if (i == podcastUrls.size() - 1) {
                    // Calls the callback after all podcasts have been parsed
                    cb.onPodcastsParsed(parsedPodcasts);
                }
            });
            ThreadManager.get().execute(task);
        }
    }

    private void syncPodcasts(List<Podcast> podcasts) {
        SyncPodcastsTask task = new SyncPodcastsTask(this, podcasts, updatedEpisodes -> {

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
            subscriptionsMenu.add(R.id.nav_subscriptions_submenu, id, Menu.NONE, podcast.getTitle()).setCheckable(true);
        }
    }

    /**
     * Parses a new podcast, and if the podcast is of a valid format, it is added to the drawer menu
     */
    private void addPodcast(String url) {
        ParsePodcastTask task = new ParsePodcastTask(this, url, podcast ->  {
            addPodcastToDrawer(podcast);

            // Sync this podcast
            ArrayList<Podcast> toSync = new ArrayList<>();
            toSync.add(podcast);
            syncPodcasts(toSync);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes all locally saved podcasts in the SQLite database and signs the user out from firebase
     */
    private void signOut() {
        DeletePodcastsTask task = new DeletePodcastsTask(this, res -> {
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
        void onPodcastsParsed(List<Podcast> podcasts);
    }
}
