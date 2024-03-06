package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST_CODE = 1;

    private CircleImageView profilePhotoImageView;
    private ImageButton editProfilePhotoButton;
    private Uri newPhotoUri; // Variable to store the URI of the newly selected photo
    // Not sure how we will add photo inside the device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePhotoImageView = findViewById(R.id.edit_image_profile_photo);
        editProfilePhotoButton = findViewById(R.id.editProfilePhotoButton);

        editProfilePhotoButton.setOnClickListener(v -> {
            // Open gallery select image
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
        });

        EditText editTextName = findViewById(R.id.edit_text_name);
        EditText editTextEmail = findViewById(R.id.edit_text_email);
        EditText editTextContact = findViewById(R.id.edit_text_contact);
        EditText editTextLocation = findViewById(R.id.edit_text_location);
        LinearLayout saveProfileLayout = findViewById(R.id.saveProfile);

        String currentName = getIntent().getStringExtra("currentName");
        String currentEmail = getIntent().getStringExtra("currentEmail");
        String currentContact = getIntent().getStringExtra("currentContact");
        String currentLocation = getIntent().getStringExtra("currentLocation");

        editTextName.setText(currentName);
        editTextEmail.setText(currentEmail);
        editTextContact.setText(currentContact);
        editTextLocation.setText(currentLocation);

        saveProfileLayout.setOnClickListener(view -> {
            String updatedName = editTextName.getText().toString();
            String updatedEmail = editTextEmail.getText().toString();
            String updatedContact = editTextContact.getText().toString();
            String updatedLocation = editTextLocation.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedName", updatedName);
            resultIntent.putExtra("updatedEmail", updatedEmail);
            resultIntent.putExtra("updatedContact", updatedContact);
            resultIntent.putExtra("updatedLocation", updatedLocation);

            if (newPhotoUri != null) {
                resultIntent.putExtra("updatedPhotoUri", newPhotoUri.toString());
            }
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_REQUEST_CODE && data != null) {
                // Retrieve the URI of the selected image
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Load the selected image into the profile photo ImageView
                    profilePhotoImageView.setImageURI(selectedImageUri);
                    // Store the URI of the selected image to pass it back to the ProfileActivity
                    newPhotoUri = selectedImageUri;
                }
            }
        }
    }
}
