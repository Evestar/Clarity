package com.riftwalkers.clarity;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by marcel on 13-4-2015.
 */
public class RoleSelector extends Activity {

    // SharedPreference and information
    SharedPreferences sharedPreferences;
    int choise = 0;
    // Different buttons
    Button pilot, rower, ect;

    // Back button override timer
    long oldTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roleselector);

        // Shared preff
        sharedPreferences = getSharedPreferences("ClarityApp", 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        if(sharedPreferences.getInt("choice",0) > choise){
            choise = sharedPreferences.getInt("choice",0);

            Intent intent = new Intent(RoleSelector.this, MainActivity.class);
            startActivity(intent);
        }

        final AssetsExtracter assetsExtracter = new AssetsExtracter();
        assetsExtracter.setContext(this);
        assetsExtracter.execute(0);

        pilot = (Button) findViewById(R.id.loods);
        rower = (Button) findViewById(R.id.roei);
        ect = (Button) findViewById(R.id.ect);

        pilot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 1;
                editor.putInt("choice", choise);
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });

        rower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 2;
                editor.putInt("choice", choise);
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choise = 3;
                editor.putInt("choice", choise);
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });
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


}
