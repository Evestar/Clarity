package com.riftwalkers.clarity.view.fragment;

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
import java.util.HashMap;

public class ARFragment extends AbstractARFragment implements LocationListenerObserver, SearchButtonClickListener {


    private Bitmap bitmap;
    private ByteBuffer byteBuffer;
    private ImageStruct texture;

    private PoiList pointOfInterestList;
    private HashMap<IGeometry, PointOfInterest> poiGeometryHashMap;

    private int drawRange;

    private Button menuBackButton;
    private CheckBox meerpalenCheckbox;
    private CheckBox ligplaatsenCheckbox;
    private CheckBox aanmeerboeienCheckbox;
    private CheckBox boldersCheckbox;
    private TextView drawRangeView;
    private SeekBar rangeSelectSeekBar;
    private ImageView switchbutton;
    private FrameLayout infoBox;

    public ARFragment() {
        pointOfInterestList = MainActivity.pointOfInterestList;
        poiGeometryHashMap = new HashMap<>();
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

        //link geometry with poi in poiList
        PointOfInterest linkedPoi = poiGeometryHashMap.get(geometry);

        //get and draw poi id
        String idText = "POI id: " + linkedPoi.getId();//poiID;
        TextView infoID = (TextView)getView().findViewById(R.id.info_id);
        infoID.setText(idText);

        //get and draw poi type
        String typeText = "Type: " + linkedPoi.getPoiType().toString();
        TextView infoType = (TextView)getView().findViewById(R.id.info_type);
        infoType.setText(typeText);

        TextView row1 = (TextView)getView().findViewById(R.id.info_row1);
        TextView row2 = (TextView)getView().findViewById(R.id.info_row2);
        TextView row3 = (TextView)getView().findViewById(R.id.info_row3);

        if(linkedPoi.getPoiType() == PoiType.Bolder) {
            String trekkracht;
            String verankering;

            if(linkedPoi.getTrekkracht() != 0) {
                trekkracht = String.valueOf(linkedPoi.getTrekkracht());
            } else {
                trekkracht = "Onbekend";
            }

            if(linkedPoi.getMethodeVerankering() != null) {
                verankering = linkedPoi.getMethodeVerankering();
            } else {
                verankering = "Onbekend";
            }

            row1.setText("Methode verankering: " + verankering);
            row2.setText("Toegestane trekkracht: " + trekkracht + "(KN)");

            if(linkedPoi.getNummer() != 0) {
                row1.setText(row1.getText() + "\tNummer: " + linkedPoi.getNummer());
            }

        } else if(linkedPoi.getPoiType() == PoiType.Meerpaal) {
            if(linkedPoi.getTypePaal() != null) {
                row1.setText("Type paal: " + linkedPoi.getTypePaal());
            }
            if(linkedPoi.getNummer() != 0) {
                row2.setText("Nummer: " + linkedPoi.getNummer());
            } else {
                row2.setVisibility(View.GONE);
            }
        } else if(linkedPoi.getPoiType() == PoiType.Ligplaats) {
            row1.setText("Eigenaar: " + linkedPoi.getEigenaar());
            row2.setText("Haven naam: " + linkedPoi.getHavenNaam() + "\tOever nummer: " + linkedPoi.getOeverFrontNummer());
        } else {
            row2.setVisibility(View.GONE);
        }

        if(linkedPoi.getDescription() != null) {
            row3.setText("Omschrijving: " + linkedPoi.getDescription());
        } else {
            row3.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        locationProvider.requestUpdate();
    }

    private void drawGeometries() {
        //draw each poi in the arrayList

        if(MainActivity.isSearchingFromMaps){
            searchFromMaps();
        } else {
            for (PointOfInterest poi : pointOfInterestList) {
                if(     ((!meerpalenCheckbox.isChecked()) && (poi.getPoiType() == PoiType.Meerpaal)) ||
                        ((!ligplaatsenCheckbox.isChecked()) && (poi.getPoiType() == PoiType.Ligplaats)) ||
                        ((!boldersCheckbox.isChecked()) && (poi.getPoiType() == PoiType.Bolder))
                        ) {

                    if(poi.getGeometry() != null) {
                        metaioSDK.unloadGeometry(poi.getGeometry());
                        poi.setGeometry(null);
                        if(poiGeometryHashMap.containsKey(poi)) {
                            poiGeometryHashMap.remove(poi);
                        }
                    }
                } else {

                    int distance = getDistance(poi);
                    if (distance < drawRange) {
                        if (poi.getGeometry() == null) {
                            //get type of POI and image
                            File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), poi.GetImageName());

                            if (POIbackground != null) {
                                LLACoordinate coordinate = new LLACoordinate(
                                        poi.getCoordinates().get(0).getLatitude(),
                                        poi.getCoordinates().get(0).getLongitude(),
                                        0,
                                        0);
                                poi.setGeometry(createGeometry(coordinate, POIbackground, 80, String.valueOf(poi.getId()), distance));

                                poiGeometryHashMap.put(poi.getGeometry(), poi);
                            }
                        }
                    } else {
                        if (poi.getGeometry() != null) {
                            metaioSDK.unloadGeometry(poi.getGeometry());
                            poi.setGeometry(null);
                            if(poiGeometryHashMap.containsKey(poi)) {
                                poiGeometryHashMap.remove(poi);
                            }
                        }
                    }
                }
            }
        }
    }

    private void searchFromMaps(){
        ligplaatsenCheckbox.setChecked(false);
//        aanmeerboeienCheckbox.setChecked(false);
        meerpalenCheckbox.setChecked(false);
        boldersCheckbox.setChecked(false);

        for(PointOfInterest poi : pointOfInterestList) {
            if(MainActivity.SearchedPOI == poi) {

                int distance = getDistance(poi);
                if (poi.getGeometry() == null) {
                    File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), "zoekPOI.png");

                    if (POIbackground != null) {
                        LLACoordinate coordinate = new LLACoordinate(
                                poi.getCoordinates().get(0).getLatitude(),
                                poi.getCoordinates().get(0).getLongitude(),
                                0,
                                0);
                        poi.setGeometry(createGeometry(coordinate, POIbackground, 80, String.valueOf(poi.getId()), distance));
                    }
                }

                break;
            }
        }
    }

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

    public IGeometry createGeometry(LLACoordinate coordinate, File iconFile, int scale, String id, int distance) {
        bitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());

        //draw text onto the poi image
        bitmap = drawTextToBitmap(bitmap, id, distance);

        byteBuffer = ByteBuffer.allocate(bitmap.getWidth()*bitmap.getHeight()*4);

        bitmap.copyPixelsToBuffer(byteBuffer);

        texture = new ImageStruct(byteBuffer.array(), bitmap.getWidth(), bitmap.getHeight(), ECOLOR_FORMAT.ECF_RGBA8, true, (System.currentTimeMillis()/1000));

        //IGeometry geometry = metaioSDK.createGeometryFromImage(iconFile, true,false);
        IGeometry geometry = metaioSDK.createGeometryFromImage(id,texture, true,false);
        if(geometry != null) {
            geometry.setTranslationLLA(coordinate);
            geometry.setLLALimitsEnabled(true);
            geometry.setScale(scale);
            //set name to id for future references for getting information out of the poi in onGeometryTouched
            geometry.setName(id);

            bitmap.recycle();
            bitmap = null;

            return geometry;
        } else {
            // todo: dit gaat niet werken, geometry altijd null, wanneer niet Null dan bovenste stuk...
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + geometry);

            bitmap.recycle();
            bitmap = null;

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

