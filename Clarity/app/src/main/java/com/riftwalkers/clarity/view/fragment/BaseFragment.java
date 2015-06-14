package com.riftwalkers.clarity.view.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;

import com.riftwalkers.clarity.data.GPSLocationProvider;
import com.riftwalkers.clarity.data.interfaces.ChangeFragmentListener;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;

/**
 * Base class from which the fragments extend. Implements some base functionality like location and switching.
 */
public abstract class BaseFragment extends Fragment {
    protected ChangeFragmentListener fragmentListener;
    protected GPSLocationProvider locationProvider;

    protected SharedPreferences.Editor editor;

    // Set object which needs to known when this event occurs. Used to switch fragments
    public void setFragmentListener(ChangeFragmentListener changeFragmentListener) {
        fragmentListener = changeFragmentListener;
    }

    // Unset the fragment listener
    public void unsetFragmentListener() {
        fragmentListener = null;
    }

    // Set object which needs to know about location updates from the location provider
    public void setLocationProvider(GPSLocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    // Unset location observer
    public void unsetLocationProvider() {
        this.locationProvider = null;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }
}
