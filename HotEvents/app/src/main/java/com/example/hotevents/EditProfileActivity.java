package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * EditProfileActivity allows users to edit their profile information and profile photo.
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST_CODE = 1;

    // UI elements
    private CircleImageView profilePhotoImageView;
    private ImageButton editProfilePhotoButton;
    private Uri newPhotoUri; // Variable to store the URI of the newly selected photo

    // Firebase
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Get unique device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UI elements
        profilePhotoImageView = findViewById(R.id.edit_image_profile_photo);
        editProfilePhotoButton = findViewById(R.id.editProfilePhotoButton);

        // Click listener for selecting profile photo from gallery
        editProfilePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
        });

        // Initialize EditText fields and save button
        EditText editTextName = findViewById(R.id.edit_text_name);
        EditText editTextEmail = findViewById(R.id.edit_text_email);
        EditText editTextContact = findViewById(R.id.edit_text_contact);
        EditText editTextLocation = findViewById(R.id.edit_text_location);
        LinearLayout saveProfileLayout = findViewById(R.id.saveProfile);

        // Fetch current user data from Firestore and populate the fields
        db.collection("Users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    editTextName.setText(document.getString("Name"));
                    editTextEmail.setText(document.getString("Email ID"));
                    editTextContact.setText(document.getString("Contact"));
                    editTextLocation.setText(document.getString("Location"));
                    // You can handle profile photo here if it's stored in Firestore
                }
            }
        });

        // Click listener for saving profile changes
        saveProfileLayout.setOnClickListener(view -> {
            // Get updated profile information
            String updatedName = editTextName.getText().toString();
            String updatedEmail = editTextEmail.getText().toString();
            String updatedContact = editTextContact.getText().toString();
            String updatedLocation = editTextLocation.getText().toString();

            // Update profile data in Firestore
            DocumentReference userRef = db.collection("Users").document(deviceId);
            userRef.update("Name", updatedName);
            userRef.update("Email ID", updatedEmail);
            userRef.update("Contact", updatedContact);
            userRef.update("Location", updatedLocation);

            // If new photo selected, update the profile picture field
            if (newPhotoUri != null) {
                // Assuming you have a method to upload the photo to storage and get the URL
                String photoUrl = uploadPhotoAndGetUrl(newPhotoUri);
                userRef.update("ProfilePicture", photoUrl);
            }

            // Set result and finish activity
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

            // Refresh the profile activity to display the updated profile data
            Intent refreshIntent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            // Add flags to clear the activity stack and create a new instance of ProfileActivity
            refreshIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(refreshIntent);
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

    /**
     * Method to upload photo to storage and get URL.
     * @param photoUri The URI of the photo to be uploaded.
     * @return String The URL of the uploaded photo.
     */
    private String uploadPhotoAndGetUrl(Uri photoUri) {
        // We have to figure out how to upload photo to storage and return the URL
        return null;
    }
}
