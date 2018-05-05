package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

/**
 * Fragment which is displayed when the user chooses "Settings" from the drawer menu
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load preferences from the resource XML
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.settings);
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
}
