package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ProfileActivity displays user profile information and allows editing the profile.
 */
public class ProfileActivity extends AppCompatActivity {

    // Request code for editing profile
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    // UI elements
    private TextView textViewName, textViewEmail, textViewContact, textViewLocation;
    private CircleImageView profilePhotoImageView;
    private ImageButton backButton;
    private LinearLayout editProfileButton;

    // Firestore instance and listener for user profile data
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewContact = findViewById(R.id.textViewPhoneNumber);
        textViewLocation = findViewById(R.id.textViewLocation);
        editProfileButton = findViewById(R.id.linearEditProfile);
        backButton = findViewById(R.id.backButton);
        profilePhotoImageView = findViewById(R.id.imageViewProfilePhoto);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Fetch user profile data from Firestore and update UI
        fetchUserDataFromFirestore();

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());
        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from editing profile activity and is successful
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Update UI with fetched data from Firestore
            fetchUserDataFromFirestore();
        }
    }

    /**
     * Fetches user profile data from Firestore and updates the UI.
     */
    private void fetchUserDataFromFirestore() {
        // Get device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Listen for changes in user profile data
        userListener = db.collection("Users").document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("ProfileActivity", "Listen failed.", e);
                return;
            }

            // Check if document exists and update UI with user data
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                String email = documentSnapshot.getString("Email ID");
                String contact = documentSnapshot.getString("Contact");
                String location = documentSnapshot.getString("Location");

                textViewName.setText(name);
                textViewEmail.setText(email);
                textViewContact.setText(contact);
                textViewLocation.setText(location);
                // You can also handle profile photo here if it's stored in Firestore
            } else {
                Log.d("ProfileActivity", "No such document");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove Firestore listener to prevent memory leaks
        if (userListener != null) {
            userListener.remove();
        }
    }
}
