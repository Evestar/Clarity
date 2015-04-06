package com.riftwalkers.clarity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.concurrent.locks.Lock;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;


public class MainActivity extends ARViewActivity {

    private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;

    private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;

    //Static geometries
    private IGeometry mObl1Geo;
    private IGeometry mObl2Geo;

    private IRadar mRadar;

    @Override
<<<<<<< HEAD
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Set GPS tracking configuration
        boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
        MetaioDebug.log("Tracking data loaded: " + result);
=======
    protected int getGUILayout() {
        return R.layout.activity_inapp;
>>>>>>> origin/master
    }

    @Override
    protected void onDestroy()
    {
        // Break circular reference of Java objects
        if (mAnnotatedGeometriesGroup != null)
        {
            mAnnotatedGeometriesGroup.registerCallback(null);
        }

        if (mAnnotatedGeometriesGroupCallback != null)
        {
            mAnnotatedGeometriesGroupCallback.delete();
            mAnnotatedGeometriesGroupCallback = null;
        }

        super.onDestroy();
    }

    @Override
    public void onDrawFrame()
    {
        if (metaioSDK != null && mSensors != null)
        {
            SensorValues sensorValues = mSensors.getSensorValues();

            float heading = 0.0f;
            if (sensorValues.hasAttitude())
            {
                float m[] = new float[9];
                sensorValues.getAttitude().getRotationMatrix(m);

                Vector3d v = new Vector3d(m[6], m[7], m[8]);
                v.normalize();

                heading = (float)(-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
            }

            IGeometry geos[] = new IGeometry[] {mObl1Geo, mObl2Geo};
            Rotation rot = new Rotation((float)(Math.PI / 2.0), 0.0f, -heading);
            for (IGeometry geo : geos)
            {
                if (geo != null)
                {
                    geo.setRotation(rot);
                }
            }
        }

        super.onDrawFrame();
    }

    public void onButtonClick(View v)
    {
        finish();
    }

    @Override
    protected int getGUILayout()
    {
        return R.layout.activity_main;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return null;
    }

    @Override
<<<<<<< HEAD
    protected void loadContents()
    {
        mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
        mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
        mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

        // Clamp geometries' Z position to range [5000;200000] no matter how close or far they are
        // away.
        // This influences minimum and maximum scaling of the geometries
        metaioSDK.setLLAObjectRenderingLimits(5, 200);

        // Set render frustum accordingly
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

        // create LLA objects
        LLACoordinate obl1 = new LLACoordinate(51.829671, 4.400168, 0, 0);
        LLACoordinate obl2 = new LLACoordinate(51.829369, 4.401981, 0, 0);

        // Load POIs. Each of them has the same shape at its geoposition. We pass a string
        // (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI
        // title
        // in the callback, in order to create an annotation image with the title on it.
        mObl1Geo = createPOIGeometry(obl1);
        mAnnotatedGeometriesGroup.addGeometry(mObl1Geo, "Obl1");

        mObl2Geo = createPOIGeometry(obl2);
        mAnnotatedGeometriesGroup.addGeometry(mObl2Geo, "Obl2");

        // create radar
        mRadar = metaioSDK.createRadar();
        mRadar.setBackgroundTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                "Main/Assets/radar.png"));
        mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                "Main/Assets/yellow.png"));
        mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

        // add geometries to the radar
        mRadar.add(mObl1Geo);
        mRadar.add(mObl2Geo);
    }

    private IGeometry createPOIGeometry(LLACoordinate lla)
    {
        final File path =
                AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "Main/Assets/ExamplePOI.obj");
        if (path != null)
        {
            IGeometry geo = metaioSDK.createGeometry(path);
            geo.setTranslationLLA(lla);
            geo.setLLALimitsEnabled(true);
            geo.setScale(100);
            return geo;
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
            return null;
        }
    }


    @Override
    protected void onGeometryTouched(final IGeometry geometry)
    {
        MetaioDebug.log("Geometry selected: " + geometry);

        mSurfaceView.queueEvent(new Runnable()
        {

            @Override
            public void run()
            {
                mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "Main/Assets/yellow.png"));
                mRadar.setObjectTexture(geometry, AssetsManager.getAssetPathAsFile(getApplicationContext(),
                        "Main/Assets/red.png"));
                mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
            }
        });
    }

    final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback
    {
        Bitmap mAnnotationBackground, mEmptyStarImage, mFullStarImage;
        int mAnnotationBackgroundIndex;
        ImageStruct texture;
        String[] textureHash = new String[1];
        TextPaint mPaint;
        Lock geometryLock;


        Bitmap inOutCachedBitmaps[] = new Bitmap[] {mAnnotationBackground, mEmptyStarImage, mFullStarImage};
        int inOutCachedAnnotationBackgroundIndex[] = new int[] {mAnnotationBackgroundIndex};

        public MyAnnotatedGeometriesGroupCallback()
        {
            mPaint = new TextPaint();
            mPaint.setFilterBitmap(true); // enable dithering
            mPaint.setAntiAlias(true); // enable anti-aliasing
        }

        @Override
        public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData, IGeometry existingAnnotation)
        {
            if (userData == null)
            {
                return null;
            }

            if (existingAnnotation != null)
            {
                // We don't update the annotation if e.g. distance has changed
                return existingAnnotation;
            }

            String title = (String)userData; // as passed to addGeometry
            LLACoordinate location = geometry.getTranslationLLA();
            float distance = (float)MetaioCloudUtils.getDistanceBetweenTwoCoordinates(location, mSensors.getLocation());
            Bitmap thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.launcher);
            try
            {
                texture =
                        ARELInterpreterAndroidJava.getAnnotationImageForPOI(title, title, distance, "5", thumbnail,
                                null,
                                metaioSDK.getRenderSize(), MainActivity.this,
                                mPaint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, textureHash);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (thumbnail != null)
                    thumbnail.recycle();
                thumbnail = null;
            }

            mAnnotationBackground = inOutCachedBitmaps[0];
            mEmptyStarImage = inOutCachedBitmaps[1];
            mFullStarImage = inOutCachedBitmaps[2];
            mAnnotationBackgroundIndex = inOutCachedAnnotationBackgroundIndex[0];

            IGeometry resultGeometry = null;

            if (texture != null)
            {
                if (geometryLock != null)
                {
                    geometryLock.lock();
                }

                try
                {
                    // Use texture "hash" to ensure that SDK loads new texture if texture changed
                    resultGeometry = metaioSDK.createGeometryFromImage(textureHash[0], texture, true, false);
                }
                finally
                {
                    if (geometryLock != null)
                    {
                        geometryLock.unlock();
                    }
                }
            }

            return resultGeometry;
        }

        @Override
        public void onFocusStateChanged(IGeometry geometry, Object userData, EGEOMETRY_FOCUS_STATE oldState,
                                        EGEOMETRY_FOCUS_STATE newState)
        {
            MetaioDebug.log("onFocusStateChanged for " + (String)userData + ", " + oldState + "->" + newState);
        }
=======
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
>>>>>>> origin/master
    }
}
