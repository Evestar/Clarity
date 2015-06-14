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
    private OnMyDialogResult mDialogResult;

    private Activity activity;

    private Spinner objectTypeSpinner;
    private Spinner areaSpinner;
    private Spinner objectSpinner;
    private Button button;

    private ArrayAdapter<CharSequence> objectTypeSpinnerAdapter;
    private ArrayAdapter areaSpinnerAdapter;
    private PointOfInterestAdapter objectSpinnerAdapter;

    private ArrayList<PointOfInterest> boldersArray;
    private ArrayList<PointOfInterest> koningsPalenArray;
    private ArrayList<PointOfInterest> hogePalenArray;
    private ArrayList<PointOfInterest> ligplaatsenArray;

    private ArrayList<String> areasArray;

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

    public SearchDialog(Activity activity, ArrayList<PointOfInterest> pointOfInterests) {
        super(activity);
        this.activity = activity;
        this.setContentView(R.layout.search_dialog);
        this.setTitle(R.string.search_dialog_title);

        boldersArray = new ArrayList<>();
        koningsPalenArray = new ArrayList<>();
        hogePalenArray = new ArrayList<>();
        ligplaatsenArray = new ArrayList<>();

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

        objectTypeSpinner = (Spinner) findViewById(R.id.objectTypeSpinner);
        objectSpinner = (Spinner) findViewById(R.id.objectSpinner);
        areaSpinner = (Spinner) findViewById(R.id.areaSpinner);
        button = (Button) findViewById(R.id.button);

        objectTypeSpinnerAdapter = ArrayAdapter.createFromResource(activity, R.array.drawer_array, android.R.layout.simple_spinner_item);
        objectTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        objectTypeSpinner.setAdapter(objectTypeSpinnerAdapter);
        objectTypeSpinner.setOnItemSelectedListener(this);

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



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent == objectTypeSpinner) {
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
        } else if(parent == areaSpinner) {
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

    private void fillAreaSpinner(ArrayList<PointOfInterest> sourceArray) {
        areasArray = new ArrayList<>();

        for(PointOfInterest poi : sourceArray) {
            String area = "";

            if((sourceArray == hogePalenArray) || (sourceArray == koningsPalenArray) || (sourceArray == boldersArray)) {
                String[] description = poi.getDescription().split(" ");

                for (int i = 0; i < description.length - 1; i++) {
                    area += description[i] + " ";
                }
            } else if(sourceArray == ligplaatsenArray) {
                area = poi.getHavenNaam();
            }

            area.trim();

            if(!areasArray.contains(area)) {
                areasArray.add(area);
            }
        }

        Collections.sort(areasArray, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        areasArray.add(0,"Gebied..");

        areaSpinnerAdapter = new ArrayAdapter(activity, R.layout.searchbox_spinner_listview, areasArray);
        areaSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(areaSpinnerAdapter);
        areaSpinner.setOnItemSelectedListener(this);

        areaSpinner.setVisibility(View.VISIBLE);
    }

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

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(PointOfInterest poi);
    }
}
