package com.riftwalkers.clarity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;


public class MainActivity extends ARViewActivity {

    @Override
    protected int getGUILayout() {
        return R.layout.activity_inapp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp);

        new AssetsExtracter().execute(0);

        createListView();
    }

    public void createListView() {
        //Create list of items
        String[] items = {"Meerpalen", "Schepen", "Incidenten"};

        // build Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.listview_items,
                items
        );

        // Config the list view
        ListView list = (ListView) findViewById(R.id.legend);

        list.setAdapter(adapter);
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

        LLACoordinate rotterdam = new LLACoordinate(51.924161, 4.475128, 0, 0);

        File POIbackground =
                AssetsManager.getAssetPathAsFile(getApplicationContext(), "ExamplePOI.png");
        if (POIbackground != null)
        {
            IGeometry rotterdamGeo = metaioSDK.createGeometryFromImage(POIbackground,true,false);
            if (rotterdamGeo != null)
            {
                rotterdamGeo.setTranslationLLA(rotterdam);
                rotterdamGeo.setLLALimitsEnabled(true);
                rotterdamGeo.setScale(100);
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
}
