package com.riftwalkers.clarity.view.fragment;

import android.app.Fragment;

import com.riftwalkers.clarity.data.GPSLocationProvider;
import com.riftwalkers.clarity.data.interfaces.ChangeFragmentListener;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;

public abstract class BaseFragment extends Fragment {
    protected ChangeFragmentListener fragmentListener;
    protected GPSLocationProvider locationProvider;

    public void setFragmentListener(ChangeFragmentListener changeFragmentListener) {
        fragmentListener = changeFragmentListener;
    }

    public void unsetFragmentListener() {
        fragmentListener = null;
    }

    public void setLocationProvider(GPSLocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }
}
