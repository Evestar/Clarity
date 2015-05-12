package com.riftwalkers.clarity.view.activities;

import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.GPSLocationProvider;
import com.riftwalkers.clarity.data.interfaces.ChangeFragmentListener;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.view.fragment.ARFragment;
import com.riftwalkers.clarity.view.fragment.BaseFragment;
import com.riftwalkers.clarity.view.fragment.MapsFragment;
import com.riftwalkers.clarity.view.fragment.NavigationDrawerFragment;
import com.riftwalkers.clarity.view.fragment.RoleSelectorFragment;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, ChangeFragmentListener {

    private SharedPreferences sharedPreferences; // SharedPreference and information
    private SharedPreferences.Editor editor;
    public static PoiList pointOfInterestList;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private BaseFragment currentFragment;

    private GPSLocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigationdrawer_layout);

        sharedPreferences = getSharedPreferences("ClarityApp", 0);
        editor = sharedPreferences.edit();

        pointOfInterestList = new PoiList(this);

        locationProvider = new GPSLocationProvider(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        currentFragment = new RoleSelectorFragment();
        currentFragment.setFragmentListener(this);
        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_activity2, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_search) {
            //Search(getCurrentFocus());
            return true;
        } else if(id == R.id.refreshPosition) {
            //gpsLocationProvider.requestUpdate();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Use the slide menu to go back.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) { }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public void ChangeFragment(Class fragmentClass) {
        currentFragment.unsetFragmentListener();

        if(fragmentClass.equals(ARFragment.class)) {
            currentFragment = new ARFragment();
            currentFragment.setEditor(editor);
        } else if(fragmentClass.equals(MapsFragment.class)) {
            currentFragment = new MapsFragment();
            currentFragment.setLocationProvider(locationProvider);
        } else if(fragmentClass.equals(RoleSelectorFragment.class)) {
            currentFragment = new RoleSelectorFragment();
        }

        currentFragment.setFragmentListener(this);

        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }
}
