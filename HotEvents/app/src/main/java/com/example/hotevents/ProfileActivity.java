package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

                // Generate default profile photo based on the first letter of the name
                char firstLetter = name.charAt(0);
                Bitmap defaultProfilePhoto = generateDefaultProfilePhoto(firstLetter);
                profilePhotoImageView.setImageBitmap(defaultProfilePhoto);
            } else {
                Log.d("ProfileActivity", "No such document");
            }
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

    @Override
    protected void onStop() {
        super.onStop();
        // Remove Firestore listener to prevent memory leaks
        if (userListener != null) {
            userListener.remove();
        }
    }
}
