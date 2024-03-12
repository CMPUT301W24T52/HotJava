package com.example.hotevents;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Provider;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Milestones extends Service{

    Event myEvent;
    UserProfiles myProfile;
    String OrganiserId;
    String deviceId;
    String eventTitle;
    String fcmToken;
    String eventId;
    String TAG = "Milestones";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (myEvent.getOrganiserId() != null) {
            OrganiserId = myEvent.getOrganiserId();
        }
        if (myEvent.getTitle() != null) {
            eventTitle = myEvent.getTitle();
        }
        if (myEvent.getEventId() != null) {
            eventId = myEvent.getEventId();
        }
        if (myProfile.getUid() != null) {
            deviceId = myEvent.getOrganiserId();
        }

        // Retrieve the FCM registration token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // Get the token
                            fcmToken = task.getResult();
                            Log.d(TAG, "FCM Registration Token: " + fcmToken);
                            Toast.makeText(new Milestones(), "FCM Registration Token: " + fcmToken, Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle the error
                            Log.e(TAG, "Failed to retrieve FCM Registration Token", task.getException());
                        }
                    }
                });

        if (OrganiserId == deviceId) {
            sendNotificationIfConditionMet();
        }
        return flags;
    }
    private void sendNotificationIfConditionMet() {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Events").document().collection("signups")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        long signupsCount = queryDocumentSnapshots.size();

                        // Check if conditions for sending notifications are met
                        if (signupsCount == 1 || signupsCount == 3) {
                            // Create a notification message based on the signups count
                            String notificationMessage = "Announcement: Signups count for event '" + eventTitle + "' is " + signupsCount + ".";
                            // Send the notification to the organizer
                            sendPushNotification(fcmToken, notificationMessage, eventId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to retrieve signups count", e);
                        Toast.makeText(new Milestones(), "Failed to retrieve signups count", Toast.LENGTH_SHORT).show();
                    });
    }


    /**
     * Sends a push notification to the specified FCM token with the given message.
     *
     * @param fcmToken    The FCM token of the device.
     * @param messageText The message to be sent.
     */
    private void sendPushNotification(String fcmToken, String messageText, String eventId) {
        // Create a message with the given payload
//        RemoteMessage notificationMessage = new RemoteMessage.Builder(fcmToken)
//                .addData("message", "Hello")
//                .build();
//
//        // Send the message to the device corresponding to the provided registration token
//        FirebaseMessaging.getInstance().send(notificationMessage);
//
//        // You can log a success message after sending the notification
//        Log.d(TAG, "Successfully sent message to device with token: " + fcmToken);

//            }
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonNotif = new JSONObject();
            JSONObject wholeObj = new JSONObject();
            try {
                jsonNotif.put("title", "ANNOUNCEMENT - " + eventTitle);
                jsonNotif.put("body", messageText);
                wholeObj.put("to", fcmToken);
                wholeObj.put("notification", jsonNotif);
            } catch (JSONException e) {
                Log.d(TAG, e.toString());
            }
            RequestBody rBody = RequestBody.create(mediaType, wholeObj.toString());
            Request request = new Request.Builder().url("https://fcm.googleapis.com/fcm/send")
                    .post(rBody)
                    .addHeader("Authorization", "key=AAAAlidLyZE:APA91bF1suXeK0OgT_eZIP9uEPOCarD4zMfUyLWvo5-ljaXKQp4wuZhU2Ik2C63QLZKsvKGnOuzNIh_56WCIl1R8-rENZFlPPrwAB8Corgtnba5w8pMpknuhzp7_q1dTyshB37uTu4EN")
                    .addHeader("Content-Type", "application/json").build();
            Log.d(TAG, "think working");
//            Toast.makeText(getActivity(), "Announcement sent successfully", Toast.LENGTH_SHORT).show();

            try {
                // Execute the request synchronously
                Response response = client.newCall(request).execute();
                // Check if the notification was sent successfully
                if (response.isSuccessful()) {
                    // Call the helper method to store the notification data
                    NotificationStorer.storeNotification(fcmToken, eventId, messageText);
                    Log.d(TAG, "Notification sent successfully");
                } else {
                    // Handle the case where notification sending failed
                    Log.e(TAG, "Failed to send notification: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            }
        }).start();
    }
}
