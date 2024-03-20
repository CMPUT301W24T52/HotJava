package com.example.hotevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailsAnnouncementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailsAnnouncementFragment extends Fragment {

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
        // Grab the view
        View rootView = inflater.inflate(R.layout.fragment_event_details_announcement, container, false);

        // Get the db
        db = FirebaseFirestore.getInstance();

        // Set the needed variables for announcements
        announcementList = new ArrayList<>();
        announcementAdapter = new NotificationsAdapter(getContext(), announcementList);
        announcementListView = rootView.findViewById(R.id.announcements_listview);
        announcementListView.setAdapter(announcementAdapter);

        // Grab the notifications for this event
        db.collection("Notifications")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear announcement list
                        announcementList.clear();
                        // Loop through all notifications
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