package com.example.hotevents;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


/**
 * Attendee List page
 */
public class AttendeeList extends AppCompatActivity {
    ImageButton backButton;
    private FirebaseFirestore db;
    String eventId;
    private static final String TAG = "AttendeeList";
    TextView signups_number;
    TextView checkins_number;

    ArrayList<Attendee> attendeesArray = new ArrayList<>();
    AttendeeListAdapter attendeeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_list);

        // Get DB and eventId
        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getExtras().getString("eventId");

        setViews();

        // Listen for changes in event checkin data
        db.collection("Events")
                .document(eventId)
                .collection("checkins")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AttendeeList", "Listen failed", error);
                        return;
                    }
                    // Clear attendee list
                    attendeesArray.clear();
                    // Init signup and checkin counts
                    int signups = 0;
                    int checkins = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        // Add to signup count for every attendee
                        signups++;
                        // Check if attendee has checked in at least once
                        if (doc.getLong("count") > 0) checkins++;
                        // Grab attendee's user document
                        db.collection("Users")
                                .document(doc.getString("UID"))
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userDoc = task.getResult();
                                        if (userDoc.exists()) {
                                            // Get attendees name, checkin count, and profile picture url
                                            String name = userDoc.getString("Name");
                                            int checkinCount = doc.getLong("count").intValue();
                                            String profilePicture;
                                            String profilePictureCustom = userDoc.getString("ProfilePictureCustom");
                                            String profilePictureDefault = userDoc.getString("ProfilePictureDefault");
                                            // Check if custom picture or default
                                            if (profilePictureCustom != null && !profilePictureCustom.isEmpty()) {
                                                profilePicture = profilePictureCustom;
                                            } else {
                                                profilePicture = profilePictureDefault;
                                            }
                                            // Add new Attendee and notify dataset change
                                            attendeesArray.add(new Attendee(name, checkinCount, profilePicture));
                                            attendeeListAdapter.notifyDataSetChanged();
                                            Log.d(TAG, "user added");
                                        } else {
                                            Log.d(TAG, "User doesn't exist");
                                        }
                                    } else {
                                        Log.d(TAG, "Failed to grab user");
                                    }
                                });
                    }

                    // Set the text to the counts
                    signups_number.setText(String.valueOf(signups));
                    checkins_number.setText(String.valueOf(checkins));
                });
    }

    private void setViews() {
        signups_number = findViewById(R.id.signups_number);
        checkins_number = findViewById(R.id.checkins_number);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        RecyclerView recyclerView = findViewById(R.id.attendee_recycler_view);
        attendeeListAdapter = new AttendeeListAdapter(this, attendeesArray);
        recyclerView.setAdapter(attendeeListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}