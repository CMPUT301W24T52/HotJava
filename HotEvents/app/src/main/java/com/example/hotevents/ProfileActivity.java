package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

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

    // Firebase Storage
    private FirebaseStorage storage;
    private StorageReference profilePicturesRef;

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

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        profilePicturesRef = storage.getReference().child("ProfilePictures");

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
     * Fetches user profile data from Firestore including the profile picture and updates the UI.
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

                // Generate default profile photo based on the first letter of the name
                char firstLetter = name.charAt(0);
                generateDefaultProfilePhotoAndUpload(deviceId, firstLetter);

                // Check if ProfilePicture field is present
                if (documentSnapshot.contains("ProfilePicture")) {
                    String profilePicUrl = documentSnapshot.getString("ProfilePicture");

                    // Check if profilePicUrl is not null or empty
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        // Download and set profile picture
                        downloadAndSetProfilePicture(profilePicUrl);
                    } else {
                        // Handle the case where the profile picture URL is null or empty
                        Log.d("ProfileActivity", "Profile picture URL is null or empty");
                    }
                } else {
                    // Handle the case where the ProfilePicture field is not present in the document
                    Log.d("ProfileActivity", "No ProfilePicture field in the document");
                }
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
     * Generates a default profile photo with the initial character of the name, uploads it to Firebase Storage,
     * and updates the 'ProfilePicture' field in the Firestore database.
     *
     * @param userId      The user's unique ID.
     * @param initialChar The initial character of the name.
     */
    private void generateDefaultProfilePhotoAndUpload(String userId, char initialChar) {
        // Generate default profile photo
        Bitmap defaultProfilePhoto = generateDefaultProfilePhoto(initialChar);

        // Upload default profile photo to Firebase Storage
        uploadProfilePhotoToStorage(userId, defaultProfilePhoto, () -> {
            // Upload successful, update the 'ProfilePicture' field in the database with the image URL
            String imageUrl = "gs://hotevents-hotjava.appspot.com/ProfilePictures/" + userId + ".png";
            updateProfilePictureInDatabase(userId, imageUrl);
        });
    }

    /**
     * Generates a default profile photo with the initial character of the name.
     *
     * @param initialChar The initial character of the name.
     * @return Bitmap The generated default profile photo.
     */
    private Bitmap generateDefaultProfilePhoto(char initialChar) {
        // This method generates a default profile photo with the initial character of the name
        // You can customize this method to generate the photo as you like
        // Here, we are creating a simple Bitmap with the initial character drawn on it
        // You can use any image manipulation library or implement your own method
        int imageSize = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        // Create a Canvas and draw the initial character on the Bitmap
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(getResources().getDimension(R.dimen.profile_image_text_size));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        // Adjust text position to center
        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
        canvas.drawColor(Color.GRAY); // Set background color
        canvas.drawText(String.valueOf(initialChar), x, y, paint);
        return bitmap;
    }

    /**
     * Uploads the profile photo to Firebase Storage.
     *
     * @param userId The user's unique ID.
     * @param bitmap The profile photo bitmap to upload.
     * @param onCompleteCallback Callback to execute after the upload is complete.
     */
    private void uploadProfilePhotoToStorage(String userId, Bitmap bitmap, OnUploadCompleteListener onCompleteCallback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create a reference to 'ProfilePictures/userId.png'
        StorageReference photoRef = profilePicturesRef.child(userId + ".png");

        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return photoRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    // Callback to execute after the upload is complete
                    onCompleteCallback.onUploadComplete();
                }
            } else {
                // Handle failures
                Log.e("ProfileActivity", "Failed to upload profile photo");
            }
        });
    }

    /**
     * Interface for upload complete callback.
     */
    private interface OnUploadCompleteListener {
        void onUploadComplete();
    }

    private void updateProfilePictureInDatabase(String userId, String imageUrl) {
        // Update the 'ProfilePicture' field in the database with the image URL
        db.collection("Users").document(userId).update("ProfilePicture", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "Profile picture updated successfully"))
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error updating profile picture", e));
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
