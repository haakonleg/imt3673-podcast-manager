package haakoleg.imt3673_podcast_manager;

import android.os.Handler;
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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.tasks.GetPodcastsTask;
import haakoleg.imt3673_podcast_manager.tasks.ParsePodcastTask;
import haakoleg.imt3673_podcast_manager.tasks.SyncPodcastTask;
import haakoleg.imt3673_podcast_manager.utils.CheckNetwork;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private SubMenu subscriptionsMenu;

    // This contains all podcasts the user has saved
    private SparseArray<Podcast> podcasts;

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

        this.podcasts = new SparseArray<>();

        // Add stored podcasts to the drawer
        addSavedPodcasts();
    }

    /**
     * Retrieves all saved podcasts from the SQLite database, syncs episodes if there is an
     * internet connection, and adds them to the list of podcasts in the drawer menu
     */
    private void addSavedPodcasts() {
        GetPodcastsTask task = new GetPodcastsTask(this, podcasts -> {
            for (Podcast podcast : podcasts) {
                addPodcastToDrawer(podcast);
            }
            if (CheckNetwork.hasNetwork(this)) {
                syncPodcasts(podcasts);
            }
        }, error -> {
            // TODO: Handle error
        });
        ThreadManager.get().execute(task);
    }

    private void syncPodcasts(List<Podcast> podcasts) {
        // Have to use iterator, otherwise it is impossible to access current index in a lambda
        for (ListIterator<Podcast> iter = podcasts.listIterator(); iter.hasNext();) {
            int i = iter.nextIndex();
            Podcast podcast = iter.next();

            ParsePodcastTask task = new ParsePodcastTask(this, podcast.getUrl(), parsedPodcast -> {
                SyncPodcastTask syncTask = new SyncPodcastTask(this, parsedPodcast, updatedEpisodes -> {
                    Log.e("Synced", podcast.getUrl());
                    if (i == podcasts.size() - 1) {
                        // TODO: Notify when all podcasts are synced
                    }
                }, error -> {
                    // TODO: Handle error
                });
                ThreadManager.get().execute(syncTask);
            }, error -> {
                // TODO: Handle error
            });
            ThreadManager.get().execute(task);
        }
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
        getSupportFragmentManager().popBackStack();
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
     * Fired when an item is selected in the navigation drawer
     * @param item Reference to the selected MenuItem
     * @return True if a valid item was clicked
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_add_feed:
                item.setCheckable(false);
                AddPodcastDialogFragment dialog = new AddPodcastDialogFragment();
                dialog.setListener(MainActivity.this::addPodcast);
                dialog.show(getSupportFragmentManager(), "AddPodcastDialog");
                break;
            // Selected a podcast
            default:
                Podcast podcast = podcasts.get(id);
                ArrayList<Podcast> singlePodcast = new ArrayList<>();
                singlePodcast.add(podcast);
                ShowEpisodesFragment fragment = ShowEpisodesFragment.newInstance(singlePodcast, 50);
                displayContent(fragment, "ShowEpisodes");
                break;
        }

        drawerLayout.closeDrawers();
        return true;
    }
}
