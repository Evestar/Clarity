package com.riftwalkers.clarity.view.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.MetaioSurfaceView;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.Camera;
import com.metaio.sdk.jni.ERENDER_SYSTEM;
import com.metaio.sdk.jni.ESCREEN_ROTATION;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKAndroid;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.MetaioSDK;
import com.metaio.tools.Memory;
import com.metaio.tools.Screen;
import com.metaio.tools.SystemInfo;

import com.riftwalkers.clarity.R;

public abstract class AbstractARFragment extends BaseFragment implements MetaioSurfaceView.Callback, View.OnTouchListener {

    // Fragment view
    private View view;

    // Fragment layout
    private FrameLayout frameLayout;

    // metaio SDK object
    protected IMetaioSDKAndroid metaioSDK;

    // metaio SurfaceView
    protected MetaioSurfaceView mSurfaceView;

    // flag for the renderer
    protected boolean mRendererInitialized;

    // Sensor manager
    protected SensorsComponentAndroid mSensors;

    /**
     * Provide metaio SDK callback handler if desired.
     *
     * @see IMetaioSDKCallback
     *
     * @return Return metaio SDK callback handler
     */
    protected abstract IMetaioSDKCallback getMetaioSDKCallbackHandler();

    /**
     * Load rendering contents to metaio SDK in this method, e.g. 3D models, environment map etc..
     */
    protected abstract void loadContents();

    /**
     * Called when a geometry is touched.
     *
     * @param geometry Geometry that is touched
     */
    protected abstract void onGeometryTouched(IGeometry geometry);

    /**
     * Load native libs required by the Metaio SDK
     */
    protected void loadNativeLibs() throws UnsatisfiedLinkError
    {
        IMetaioSDKAndroid.loadNativeLibs();
        MetaioDebug.log(Log.INFO, "MetaioSDK libs loaded for " + SystemInfo.getDeviceABI() + " using "
                + com.metaio.sdk.jni.SystemInfo.getAvailableCPUCores() + " CPU cores");
    }

    /**
     * Start the default back facing camera. Override this to change the camera or its parameters
     * such as resolution, image flip or frame rate.
     */
    protected void startCamera()
    {
        metaioSDK.startCamera(Camera.FACE_BACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ar_fragment,container, false);
        frameLayout = (FrameLayout) view.findViewById(R.id.arFragment_FrameLayout);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MetaioDebug.log("ARFragment.onCreate()");

        metaioSDK = null;
        mSurfaceView = null;
        mRendererInitialized = false;

        try
        {
            // Load native libs
            loadNativeLibs();

            // Create Metaio SDK by passing the Activity instance and the application signature
            metaioSDK = MetaioSDK.CreateMetaioSDKAndroid(getActivity(), getResources().getString(com.metaio.R.string.metaioSDKSignature));

            if (metaioSDK == null)
            {
                throw new Exception("Unsupported platform!");
            }

            // Create and register Android sensors component
            mSensors = new SensorsComponentAndroid(getActivity().getApplicationContext());
            metaioSDK.registerSensorsComponent(mSensors);
        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR,
                    "ARFragment.onCreate: failed to create or intialize Metaio SDK: " + e.getMessage());
            getActivity().finish();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MetaioDebug.log("ARFragment.onStart");

        try
        {
            startCamera();

            // Add Unifeye GL Surface view
            mSurfaceView = new MetaioSurfaceView(getActivity());
            mSurfaceView.registerCallback(this);
            mSurfaceView.setKeepScreenOn(true);
            mSurfaceView.setOnTouchListener(this);

            MetaioDebug.log("ARFragment.onStart: addContentView(mMetaioSurfaceView)");
            frameLayout.addView(mSurfaceView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mSurfaceView.setZOrderMediaOverlay(true);
        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "Error creating views: " + e.getMessage());
            MetaioDebug.printStackTrace(Log.ERROR, e);
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();

        MetaioDebug.log("ARFragment.onResume()");

        // make sure to resume the OpenGL surface
        if (mSurfaceView != null)
            mSurfaceView.onResume();

        metaioSDK.resume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        MetaioDebug.log("ARFragment.onPause()");

        // pause the OpenGL surface
        if (mSurfaceView != null)
            mSurfaceView.onPause();

        metaioSDK.pause();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        MetaioDebug.log("ARFragment.onStop()");

        if (metaioSDK != null)
        {
            // Disable the camera
            metaioSDK.stopCamera();
        }

        if (mSurfaceView != null)
        {
            ViewGroup v = (ViewGroup) getActivity().findViewById(android.R.id.content);
            v.removeAllViews();
        }

        System.runFinalization();
        System.gc();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try
        {
            mRendererInitialized = false;
        }
        catch (Exception e)
        {
            MetaioDebug.printStackTrace(Log.ERROR, e);
        }

        MetaioDebug.log("ARFragment.onDestroy");

        if (metaioSDK != null)
        {
            metaioSDK.delete();
            metaioSDK = null;
        }

        MetaioDebug.log("ARFragment.onDestroy releasing sensors");
        if (mSensors != null)
        {
            mSensors.registerCallback(null);
            mSensors.release();
            mSensors.delete();
            mSensors = null;
        }

        Memory.unbindViews(getActivity().findViewById(android.R.id.content));
        mSurfaceView = null;

        System.runFinalization();
        System.gc();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        final ESCREEN_ROTATION rotation = Screen.getRotation(getActivity());
        metaioSDK.setScreenRotation(rotation);

        MetaioDebug.log("onConfigurationChanged: " + rotation);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            MetaioDebug.log("ARViewActivity touched at: " + event.toString());

            try
            {

                final int x = (int)event.getX();
                final int y = (int)event.getY();

                // ask the SDK if a geometry has been hit
                IGeometry geometry = metaioSDK.getGeometryFromViewportCoordinates(x, y, true);
                if (geometry != null)
                {
                    MetaioDebug.log("ARViewActivity geometry found: " + geometry);
                    onGeometryTouched(geometry);
                }

            }
            catch (Exception e)
            {
                MetaioDebug.log(Log.ERROR, "onTouch: " + e.getMessage());
                MetaioDebug.printStackTrace(Log.ERROR, e);
            }

        }

        return true;
    }

