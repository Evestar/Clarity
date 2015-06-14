package com.riftwalkers.clarity.view.fragment;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ECOLOR_FORMAT;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.tools.io.AssetsManager;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;
import com.riftwalkers.clarity.data.interfaces.SearchButtonClickListener;
import com.riftwalkers.clarity.data.point_of_intrest.PoiList;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;
import com.riftwalkers.clarity.view.activities.MainActivity;
import com.riftwalkers.clarity.view.dialog.SearchDialog;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class ARFragment extends AbstractARFragment implements LocationListenerObserver, SearchButtonClickListener {

    // Draw settings
    private int POI_SCALE = 80;
    private int drawRange;

    // Data
    private ArrayList<PointOfInterest> bolderList;
    private ArrayList<PointOfInterest> ligplaatsenList;
    private ArrayList<PointOfInterest> meerpalenList;
    private HashMap<IGeometry, PointOfInterest> poiGeometryHashMap;

    // Views
    private Button menuBackButton;
    private CheckBox meerpalenCheckbox;
    private CheckBox ligplaatsenCheckbox;
    private CheckBox boldersCheckbox;
    private CheckBox nullDataCheckbox;
    private TextView drawRangeView;
    private SeekBar rangeSelectSeekBar;
    private ImageView switchbutton;
    private LinearLayout infoBox;


    /**
     * Creates the various ArrayLists+HashMap and loads data from MainActivity into them.
     */
    public ARFragment() {
        // Initialize ArrayLists and HashMap
        poiGeometryHashMap = new HashMap<>();
        bolderList = new ArrayList<>();
        ligplaatsenList = new ArrayList<>();
        meerpalenList = new ArrayList<>();

        // Load data into the ArrayLists
        for(PointOfInterest poi : MainActivity.pointOfInterestList) {
            switch (poi.getPoiType()) {
                case Bolder:
                    bolderList.add(poi);
                    break;
                case Meerpaal:
                    meerpalenList.add(poi);
                    break;
                case Ligplaats:
                    ligplaatsenList.add(poi);
                    break;
            }
        }
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    /**
     * Loads content after the OnStart event.<br/>
     * This is the place to load initial configurations.
     *
     */
    @Override
    protected void loadContents() {
        // Set GPS tracking configuration
        metaioSDK.setTrackingConfiguration("GPS");

        // Debug settings
        metaioSDK.setLLAObjectRenderingLimits(5, 200);
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

        // Set this fragment as the location provider observer
        locationProvider.addLocationListenObserver(this);

        // Setup the views and onclick listeners for the checkboxes
        setupViews();

        // Draw the geometries based on current settings
        drawGeometries();
    }

    /**
     * Gets called when the user touches a geometry.
     * @param geometry Geometry that is touched
     */
    @Override
    protected void onGeometryTouched(IGeometry geometry) {

        // Show POI info box
        getActivity().findViewById(R.id.poi_info).setVisibility(View.VISIBLE);

        // Get POI matching the geometry
        PointOfInterest linkedPoi = poiGeometryHashMap.get(geometry);

        // Setup textrows for info
        TextView row1 = (TextView)getActivity().findViewById(R.id.info_row1);
        TextView row2 = (TextView)getActivity().findViewById(R.id.info_row2);
        TextView row3 = (TextView)getActivity().findViewById(R.id.info_row3);
        TextView row4 = (TextView)getActivity().findViewById(R.id.info_row4);

        // If POI is of type 'Bolder'
        if(linkedPoi.getPoiType() == PoiType.Bolder) {
            String trekkracht;
            String verankering;
            String desciption;

            // Set type
            row1.setText("Bolder");

            // Add material to first row if known
            if(linkedPoi.getMateriaal() != null) {
                row1.setText(row1.getText() + " (" + linkedPoi.getMateriaal() + ")");
            }

            // If 'trekkracht' is known
            if(linkedPoi.getTrekkracht() != 0) {
                trekkracht = String.valueOf(linkedPoi.getTrekkracht());
            } else {
                trekkracht = "Onbekend";    // Set to 'Onbekend' if data is unknown
            }

            // if 'methode verankering' is known
            if(linkedPoi.getMethodeVerankering() != null) {
                verankering = linkedPoi.getMethodeVerankering();
            } else {
                verankering = "Onbekend";   // Set to 'Onbekend' if data is unknown
            }

            // Draw the strings
            row2.setVisibility(View.VISIBLE);
            row3.setVisibility(View.VISIBLE);
            row2.setText("Methode verankering: " + verankering);
            row3.setText("Toegestane trekkracht: " + trekkracht + "(KN)");

            // Set desciption
            if(linkedPoi.getDescription() != null) {
                desciption = linkedPoi.getDescription();
            } else {
                desciption = "Onbekend";
            }

            // Draw desciption
            row4.setVisibility(View.VISIBLE);
            row4.setText("Omschrijving: " + desciption);

        } else if(linkedPoi.getPoiType() == PoiType.Meerpaal) {     // if POI type is 'Meerpaal'
            String paalType;
            String description;

            // If type is known
            if(linkedPoi.getTypePaal() != null) {
                paalType = linkedPoi.getTypePaal();
            } else {
                paalType = "Meerpaal";
            }

            // Draw type
            row1.setText("Type paal: " + paalType);

            // If number is known
            if(linkedPoi.getNummer() != 0) {
                row1.setText(row1.getText() + " #" + linkedPoi.getNummer());
            }

            row2.setVisibility(View.GONE);

            // Material and max force
            if(linkedPoi.getMateriaal() != null) {
                row3.setVisibility(View.VISIBLE);
                row3.setText("Materiaal: " + linkedPoi.getMateriaal());

                if(linkedPoi.getTrekkracht() != 0) {
                    row3.setText(row3.getText() + "(max. " + linkedPoi.getTrekkracht() + "KN)");
                }
            }

            // Set description
            if(linkedPoi.getDescription() != null) {
                description = linkedPoi.getDescription();
            } else {
                description = "Onbekend";
            }

            // Draw description
            row4.setVisibility(View.VISIBLE);
            row4.setText("Omschrijving: " + description);

        } else if(linkedPoi.getPoiType() == PoiType.Ligplaats) {            // if POI type is 'Ligplaats'
            // Set type
            row1.setText("Ligplaats");

            // Add owner if known
            if(linkedPoi.getEigenaar() != null) {
                row1.setText(row1.getText() + "\nEigenaar: " + linkedPoi.getEigenaar());
            }

            // Draw harbor and place if known
            if(linkedPoi.getHavenNaam() != null) {
                row2.setVisibility(View.VISIBLE);
                row2.setText("Haven: " + linkedPoi.getHavenNaam());
                if(linkedPoi.getXmeTXT() != null) {
                    row3.setVisibility(View.VISIBLE);
                    row3.setText("Plaats: " + linkedPoi.getXmeTXT());
                }
            } else {
                row2.setVisibility(View.GONE);
                row3.setVisibility(View.GONE);
            }

            // Draw front number if known
            if(linkedPoi.getOeverFrontNummer() != null) {
                row4.setVisibility(View.VISIBLE);
                row4.setText("Oever front nummber: " + linkedPoi.getOeverFrontNummer());
            } else {
                row4.setVisibility(View.GONE);
            }

        }
    }

    /**
     * Is called when the user returns to the app
     */
    @Override
    public void onResume() {
        super.onResume();
        locationProvider.requestUpdate();       // Request update to make sure the geometries are redrawn in the right location.
    }

    /**
     * Draws the geometries based on settings like if it is Searching, if checkboxes are checked and if objects are in range
     */
    private void drawGeometries() {

        // Check if we are searching for a POI
        if(MainActivity.isSearchingFromMaps){
            searchFromMaps();
        } else {        // If we are not searching

            // Check if bolders need to be drawn
            if(boldersCheckbox.isChecked()) {
                for (PointOfInterest poi : bolderList) {
                    checkIfShouldDrawPOI(poi);
                }
            } else {
                for(PointOfInterest poi : bolderList) {
                    checkIfPOIGeometryNotNull(poi);         // Check if there is still geometry for this object. If so, delete it
                }
            }

            // Check if 'ligplaatsen' need to be drawm
            if(ligplaatsenCheckbox.isChecked()) {
                for(PointOfInterest poi : ligplaatsenList) {
                    checkIfShouldDrawPOI(poi);
                }
            } else {
                for(PointOfInterest poi : ligplaatsenList) {
                    checkIfPOIGeometryNotNull(poi);         // Check if there is still geometry for this object. If so, delete it
                }
            }

            // Check if 'meerpalen' need to be drawn
            if(meerpalenCheckbox.isChecked()) {
                for(PointOfInterest poi : meerpalenList) {
                    checkIfShouldDrawPOI(poi);
                }
            } else {
                for(PointOfInterest poi : meerpalenList) {
                    checkIfPOIGeometryNotNull(poi);         // Check if there is still geometry for this object. If so, delete it
                }
            }
        }
    }

    /**
     * Checks if the geometry needs to be generated, based on drawrange
     * @param poi The POI for which it checks
     */
    private void checkIfShouldDrawPOI(PointOfInterest poi) {
        // Get distance to POI
        int distance = getDistance(poi);

        // Check if POI is in range, based on range from the slider
        if (distance < drawRange) {

            // If there is no geometry for this object yet, create it
            if (poi.getGeometry() == null) {
                //get type of POI and image
                File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), poi.GetImageName());       // Get the right image for the POI

                createPOIGeometry(poi, POIbackground, distance);    // Generate it
            }
        } else {
            checkIfPOIGeometryNotNull(poi);     // Check if there is still geometry for this object. If so, delete it
        }
    }

    /**
     * Checks if the geometry is already null. If not, it unloads the geometry
     * @param poi The POI for which it checks
     */
    private void checkIfPOIGeometryNotNull(PointOfInterest poi) {
        // Check if geometry is null yet
        if (poi.getGeometry() != null) {
            metaioSDK.unloadGeometry(poi.getGeometry());                // Unload it from metaio

            if(poiGeometryHashMap.containsKey(poi.getGeometry())) {
                poiGeometryHashMap.remove(poi.getGeometry());           // Remove it from list of currently drawn objects
            }

            poi.setGeometry(null);                                      // Set geometry to null
        }
    }

    /**
     * Creates coordinates with which metaio can work. After creating the geometry if places a reference to the geometry in the HashMap.
     * @param poi Point of Interest for which a geometry needs to be created
     * @param poiBackground Image to load onto the geometry as base image
     * @param distance Distance to the POI
     */
    private void createPOIGeometry(PointOfInterest poi, File poiBackground, int distance) {
        if (poiBackground != null) {
            LLACoordinate coordinate = new LLACoordinate(
                    poi.getCoordinates().get(0).getLatitude(),
                    poi.getCoordinates().get(0).getLongitude(),
                    0,
                    0);

            // Generate geometry
            poi.setGeometry(createGeometry(coordinate, poiBackground, POI_SCALE, String.valueOf(poi.getId()), distance));

            // Place reference to the geometry in the HashMap
            poiGeometryHashMap.put(poi.getGeometry(), poi);
        }
    }

    /**
     * Called from drawGeometries when the user is searching for a POI
     */
    private void searchFromMaps(){
        // Disable drawing of other objects
        ligplaatsenCheckbox.setChecked(false);
        meerpalenCheckbox.setChecked(false);
        boldersCheckbox.setChecked(false);

        // Get distance to POI
        int distance = getDistance(MainActivity.SearchedPOI);

        if (MainActivity.SearchedPOI.getGeometry() == null) {
            File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), "zoekPOI.png");    // Use a special image for the POI

            createPOIGeometry(MainActivity.SearchedPOI, POIbackground, distance);
        }
    }

    /**
     * Gets the distance between the user and the POI
     * @param poi POI for which the distance needs to be calculated
     * @return Distance in meters
     */
    private int getDistance(PointOfInterest poi){
        float[] results = new float[3];
        Location.distanceBetween(
                poi.getCoordinates().get(0).getLatitude(),
                poi.getCoordinates().get(0).getLongitude(),
                mSensors.getLocation().getLatitude(),
                mSensors.getLocation().getLongitude(),
                results
        );

        return (int) results[0];
    }

    /**
     * Creates the geometry and places it in the world
     * @param coordinate Coordinates of the POI, as LLACoordinate
     * @param iconFile Image to use as a base
     * @param scale Scale of the geometry
     * @param id Id of the POI, is placed in the center of the image
     * @param distance Distance to the POI, is placed below the base image
     * @return
     */
    public IGeometry createGeometry(LLACoordinate coordinate, File iconFile, int scale, String id, int distance) {
        // Load base image
        Bitmap bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());

        // Draw id onto the base image
        bitmap = drawTextToBitmap(bitmap, id, distance);

        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 4);
        bitmap.copyPixelsToBuffer(byteBuffer);

        // Create an ImageStruct object, this is for metaio
        ImageStruct texture = new ImageStruct(byteBuffer.array(), bitmap.getWidth(), bitmap.getHeight(), ECOLOR_FORMAT.ECF_RGBA8, true, (System.currentTimeMillis() / 1000));

        // Generate geometry from the base image
        IGeometry geometry = metaioSDK.createGeometryFromImage(id, texture, true,false);

        // If previous operation was succesfull, set up the geometry
        if(geometry != null) {
            geometry.setTranslationLLA(coordinate);
            geometry.setLLALimitsEnabled(true);
            geometry.setScale(scale);
        } else {
            // Log an error if generation of geometry was not succesfull. The user does not see this.
            MetaioDebug.log(Log.ERROR, "Error loading geometry");
        }

        // Release resources used for drawing the bitmap
        bitmap.recycle();
        bitmap = null;

        // Return geometry object, or null if the creation did not succeed
        return geometry;
    }

    /**
     * Set up the views, like checkboxes and buttons. Add onclick listeners to it.
     */
    public void setupViews() {
        // Setup 'back to menu' button
        menuBackButton = (Button) getActivity().findViewById(R.id.backbuttonMenu);
        menuBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("choice", 0);
                editor.commit();
                if(fragmentListener != null)
                    fragmentListener.ChangeFragment(RoleSelectorFragment.class);
            }
        });

        // Setup 'meerpalen' checkbox
        meerpalenCheckbox = (CheckBox) getActivity().findViewById(R.id.meerpalenCheckbox);
        meerpalenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.isSearchingFromMaps = false;
                    MainActivity.SearchedPOI = null;
                }

                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        drawGeometries();
                    }
                });
            }
        });

        // Setup 'ligplaatsen' checkbox
        ligplaatsenCheckbox = (CheckBox) getActivity().findViewById(R.id.ligplaatsenCheckbox);
        ligplaatsenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.isSearchingFromMaps = false;
                    MainActivity.SearchedPOI = null;
                }

                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        drawGeometries();
                    }
                });
            }
        });

        // Setup bolders checkbox
        boldersCheckbox = (CheckBox) getActivity().findViewById(R.id.boldersCheckbox);
        boldersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.isSearchingFromMaps = false;
                    MainActivity.SearchedPOI = null;
                }

                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        drawGeometries();
                    }
                });
            }
        });

        // Setup 'load object with unknown data' checkbox
        nullDataCheckbox = (CheckBox) getActivity().findViewById(R.id.showNullers);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ClarityApp",0);
        if(sharedPreferences.getBoolean("showAllData",true)) {
            nullDataCheckbox.setChecked(true);
        } else {
            nullDataCheckbox.setChecked(false);
        }
        nullDataCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("showAllData",isChecked);
                editor.commit();

                MainActivity.pointOfInterestList = new PoiList(getActivity());
                if(fragmentListener != null)
                    fragmentListener.ChangeFragment(ARFragment.class);
            }
        });

        // Setup range slider
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView rangeLabel = (TextView) getActivity().findViewById(R.id.rangeLabel);
                rangeLabel.setVisibility(View.VISIBLE);

                drawRangeView = (TextView) getActivity().findViewById(R.id.drawRangeView);
                drawRangeView.setVisibility(View.VISIBLE);

                rangeSelectSeekBar = (SeekBar) getActivity().findViewById(R.id.rangeSeekbar);
                rangeSelectSeekBar.setVisibility(View.VISIBLE);
                rangeSelectSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        drawRangeView.setText(rangeSelectSeekBar.getProgress() + " m");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        drawRange = rangeSelectSeekBar.getProgress();
                        mSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                drawGeometries();
                            }
                        });
                    }
                });

                drawRange = rangeSelectSeekBar.getProgress();
                drawRangeView.setText(drawRange + " m");
            }
        });

        // Setup switchbutton to switch to Maps
        switchbutton = (ImageView) getActivity().findViewById(R.id.switchbuttonAR);
        switchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentListener != null)
                    fragmentListener.ChangeFragment(MapsFragment.class);
            }
        });

        // Set up info box
        infoBox = (LinearLayout) getActivity().findViewById(R.id.poi_info);
        infoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoBox.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Unloads all geometries. Is called when the user wants to search for a POI
     */
    private void unloadAllGeometries(){
        final ArrayList<PointOfInterest> removeList = new ArrayList<>();

        for (PointOfInterest poi: poiGeometryHashMap.values()){
            removeList.add(poi);
        }

        for(final PointOfInterest poi : removeList) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    checkIfPOIGeometryNotNull(poi);
                }
            });
        }
    }

    /**
     * Gets called if there is a location update from location provider. Sets location in metaio manually.
     * @param location The new location of the user
     */
    @Override
    public void observerOnLocationChanged(Location location) {
        if(mSensors != null)
            mSensors.setManualLocation(new LLACoordinate(location.getLatitude(), location.getLongitude(), 0, 0));
    }

    /**
     * Called when the user presses the search button in the top bar.
     * Shows the search dialog and handles the 'search' event from the dialog.
     */
    @Override
    public void onSearchClick() {
        SearchDialog searchDialog = new SearchDialog(getActivity(), MainActivity.pointOfInterestList);
        searchDialog.setDialogResult(new SearchDialog.OnMyDialogResult() {
            @Override
            public void finish(final PointOfInterest poi) {
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if ((MainActivity.SearchedPOI != null)) {
                            checkIfPOIGeometryNotNull(MainActivity.SearchedPOI);
                        }

                        unloadAllGeometries();

                        MainActivity.SearchedPOI = poi;
                        MainActivity.isSearchingFromMaps = true;

                        drawGeometries();
                    }
                });
            }
        });
        searchDialog.show();
    }

    /**
     * Helper function to draw the POI id to the center of the base image
     * @param baseImage Base image, text gets drawn on top of this
     * @param text Text, the POI id, to draw on top of the base image
     * @param distance The distance to the POI, gets drawn in a white box on the bottom
     * @return The bitmap which was generated
     */
    public Bitmap drawTextToBitmap(Bitmap baseImage, String text, int distance) {

        Bitmap.Config bitmapConfig = baseImage.getConfig();

        // Set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }

        // Create a mutable copy of the bitmap
        baseImage = baseImage.copy(bitmapConfig, true);

        // Create a canvas from the base image
        Canvas canvas = new Canvas(baseImage);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);     // Use anti-aliasing
        paint.setColor(Color.rgb(61, 61, 61));              // The text color, #3D3D3D
        paint.setTextSize(24);                              // Text size in pixels
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));    // Make the text bold

        // Draw text to the canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (baseImage.getWidth() - bounds.width())/2;
        int y = (baseImage.getHeight() + bounds.height())/3;
        canvas.drawText(text, x, y, paint);

        // Create rectangle for the distance
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(40, ((baseImage.getHeight() / 5) * 4), baseImage.getWidth()-40, baseImage.getHeight(), paint);

        // Draw the distance onto the rectangle
        paint.setColor(Color.BLUE);
        bounds = new Rect();
        text = String.valueOf(distance) + " m";
        paint.getTextBounds(text, 0, text.length(), bounds);
        x = (baseImage.getWidth() - bounds.width())/2;
        y = baseImage.getHeight()-12;
        canvas.drawText(String.valueOf(distance) + " m", x, y, paint);

        // Return generated image
        return baseImage;
    }
}
