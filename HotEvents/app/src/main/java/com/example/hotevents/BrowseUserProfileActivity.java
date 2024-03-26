package com.example.hotevents;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for browsing and viewing user profiles.
 */
public class BrowseUserProfileActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private TextView textViewName, textViewEmail, textViewContact, textViewLocation;
    private LinearLayout editProfileButton, deleteProfileButton;
    private ImageButton backButton;

    private ListenerRegistration userListener;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_profile);


        // Initialize UI elements
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewContact = findViewById(R.id.textViewPhoneNumber);
        textViewLocation = findViewById(R.id.textViewLocation);
        editProfileButton = findViewById(R.id.linearEditProfile);
        deleteProfileButton = findViewById(R.id.linearDeleteProfile);
        backButton = findViewById(R.id.backButton);


        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Retrieve the device ID from the Intent
        String deviceId = getIntent().getStringExtra("deviceId");

        // Use the device ID to fetch user profile data from Firestore
        fetchUserDataFromFirestore(deviceId);

        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());

        deleteProfileButton.setOnClickListener(view -> {
            // Delete profile when the delete button is clicked
            deleteProfile();
        });

    }

    /**
     * Fetches user profile data from Firestore and updates the UI.
     *
     * @param deviceId The unique identifier of the user profile.
     */
    private void fetchUserDataFromFirestore(String deviceId) {
        // Listen for changes in user profile data
        userListener = db.collection("Users").document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            Log.d("ProfileActivity", "Fetching user data from Firestore");

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

                // Update UI elements with retrieved data
                textViewName.setText(name);
                textViewEmail.setText(email);
                textViewContact.setText(contact);
                textViewLocation.setText(location);
                Log.d("ProfileActivity", "Name: " + name);
                Log.d("ProfileActivity", "Email: " + email);
                // Add similar log statements for other fields

                // You can also handle profile photo here if it's stored in Firestore
            } else {
                Log.d("ProfileActivity", "No such document");
            }
        });
    }

    /**
     * Deletes the user profile from Firestore.
     */
    private void deleteProfile() {
        // Get device ID
        String deviceId = getIntent().getStringExtra("deviceId");

        // Delete the user profile from Firestore
        db.collection("Users").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Display success message
                    Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();

                    // Set result and finish activity
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Display error message
                    Toast.makeText(this, "Error deleting profile", Toast.LENGTH_SHORT).show();
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
