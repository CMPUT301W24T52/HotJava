package com.example.hotevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to browse user profiles.
 */
public class BrowseProfilesActivity extends AppCompatActivity {

    private ListView profilesListView;
    private List<UserProfiles> profilesList;
    private arr_adapter profilesAdapter;
    private FirebaseFirestore db;
    private ImageButton backButton;
    private static final int BROWSE_USER_PROFILE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_list_view);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize lists with dummy data
        profilesList = new ArrayList<>();

        // Find your ListView
        profilesListView = findViewById(R.id.usersList);

        // Initialize adapter
        profilesAdapter = new arr_adapter(this, profilesList);

        // Set the adapter for the ListView
        profilesListView.setAdapter(profilesAdapter);

        // Set item click listener for the ListView
        profilesListView.setOnItemClickListener((parent, view, position, id) -> {
            // Retrieve the selected user profile
            UserProfiles selectedUserProfile = profilesList.get(position);

            // Get the user ID of the selected profile
            String userId = selectedUserProfile.getUid();

            // Launch BrowseUserProfileActivity with the selected user's ID
            Intent intent = new Intent(BrowseProfilesActivity.this, BrowseUserProfileActivity.class);
            intent.putExtra("deviceId", userId);
            startActivityForResult(intent, BROWSE_USER_PROFILE_REQUEST_CODE);
        });

        backButton = findViewById(R.id.backButton);
        // Set click listeners
        backButton.setOnClickListener(v -> onBackPressed());
        // Load profiles from Firestore
        loadProfilesFromFirestore();
    }

    /**
     * Method to load profiles from Firestore.
     */
    private void loadProfilesFromFirestore() {
        // Assuming "Users" is the collection name in Firestore
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Clear existing profiles to avoid duplicates
                profilesList.clear();

                for (DocumentSnapshot document : task.getResult()) {
                    // Retrieve data from Firestore document
                    String userName = document.getString("Name");
                    String uid = document.getString("UID");
                    String profilePicture;
                    String profilePictureCustom = document.getString("ProfilePictureCustom");
                    String profilePictureDefault = document.getString("ProfilePictureDefault");
                    // Check if custom picture or default
                    if (profilePictureCustom != null && !profilePictureCustom.isEmpty()) {
                        profilePicture = profilePictureCustom;
                    } else if (profilePictureDefault != null && !profilePictureDefault.isEmpty()) {
                        profilePicture = profilePictureDefault;
                    } else {
                        profilePicture = "gs://hotevents-hotjava.appspot.com/ProfilePictures/profilePictureDefault.png";
                    }
                    // Check if any of the fields is null (handle this based on your app's logic)
                    if (userName != null && uid != null && profilePicture != null) {
                        // Create a UserProfiles object with the retrieved data
                        UserProfiles user = new UserProfiles(profilePicture, userName, uid);

                        // Add the user to the list
                        profilesList.add(user);
                    }
                }

                // Notify the adapter that the data set has changed
                if (profilesList.isEmpty()) {
                    // Handle empty state
                    Toast.makeText(this, "No profiles available", Toast.LENGTH_SHORT).show();
                } else {
                    profilesAdapter.notifyDataSetChanged();
                }
            } else {
                // Handle the error
                Log.e("BrowseProfilesActivity", "Error getting profiles", task.getException());
                Toast.makeText(this, "Error loading profiles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSE_USER_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reload profiles from Firestore after deleting a profile
            loadProfilesFromFirestore();
        }
    }
}
