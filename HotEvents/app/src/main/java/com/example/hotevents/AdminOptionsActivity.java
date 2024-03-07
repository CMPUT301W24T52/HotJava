package com.example.hotevents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminOptionsActivity extends AppCompatActivity {
    private Button browseProfiles;
    private Button browseEvents;
    private Button browsePictures;
    private ImageView backButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_options);

//        browseProfiles = findViewById(R.id.browseProfiles);
//        browseEvents = findViewById(R.id.browseEvents);
//        browsePictures = findViewById(R.id.browsePictures);

        backButton = findViewById(R.id.backButton);

//        browseProfiles.setOnClickListener(view -> {
//            startActivity(new Intent(AdminOptionsActivity.this, BrowseProfilesActivity.class));
//        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        backButton.setOnClickListener(view -> {
//            startActivity(new Intent(AdminOptionsActivity.this, MainActivity.class));
//        });



    }

}
