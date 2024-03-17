package com.example.hotevents;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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

    // TODO: Rename and change types of parameters
    private String eventId;
    ArrayList<Notification> announcementList;
    NotificationsAdapter announcementAdapter;
    ListView announcementListView;
    FirebaseFirestore db;
    private static final String TAG = "EventDetailsAnnouncement";

    public EventDetailsAnnouncementFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId ID of event.
     * @return A new instance of fragment EventDetailsAnnouncementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventDetailsAnnouncementFragment newInstance(String eventId) {
        EventDetailsAnnouncementFragment fragment = new EventDetailsAnnouncementFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        FirebaseApp.initializeApp(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_details_announcement, container, false);

        db = FirebaseFirestore.getInstance();
        announcementList = new ArrayList<>();
        announcementAdapter = new NotificationsAdapter(getContext(), announcementList);
        announcementListView = rootView.findViewById(R.id.announcements_listview);
        announcementListView.setAdapter(announcementAdapter);

        db.collection("Notifications")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        announcementList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve data from Firestore document
                            String fcmToken = document.getString("fcmToken");
                            String eventId = document.getString("eventId");
                            String notificationMessage = document.getString("notificationMessage");
                            Date timestamp = document.getDate("timestamp");

                            // Check if any of the fields is null
                            if (fcmToken != null && eventId != null && notificationMessage != null) {
                                Notification announcement = new Notification(fcmToken, eventId, notificationMessage, timestamp);
                                announcementList.add(announcement);
                            }
                        }
                        announcementAdapter.notifyDataSetChanged();
                    }
                });

        return rootView;
    }
}