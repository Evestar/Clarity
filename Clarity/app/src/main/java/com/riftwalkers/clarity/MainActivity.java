package com.riftwalkers.clarity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;


public class MainActivity extends ARViewActivity {

    @Override
    protected int getGUILayout() {
        return R.layout.activity_inapp;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {

    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp);

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
}
