package com.example.hotevents;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for browsing and viewing user profiles.
 */
public class BrowseUserProfileActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;

    private TextView textViewName, textViewEmail, textViewContact, textViewLocation;
    private LinearLayout editProfileButton, deleteProfileButton;
    private ImageButton backButton;
    private CircleImageView profilePhotoImageView;


    private ListenerRegistration userListener;
    private FirebaseFirestore db;
    private FirebaseStorage storage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_profile);


        // Initialize UI elements
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewContact = findViewById(R.id.textViewPhoneNumber);
        textViewLocation = findViewById(R.id.textViewLocation);
        deleteProfileButton = findViewById(R.id.linearDeleteProfile);
        backButton = findViewById(R.id.backButton);
        profilePhotoImageView = findViewById(R.id.imageViewProfilePhoto);



        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();


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
                if (documentSnapshot.contains("ProfilePictureCustom")) {
                    String profilePicUrl = documentSnapshot.getString("ProfilePictureCustom");

                    // Check if profilePicUrl is not null or empty
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        // Download and set profile picture
                        downloadAndSetProfilePicture(profilePicUrl);
                    } else {
                        // Generate default profile photo based on the first letter of the name
                        String profilePicUrl1 = documentSnapshot.getString("ProfilePictureDefault");
                        downloadAndSetProfilePicture(profilePicUrl1);

                    }
                } else {
                    // Handle the case where the ProfilePicture field is not present in the document
                    Log.d("ProfileActivity", "No ProfilePicture field in the document");
                }

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
     * Downloads the profile picture from the given URL and sets it to the profile photo image view.
     *
     * @param profilePictureUrl The URL of the profile picture.
     */
    private void downloadAndSetProfilePicture(String profilePictureUrl) {
        // Create a reference to the Firebase Storage URL
        StorageReference photoRef = storage.getReferenceFromUrl(profilePictureUrl);

        // Download the image into a Bitmap
        final long FIVE_MEGABYTE = 5 * 1024 * 1024;
        photoRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Check if the byte array is not null
            if (bytes != null && bytes.length > 0) {
                // Decode the byte array into a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Check if the bitmap is not null
                if (bitmap != null) {
                    // Set the downloaded profile picture to the image view
                    profilePhotoImageView.setImageBitmap(bitmap);
                } else {
                    Log.e("ProfileActivity", "Failed to decode byte array into Bitmap");
                }
            } else {
                Log.e("ProfileActivity", "Downloaded byte array is null or empty");
            }
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("ProfileActivity", "Failed to download profile picture: " + exception.getMessage());
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
