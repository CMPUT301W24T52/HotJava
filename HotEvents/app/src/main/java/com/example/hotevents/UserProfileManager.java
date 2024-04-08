package com.example.hotevents;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * The UserProfileManager class manages user profile-related functionalities.
 */
public class UserProfileManager {

    private final Context context; // Add context field
    private static UserProfileManager instance;

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final StorageReference profilePicturesRef;
    private ListenerRegistration userListener;

    /**
     * Constructs a new UserProfileManager object.
     *
     * @param context The context of the application.
     */
    public UserProfileManager(Context context) {
        this.context = context; // Initialize context

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        profilePicturesRef = storage.getReference().child("ProfilePictures");
    }

    /**
     * Retrieves the singleton instance of UserProfileManager.
     *
     * @param context The context of the application.
     * @return The singleton instance of UserProfileManager.
     */
    public static synchronized UserProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserProfileManager(context);
        }
        return instance;
    }

    /**
     * Fetches user data from Firestore and provides it via a callback.
     *
     * @param callback The callback to handle fetched user data.
     */
    public void fetchUserData(UserDataCallback callback) {
        // Get device ID or user ID
        String userId = "unique_user_id"; // Replace with actual user ID retrieval logic

        userListener = db.collection("Users").document(userId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("UserProfileManager", "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Call the callback with the fetched document snapshot
                callback.onDataReceived(documentSnapshot);
            } else {
                Log.d("UserProfileManager", "No such document");
            }
        });
    }

    /**
     * Loads the profile picture into the provided ImageView based on the DocumentSnapshot.
     *
     * @param imageView        The ImageView to load the profile picture into.
     * @param documentSnapshot The DocumentSnapshot containing user data.
     */
    public void loadProfilePicture(ImageView imageView, DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            if (documentSnapshot.contains("ProfilePictureCustom")) {
                String profilePicUrl = documentSnapshot.getString("ProfilePictureCustom");

                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                    downloadAndSetProfilePicture(imageView, profilePicUrl);
                } else {
                    // Generate and set default profile picture
                    String name = documentSnapshot.getString("Name");
                    char firstLetter = name.charAt(0);
                    Bitmap defaultProfilePhoto = generateDefaultProfilePhoto(firstLetter);
                    imageView.setImageBitmap(defaultProfilePhoto);
                }
            } else {
                String name = documentSnapshot.getString("Name");
                char firstLetter = name.charAt(0);
                Bitmap defaultProfilePhoto = generateDefaultProfilePhoto(firstLetter);
                imageView.setImageBitmap(defaultProfilePhoto);
            }
        }
    }

    /**
     * Downloads the profile picture from Firebase Storage and sets it into the provided ImageView.
     *
     * @param imageView         The ImageView to set the profile picture into.
     * @param profilePictureUrl The URL of the profile picture in Firebase Storage.
     */
    public void downloadAndSetProfilePicture(ImageView imageView, String profilePictureUrl) {
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
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e("UserProfileManager", "Failed to decode byte array into Bitmap");
                }
            } else {
                Log.e("UserProfileManager", "Downloaded byte array is null or empty");
            }
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("UserProfileManager", "Failed to download profile picture: " + exception.getMessage());
        });
    }

    /**
     * Generates a default profile photo with the provided initial character.
     *
     * @param initialChar The initial character of the user's name.
     * @return The generated default profile photo as a Bitmap.
     */
    public Bitmap generateDefaultProfilePhoto(char initialChar) {
        // Access resources using context
        int imageSize = context.getResources().getDimensionPixelSize(R.dimen.profile_image_size);
        int textSize = context.getResources().getDimensionPixelSize(R.dimen.profile_image_text_size);

        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() / 2f) - ((paint.descent() + paint.ascent()) / 2f);
        canvas.drawColor(Color.GRAY);
        canvas.drawText(String.valueOf(initialChar), x, y, paint);

        return bitmap;
    }

    /**
     * Stops listening for changes in user data.
     */
    public void stopListening() {
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Interface for handling fetched user data.
     */
    public interface UserDataCallback {
        /**
         * Invoked when user data is received.
         *
         * @param documentSnapshot The DocumentSnapshot containing user data.
         */
        void onDataReceived(DocumentSnapshot documentSnapshot);
    }
}