/*        aanmeerboeienCheckbox = (CheckBox) getActivity().findViewById(R.id.aanmeerboeienCheckbox);
        aanmeerboeienCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getPoiType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(false);
                        }
                    }
                    setSearch(false);
                } else {
                    for (PointOfInterest poi : pointOfInterestList) {
                        if (poi.getPoiType().equals(PoiType.Boei)) {
                            if(poi.getGeometry() != null)
                                poi.getGeometry().setVisible(true);
                        }
                    }
                }

            }
        });*/

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

        //setup onClickListener for the info box's new msg button
        /*infoNewMsgButton = (Button) getView().findViewById(R.id.button_new_msg);

        infoNewMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MetaioDebug.log("whuat?");

            }
        });*/
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
                        if ((MainActivity.SearchedPOI != null)) {
                            metaioSDK.unloadGeometry(MainActivity.SearchedPOI.getGeometry());
                        }

                        ligplaatsenCheckbox.setChecked(false);
//                        aanmeerboeienCheckbox.setChecked(false);
                        meerpalenCheckbox.setChecked(false);
                        boldersCheckbox.setChecked(false);

                        MainActivity.SearchedPOI = poi;

                        File POIbackground = AssetsManager.getAssetPathAsFile(getActivity(), "zoekPOI.png");

                        LLACoordinate coordinate = new LLACoordinate(
                                poi.getCoordinates().get(0).getLatitude(),
                                poi.getCoordinates().get(0).getLongitude(),
                                0,
                                0);

                        float[] results = new float[3];
                        Location.distanceBetween(
                                MainActivity.SearchedPOI.getCoordinates().get(0).getLatitude(),
                                MainActivity.SearchedPOI.getCoordinates().get(0).getLongitude(),
                                mSensors.getLocation().getLatitude(),
                                mSensors.getLocation().getLongitude(),
                                results
                        );

                        int distance = (int) results[0];

                        MainActivity.SearchedPOI.setGeometry(createGeometry(coordinate, POIbackground, 100, String.valueOf(MainActivity.SearchedPOI.getId()), distance));
                        MainActivity.SearchedPOI.getGeometry().setVisible(true);
                    }
                });
            }
        });
        searchDialog.show();
    }

    public Bitmap drawTextToBitmap(Bitmap baseImage, String text, int distance) {

        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }

        baseImage = baseImage.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(baseImage);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize(24);
        // bold text
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (baseImage.getWidth() - bounds.width())/2;
        int y = (baseImage.getHeight() + bounds.height())/3;

        canvas.drawText(text, x, y, paint);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(40, ((baseImage.getHeight() / 5) * 4), baseImage.getWidth()-40, baseImage.getHeight(), paint);

        paint.setColor(Color.BLUE);
        bounds = new Rect();
        text = String.valueOf(distance) + " m";
        paint.getTextBounds(text, 0, text.length(), bounds);
        x = (baseImage.getWidth() - bounds.width())/2;
        y = baseImage.getHeight()-12;
        canvas.drawText(String.valueOf(distance) + " m", x, y, paint);

        return baseImage;
    }
}
