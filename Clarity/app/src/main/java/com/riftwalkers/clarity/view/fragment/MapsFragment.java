package com.riftwalkers.clarity.view.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"UnusedAssignment", "FieldCanBeLocal", "ConstantConditions"})
public class MapsFragment extends BaseFragment implements OnMapReadyCallback,LocationListenerObserver,Runnable,SearchButtonClickListener {

    private MapFragment map;                                //
    private GoogleMap googleMap;                            //
    private Marker user;                                    //

    private Thread thread;                                  //
    private static boolean mapsActive = true;               //
    private long lastMovedTime;                             //
    private boolean hasMoved = false;                       //
    private boolean hasClicked = false;                     //
    private long currentTime;                               //

    private PoiList pointOfInterestList;                    //
    private PoiList tempPoiList;                            //
    private PointOfInterest zoekPOI;                        //

    private Button menuBackButton;                          //
    private CheckBox meerpalenCheckbox;                     //
    private CheckBox ligplaatsenCheckbox;                   //
    private CheckBox boldersCheckbox;                       //
    private CheckBox nullerCheckbox;                        //
    private ImageView switchbutton;                         //

    private Marker LastMarkerClicked;                       //
    private int clicks = 0;                                 //
    private HashMap<Marker, PointOfInterest> savedMarkers;  //

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

