package com.riftwalkers.clarity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;

import android.util.Log;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
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

        //new AssetsExtracter().execute(0);

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
        MetaioDebug.log("Tracking data loaded: " + result);

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
