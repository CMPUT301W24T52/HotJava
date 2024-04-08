package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    protected static final int EDIT_PROFILE_REQUEST_CODE = 1;

    protected TextView textViewName;
    protected TextView textViewEmail;
    protected TextView textViewContact;
    protected TextView textViewLocation;
    protected CircleImageView profilePhotoImageView;
    protected ImageButton backButton;
    protected LinearLayout editProfileButton;

    protected FirebaseFirestore db;
    protected ListenerRegistration userListener;

    protected FirebaseStorage storage;
    private StorageReference profilePicturesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupUI();
        UserProfileManager userProfileManager = UserProfileManager.getInstance(this);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        profilePicturesRef = storage.getReference().child("ProfilePictures");

        fetchUserDataFromFirestore();

        backButton.setOnClickListener(v -> onBackPressed());
        editProfileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });
    }

    protected void setupUI() {
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewContact = findViewById(R.id.textViewPhoneNumber);
        textViewLocation = findViewById(R.id.textViewLocation);
        editProfileButton = findViewById(R.id.linearEditProfile);
        backButton = findViewById(R.id.backButton);
        profilePhotoImageView = findViewById(R.id.imageViewProfilePhoto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchUserDataFromFirestore();
        }
    }

    protected void fetchUserDataFromFirestore() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        userListener = db.collection("Users").document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("ProfileActivity", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                String email = documentSnapshot.getString("Email ID");
                String contact = documentSnapshot.getString("Contact");
                String location = documentSnapshot.getString("Location");

                textViewName.setText(name);
                textViewEmail.setText("Email ID: " + email);
                textViewContact.setText("Contact: " + contact);
                textViewLocation.setText("Location: " + location);

                if (documentSnapshot.contains("ProfilePictureCustom")) {
                    String profilePicUrl = documentSnapshot.getString("ProfilePictureCustom");
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        UserProfileManager.getInstance(this).downloadAndSetProfilePicture(profilePhotoImageView, profilePicUrl);
                    } else {
                        char firstLetter = name.charAt(0);
                        Bitmap defaultProfilePhoto = UserProfileManager.getInstance(this).generateDefaultProfilePhoto(firstLetter);
                        profilePhotoImageView.setImageBitmap(defaultProfilePhoto);
                    }
                } else {
                    char firstLetter = name.charAt(0);
                    Bitmap defaultProfilePhoto = UserProfileManager.getInstance(this).generateDefaultProfilePhoto(firstLetter);
                    profilePhotoImageView.setImageBitmap(defaultProfilePhoto);
                }
            } else {
                Log.d("ProfileActivity", "No such document");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserProfileManager.getInstance(this).stopListening();
    }
}
