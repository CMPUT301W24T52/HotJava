package com.example.hotevents;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * A utility class for storing notifications in Firestore when announcement is made.
 * Currently its only used in EventDetailAnnouncementFragment to store announceent.
 * In future this will be used for milestone as well same as announcement,
 */
public class NotificationStorer {
    private static final String TAG = "NotificationHelper";
    // Get the current timestamp

    public static void storeNotification(String fcmToken, String eventId, String notificationMessage) {
        // Access Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current timestamp
        Date timestamp = new Date(System.currentTimeMillis());

        // Create a map with the data to be stored
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("fcmToken", fcmToken);
        notificationData.put("eventId", eventId);
        notificationData.put("notificationMessage", notificationMessage);
        notificationData.put("timestamp", timestamp);


        // Add data to the Notifications collection
        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Notification stored successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error storing notification", e));
    }
}
