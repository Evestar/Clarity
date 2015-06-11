package com.riftwalkers.clarity.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

import java.util.ArrayList;

public class SearchDialog extends Dialog {
    private OnMyDialogResult mDialogResult;

    private Spinner spinner;
    private Spinner spinner2;
    private Button button;

    private ArrayAdapter<CharSequence> spinnerAdapter;
    private ArrayAdapter<CharSequence> spinner2Adapter;

    private ArrayList<PointOfInterest> boeienArray;
    private ArrayList<PointOfInterest> palenArray;
    private ArrayList<PointOfInterest> ligplaatsenArray;

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

    public SearchDialog(final Context context, ArrayList<PointOfInterest> pointOfInterests) {
        super(context);
        this.setContentView(R.layout.search_dialog);
        this.setTitle(R.string.search_dialog_title);

        boeienArray = new ArrayList<>();
        palenArray = new ArrayList<>();
        ligplaatsenArray = new ArrayList<>();

        for(PointOfInterest poi: pointOfInterests) {
            if(poi.getPoiType().equals(PoiType.Meerpaal)) {
                palenArray.add(poi);
            } else if(poi.getPoiType().equals(PoiType.Ligplaats)) {
                ligplaatsenArray.add(poi);
            } else {
                boeienArray.add(poi);
            }
        }

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        button = (Button) findViewById(R.id.button);

        setSpinnerAdapter(ArrayAdapter.createFromResource(context, R.array.drawer_array, android.R.layout.simple_spinner_item));
        getSpinnerAdapter().setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getSpinner().setAdapter(getSpinnerAdapter());

        getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((id != 0) && (getSpinner2().getVisibility() == View.GONE)) {
                    getSpinner2().setVisibility(View.VISIBLE);
                    getButton().setVisibility(View.VISIBLE);
                }

                if (id == 0) {
                    getSpinner2().setVisibility(View.GONE);
                    getButton().setVisibility(View.GONE);
                }

                if (id == 1) {
                    setSpinner2Adapter(new ArrayAdapter(context, R.layout.searchbox_spinner_listview, boeienArray));
                } else if (id == 2) {
                    setSpinner2Adapter(new ArrayAdapter(context, R.layout.searchbox_spinner_listview, ligplaatsenArray));
                } else {
                    setSpinner2Adapter(new ArrayAdapter(context, R.layout.searchbox_spinner_listview, palenArray));
                }

                getSpinner2Adapter().setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                getSpinner2().setAdapter(getSpinner2Adapter());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mDialogResult != null ){
                    mDialogResult.finish((PointOfInterest) getSpinner2().getSelectedItem());
                }
                SearchDialog.this.dismiss();
            }
        });
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public Spinner getSpinner2() {
        return spinner2;
    }

    public Button getButton() {
        return button;
    }

    public ArrayAdapter<CharSequence> getSpinnerAdapter() {
        return spinnerAdapter;
    }

    public void setSpinnerAdapter(ArrayAdapter<CharSequence> spinnerAdapter) {
        this.spinnerAdapter = spinnerAdapter;
    }

    public ArrayAdapter<CharSequence> getSpinner2Adapter() {
        return spinner2Adapter;
    }

    public void setSpinner2Adapter(ArrayAdapter<CharSequence> spinner2Adapter) {
        this.spinner2Adapter = spinner2Adapter;
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(PointOfInterest poi);
    }
}
