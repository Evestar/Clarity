package com.riftwalkers.clarity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SearchDialog extends Dialog {
    private Spinner spinner;
    private Spinner spinner2;
    private Button button;

    private ArrayAdapter<CharSequence> spinnerAdapter;
    private ArrayAdapter<CharSequence> spinner2Adapter;

    String[] boeienArray = {"","boei1","boei2","boei3","boei4","boei5"};
    String[] palenArray = {"","paal1","paal2","paal3"};

    public SearchDialog(final Context context) {
        super(context);
        this.setContentView(R.layout.search_dialog);
        this.setTitle(R.string.search_dialog_title);

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
                    setSpinner2Adapter(new ArrayAdapter<CharSequence>(context, R.layout.searchbox_spinner_listview, boeienArray));
                } else if (id == 2) {
                    setSpinner2Adapter(new ArrayAdapter<CharSequence>(context, R.layout.searchbox_spinner_listview, palenArray));
                } else {
                    setSpinner2Adapter(new ArrayAdapter<CharSequence>(context, R.layout.searchbox_spinner_listview, palenArray));
                }

                getSpinner2Adapter().setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                getSpinner2().setAdapter(getSpinner2Adapter());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getSpinner2().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if((id != 0) && (getButton().getVisibility() == View.GONE)) {
                    getButton().setVisibility(View.VISIBLE);
                }

                if(id == 0) {
                    getButton().setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
}
