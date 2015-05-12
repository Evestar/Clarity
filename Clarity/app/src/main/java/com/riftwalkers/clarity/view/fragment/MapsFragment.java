package com.riftwalkers.clarity.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback,LocationListenerObserver,Runnable {

    private MapFragment map;
    private GoogleMap googleMap;

    private Marker user;

    Thread thread;
    private boolean active = true;

    long lastMovedTime;
    boolean hasMoved = false;
    long currentTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.maps_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapContent));
        map.getMapAsync(this);

        //locationProvider.addLocationListenObserver(this);

        thread = new Thread(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (map != null)
            getFragmentManager().beginTransaction().remove(map).commit();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(locationProvider.getLastKnownLocation().getLatitude(), locationProvider.getLastKnownLocation().getLongitude()), 16, 0, 0)));

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                lastMovedTime = currentTime;
                hasMoved = true;
            }
        });

        LatLngBounds bounds = googleMap.getProjection()
                .getVisibleRegion().latLngBounds;
        createMarker(bounds);

        thread.start();
    }

    @Override
    public void observerOnLocationChanged(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16, 0, 0)));
        user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void createMarker(LatLngBounds bounds) {
        googleMap.clear();

        Location sw = new Location("");
        sw.setLongitude(bounds.southwest.longitude);
        sw.setLatitude(bounds.southwest.latitude);

        Location ne = new Location("");
        ne.setLongitude(bounds.northeast.longitude);
        ne.setLatitude(bounds.northeast.latitude);


        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // user
        Bitmap userBitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, 0, 255, 255);
        Canvas userCanvas = new Canvas(userBitmap);
        userCanvas.drawCircle(12, 12, 12, paint);

        user = googleMap.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(
                                        locationProvider.getLastKnownLocation().getLatitude(),
                                        locationProvider.getLastKnownLocation().getLongitude()
                                ))
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        userBitmap
                                ))
        );

        // poi's

        // RED
        Bitmap red = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, 255, 0, 0);
        Canvas redCanvas = new Canvas(red);
        redCanvas.drawCircle(12, 12, 12, paint);

        // YELLOW
        Bitmap yellow = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, 255, 255, 0);
        Canvas yellowCanvas = new Canvas(yellow);
        yellowCanvas.drawCircle(12, 12, 12, paint);

        // GREEN
        Bitmap green = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, 0, 255, 0);
        Canvas greenCanvas = new Canvas(green);
        greenCanvas.drawCircle(12, 12, 12, paint);

        PoiList list = new PoiList(getActivity());
//      for (int i = 0; i < 20; i++) { // Use this during debug!
        for (int i = 0; i < list.size(); i++) {
            PointOfInterest poi = list.get(i);

            if (poi.getCoordinate().getLatitude() > sw.getLatitude() &&
                    poi.getCoordinate().getLatitude() < ne.getLatitude() &&
                    poi.getCoordinate().getLongitude() > sw.getLongitude() &&
                    poi.getCoordinate().getLongitude() < ne.getLongitude()) {

                switch (poi.getType()) {
                    case Boei:
                        addAnMarker(yellow, poi, "Buoy: [" + poi.getId() + "]");
                        break;
                    case Ligplaats:
                        addAnMarker(green, poi, "Berth: [" + poi.getId() + "]");
                        break;
                    case Meerpaal:
                        addAnMarker(red, poi, "Boulder: [" + poi.getId() + "]");
                        break;
                }
            }
        }
    }

    private void addAnMarker(Bitmap color, PointOfInterest poi, String title) {
        googleMap.addMarker(
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
                        .title(title)
        );
    }

    @Override
    public void run() {
        while (active) {
            currentTime = System.currentTimeMillis();
            if (hasMoved) {
                if (lastMovedTime + 500 < currentTime) {

                    // Drawing of makers
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LatLngBounds bounds = googleMap.getProjection()
                                    .getVisibleRegion().latLngBounds;
                            Log.wtf("MOVED AND STOPPED: ", String.valueOf(bounds));

                            createMarker(bounds);
                        }
                    });

                    hasMoved = false;
                }
            }
        }
    }
}
