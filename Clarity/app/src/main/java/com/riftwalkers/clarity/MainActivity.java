package com.riftwalkers.clarity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

<<<<<<< HEAD
=======
import java.io.File;
import java.io.IOException;

>>>>>>> origin/master
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;

import java.io.IOException;


public class MainActivity extends ARViewActivity {

    // Drawer Variables
    private String[] drawerItems;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

    private IGeometry vuurtoren,standbeeld,blaak,havenBedrijf;

    @Override
    protected int getGUILayout() {
        return R.layout.activity_inapp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp);

        new AssetsExtracter().execute(0);

        createDrawMenu();
    }

    public void createDrawMenu() {
        // TODO : Fix drawerLayout menu

        drawerItems = getResources().getStringArray(R.array.drawer_array);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.legend);

        // Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.listview_items,
                drawerItems
        );
        drawerList.setAdapter(adapter);

        // Configuration

        // Drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,          /* DrawerLayout object */
                R.drawable.drawer_icon,/* Drawer Icon */
                R.string.drawer_open,  /* "open drawerLayout" description for accessibility */
                R.string.drawer_close  /* "close drawerLayout" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(R.string.drawer_title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.drawer_title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        drawerList.setOnItemClickListener(new DrawerItemClickListener());


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

        LLACoordinate vuurtorenLLA = new LLACoordinate(51.916801, 4.482380, 0, 0);
        LLACoordinate blaakLLA = new LLACoordinate(51.919855, 4.489412, 0, 0);
        LLACoordinate standbeeldLLA = new LLACoordinate(51.917648, 4.483057, 0, 0);
        LLACoordinate havenBedrijfLLA = new LLACoordinate(51.904794,4.484548, 0, 0);

        File POIbackground =
                AssetsManager.getAssetPathAsFile(getApplicationContext(), "ExamplePOI.png");
        if (POIbackground != null)
        {
            vuurtoren = metaioSDK.createGeometryFromImage(POIbackground,true,false);
            blaak = metaioSDK.createGeometryFromImage(POIbackground,true,false);
            standbeeld = metaioSDK.createGeometryFromImage(POIbackground,true,false);
            havenBedrijf = metaioSDK.createGeometryFromImage(POIbackground,true,false);

            if (vuurtoren != null)
            {
                vuurtoren.setTranslationLLA(vuurtorenLLA);
                vuurtoren.setLLALimitsEnabled(true);
                vuurtoren.setScale(100);
            }
            else
            {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
            }
            if (blaak != null)
            {
                blaak.setTranslationLLA(blaakLLA);
                blaak.setLLALimitsEnabled(true);
                blaak.setScale(100);
            }
            else
            {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
            }
            if (standbeeld != null)
            {
                standbeeld.setTranslationLLA(standbeeldLLA);
                standbeeld.setLLALimitsEnabled(true);
                standbeeld.setScale(100);
            }
            else
            {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
            }
            if (havenBedrijf != null)
            {
                havenBedrijf.setTranslationLLA(havenBedrijfLLA);
                havenBedrijf.setLLALimitsEnabled(true);
                havenBedrijf.setScale(100);
            }
            else
            {
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
            }

        }
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {
    }

    private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
    {
        @Override
        protected Boolean doInBackground(Integer... params)
        {
            try
            {
                AssetsManager.extractAllAssets(getApplicationContext(), false);
            }
            catch (IOException e)
            {
                MetaioDebug.printStackTrace(Log.ERROR, e);
                return false;
            }

            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawerLayout.
        // ActionBarDrawerToggle will take care of this.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.array.drawer_array:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // TODO : update visable icons

    }
}
