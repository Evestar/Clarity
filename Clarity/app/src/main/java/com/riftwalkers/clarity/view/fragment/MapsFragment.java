package com.riftwalkers.clarity.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolygonOptions;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;
import com.riftwalkers.clarity.data.interfaces.SearchButtonClickListener;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;
import com.riftwalkers.clarity.view.activities.MainActivity;
import com.riftwalkers.clarity.view.dialog.SearchDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"UnusedAssignment", "FieldCanBeLocal", "ConstantConditions"})
public class MapsFragment extends BaseFragment implements OnMapReadyCallback,LocationListenerObserver,Runnable,SearchButtonClickListener {

    private MapFragment map;
    private GoogleMap googleMap;
    private Marker user;

    private Thread thread;
    private static boolean mapsActive = true;
    private long lastMovedTime;
    private boolean hasMoved = false;
    private boolean hasClicked = false;
    private long currentTime;

    private PoiList pointOfInterestList;
    private PoiList tempPoiList;
    private PointOfInterest zoekPOI;

    private Button menuBackButton;
    private CheckBox meerpalenCheckbox;
    private CheckBox ligplaatsenCheckbox;
    private CheckBox nullerCheckbox;
    private CheckBox boldersCheckbox;
    private ImageView switchbutton;

    private Marker LastMarkerClicked;
    private int clicks = 0;
    private LinkedHashMap<Marker, PointOfInterest> savedMarkers;


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
        pointOfInterestList = MainActivity.pointOfInterestList;
        tempPoiList = (PoiList) pointOfInterestList.clone();

        savedMarkers = new LinkedHashMap<>();
        setupViews();
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

