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
}
