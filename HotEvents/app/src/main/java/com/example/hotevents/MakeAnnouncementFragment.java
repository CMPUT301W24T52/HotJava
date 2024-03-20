package com.example.hotevents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MakeAnnouncementFragment extends DialogFragment {

    EditText notificationMessageInput;
    String eventId;
    String eventTitle;
    private static final String TAG = "MakeAnnouncement";

    public static MakeAnnouncementFragment newInstance(String eventId, String eventTitle) {
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("eventTitle", eventTitle);
        MakeAnnouncementFragment fragment = new MakeAnnouncementFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Get view
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_make_announcement, null);

        // Set announcement input
        notificationMessageInput = view.findViewById(R.id.edit_text_announcement_text);

        // Grab the eventId and eventTitle
        eventId = getArguments().getString("eventId");
        eventTitle = getArguments().getString("eventTitle");

        // Make a new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Make an announcement")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send", ((dialog, which) -> {
                    sendNotificationToAllSignups();
                })).create();
    }

    /**
     * Sends notification to all signups for the event.
     */
    private void sendNotificationToAllSignups() {
        String notificationMessage = notificationMessageInput.getText().toString().trim();
        if (notificationMessage.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a notification message", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            // Handle case where eventId is null or empty
            Toast.makeText(getActivity(), "Invalid eventId", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId).collection("signups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String fcmToken = documentSnapshot.getString("fcmToken"); // Get the FCM token from the document
                        sendPushNotification(fcmToken, notificationMessage, eventId);
                    }
                    Log.d("EventDetailsFragment", "Announcement sent successfully");
                    //                    Toast.makeText(getActivity(), "Announcement sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailsFragment", "Failed to send announcement", e);
                    Toast.makeText(getActivity(), "Failed to send announcement", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sends a push notification to the specified FCM token with the given message.
     *
     * @param fcmToken    The FCM token of the device.
     * @param messageText The message to be sent.
     */
    private void sendPushNotification(String fcmToken, String messageText, String eventId) {
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
