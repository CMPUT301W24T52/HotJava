package com.example.hotevents;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class NavigationMenu extends AppCompatActivity{

    private Button profileButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            startActivity(new Intent(NavigationMenu.this, ProfileActivity.class));
        });

    }

}
