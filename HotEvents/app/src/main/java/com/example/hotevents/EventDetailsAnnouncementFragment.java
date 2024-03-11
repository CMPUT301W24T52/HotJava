package com.example.hotevents;

import com.google.firebase.messaging.FirebaseMessaging;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailsAnnouncementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailsAnnouncementFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String eventId;
    private String eventTitle;
    Button sendNotificationButton;
    private EditText notificationMessageInput;
    private static final String TAG = "EventDetailsAnnouncement";

    public EventDetailsAnnouncementFragment() {
        // Required empty public constructor
        firebaseMessaging = FirebaseMessaging.getInstance();
    }

    private FirebaseMessaging firebaseMessaging;

    // Constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventDetailsAnnouncementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailsAnnouncementFragment newInstance(String eventId, String eventTitle) {
        EventDetailsAnnouncementFragment fragment = new EventDetailsAnnouncementFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        args.putString("eventId", eventId);
        args.putString("eventTitle", eventTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            eventId = getArguments().getString("eventId");
            eventTitle = getArguments().getString("eventTitle");
        }
        FirebaseApp.initializeApp(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_details_announcement, container, false);

        // Initialize notificationMessageInput and sendNotificationButton
        notificationMessageInput = rootView.findViewById(R.id.announcement_message_textinputedittext);
        sendNotificationButton = rootView.findViewById(R.id.send_notification_button);

        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotificationToAllSignups();
            }
        });

        return rootView;
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