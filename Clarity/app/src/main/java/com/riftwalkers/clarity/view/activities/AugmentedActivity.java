package com.riftwalkers.clarity.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.GPSLocationProvider;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;
import com.riftwalkers.clarity.view.dialog.SearchDialog;
import com.riftwalkers.clarity.view.fragment.NavigationDrawerFragment;

import java.io.File;
import java.util.ArrayList;

public class AugmentedActivity extends ARViewActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private SharedPreferences sharedPreferences; // SharedPreference and information
    private SharedPreferences.Editor editor;
    private int drawRange;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private ArrayList<PointOfInterest> meerpalenList;
    private PoiList pointOfInterestList;
    private PointOfInterest zoekPOI;
    private GPSLocationProvider gpsLocationProvider;

    private Button menuBackButton;
    private CheckBox meerpalenCheckbox;
    private CheckBox ligplaatsenCheckbox;
    private CheckBox aanmeerboeienCheckbox;
    private SeekBar rangeSelectSeekBar;
    private TextView drawRangeView;

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

        gpsLocationProvider = new GPSLocationProvider(mSensors, this);

        // Shared preff
        sharedPreferences = getSharedPreferences("ClarityApp", 0);
        editor = sharedPreferences.edit();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

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

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
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
            Search(getCurrentFocus());
            return true;
        } else if(id == R.id.refreshPosition) {
            gpsLocationProvider.requestUpdate();
        }

        return super.onOptionsItemSelected(item);
    }

    public void Search(View view) {
        SearchDialog searchDialog = new SearchDialog(this, pointOfInterestList);
        searchDialog.setDialogResult(new SearchDialog.OnMyDialogResult() {
            @Override
            public void finish(final PointOfInterest poi) {
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if ((zoekPOI != null)) {
                            metaioSDK.unloadGeometry(zoekPOI.getGeometry());
                        }

                        ligplaatsenCheckbox.setChecked(false);
                        aanmeerboeienCheckbox.setChecked(false);
                        meerpalenCheckbox.setChecked(false);

                        zoekPOI = poi;

                        File POIbackground = AssetsManager.getAssetPathAsFile(getApplicationContext(), "zoekPOI.png");
                        zoekPOI.setGeometry(createGeometry(poi.getCoordinate(), POIbackground, 100));
                        zoekPOI.getGeometry().setVisible(true);
                    }
                });
            }
        });
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

        setupViews();

        pointOfInterestList = new PoiList(this);
        drawGeometries();
    }

    private void drawGeometries() {
        //draw each poi in the arrayList
        for(PointOfInterest poi : pointOfInterestList) {
            //get type of POI and image
            File POIbackground = AssetsManager.getAssetPathAsFile(getApplicationContext(), poi.GetImageName());

            float[] results = new float[3];
            Location.distanceBetween(
                    poi.getCoordinate().getLatitude(),
                    poi.getCoordinate().getLongitude(),
                    mSensors.getLocation().getLatitude(),
                    mSensors.getLocation().getLongitude(),
                    results
            );

            if(results[0] < drawRange) {
                if (POIbackground != null) {
                    if(poi.getGeometry() == null) {
                        poi.setGeometry(createGeometry(poi.getCoordinate(), POIbackground, 80));
                    }
                } else {
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + POIbackground);
                }
            } else {
                if(poi.getGeometry() != null) {
                    metaioSDK.unloadGeometry(poi.getGeometry());
                    poi.setGeometry(null);
                }
            }
        }
    }

    /**
     *
     * @param coordinate - The coordinate for the geometry
     * @param iconFile - The File which you want to use for the icon
     * @param scale - The scale of the geometry
     */
    public IGeometry createGeometry(LLACoordinate coordinate, File iconFile, int scale) {
        IGeometry geometry = metaioSDK.createGeometryFromImage(iconFile, true,false);
        if(geometry != null) {
            geometry.setTranslationLLA(coordinate);
            geometry.setLLALimitsEnabled(true);
            geometry.setScale(scale);

            return geometry;
        } else {
            // todo: dit gaat niet werken, geometry altijd null, wanneer niet Null dan bovenste stuk...
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + geometry);
            return null;
        }
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {
        //todo: Maak event voor object clicks...

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Use the slide menu to go back.", Toast.LENGTH_SHORT).show();
    }

    public void setupViews() {
        menuBackButton = (Button) findViewById(R.id.backbuttonMenu);
        menuBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("choice", 0);
                editor.commit();
                Intent i = new Intent(getApplicationContext(), RoleSelector.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        meerpalenCheckbox = (CheckBox) findViewById(R.id.meerpalenCheckbox);
        meerpalenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Meerpaal)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Meerpaal)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

        ligplaatsenCheckbox = (CheckBox) findViewById(R.id.ligplaatsenCheckbox);
        ligplaatsenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Ligplaats)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Ligplaats)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

        aanmeerboeienCheckbox = (CheckBox) findViewById(R.id.aanmeerboeienCheckbox);
        aanmeerboeienCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

        drawRangeView = (TextView) findViewById(R.id.drawRangeView);

        rangeSelectSeekBar = (SeekBar) findViewById(R.id.rangeSeekbar);
        rangeSelectSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drawRange = rangeSelectSeekBar.getProgress();
                drawRangeView.setText(rangeSelectSeekBar.getProgress() + " m");
                drawGeometries();
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawRange = rangeSelectSeekBar.getProgress();
                drawRangeView.setText(drawRange + " m");
            }
        });

    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main_activity, container, false);
        }
    }
}
