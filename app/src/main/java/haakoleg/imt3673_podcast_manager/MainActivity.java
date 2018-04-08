package haakoleg.imt3673_podcast_manager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;
import haakoleg.imt3673_podcast_manager.tasks.ParsePodcastTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private SubMenu subscriptionsMenu;

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

        addFeed();

        // TEST PARSER
        ParsePodcastTask task = new ParsePodcastTask(this, "http://feeds.gimletmedia.com/hearreplyall", result ->  {
            Log.e("Title", result.getTitle());
            Log.e("Description", result.getDescription());
            Log.e("Image", result.getImage());
            Log.e("Category", result.getCategory());
            Log.e("Date", Long.toString(result.getUpdated()));

            for(PodcastEpisode ep : result.getEpisodes()) {
                Log.e("TITLE", ep.getTitle());
                Log.e("DESCRIPTION", ep.getDescription());
                Log.e("UPDATED", Long.toString(ep.getUpdated()));
                Log.e("AUDIO", ep.getAudioUrl());
                Log.e("DURATION", Integer.toString(ep.getDuration()));
            }

        }, error -> {
            Log.e("ERROR", Integer.toString(error));
        });
        ThreadManager.get().execute(task);
    }

    /**
     * Adds a new feed to the menu in the drawer
     */
    public void addFeed() {
        subscriptionsMenu.add(R.id.nav_subscriptions_submenu, 1, Menu.NONE, "Test 1").setCheckable(true);
        subscriptionsMenu.add(R.id.nav_subscriptions_submenu, 2, Menu.NONE, "Test 2").setCheckable(true);
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
        Log.e("MenuItem", Integer.toString(item.getItemId()));
        return true;
    }
}
