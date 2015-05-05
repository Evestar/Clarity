package com.riftwalkers.clarity.view.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

public class MapsActivity extends Activity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private LocationManager locationManager;
    private Location lastKnown;

    private static GoogleMap map;
    private static Marker user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        // Shared preff
        sharedPreferences = getSharedPreferences("ClarityApp", 0);
        editor = sharedPreferences.edit();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        new MyLocationListener();

        setupMap();
        createMarker();
    }

    public void setupMap() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude()), 16, 0, 0)));
    }

    private void createMarker() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // user
        Bitmap userBitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255,0,255,255);
        Canvas userCanvas = new Canvas(userBitmap);
        userCanvas.drawCircle(12, 12, 12, paint);

        user = map.addMarker(
                new MarkerOptions()
                    .position(
                            new LatLng(
                                    lastKnown.getLatitude(),
                                    lastKnown.getLongitude()
                            ))
                    .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                    userBitmap
                            ))
        );

        // poi's

        // RED
        Bitmap red = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255,255,0,0);
        Canvas redCanvas = new Canvas(red);
        redCanvas.drawCircle(12, 12, 12, paint);

        // YELLOW
        Bitmap yellow = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255,255,255,0);
        Canvas yellowCanvas = new Canvas(yellow);
        yellowCanvas.drawCircle(12, 12, 12, paint);

        // GREEN
        Bitmap green = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255,0,255,0);
        Canvas greenCanvas = new Canvas(green);
        greenCanvas.drawCircle(12, 12, 12, paint);

        PoiList list = new PoiList(this);
//      for (int i = 0; i < 20; i++) { // Use this during debug!
        for (int i = 0; i < list.size(); i++) {
            PointOfInterest poi = list.get(i);

            switch (poi.getType()){
                case Boei:
                    addAnMarker(yellow, poi);
                    break;
                case Ligplaats:
                    addAnMarker(green, poi);
                    break;
                case Meerpaal:
                    addAnMarker(red, poi);
                    break;
            }
        }
    }

    private void addAnMarker(Bitmap color, PointOfInterest poi){
        map.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(
                                        poi.getCoordinate().getLatitude(),
                                        poi.getCoordinate().getLongitude()
                                ))
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        color
                                ))
                        .title(
                                String.valueOf(
                                        poi.getId()
                                ))
        );
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16, 0, 0)));
            user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

            lastKnown.setLatitude(location.getLatitude());
            lastKnown.setLongitude(location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MapsActivity.this, provider + "'s status changed to " + status + "!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MapsActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MapsActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class MyMarkerClickListener implements GoogleMap.OnMarkerClickListener {


        @Override
        public boolean onMarkerClick(Marker marker) {
            if (marker.getTitle() != null && !marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            } else if (marker.getTitle() != null){
                marker.hideInfoWindow();
            }
            return true;
        }
    }
}