        savedMarkers = new HashMap<>();
        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (map != null)
            getFragmentManager().beginTransaction().remove(map).commit();

    }

    /**
     * <p>OnMapReady runs when the activity is done loading the mapfragment.
     * It sets all the settings for the map and moves to camera to the starting position</p>
     *
     * @param googleMap The ready GoogleMap.
     */
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
        createMarkers(bounds);


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            /**
             * <p>OnMarkerClick runs on the markerClick event.
             * It shows the infoWindow of the clicked marker and can be used to start the search function in the AR-Fragment</p>
             *
             * @param marker the clicked marker.
             * @return true if a marker has been click, false if you click on the user-marker.
             */
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
                    MainActivity.isSearchingFromMaps = true;

                    MainActivity.SearchedPOI = savedMarkers.get(marker);

                    fragmentListener.ChangeFragment(ARFragment.class);
                }

                clicks++;
                return true;
            }
        });

        thread.start();
    }

    /**
     * <p>ObserverOnLocationChanged checks if the users location has been changed.
     * It moves the camera to the new location and moves the marker. </p>
     *
     * @param location The GPS location (holding latitude and longitude) of users new location
     */
    @Override
    public void observerOnLocationChanged(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 16, 0, 0)));
        user.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    //////////////////////////////////////////////////
    //                 MARKER STUFF                 //
    //////////////////////////////////////////////////

    /**
     * <p>CreateMarkers creates a marker of each point of interest within the users screen (bounds).
     * This function is also responsible for the marker of the user itself.</p>
     *
     * @param bounds The GPS location from the North-East corner until the South-West corner of the screen
     */
    private void createMarkers(LatLngBounds bounds) {
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

            address = addresses.get(0).getPostalCode() + ", " + addresses.get(0).getLocality() + "\n" + addresses.get(0).getCountryName();
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

                createSingleMarker(poi);

                if (zoekPOI != null) {
                    addAnMarker(getMarkerBitmap(255, 0, 255), zoekPOI, String.valueOf(zoekPOI.getId()), "");
                }

            }
        }
    }

    /**
     * createSingleMarker creates one marker and places it on the Map.
     *
     * @param poi The point of interest which is going to be drawn to the map
     */
    private void createSingleMarker(PointOfInterest poi) {

        float[] results = new float[3];
        Location.distanceBetween(
                poi.getCoordinates().get(0).getLatitude(),
                poi.getCoordinates().get(0).getLongitude(),
                user.getPosition().latitude,
                user.getPosition().longitude,
                results
        );
        int distance = (int) results[0];

        switch (poi.getPoiType()) {
            case Ligplaats:
                addAnMarker(
                        getMarkerBitmap(0, 155, 0),
                        poi,
                        getMarkerTitle(poi, distance),
                        getMarkerSnippet(poi)
                );
                break;

            case Bolder:
                addAnMarker(
                        getMarkerBitmap(230, 99, 24),
                        poi,
                        getMarkerTitle(poi, distance),
                        getMarkerSnippet(poi)
                );

                break;
        }
    }

    /**
     * GetMarkerSnippet is used to get a snippet for the according point of interest types
     *
     * @param poi The point of interest that requests a Snippet
     * @return A snippet with the description and values inside of the Point Of Interest
     */
    private String getMarkerSnippet(PointOfInterest poi) {

        return String.valueOf(poi.getPoiType())
                .concat("");
    }

    /**
     * @param poi This is the PointOfInterest which requests the title
     * @param distance This is the distance between the User and the given Point Of Interest.
     * @return A String of text holding both an ID and the Distance.
     */
    private String getMarkerTitle(PointOfInterest poi, int distance) {
        String title = "";

        title = title.concat(String.valueOf(poi.getId()))
                .concat(" " + distance);

        return title;
    }

    /**
     * <p>GetMarkerBitmap is fired when a marker is created.
     * It created a Circle bitmap with the color that is given in RGB</p>
     *
     * @param red   R value in RGB
     * @param green G value in RGB
     * @param blue  B value in RGB
     * @return Bitmap circle that can be drawn into a map
     */
    private Bitmap getMarkerBitmap(int red, int green, int blue) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        paint.setARGB(255, red, green, blue);
        Canvas redCanvas = new Canvas(bitmap);
        redCanvas.drawCircle(12, 12, 12, paint);

        return bitmap;
    }

    /**
     * <p>AddAnMarker combines everything together about a marker.
     * When needed it wil provide a polygon for the size of a "Ligplaats"</p>
     *
     * @param color   The bitmap created by getMarkerBitmap(r,g,b)
     * @param poi     Point of Interest used to get location of the maker and its type
     * @param title   Title of the marker (holds the name of the Point of interest)
     * @param snippet Description of a marker (holds information about a Point of Interest)
     */
    private void addAnMarker(Bitmap color, PointOfInterest poi, String title, String snippet) {

        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < poi.getCoordinates().size(); i++) {
            locations.add(poi.getCoordinates().get(i));
        }

        Location center = getCentralCoordinate(locations);

        Marker m = googleMap.addMarker(
                new MarkerOptions()
                        .position(
                                new LatLng(
                                        center.getLatitude(),
                                        center.getLongitude()
                                ))
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        color
                                ))
                        .title(title)
                        .snippet(snippet)
        );
        savedMarkers.put(m, poi);

        if (poi.getPoiType() == PoiType.Ligplaats) {

            PolygonOptions polygonOptions = new PolygonOptions();

            for (int i = 0; i < locations.size() - 1; i++) {
                polygonOptions.add(
                    new LatLng(
                        locations.get(i).getLatitude(),
                        locations.get(i).getLongitude()
                    )
                );
            }

            googleMap.addPolygon(polygonOptions);
        }
    }

    /**
     * <p>Calculates the center of a Array(list) of Locations. If only one location is within the ArrayList, it returns just the one.</p>
     *
     * @param locations A list of locations.
     * @return The center of the locations whitin the given list.
     */
    private Location getCentralCoordinate(ArrayList<Location> locations) {
        if (locations.size() == 1) {
            return locations.get(0);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (int i = 0; i < locations.size()-1; i++) {
            double latitude = locations.get(i).getLatitude() * Math.PI / 180;
            double longitude = locations.get(i).getLongitude() * Math.PI / 180;

            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }

        double total = locations.size();

        x = x / total;
        y = y / total;
        z = z / total;

        double centralLongitude = Math.atan2(y, x);
        double centralSquareRoot = Math.sqrt(x * x + y * y);
        double centralLatitude = Math.atan2(z, centralSquareRoot);

        Location center = new Location("");
        center.setLatitude(centralLatitude * 180 / Math.PI);
        center.setLongitude(centralLongitude * 180 / Math.PI);

        return center;
    }

    //////////////////////////////////////////////////
    //                 SWIPE THREAD                 //
    //////////////////////////////////////////////////

    /**
     * <p>This thread runs during the use of the mapsFragment.
     * It checks if the user has moved the map to redraw the markers within its new bounds.</p>
     */
    @Override
    public void run() {
        while (mapsActive) {
            currentTime = System.currentTimeMillis();
            if (hasMoved && !hasClicked) {
                if (lastMovedTime + 500 < currentTime) {

                    // Drawing of makers
                    getActivity().runOnUiThread(new Runnable() {
                        /**
                         * <p>Runnable over UI thread to draw whenever the user stopped swiping</p>
                         */
                        @Override
                        public void run() {
                            LatLngBounds bounds = googleMap.getProjection()
                                    .getVisibleRegion().latLngBounds;
                            Log.wtf("MOVED AND STOPPED: ", String.valueOf(bounds));

                            createMarkers(bounds);
                        }
                    });

                    hasMoved = false;
                }
            } else if (hasClicked) {
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

    /**
     * <p>SetActive is used when the fragment is changed. It stops the Thread from running unnecessarily when other fragments are in use.</p>
     *
     * @param active new state of active.
     */
    public static void setActive(boolean active) {
        mapsActive = active;
    }

    //////////////////////////////////////////////////
    //                  VIEW STUFF                  //
    //////////////////////////////////////////////////

    /**
     * <p>SetupViews gets all the interactive buttons and checkboxes and binds them to a listener.</p>
     */
    public void setupViews() {
        meerpalenCheckbox = (CheckBox) getActivity().findViewById(R.id.meerpalenCheckbox);
        meerpalenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * <p>onCheckedChanged is called whenever the user checks or unchecked the checkbox.
             * <p/>This function deletes or adds all the markers into the temporary array.
             * </p>
             *
             * @param buttonView The button or checkbox that was clicked. Might be used to change settings on this checkbox.
             * @param isChecked The value of the checkbox
             */
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
            /**
             * <p>onCheckedChanged is called whenever the user checks or unchecked the checkbox.
             * <p/>This function deletes or adds all the markers into the temporary array.
             * </p>
             *
             * @param buttonView The button or checkbox that was clicked. Might be used to change settings on this checkbox.
             * @param isChecked The value of the checkbox
             */
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
            /**
             * <p>onCheckedChanged is called whenever the user checks or unchecked the checkbox.
             * <p/>This function deletes or adds all the markers into the temporary array.
             * </p>
             *
             * @param buttonView The button or checkbox that was clicked. Might be used to change settings on this checkbox.
             * @param isChecked The value of the checkbox
             */
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ClarityApp",0);
        if(sharedPreferences.getBoolean("showAllData",true)) {
            nullerCheckbox.setChecked(true);
        } else {
            nullerCheckbox.setChecked(false);
        }
        nullerCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * <p>The nullerCheckbox can be changed by the user to use or don't use PointsOfInterests that have missing data.</p>
             *
             * @param buttonView The button or checkbox that was clicked. Might be used to change settings on this checkbox.
             * @param isChecked The value of the checkbox
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("showAllData",isChecked);
                editor.commit();

                MainActivity.pointOfInterestList = new PoiList(getActivity());

                if (map != null)
                    getFragmentManager().beginTransaction().remove(map).commit();

                if(fragmentListener != null)
                    fragmentListener.ChangeFragment(MapsFragment.class);
                if (isChecked) {
                    tempPoiList.clear();
                    boldersCheckbox.setChecked(false);
                    meerpalenCheckbox.setChecked(false);
                    ligplaatsenCheckbox.setChecked(false);

                    for (PointOfInterest poi : pointOfInterestList) {
                        switch (poi.getPoiType()) {

                            case Bolder:
                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
                                    tempPoiList.add(poi);
                                }
                                break;
                            case Meerpaal:
                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
                                    tempPoiList.add(poi);
                                }
                                break;
                            case Ligplaats:
                                if (poi.getDescription() != null && poi.getTrekkracht() != 0 && poi.getMateriaal() != null) {
                                    tempPoiList.add(poi);
                                }
                                break;
                            default: /* nothing, just nothing */
                                break;

                        }
                    }
                }
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            /**
             * <p>This inner thread is used to update the range label, for maps this means it will be invisible</p>
             */
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

        menuBackButton = (Button) getActivity().findViewById(R.id.backbuttonMenu);
        menuBackButton.setOnClickListener(new View.OnClickListener() {
            /**
             * <p>Changes the fragment back to the RoleSelectorFragment</p>
             */
            @Override
            public void onClick(View v) {
                editor.putInt("choice", 0);
                editor.commit();
                if (fragmentListener != null)
                    fragmentListener.ChangeFragment(RoleSelectorFragment.class);
            }
        });

        switchbutton = (ImageView) getView().findViewById(R.id.switchbuttonMaps);
        switchbutton.setOnClickListener(new View.OnClickListener() {
            /**
             * <p>Switches the fragment to the AR fragment.</p>
             */
            @Override
            public void onClick(View v) {
                if (fragmentListener != null)
                    fragmentListener.ChangeFragment(ARFragment.class);
            }
        });

    }

    /**
     * <p>DrawMarkers is a helper function to draw markers when a checkbox has changed from value.</p>
     */
    public void drawMarkers() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLngBounds bounds = googleMap.getProjection()
                        .getVisibleRegion().latLngBounds;

                createMarkers(bounds);
            }
        });
    }

    /**
     * <p>onSearchClick handles the search functionality in the maps.
     * <p/>
     * When the users searches for an marker via the search dialog,
     * this function handles all the information that needs to be processed.</p>
     */
    @Override
    public void onSearchClick() {
        SearchDialog searchDialog = new SearchDialog(getActivity(), pointOfInterestList);
        searchDialog.setDialogResult(new SearchDialog.OnMyDialogResult() {
            @Override
            public void finish(final PointOfInterest poi) {

                zoekPOI = poi;

                meerpalenCheckbox.setChecked(false);
                ligplaatsenCheckbox.setChecked(false);
                boldersCheckbox.setChecked(false);

                tempPoiList.add(zoekPOI);

                createMarkers(googleMap.getProjection()
                        .getVisibleRegion().latLngBounds);

                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(zoekPOI.getCoordinates().get(0).getLatitude(), zoekPOI.getCoordinates().get(0).getLongitude()), 16, 0, 0)));
            }
        });
        searchDialog.show();
    }

    //////////////////////////////////////////////////
    //          CUSTOM INFO WINDOW ADAPTER          //
    //////////////////////////////////////////////////

    /**
     * <p>Private inner class that makes a custom window info adapter.</p>
     */
    private class MyCustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View myMarkerView;

        /**
         * <p>When instantiating the Custom infoWindowAdapter, this constructor gets the right layout for the custom marker view.</p>
         */
        @SuppressLint("InflateParams")
        private MyCustomInfoWindowAdapter() {
            this.myMarkerView = getActivity().getLayoutInflater().inflate(R.layout.custom_infowindow_layout, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        /**
         * <p>GetInfoContents opens up the infoWindow of the marker, this includes the Title as well as the Snippet
         * The custom info window is used to display data in a way the normal InfoWoindAdapter doest not allow.</p>
         *
         * @param marker The given marker equals a marker that has resently been clicked
         * @return It returns the View of the infoWindow with the right text.
         */
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
