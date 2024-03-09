package com.example.hotevents;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class NavigationMenu extends AppCompatActivity{

    private Button profileButton;
    private ImageView backArrow;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);

        profileButton = findViewById(R.id.profileButton);

        backArrow = findViewById(R.id.back);

        profileButton.setOnClickListener(view -> {
            startActivity(new Intent(NavigationMenu.this, ProfileActivity.class));
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

}
