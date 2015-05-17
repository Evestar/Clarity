package com.riftwalkers.clarity.view.fragment;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.AssetsExtracter;
import com.riftwalkers.clarity.data.interfaces.LocationListenerObserver;

public class RoleSelectorFragment extends BaseFragment{

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int choise = 0;

    private ImageButton threeDimAR, twoDimMaps;

    private long oldTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.roleselector_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        sharedPreferences = getActivity().getSharedPreferences("ClarityApp", 0);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean("SETUPCOMPLETED",false)) {
            activateButtons();
        } else {
            Toast.makeText(getActivity(), "Please wait while required data is loaded...", Toast.LENGTH_LONG).show();

            AssetsExtracter assetsExtracter = new AssetsExtracter();
            assetsExtracter.setContext(getActivity());
            assetsExtracter.setActivity(getActivity());
            assetsExtracter.setAssetExtracterInterface(new AssetsExtracter.MyAssetsExtracterInterface() {
                @Override
                public void finished() {
                    activateButtons();
                    editor.putBoolean("SETUPCOMPLETED", true);
                    editor.apply();
                    Toast.makeText(getActivity().getApplicationContext(), "All assets are loaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnStageChange(final String stage) { }
            });
            assetsExtracter.execute(0);
        }

        /*if((sharedPreferences.getInt("choice",0) > choise) && (sharedPreferences.getBoolean("SETUPCOMPLETED",false))) {
            choise = sharedPreferences.getInt("choice", 0);

            if (choise == 1) {
                editor.putInt("choice", 1);
                editor.apply();
                if(fragmentListener != null) {
                    fragmentListener.ChangeFragment(ARFragment.class);
                }
            } else if (choise == 2) {
                editor.putInt("choice", 2);
                editor.apply();
                if(fragmentListener != null) {
                    fragmentListener.ChangeFragment(MapsFragment.class);
                }
            }
        }*/
    }

    private void activateButtons() {
        threeDimAR = (ImageButton) getView().findViewById(R.id.three_dim);
        twoDimMaps = (ImageButton) getView().findViewById(R.id.two_dim);

        threeDimAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 1;
                editor.putInt("choice", choise);
                editor.commit();
                if(fragmentListener != null) {
                    fragmentListener.ChangeFragment(ARFragment.class);
                }
            }
        });

        twoDimMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 2;
                editor.putInt("choice", choise);
                editor.commit();
                if(fragmentListener != null) {
                    fragmentListener.ChangeFragment(MapsFragment.class);
                }
            }
        });
    }
}