        MyCustomInfoWindowAdapter customInfoWindowAdapter = new MyCustomInfoWindowAdapter();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.setInfoWindowAdapter(customInfoWindowAdapter);
        googleMap.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(
                                new LatLng(                                                     // Location
                                        locationProvider.getLastKnownLocation().getLatitude(),  // Latitude
                                        locationProvider.getLastKnownLocation().getLongitude()),// Longitude
                                16,                                                             // Zoom level
                                0,                                                              // Tilt level
                                0)                                                              // Bearing level
                )
        );

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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                hasClicked = true;

                if (marker.getTitle().equals("you")) {
                    return false;
                }

                Log.wtf("CLICKED MARKER ", String.valueOf(marker));

                if (LastMarkerClicked == null || (!LastMarkerClicked.equals(marker))) {
                    LastMarkerClicked = marker;
                    System.out.println(LastMarkerClicked);
                    marker.showInfoWindow();
                    if (clicks % 5 == 0) {
                        Toast.makeText(getActivity(), "Tap again to open Augmented Reality", Toast.LENGTH_SHORT).show();
                    }
                } else if (LastMarkerClicked.equals(marker)) {
                    // SAME MARKER -> SEARCH!
                    ((MainActivity) getActivity()).isSearchingFromMaps = true;

                    PointOfInterest searchPOI = savedMarkers.get(marker);
                    ((MainActivity) getActivity()).SearchedPOI = searchPOI;

                    fragmentListener.ChangeFragment(ARFragment.class);
                }

                clicks++;
                return true;
            }
        });

        thread.start();
    }

    @Override
    public void observerOnLocationChanged(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16, 0, 0)));
        user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void createMarker(LatLngBounds bounds) {
        googleMap.clear();
        savedMarkers.clear();

        Location sw = new Location("");
        sw.setLongitude(bounds.southwest.longitude);
        sw.setLatitude(bounds.southwest.latitude);

        Location ne = new Location("");
        ne.setLongitude(bounds.northeast.longitude);
        ne.setLatitude(bounds.northeast.latitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        LatLng lastKnown = new LatLng(
                locationProvider.getLastKnownLocation().getLatitude(),
                locationProvider.getLastKnownLocation().getLongitude()
        );
        String address = "Locatie informatie niet beschikbaar";
        try {
            addresses = geocoder.getFromLocation(lastKnown.latitude, lastKnown.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getPostalCode() +", "+ addresses.get(0).getLocality() +"\n"+addresses.get(0).getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }



        user = googleMap.addMarker(
                new MarkerOptions()
                        .position(lastKnown)
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        getMarkerBitmap(102, 0, 102)
                                )
                        )
                        .title("you")
                        .snippet(address)
        );

        // poi's
        for (int i = 0; i < tempPoiList.size(); i++) {
            PointOfInterest poi = tempPoiList.get(i);

            if (poi.getCoordinates().get(0).getLatitude() > sw.getLatitude() &&
                    poi.getCoordinates().get(0).getLatitude() < ne.getLatitude() &&
                    poi.getCoordinates().get(0).getLongitude() > sw.getLongitude() &&
                    poi.getCoordinates().get(0).getLongitude() < ne.getLongitude()) {

                float[] results = new float[3];
                Location.distanceBetween(
                        poi.getCoordinates().get(0).getLatitude(),
                        poi.getCoordinates().get(0).getLongitude(),
                        user.getPosition().latitude,
                        user.getPosition().longitude,
                        results
                );
                int distance = (int) results[0];

                String snippet;

                switch (poi.getPoiType()) {
                    case Ligplaats:

                        snippet = "";

                        if(poi.getEigenaar()!=null){
                            snippet += "Eigenaar : "+poi.getEigenaar()+"\n";
                        } else {
                            snippet += "Eigenaar : onbekend\n";
                        }
                        if(poi.getHavenNaam()!=null){
                            snippet += "Haven : "+poi.getHavenNaam()+"\n";
                        } else {
                            snippet += "Haven : onbekend\n";
                        }

                        addAnMarker(
                                getMarkerBitmap(0,155,0),
                                poi,
                                String.valueOf(poi.getPoiType()) + " ('" + distance +"m')",
                                snippet
                        );
                        break;
                    case Meerpaal:

                        snippet = "";

                        if(poi.getTypePaal()!=null){
                            snippet += "Type paal: "+poi.getTypePaal()+"\n";
                        } else {
                            snippet += "Type paal: onbekend\n";
                        }

                        if(poi.getTrekkracht() != 0 ){
                            snippet += "Trekkracht : "+poi.getTrekkracht()+"KN\n";
                        } else {
                            snippet += "Trekkracht : onbekend\n";
                        }

                        if(poi.getPaalHaven() != null){
                            snippet += "Haven : "+poi.getPaalHaven();
                        } else {
                            snippet += "Haven : onbekend";
                        }

                        if(poi.getPaalNummer() != null){
                            addAnMarker(
                                    getMarkerBitmap(255,0,0),
                                    poi,
                                    String.valueOf(poi.getPaalNummer()) + " ('" + distance +"m')",
                                    snippet
                            );
                        }
                        break;
                    case Bolder:

                        snippet = "";

                        if(poi.getMateriaal()!=null){
                            snippet += "Matriaal : "+poi.getMateriaal()+"\n";
                        } else {
                            snippet += "Matriaal : onbekend\n";
                        }
                        if(poi.getTrekkracht() != 0 ){
                            snippet += "Trekkracht : "+poi.getTrekkracht()+"";
                        } else {
                            snippet += "Haven : onbekend";
                        }

                        if(poi.getPaalNummer() != null){
                            addAnMarker(
                                    getMarkerBitmap(230,99,24),
                                    poi,
                                    String.valueOf(poi.getPaalNummer()) + " ('" + distance +"m')",
                                    snippet
                            );
                        }
                        break;
                }

                if(zoekPOI != null) {
                    addAnMarker(getMarkerBitmap(255,0,255), zoekPOI, String.valueOf(zoekPOI.getId()),"");
                }


            }
        }
    }

    private String getMarkerSnippet(PointOfInterest poi) {
        String snippet = "";

        if(poi.getDescription() != null) {
            snippet += poi.getDescription()+"\n";
        }

        return snippet;
    }

    private Bitmap getMarkerBitmap(int red, int green, int blue) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, red, green, blue);
        Canvas redCanvas = new Canvas(bitmap);
        redCanvas.drawCircle(12, 12, 12, paint);

        return bitmap;
    }

    private void addAnMarker(Bitmap color, PointOfInterest poi, String title, String snippet) {
        Marker[] markers = new Marker[poi.getCoordinates().size()];

        markers[0] = googleMap.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(
                                        poi.getCoordinates().get(0).getLatitude(),
                                        poi.getCoordinates().get(0).getLongitude()
                                ))
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        color
                                ))
                        .title(title)
                        .snippet(snippet)
        );
        savedMarkers.put(markers[0],poi);

        if(poi.getPoiType() == PoiType.Ligplaats) {
            for (int i = 1; i < markers.length-1; i++) {
                markers[i] = googleMap.addMarker(
                        new MarkerOptions()
                                .position(
                                        new LatLng(
                                                poi.getCoordinates().get(i).getLatitude(),
                                                poi.getCoordinates().get(i).getLongitude()
                                        ))
                                .visible(false)
                );
            }

            PolygonOptions polygonOptions = new PolygonOptions();

            for (int i = 0; i < markers.length-1; i++) {
                polygonOptions.add(markers[i].getPosition());
            }

            googleMap.addPolygon(polygonOptions);
        }
    }

    @Override
    public void run() {
        while (mapsActive) {
            currentTime = System.currentTimeMillis();
            if (hasMoved && !hasClicked) {
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
            } else if(hasClicked){
                try {
                    Thread.sleep(500);
                    hasMoved = false;
                    hasClicked = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setupViews() {
        menuBackButton = (Button) getActivity().findViewById(R.id.backbuttonMenu);
        menuBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("choice", 0);
                editor.commit();
                if (fragmentListener != null)
                    fragmentListener.ChangeFragment(RoleSelectorFragment.class);
            }
        });

        meerpalenCheckbox = (CheckBox) getActivity().findViewById(R.id.meerpalenCheckbox);
        meerpalenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {
                    ArrayList<PointOfInterest> tempList = new ArrayList<>();
                    for (PointOfInterest poi : tempPoiList) {
                        if (poi.getPoiType().equals(PoiType.Meerpaal)) {
                            tempList.add(poi);
                        }
                    }
                    tempPoiList.removeAll(tempList);
                    tempList = null;
                } else {
                    nullerCheckbox.setChecked(false);
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getPoiType().equals(PoiType.Meerpaal)) {
                            tempPoiList.add(poi);
                        }
                    }
                }
                drawMarkers();
            }
        });

        ligplaatsenCheckbox = (CheckBox) getActivity().findViewById(R.id.ligplaatsenCheckbox);
        ligplaatsenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    ArrayList<PointOfInterest> removeList = new ArrayList<>();
                    for (PointOfInterest poi : tempPoiList) {
                        if (poi.getPoiType().equals(PoiType.Ligplaats)) {
                            removeList.add(poi);
                        }
                    }
                    tempPoiList.removeAll(removeList);
                    removeList = null;
                } else {
                    nullerCheckbox.setChecked(false);
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getPoiType().equals(PoiType.Ligplaats)) {
                            tempPoiList.add(poi);
                        }
                    }
                }
                drawMarkers();
            }
        });

        boldersCheckbox = (CheckBox) getActivity().findViewById(R.id.boldersCheckbox);
        boldersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    ArrayList<PointOfInterest> removeList = new ArrayList<>();
                    for (PointOfInterest poi : tempPoiList) {
                        if (poi.getPoiType().equals(PoiType.Bolder)) {
                            removeList.add(poi);
                        }
                    }
                    tempPoiList.removeAll(removeList);
                    removeList = null;
                } else {
                    nullerCheckbox.setChecked(false);
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getPoiType().equals(PoiType.Bolder)) {
                            tempPoiList.add(poi);
                        }
                    }
                }
                drawMarkers();
            }
        });

        nullerCheckbox = (CheckBox) getActivity().findViewById(R.id.showNullers);
        nullerCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    tempPoiList.clear();

                    boldersCheckbox.setChecked(false);
                    meerpalenCheckbox.setChecked(false);
                    ligplaatsenCheckbox.setChecked(false);

