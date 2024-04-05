package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * EditProfileActivity allows users to edit their profile information and profile photo.
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_REQUEST_CODE = 1;

    // UI elements
    private CircleImageView profilePhotoImageView;
    private ImageButton editProfilePhotoButton;
    private ImageButton removeProfilePhotoButton;

    private Uri newPhotoUri; // Variable to store the URI of the newly selected photo

    // Firebase
    private FirebaseFirestore db;
    private String deviceId;
    private FirebaseStorage storage;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        // Get unique device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UI elements
        profilePhotoImageView = findViewById(R.id.edit_image_profile_photo);
        editProfilePhotoButton = findViewById(R.id.editProfilePhotoButton);
        removeProfilePhotoButton = findViewById(R.id.removeProfilePhotoButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());


        // Click listener for selecting profile photo from gallery
        editProfilePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
        });

        // Click listener for removing custom profile photo
        removeProfilePhotoButton.setOnClickListener(v -> {
            // Remove the custom profile photo URL from Firestore
            updateProfilePictureInDatabase(deviceId, "");

            // Set the default profile photo
            // Assuming "ProfilePictureDefault" is the field containing the default profile photo URL
            //            String defaultProfilePhotoUrl = documentSnapshot.getString("ProfilePictureDefault");
            //            downloadAndSetProfilePicture(defaultProfilePhotoUrl);
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
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    editTextName.setText(documentSnapshot.getString("Name"));
                    editTextEmail.setText(documentSnapshot.getString("Email ID"));
                    editTextContact.setText(documentSnapshot.getString("Contact"));
                    editTextLocation.setText(documentSnapshot.getString("Location"));
                    // You can handle profile photo here if it's stored in Firestore
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

    private void uploadPhoto(Uri photoUri) {
        // Generating unique name for the image
        String imageName = deviceId + "EDITED.png";

        // Getting reference to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("ProfilePictures")
                .child(imageName);

        // Uploading image to Firebase Storage
        storageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    Log.d("Upload", "Image uploaded successfully");

                    // Constructing the image URL based on the storage location and file name
                    String imageUrl = "gs://hotevents-hotjava.appspot.com/ProfilePictures/" + imageName;

                    // Update the URL in the Firestore database
                    updateProfilePictureInDatabase(deviceId, imageUrl);
                })
                .addOnFailureListener(e -> {
                    // Handle failure to upload image
                    Log.e("Upload", "Failed to upload image: " + e.getMessage());
                });
    }

    private void updateProfilePictureInDatabase(String userId, String imageUrl) {
        // Update the 'ProfilePicture' field in the database collection with the image URL
        db.collection("Users").document(userId).update("ProfilePictureCustom", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "Profile picture updated successfully"))
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error updating profile picture", e));
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

                    // Upload the selected image to Firebase Storage
                    uploadPhoto(newPhotoUri);
                }
            }
        }
    }

}
