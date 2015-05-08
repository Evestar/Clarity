package com.riftwalkers.clarity.data;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.LLACoordinate;

public class GPSLocationProvider implements LocationListener {

    private SensorsComponentAndroid mSensors;
    private Activity activity;
    private LocationManager locationManager;

    public GPSLocationProvider(SensorsComponentAndroid sensorsComponentAndroid, Activity activity) {
        mSensors = sensorsComponentAndroid;
        this.activity = activity;

        locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mSensors.setManualLocation(new LLACoordinate(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 0,0));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        mSensors.setManualLocation(new LLACoordinate(location.getLatitude(), location.getLongitude(), 0,0));
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
}