//                    for (PointOfInterest poi : pointOfInterestList) {
//                        switch (poi.getPoiType()) {
//
//                            case Bolder:
//                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
//                                    tempPoiList.add(poi);
//                                }
//                                break;
//                            case Meerpaal:
//                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
//                                    tempPoiList.add(poi);
//                                }
//                                break;
//                            case Ligplaats:
//                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
//                                    tempPoiList.add(poi);
//                                }
//                                break;
//                            default: /* nothing, just nothing */
//                                break;
//                        }
//                    }
                }
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView rangeLabel = (TextView) getActivity().findViewById(R.id.rangeLabel);
                rangeLabel.setVisibility(View.GONE);

                TextView drawRangeView = (TextView) getActivity().findViewById(R.id.drawRangeView);
                drawRangeView.setVisibility(View.GONE);

                SeekBar rangeSelectSeekBar = (SeekBar) getActivity().findViewById(R.id.rangeSeekbar);
                rangeSelectSeekBar.setVisibility(View.GONE);
            }
        });

        switchbutton = (ImageView) getView().findViewById(R.id.switchbuttonMaps);

        switchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentListener != null)
                    fragmentListener.ChangeFragment(ARFragment.class);
            }
        });

    }

    public void drawMarkers() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLngBounds bounds = googleMap.getProjection()
                        .getVisibleRegion().latLngBounds;

                createMarker(bounds);
            }
        });
    }

    public static void setActive(boolean active) {
        mapsActive = active;
    }

    @Override
    public void onSearchClick() {
        SearchDialog searchDialog = new SearchDialog(getActivity(), pointOfInterestList);
        searchDialog.setDialogResult(new SearchDialog.OnMyDialogResult() {
            @Override
            public void finish(final PointOfInterest poi) {

                zoekPOI = poi;

                meerpalenCheckbox.setChecked(false);
//                aanmeerboeienCheckbox.setChecked(false);
                ligplaatsenCheckbox.setChecked(false);
                boldersCheckbox.setChecked(false);

                tempPoiList.add(zoekPOI);

                createMarker(googleMap.getProjection()
                        .getVisibleRegion().latLngBounds);

                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(zoekPOI.getCoordinates().get(0).getLatitude(), zoekPOI.getCoordinates().get(0).getLongitude()), 16, 0, 0)));
            }
        });
        searchDialog.show();
    }

    private class MyCustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View myMarkerView;

        private MyCustomInfoWindowAdapter() {
            this.myMarkerView = getActivity().getLayoutInflater().inflate(R.layout.custom_infowindow_layout, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }


        @Override
        public View getInfoContents(Marker marker) {
            TextView title = (TextView) myMarkerView.findViewById(R.id.titleview);
            TextView info = (TextView) myMarkerView.findViewById(R.id.infoview);

            title.setText(marker.getTitle());
            info.setText(marker.getSnippet());

            return myMarkerView;
        }
    }
}
