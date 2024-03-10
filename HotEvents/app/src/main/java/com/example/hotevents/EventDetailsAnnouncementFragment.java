package com.example.hotevents;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

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
    Button sendNotificationButton;
    private EditText notificationMessageInput;
    private static final String TAG = "EventDetailsAnnouncement";

    public EventDetailsAnnouncementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventDetailsAnnouncementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailsAnnouncementFragment newInstance(String eventId) {
        EventDetailsAnnouncementFragment fragment = new EventDetailsAnnouncementFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        args.putString("eventId", eventId);
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
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_details_announcement, container, false);

        // Initialize notificationMessageInput and sendNotificationButton
        notificationMessageInput = rootView.findViewById(R.id.announcement_message);
        sendNotificationButton = rootView.findViewById(R.id.send_announcement_button);

        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotificationToAllSignups();
            }
        });

        return rootView;
    }
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
                        String deviceId = documentSnapshot.getId(); // Get the device ID from the document ID
                        sendPushNotification(deviceId, notificationMessage);
                    }
                    Log.d("EventDetailsFragment", "Announcement sent successfully");
                    Toast.makeText(getActivity(), "Announcement sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetailsFragment", "Failed to send announcement", e);
                    Toast.makeText(getActivity(), "Failed to send announcement", Toast.LENGTH_SHORT).show();
                });
    }


    private void sendPushNotification(String deviceId, String message) {
        // Create a data payload for the notification
//        Map<String, String> dataPayload = new HashMap<>();
//        dataPayload.put("message", message);
//
//        // Create the RemoteMessage
//        RemoteMessage.Builder remoteMessageBuilder = new RemoteMessage.Builder(deviceId);
//        remoteMessageBuilder.setData(dataPayload);
//
//        // Send the message
//        FirebaseMessaging.getInstance().send(remoteMessageBuilder.build())
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (task.isSuccessful()) {
//                            // Notification sent successfully
//                            Log.d(TAG, "Notification sent successfully");
//                        } else {
//                            // Failed to send notification
//                            Log.e(TAG, "Failed to send notification", task.getException());
//                        }
//                    }
//                });
    }
}