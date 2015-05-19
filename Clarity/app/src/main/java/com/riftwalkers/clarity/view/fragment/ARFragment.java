package com.riftwalkers.clarity.view.fragment;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
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

public class ARFragment extends AbstractARFragment implements LocationListenerObserver, SearchButtonClickListener {

    private PointOfInterest zoekPOI;
    private PoiList pointOfInterestList;

    private int drawRange;

    private Button menuBackButton;
    private CheckBox meerpalenCheckbox;
    private CheckBox ligplaatsenCheckbox;
    private CheckBox aanmeerboeienCheckbox;
    private TextView drawRangeView;
    private SeekBar rangeSelectSeekBar;
    private ImageView switchbutton;
    private FrameLayout infoBox;

    public ARFragment() {
        pointOfInterestList = ((MainActivity) getActivity()).pointOfInterestList;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return null;
    }

    @Override
    protected void loadContents() {
        // Set GPS tracking configuration
        metaioSDK.setTrackingConfiguration("GPS");

        metaioSDK.setLLAObjectRenderingLimits(5, 200);
        metaioSDK.setRendererClippingPlaneLimits(10, 220000);

        locationProvider.addLocationListenObserver(this);

        setupViews();
        drawGeometries();
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

        Log.wtf("",geometry.toString());

        //Show poi info in the poi info box when touched
        getView().findViewById(R.id.poi_info).setVisibility(View.VISIBLE);

        //get poi id and show it in the info box (for now it takes the first poi in the list)
        String poiID = Integer.toString(pointOfInterestList.get(0).getId());

        String infoText = "POI id: " + poiID;
        TextView infoID = (TextView)getView().findViewById(R.id.info_id);
        infoID.setText(infoText);
    }

    @Override
    public void onResume() {
        super.onResume();
        locationProvider.requestUpdate();
    }

    private void drawGeometries() {
        //draw each poi in the arrayList
        for(PointOfInterest poi : pointOfInterestList) {
            float[] results = new float[3];
            Location.distanceBetween(
                    poi.getCoordinate().getLatitude(),
                    poi.getCoordinate().getLongitude(),
                    mSensors.getLocation().getLatitude(),
                    mSensors.getLocation().getLongitude(),
                    results
            );

            if(results[0] < drawRange) {
                if(poi.getGeometry() == null) {
                    //get type of POI and image
                    File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), poi.GetImageName());

                    if (POIbackground != null) {
                            LLACoordinate coordinate = new LLACoordinate(
                                    poi.getCoordinate().getLatitude(),
                                    poi.getCoordinate().getLongitude(),
                                    0,
                                    0);
                            poi.setGeometry(createGeometry(coordinate, POIbackground, 80));
                    } else {
                        MetaioDebug.log(Log.ERROR, "Error loading POIbackground: " + POIbackground);
                    }
                }
            } else {
                if(poi.getGeometry() != null) {
                    metaioSDK.unloadGeometry(poi.getGeometry());
                    poi.setGeometry(null);
                }
            }
        }
    }

    public IGeometry createGeometry(LLACoordinate coordinate, File iconFile, int scale) {
        IGeometry geometry = metaioSDK.createGeometryFromImage(iconFile, true,false);
        if(geometry != null) {
            geometry.setTranslationLLA(coordinate);
            geometry.setLLALimitsEnabled(true);
            geometry.setScale(scale);

            return geometry;
        } else {
            // todo: dit gaat niet werken, geometry altijd null, wanneer niet Null dan bovenste stuk...
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + geometry);
            return null;
        }
    }

    public void setupViews() {
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

        meerpalenCheckbox = (CheckBox) getActivity().findViewById(R.id.meerpalenCheckbox);
        meerpalenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Meerpaal)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Meerpaal)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

        ligplaatsenCheckbox = (CheckBox) getActivity().findViewById(R.id.ligplaatsenCheckbox);
        ligplaatsenCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Ligplaats)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Ligplaats)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

        aanmeerboeienCheckbox = (CheckBox) getActivity().findViewById(R.id.aanmeerboeienCheckbox);
        aanmeerboeienCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }
            }
        });

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

        switchbutton = (ImageView) getView().findViewById(R.id.switchbuttonAR);

        switchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentListener != null)
                    fragmentListener.ChangeFragment(MapsFragment.class);
            }
        });

        //setup onClickListener for the info box
        infoBox = (FrameLayout) getView().findViewById(R.id.poi_info);

        infoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoBox.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void observerOnLocationChanged(Location location) {
        if(mSensors != null)
            mSensors.setManualLocation(new LLACoordinate(location.getLatitude(), location.getLongitude(), 0, 0));
    }

    @Override
    public void onSearchClick() {
        SearchDialog searchDialog = new SearchDialog(getActivity(), pointOfInterestList);
        searchDialog.setDialogResult(new SearchDialog.OnMyDialogResult() {
            @Override
            public void finish(final PointOfInterest poi) {
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if ((zoekPOI != null)) {
                            metaioSDK.unloadGeometry(zoekPOI.getGeometry());
                        }

                        ligplaatsenCheckbox.setChecked(false);
                        aanmeerboeienCheckbox.setChecked(false);
                        meerpalenCheckbox.setChecked(false);

                        zoekPOI = poi;

                        File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), "zoekPOI.png");

                        LLACoordinate coordinate = new LLACoordinate(
                                poi.getCoordinate().getLatitude(),
                                poi.getCoordinate().getLongitude(),
                                0,
                                0);

                        zoekPOI.setGeometry(createGeometry(coordinate, POIbackground, 100));
                        zoekPOI.getGeometry().setVisible(true);
                    }
                });
            }
        });
        searchDialog.show();
    }
}
