package com.riftwalkers.clarity.view.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;

import com.riftwalkers.clarity.data.GPSLocationProvider;
import com.riftwalkers.clarity.data.interfaces.ChangeFragmentListener;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;

public abstract class BaseFragment extends Fragment {
    protected ChangeFragmentListener fragmentListener;
    protected GPSLocationProvider locationProvider;

    protected SharedPreferences.Editor editor;

    public void setFragmentListener(ChangeFragmentListener changeFragmentListener) {
        fragmentListener = changeFragmentListener;
    }

    public void unsetFragmentListener() {
        fragmentListener = null;
    }

    public void setLocationProvider(GPSLocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }
}
