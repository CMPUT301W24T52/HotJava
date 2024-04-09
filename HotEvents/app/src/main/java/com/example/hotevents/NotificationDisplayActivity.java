package com.example.hotevents;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;

/**
 * Activity responsible for displaying notifications retrieved from Firestore.
 */
public class NotificationDisplayActivity extends AppCompatActivity {

    private ImageView backButton;
    private ListView notificationsListView;
    private ArrayList<Notification> notificationsList;
    private NotificationsAdapter notificationsAdapter;
    private FirebaseFirestore db;
    private String TAG = "NotificationsDisplayActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize notifications list
        notificationsList = new ArrayList<>();

        // Find the ListView in your layout
        notificationsListView = findViewById(R.id.notification_listview);

        // Initialize adapter
        notificationsAdapter = new NotificationsAdapter(this, notificationsList);

        // Set the adapter for the ListView
        notificationsListView.setAdapter(notificationsAdapter);

        // Retrieve FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // Get the token
                            String userfcmToken = task.getResult();
                            Log.d(TAG, "FCM Registration Token: " + userfcmToken);
                            fetchNotificationsForToken(userfcmToken);
                        } else {
                            // Handle the error
                            Log.e(TAG, "Failed to retrieve FCM Registration Token", task.getException());
                        }
                    }
                });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchNotificationsForToken(String userFcmToken) {
        db.collection("Notifications")
                .whereEqualTo("fcmToken", userFcmToken)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(notificationTask -> {
                    Log.d(TAG, "Fetching Notifications");
                    if (notificationTask.isSuccessful()) {
                        // Clear existing notifications to avoid duplicates
                        notificationsList.clear();
                        for (DocumentSnapshot document : notificationTask.getResult()) {
                            // Retrieve data from Firestore document
                            String fcmToken = document.getString("fcmToken");
                            String eventId = document.getString("eventId");
                            String notificationMessage = document.getString("notificationMessage");
                            Date timestamp = document.getDate("timestamp"); // Retrieve timestamp as Timestamp

                            // Check if any of the fields is null
                            if (fcmToken != null && eventId != null && notificationMessage != null) {
                                // Retrieve poster URL from the event collection
                                db.collection("Events").document(eventId).get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful()) {
                                                DocumentSnapshot eventDocument = eventTask.getResult();
                                                if (eventDocument.exists()) {
                                                    String posterUrl = eventDocument.getString("Poster");
                                                    // Create a Notification object with the retrieved data
                                                    Notification notification = new Notification(fcmToken, eventId, notificationMessage, timestamp, posterUrl);
                                                    // Add the notification to the list
                                                    notificationsList.add(notification);
                                                    // Notify the adapter that the data set has changed
                                                    notificationsAdapter.notifyDataSetChanged();
                                                } else {
                                                    Log.e(TAG, "Event document doesn't exist");
                                                }
                                            } else {
                                                Log.e(TAG, "Error fetching event document", eventTask.getException());
                                            }
                                        });
                            }
                        }
                        Log.d(TAG, "All Notifications fetched");
                    } else {
                        // Handle the error
                        Log.e(TAG, "Error getting notifications", notificationTask.getException());
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}