package com.riftwalkers.clarity.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SearchDialog extends Dialog implements AdapterView.OnItemSelectedListener {
    // Listener for 'search'
    private OnMyDialogResult mDialogResult;

    // Activity from which the dialog spawns
    private Activity activity;

    // Views
    private Spinner objectTypeSpinner;
    private Spinner areaSpinner;
    private Spinner objectSpinner;
    private Button button;

    // ArrayAdapters
    private ArrayAdapter<CharSequence> objectTypeSpinnerAdapter;
    private ArrayAdapter areaSpinnerAdapter;
    private PointOfInterestAdapter objectSpinnerAdapter;

    // ArrayLists
    private ArrayList<PointOfInterest> boldersArray;
    private ArrayList<PointOfInterest> koningsPalenArray;
    private ArrayList<PointOfInterest> hogePalenArray;
    private ArrayList<PointOfInterest> ligplaatsenArray;
    private ArrayList<String> areasArray;

    /**
     * Set the app to fullscreen
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Constructor
     * @param activity Activity from which the search dialog is spawned
     * @param pointOfInterests List of POI's
     */
    public SearchDialog(Activity activity, ArrayList<PointOfInterest> pointOfInterests) {
        super(activity);

        this.activity = activity;
        this.setContentView(R.layout.search_dialog);        // Set custom layout
        this.setTitle(R.string.search_dialog_title);        // Set title from strings.xml

        // Initialize ArrayLists
        boldersArray = new ArrayList<>();
        koningsPalenArray = new ArrayList<>();
        hogePalenArray = new ArrayList<>();
        ligplaatsenArray = new ArrayList<>();

        // Split POI list in sub arrayLists for fast processing
        for(PointOfInterest poi: pointOfInterests) {
            if(poi.getPoiType().equals(PoiType.Meerpaal)) {
                if(poi.getDescription() != null) {
                    String[] descriptionData = poi.getDescription().split(" ");
                    String paalType = descriptionData[descriptionData.length-1].substring(0, 2);

                    if(paalType.equals("KP")) {
                        koningsPalenArray.add(poi);
                    } else if(paalType.equals("HP")) {
                        hogePalenArray.add(poi);
                    }
                }
            } else if(poi.getPoiType().equals(PoiType.Ligplaats)) {
                if(poi.getLxmeTXT() != null) {
                    ligplaatsenArray.add(poi);
                }
            } else {
                if(poi.getDescription() != null) {
                    boldersArray.add(poi);
                }
            }
        }

        // Setup spinners
        objectTypeSpinner = (Spinner) findViewById(R.id.objectTypeSpinner);
        objectSpinner = (Spinner) findViewById(R.id.objectSpinner);
        areaSpinner = (Spinner) findViewById(R.id.areaSpinner);
        button = (Button) findViewById(R.id.button);

        // Setup objectType spinner
        objectTypeSpinnerAdapter = ArrayAdapter.createFromResource(activity, R.array.drawer_array, android.R.layout.simple_spinner_item);
        objectTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        objectTypeSpinner.setAdapter(objectTypeSpinnerAdapter);
        objectTypeSpinner.setOnItemSelectedListener(this);

        // Setup 'Search' button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mDialogResult != null ){
                    mDialogResult.finish((PointOfInterest) objectSpinner.getSelectedItem());
                }
                SearchDialog.this.dismiss();
            }
        });
    }

    /**
     * Called when an item is selected from a spinner
     * @param parent View from which the event is called
     * @param position Position of the item in the list
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // If the event is called in the objectType spinner
        if(parent == objectTypeSpinner) {

            // Hide other views if nothing is selected
            if(position == 0) {
                areaSpinner.setVisibility(View.GONE);
                objectSpinner.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
            } else if(position == 1) {
                System.out.println("Bolders selected");
                fillAreaSpinner(boldersArray);
            } else if(position == 2) {
                System.out.println("Hoge palen selected");
                fillAreaSpinner(hogePalenArray);
            } else if(position == 3) {
                System.out.println("Koningspalen selected");
                fillAreaSpinner(koningsPalenArray);
            } else if(position == 4) {
                System.out.println("Ligplaatsen selected");
                fillAreaSpinner(ligplaatsenArray);
            }
        } else if(parent == areaSpinner) {      // If event comes from area spinner
            if(position == 0) {
                objectSpinner.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
            } else {
                if(objectSpinner.getVisibility() == View.GONE) {
                    objectSpinner.setVisibility(View.GONE);
                }

                if(objectTypeSpinner.getSelectedItemPosition() == 1) {
                    fillObjectsSpinner(areasArray, boldersArray);
                } else if(objectTypeSpinner.getSelectedItemPosition() == 2) {
                    fillObjectsSpinner(areasArray, hogePalenArray);
                } else if(objectTypeSpinner.getSelectedItemPosition() == 3) {
                    fillObjectsSpinner(areasArray, koningsPalenArray);
                } else if(objectTypeSpinner.getSelectedItemPosition() == 4) {
                    fillObjectsSpinner(areasArray, ligplaatsenArray);
                }
            }
        }
    }

    /**
     * Fill area spinner with area's from sourceArray
     * @param sourceArray Array from which the area must be looked up
     */
    private void fillAreaSpinner(ArrayList<PointOfInterest> sourceArray) {
        areasArray = new ArrayList<>();

        for(PointOfInterest poi : sourceArray) {
            String area = "";

            // If Bolder, 'Koningspaal' or 'Hoge paal': find area in desciption
            if((sourceArray == hogePalenArray) || (sourceArray == koningsPalenArray) || (sourceArray == boldersArray)) {
                String[] description = poi.getDescription().split(" ");

                for (int i = 0; i < description.length - 1; i++) {
                    area += description[i] + " ";
                }
            } else if(sourceArray == ligplaatsenArray) {
                area = poi.getHavenNaam();      // 'Ligplaatsen' has a property for area
            }

            area.trim();

            // If area not already known, add it to areas array
            if(!areasArray.contains(area)) {
                areasArray.add(area);
            }
        }

        // Sort the areas array
        Collections.sort(areasArray, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        // Add placeholder
        areasArray.add(0,"Gebied..");

        // Set up area spinner
        areaSpinnerAdapter = new ArrayAdapter(activity, R.layout.searchbox_spinner_listview, areasArray);
        areaSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaSpinnerAdapter);
        areaSpinner.setOnItemSelectedListener(this);

        areaSpinner.setVisibility(View.VISIBLE);
    }

    /**
     * Fill objects spinner with object from selected area
     * @param areasArray Area array from with the selected are is chosen
     * @param pointOfInterests POI array from which the objects must be loaded
     */
    private void fillObjectsSpinner(ArrayList<String> areasArray, ArrayList<PointOfInterest> pointOfInterests) {
        ArrayList<PointOfInterest> pointOfInterestArrayList = new ArrayList<>();
        String poiArea = "";

        for (String area : areasArray) {
            if(areaSpinner.getSelectedItem().equals(area)) {
                poiArea = area;
            }
        }

        for (PointOfInterest poi : pointOfInterests) {
                if(poi.getPoiType() == PoiType.Ligplaats) {
                    if(poi.getHavenNaam().equals(poiArea)) {
                        pointOfInterestArrayList.add(poi);
                    }
                } else {
                    if(poi.getDescription().contains(poiArea)) {
                        pointOfInterestArrayList.add(poi);
                    }
                }
        }

        objectSpinnerAdapter = new PointOfInterestAdapter(activity, R.layout.searchbox_spinner_listview, pointOfInterestArrayList);
        objectSpinner.setAdapter(objectSpinnerAdapter);
        objectSpinner.setOnItemSelectedListener(this);

        objectSpinner.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Sets the object which observers the search event
     * @param dialogResult Object which implements OnMyDialogResult interface
     */
    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    /**
     * OnMyDialogResult<br/>
     * Interface for search event listener
     */
    public interface OnMyDialogResult{
        void finish(PointOfInterest poi);
    }
}
