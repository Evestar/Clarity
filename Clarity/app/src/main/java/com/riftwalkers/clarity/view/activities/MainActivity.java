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
import com.riftwalkers.clarity.data.interfaces.SearchButtonClickListener;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;
import com.riftwalkers.clarity.view.fragment.ARFragment;
import com.riftwalkers.clarity.view.fragment.BaseFragment;
import com.riftwalkers.clarity.view.fragment.MapsFragment;
import com.riftwalkers.clarity.view.fragment.NavigationDrawerFragment;
import com.riftwalkers.clarity.view.fragment.RoleSelectorFragment;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends ActionBarActivity implements ChangeFragmentListener {

    private SearchButtonClickListener searchButtonClickListener;

    private SharedPreferences sharedPreferences; // SharedPreference and information
    private SharedPreferences.Editor editor;

    public static PoiList pointOfInterestList;
    public static boolean isSearchingFromMaps = false;
    public static PointOfInterest SearchedPOI;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private BaseFragment currentFragment;

    private GPSLocationProvider locationProvider;

    private View navigationDrawerView;
    private View blankFragmentView;

    private Menu menu;

    private boolean onRoleselector = true;

    private long oldTime;

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
        currentFragment = new RoleSelectorFragment();
        currentFragment.setFragmentListener(this);
        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.global, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(menu != null)
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
            if(searchButtonClickListener != null)
                searchButtonClickListener.onSearchClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentTimeMillis() < oldTime+1500 ){

            if(onRoleselector){
                System.exit(0);
            } else {
                ChangeFragment(RoleSelectorFragment.class);
            }

        } else{
            oldTime = currentTimeMillis();

            if(onRoleselector){
                Toast.makeText(getApplicationContext(),"Press back again to exit.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),"Press back again to return.", Toast.LENGTH_SHORT).show();
            }
        }
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
            MapsFragment.setActive(false);
            onRoleselector = false;
            searchButtonClickListener = ((ARFragment) currentFragment);
        } else if(fragmentClass.equals(MapsFragment.class)) {
            setContentView(navigationDrawerView);
            currentFragment = new MapsFragment();
            currentFragment.setLocationProvider(locationProvider);
            MapsFragment.setActive(true);
            onRoleselector = false;
            searchButtonClickListener = ((MapsFragment) currentFragment);
        } else if(fragmentClass.equals(RoleSelectorFragment.class)) {
            setContentView(blankFragmentView);
            currentFragment = new RoleSelectorFragment();
            MapsFragment.setActive(false);
            onRoleselector = true;
            searchButtonClickListener = null;
        }

        onPrepareOptionsMenu(menu);

        currentFragment.setFragmentListener(this);
        currentFragment.setEditor(editor);

        getFragmentManager().beginTransaction().replace(R.id.container, currentFragment).commit();
    }
}
