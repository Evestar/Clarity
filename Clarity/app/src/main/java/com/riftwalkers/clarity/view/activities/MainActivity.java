package com.riftwalkers.clarity.view.activities;

import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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

public class MainActivity extends ActionBarActivity implements ChangeFragmentListener {

    private SharedPreferences sharedPreferences; // SharedPreference and information
    private SharedPreferences.Editor editor;
    public static PoiList pointOfInterestList;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private BaseFragment currentFragment;

    private GPSLocationProvider locationProvider;

    private View navigationDrawerView;
    private View blankFragmentView;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navigationDrawerView = getLayoutInflater().inflate(R.layout.navigationdrawer_layout, null);
        blankFragmentView = getLayoutInflater().inflate(R.layout.blank_fragment_layout, null);
        setContentView(navigationDrawerView);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        setContentView(blankFragmentView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentFragment = new RoleSelectorFragment();
        currentFragment.setFragmentListener(this);
        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if((currentFragment instanceof ARFragment) || (currentFragment instanceof MapsFragment)) {
            if(mNavigationDrawerFragment.isClosed()) {
                getMenuInflater().inflate(R.menu.main_activity2, menu);
                restoreActionBar();
            } else {
                getMenuInflater().inflate(R.menu.global, menu);
                restoreActionBar();
            }
        } else {
            getMenuInflater().inflate(R.menu.global, menu);
            hideActionBar();
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_search) {
            //Search(getCurrentFocus());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Use the slide menu to go back.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public void ChangeFragment(Class fragmentClass) {
        currentFragment.unsetFragmentListener();
        currentFragment.unsetLocationProvider();
        mNavigationDrawerFragment.CloseDrawer();

        if(fragmentClass.equals(ARFragment.class)) {
            setContentView(navigationDrawerView);
            currentFragment = new ARFragment();
            currentFragment.setLocationProvider(locationProvider);
        } else if(fragmentClass.equals(MapsFragment.class)) {
            setContentView(navigationDrawerView);
            currentFragment = new MapsFragment();
            currentFragment.setLocationProvider(locationProvider);
        } else if(fragmentClass.equals(RoleSelectorFragment.class)) {
            setContentView(blankFragmentView);
            currentFragment = new RoleSelectorFragment();
        }

        onPrepareOptionsMenu(menu);

        currentFragment.setFragmentListener(this);
        currentFragment.setEditor(editor);

        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }
}
