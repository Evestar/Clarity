package com.riftwalkers.clarity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;

import org.json.JSONArray;


public class MainActivity extends ARViewActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks{
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private ArrayList<PointOfInterest> pointOfInterestList;

    @Override
    protected int getGUILayout() {
        return R.layout.activity_inapp;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mGUIView);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*if (id == R.id.action_settings) {
            return true;
        } else*/ if(id == R.id.action_search) {
            Search(getCurrentFocus());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Search(View view) {
        SearchDialog searchDialog = new SearchDialog(this);
        searchDialog.show();
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return null;
    }

    @Override
    protected void loadContents()
    {
        // Set GPS tracking configuration
        metaioSDK.setTrackingConfiguration("GPS");

        metaioSDK.setLLAObjectRenderingLimits(5, 200);
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

        // Replace this by converting JSON to the object model
        pointOfInterestList = new ArrayList<PointOfInterest>();

        createPOI(0,"Schip info x meter",  51.888689, 4.487128, PoiType.Schip);
        createPOI(1,"boei info x meter",   51.888348, 4.484435, PoiType.Boei);
        createPOI(2,"boei info x meter",   51.887325, 4.483684, PoiType.Boei);
        createPOI(3,"bolder info x meter", 51.885825, 4.484510, PoiType.Bolder);
        createPOI(4,"bolder info x meter", 51.885239, 4.486806, PoiType.Bolder);
        createPOI(5,"bolder info x meter", 51.888547, 4.492725, PoiType.Bolder);
        createPOI(6,"bolder info x meter", 51.886077, 4.490636, PoiType.Bolder);
        createPOI(7,"bolder info x meter", 51.887682, 4.488394, PoiType.Bolder);
        createPOI(8,"bolder info x meter", 51.887149, 4.489338, PoiType.Bolder);
        createPOI(9,"bolder info x meter", 51.889334, 4.493962, PoiType.Bolder);


        //draw each poi in the arrayList
        for(int i=0;i<pointOfInterestList.size();i++) {
            //get type of POI and image
            File POIbackground = AssetsManager.getAssetPathAsFile(getApplicationContext(), pointOfInterestList.get(i).GetImageName());
            String name = Integer.toString(pointOfInterestList.get(i).getId());
            IGeometry geometry = pointOfInterestList.get(i).getGeometry();

            if(POIbackground != null) {
                createGeometry(geometry, pointOfInterestList.get(i).getCoordinate(), POIbackground, 100);
            } else {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
            }
        }
    }

    /**
     *
     * @param geometry - The IGeometry which you want to create
     * @param coordinate - The coordinate for the geometry
     * @param iconFile - The File which you want to use for the icon
     * @param scale - The scale of the geometry
     */
    public void createGeometry(IGeometry geometry, LLACoordinate coordinate, File iconFile, int scale) {
        geometry = metaioSDK.createGeometryFromImage(iconFile, true,false);
        if(geometry != null) {
            geometry.setTranslationLLA(coordinate);
            geometry.setLLALimitsEnabled(true);
            geometry.setScale(scale);
        } else {
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + geometry);
        }
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {
    }


    private void createPOI(int id, String desc, LLACoordinate cord, PoiType type){
        PointOfInterest poi = new PointOfInterest();

        poi.setId(id);
        poi.setDescription(desc);
        poi.setCoordinate(cord);
        poi.setType(type);

        pointOfInterestList.add(poi);
    }

    private void createPOI(int id, String desc, double lat, double lng, PoiType type){
        PointOfInterest poi = new PointOfInterest();

        poi.setId(id);
        poi.setDescription(desc);
        poi.setCoordinate(new LLACoordinate(lat, lng, 0 ,0));
        poi.setType(type);

        pointOfInterestList.add(poi);
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
