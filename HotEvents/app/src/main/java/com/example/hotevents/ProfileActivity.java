package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotevents.EditProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private TextView textViewName, textViewEmail, textViewContact, textViewLocation;
    private CircleImageView profilePhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewContact = findViewById(R.id.textViewPhoneNumber);
        textViewLocation = findViewById(R.id.textViewLocation);
        TextView editProfileButton = findViewById(R.id.textViewEditProfile);
        ImageButton backButton = findViewById(R.id.backButton);
        profilePhotoImageView = findViewById(R.id.imageViewProfilePhoto);

        // Will implement firebase afterwards
        textViewName.setText("John Doe");
        textViewEmail.setText("john.doe@example.com");
        textViewContact.setText("123-456-7890");
        textViewLocation.setText("City, Country");

        backButton.setOnClickListener(v -> onBackPressed());
        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("currentName", textViewName.getText().toString());
            intent.putExtra("currentEmail", textViewEmail.getText().toString());
            intent.putExtra("currentContact", textViewContact.getText().toString());
            intent.putExtra("currentLocation", textViewLocation.getText().toString());
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String updatedName = data.getStringExtra("updatedName");
                String updatedEmail = data.getStringExtra("updatedEmail");
                String updatedContact = data.getStringExtra("updatedContact");
                String updatedLocation = data.getStringExtra("updatedLocation");
                textViewName.setText(updatedName);
                textViewEmail.setText(updatedEmail);
                textViewContact.setText(updatedContact);
                textViewLocation.setText(updatedLocation);

                // Check if profile photo has been updated
                if (data.hasExtra("updatedPhotoUri")) {
                    Uri updatedPhotoUri = Uri.parse(data.getStringExtra("updatedPhotoUri"));
                    profilePhotoImageView.setImageURI(updatedPhotoUri);
                }
            }
        }
    }
}
