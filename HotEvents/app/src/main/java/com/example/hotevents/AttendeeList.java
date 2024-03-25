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
import java.util.List;

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

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getExtras().getString("eventId");


        setViews();

        RecyclerView recyclerView = findViewById(R.id.attendee_recycler_view);

        attendeeListAdapter = new AttendeeListAdapter(this, attendeesArray);
        recyclerView.setAdapter(attendeeListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Listen for changes in event checkin data
        db.collection("Events")
                .document(eventId)
                .collection("checkins")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AttendeeList", "Listen failed", error);
                        return;
                    }
                    attendeesArray.clear();
                    List<String> signups = new ArrayList<>();
                    List<String> checkins = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        signups.add(doc.getString("UID"));
                        if (doc.getLong("count") > 0) {
                            checkins.add(doc.getString("UID"));
                        }
                        db.collection("Users")
                                .document(doc.getString("UID"))
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userDoc = task.getResult();
                                        if (userDoc.exists()) {
                                            String name = userDoc.getString("Name");
                                            int checkinCount = doc.getLong("count").intValue();
                                            String profilePicture;
                                            String profilePictureCustom = userDoc.getString("ProfilePictureCustom");
                                            String profilePictureDefault = userDoc.getString("ProfilePictureDefault");
                                            if (profilePictureCustom != null && !profilePictureCustom.isEmpty()) {
                                                profilePicture = profilePictureCustom;
                                            } else {
                                                profilePicture = profilePictureDefault;
                                            }
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
                    signups_number.setText(String.valueOf(signups.size()));
                    checkins_number.setText(String.valueOf(checkins.size()));
                });
    }

    private void setViews() {
        signups_number = findViewById(R.id.signups_number);
        checkins_number = findViewById(R.id.checkins_number);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
}

class Attendee {
    private String name;
    private int checkinCount;
    private String profileImageUrl;

    public Attendee(String name, int checkinCount, String profileImageUrl) {
        this.name = name;
        this.checkinCount = checkinCount;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public int getCheckinCount() {
        return checkinCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}