    @Override
    public void onDrawFrame()
    {
        try
        {
            if (mRendererInitialized)
                metaioSDK.render();
        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "ARFragment.onDrawFrame: Rendering failed with error " + e.getMessage());
        }
    }

    @Override
    public void onSurfaceCreated()
    {
        MetaioDebug.log("ARFragment.onSurfaceCreated: " + mSurfaceView);
        MetaioDebug.log("ARFragment.onSurfaceCreated: GL thread: " + Thread.currentThread().getId());
        try
        {
            // initialize the renderer
            if (!mRendererInitialized)
            {
                MetaioDebug.log("ARFragment.onSurfaceCreated: initializing renderer...");
                final ESCREEN_ROTATION rotation = Screen.getRotation(getActivity());
                metaioSDK.setScreenRotation(rotation);
                metaioSDK.initializeRenderer(mSurfaceView.getWidth(), mSurfaceView.getHeight(), rotation,
                        ERENDER_SYSTEM.ERENDER_SYSTEM_OPENGL_ES_2_0);
                mRendererInitialized = true;

                final IMetaioSDKCallback callback = getMetaioSDKCallbackHandler();
                if (callback != null)
                    metaioSDK.registerCallback(callback);

                // Add loadContent to the event queue to allow rendering to start
                mSurfaceView.queueEvent(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        loadContents();
                    }
                });
            }
            else
            {
                MetaioDebug.log("ARFragment.onSurfaceCreated: Reloading OpenGL resources...");
                metaioSDK.reloadOpenGLResources();
            }

        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "ARFragment.onSurfaceCreated: " + e.getMessage());
        }
    }


    @Override
    public void onSurfaceDestroyed()
    {
        MetaioDebug.log("ARFragment.onSurfaceDestroyed: " + mSurfaceView);
        mSurfaceView = null;
    }

    @Override
    public void onSurfaceChanged(int width, int height)
    {
        MetaioDebug.log("ARFragment.onSurfaceChanged: " + width + ", " + height);

        // resize renderer viewport
        metaioSDK.resizeRenderer(width, height);
    }

    @Override
    public void onLowMemory()
    {
        MetaioDebug.log(Log.ERROR, "Low memory");
        MetaioDebug.logMemory(getActivity().getApplicationContext());
    }

}
