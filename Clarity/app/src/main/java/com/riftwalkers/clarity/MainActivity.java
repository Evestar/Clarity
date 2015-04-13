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


public class MainActivity extends ARViewActivity {

enum PoiType {
    Schip,
    Bolder,
    Boei
}


class PointOfInterest {
    int Id;
    PoiType Type;
    String Description;
    LLACoordinate Coordinate;

    String GetImageName ()
    {
        switch (this.Type){
            case Schip: return "POIc.png";
            case Boei: return "POIb.png";
            case Bolder: return "ExamplePOI.png";
            default: return "ExamplePOI.png";
        }
    }
}


    //TIJDELIJKE JSON MET DUMMY DATA, WORDT LATER UIT JSON FILES GEHAALD

    // Drawer Variables
    private String[] drawerItems;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;

    //private IGeometry vuurtoren,standbeeld,blaak,havenBedrijf;
    private ArrayList<PointOfInterest> pointOfInterestList;

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

    public void Search(View view) {
        SearchDialog searchDialog = new SearchDialog(this);
        searchDialog.show();
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

        // Replace this by converting JSON to the object model
        pointOfInterestList = new ArrayList<PointOfInterest>();

        PointOfInterest schip1 = new PointOfInterest();
        schip1.Id =  1;
        schip1.Description = "schip info x meter";
        schip1.Coordinate = new LLACoordinate(51.916801, 4.482380, 0, 0);
        schip1.Type = PoiType.Schip;
        pointOfInterestList.add(schip1);

        PointOfInterest bolder1 = new PointOfInterest();
        bolder1.Id =  2;
        bolder1.Description = "bolder info x meter";
        bolder1.Coordinate = new LLACoordinate(51.919855, 4.489412, 0, 0);
        bolder1.Type = PoiType.Bolder;
        pointOfInterestList.add(bolder1);

        PointOfInterest bolder2 = new PointOfInterest();
        bolder2.Id =  3;
        bolder2.Description = "bolder info x meter";
        bolder2.Coordinate = new LLACoordinate(51.917648, 4.483057, 0, 0);
        bolder2.Type = PoiType.Bolder;
        pointOfInterestList.add(bolder2);

        PointOfInterest boei1 = new PointOfInterest();
        boei1.Id =  4;
        boei1.Description = "boei info x meter";
        boei1.Coordinate = new LLACoordinate(51.904794,4.484548, 0, 0);
        boei1.Type = PoiType.Boei;
        pointOfInterestList.add(boei1);

        //AssetsManager.extractAllAssets(this, true);

        //draw each poi in the arrayList
        for(int i=0;i<pointOfInterestList.size();i++) {

            //get type of POI and image
            File POIbackground = AssetsManager.getAssetPathAsFile(getApplicationContext(), pointOfInterestList.get(i).GetImageName());
            //File POIbackground = AssetsManager.getAssetPathAsFile(getApplicationContext(), "ExamplePOI.png");
            String name = Integer.toString(pointOfInterestList.get(i).Id);
            IGeometry geometry = null;
            if(POIbackground != null) {
                createGeometry(geometry, pointOfInterestList.get(i).Coordinate, POIbackground, 100);
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
