package com.riftwalkers.clarity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by marcel on 13-4-2015.
 */
public class RoleSelector extends Activity {

    Button pilot, rower, ect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roleselector);

        pilot = (Button) findViewById(R.id.loods);
        rower = (Button) findViewById(R.id.roei);
        ect = (Button) findViewById(R.id.ect);

        pilot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });

        rower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelector.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
