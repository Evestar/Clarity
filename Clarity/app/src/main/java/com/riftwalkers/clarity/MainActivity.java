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
    private ArrayList<PointOfInterest> pointOfInterestList;

    @Override
    protected int getGUILayout() {
        return R.layout.activity_inapp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp);
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

        PointOfInterest schip1 = new PointOfInterest();
        schip1.setId(1);
        schip1.setDescription("schip info x meter");
        schip1.setCoordinate(new LLACoordinate(51.916801, 4.482380, 0, 0));
        schip1.setType(PoiType.Schip);
        pointOfInterestList.add(schip1);

        PointOfInterest bolder1 = new PointOfInterest();
        bolder1.setId(2);
        bolder1.setDescription("bolder info x meter");
        bolder1.setCoordinate(new LLACoordinate(51.919855, 4.489412, 0, 0));
        bolder1.setType(PoiType.Bolder);
        pointOfInterestList.add(bolder1);

        PointOfInterest bolder2 = new PointOfInterest();
        bolder2.setId(3);
        bolder2.setDescription("bolder info x meter");
        bolder2.setCoordinate(new LLACoordinate(51.917648, 4.483057, 0, 0));
        bolder2.setType(PoiType.Bolder);
        pointOfInterestList.add(bolder2);

        PointOfInterest boei1 = new PointOfInterest();
        boei1.setId(4);
        boei1.setDescription("boei info x meter");
        boei1.setCoordinate(new LLACoordinate(51.904794,4.484548, 0, 0));
        boei1.setType(PoiType.Boei);
        pointOfInterestList.add(boei1);

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
}
