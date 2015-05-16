package com.riftwalkers.clarity.view.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.AssetsExtracter;

/**
 * Created by marcel on 13-4-2015.
 */
/*public class RoleSelector extends Activity {

    // SharedPreference and information
    SharedPreferences sharedPreferences;
    int choise = 0;
    // Different buttons
    ImageButton threeDimAR, twoDimMaps;

    // Back button override timer
    long oldTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Shared preff
        sharedPreferences = getSharedPreferences("ClarityApp", 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean("SETUPCOMPLETED",false)) {
            changeLayout(editor);
        } else {
            setContentView(R.layout.loading_progress);
        }

        if((sharedPreferences.getInt("choice",0) > choise) && (sharedPreferences.getBoolean("SETUPCOMPLETED",false))){
            choise = sharedPreferences.getInt("choice",0);

            Intent intent;
            if(choise == 1){
                intent = new Intent(RoleSelector.this, AugmentedActivity.class);
            } else if(choise == 2) {
                intent = new Intent(RoleSelector.this, RoleSelector.class);
                editor.putInt("choice", 0);
                editor.apply();
            } else {
                intent = new Intent(RoleSelector.this, RoleSelector.class);
                editor.putInt("choice", 0);
                editor.apply();
            }
            startActivity(intent);
        } else if(!sharedPreferences.getBoolean("SETUPCOMPLETED",false)) {
            Toast.makeText(this, "Please wait while required data is loaded...", Toast.LENGTH_LONG).show();

            final TextView loadingTextView = (TextView) findViewById(R.id.loadingScreenText);

            final AssetsExtracter assetsExtracter = new AssetsExtracter();
            assetsExtracter.setContext(this);
            assetsExtracter.setActivity(this);
            assetsExtracter.setAssetExtracterInterface(new AssetsExtracter.MyAssetsExtracterInterface() {
                @Override
                public void finished() {
                    changeLayout(editor);
                    editor.putBoolean("SETUPCOMPLETED", true);
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "All assets are loaded", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnStageChange(final String stage) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (stage) {
                                case "Meerpalen":
                                    loadingTextView.setText("Meerpalen inladen");
                                    break;
                                case "Ligplaatsen":
                                    loadingTextView.setText("Ligplaatsen inladen");
                                    break;
                                case "Boeien":
                                    loadingTextView.setText("Boeien inladen");
                                    break;
                            }
                        }
                    });
                }
            });
            assetsExtracter.execute(0);
        }
    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() < oldTime+1500 ){

            Thread closeMain = new Thread(){
                @Override
                public void run(){
                    try{
                        sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally{
                        finish();
                    }
                }
            };
            closeMain.start();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else{
            oldTime = System.currentTimeMillis();

            Toast.makeText(getApplicationContext(),"Press back again to log off.", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeLayout(final SharedPreferences.Editor editor){
        setContentView(R.layout.roleselector_fragment);

        threeDimAR = (ImageButton) findViewById(R.id.three_dim);
        twoDimMaps = (ImageButton) findViewById(R.id.two_dim);

        threeDimAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 1;
                editor.putInt("choice", choise);
                editor.commit();
                Intent intent = new Intent(RoleSelector.this, AugmentedActivity.class);
                startActivity(intent);
            }
        });

        twoDimMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 2;
                editor.putInt("choice", choise);
                editor.commit();
                Intent intent = new Intent(RoleSelector.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    private Bitmap pictureDrawable2Bitmap(Picture picture) {
        PictureDrawable pd = new PictureDrawable(picture);
        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pd.getPicture());
        return bitmap;
    }
}*/