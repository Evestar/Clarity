package com.riftwalkers.clarity.data;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;

import java.util.ArrayList;

public class GPSLocationProvider implements LocationListener {

    private Activity activity;
    private LocationManager locationManager;
    private Location lastKnownLocation;

    private ArrayList<LocationListenerObserver> locationListenObservers;

    public GPSLocationProvider(Activity activity) {
        this.activity = activity;

        locationListenObservers = new ArrayList<>();

        locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        for(LocationListenerObserver locationListenerObserver : locationListenObservers) {
            locationListenerObserver.observerOnLocationChanged(location);
        }

        lastKnownLocation = location;

        System.out.println("Location updated!");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void requestUpdate() {
        locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, this, null);
    }

    public void addLocationListenObserver(LocationListenerObserver locationListenerObserver) {
        locationListenObservers.add(locationListenerObserver);
    }

    public void removeLocationListenObserver(LocationListenerObserver locationListenerObserver) {
        locationListenObservers.remove(locationListenerObserver);
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}
