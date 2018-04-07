package haakoleg.imt3673_podcast_manager;

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
