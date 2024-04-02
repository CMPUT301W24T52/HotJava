package com.example.hotevents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class OrganizedEventsActivity extends Activity {

    private FirebaseFirestore db;
    private CollectionReference organizedEventsRef;
    ArrayList<Event> organizedEventDataArray;
    RecyclerView organizedEventView;
    LinearLayoutManager organizedEventHorizantleManager;
    OrganizedEventsAdapter organizedEventsAdapter;
    private ImageView backButton;
    private static final String TAG = "OrganizedBrowseActivity";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organized_events);

        organizedEventDataArray = new ArrayList<Event>();
        organizedEventView = (RecyclerView) findViewById(R.id.organized_event_list);
        organizedEventHorizantleManager = new LinearLayoutManager(this);
        organizedEventHorizantleManager.setOrientation(LinearLayoutManager.VERTICAL);
        organizedEventView.setHasFixedSize(false);
        organizedEventView.setLayoutManager(organizedEventHorizantleManager);

        db = FirebaseFirestore.getInstance();
        organizedEventsRef = db.collection("Events");
        organizedEventsRef = db.collection("Events");

        organizedEventsAdapter = new OrganizedEventsAdapter(organizedEventDataArray, this);

        organizedEventView.setAdapter(organizedEventsAdapter);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Adds listener to event reference. Populates Event Array with Event data from DB
        String userId = deviceId; // Assuming the document ID in the Users collection

        Log.d(TAG, "Fetching events for user: " + userId);

        db.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot userSnapshot = task.getResult();
                        if (userSnapshot.exists()) {
                            ArrayList<String> createdEvents = (ArrayList<String>) userSnapshot.get("CreatedEvents");
                            if (createdEvents != null && !createdEvents.isEmpty()) {
                                Log.d(TAG, "Found created events for user " + userId + ": " + createdEvents);
                                organizedEventsRef.whereIn(FieldPath.documentId(), createdEvents)
                                        .get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful()) {
                                                for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                                    String eventId = eventDoc.getId();
                                                    String title = eventDoc.getString("Title");
                                                    Date startDate = eventDoc.getDate("StartDateTime");
                                                    Date endDate = eventDoc.getDate("EndDateTime");
                                                    String description = eventDoc.getString("Description");
                                                    String organizerId = eventDoc.getString("Organizer Id");
                                                    String posterStr = eventDoc.getString("Poster");
                                                    String location = eventDoc.getString("Location");

                                                    Event newEvent = new Event(title);
                                                    newEvent.setEventId(eventId);
                                                    newEvent.setStartDateTime(startDate);
                                                    newEvent.setEndDateTime(endDate);
                                                    newEvent.setDescription(description);
                                                    newEvent.setOrganiserId(organizerId);
                                                    newEvent.setPosterStr(posterStr);
                                                    newEvent.setLocation(location);
                                                    organizedEventDataArray.add(newEvent);
                                                // Notify the adapter after adding events
                                                organizedEventsAdapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                Log.e(TAG, "Error getting events: ", eventTask.getException());
                                            }
                                        });
                            } else {
                                Log.d(TAG, "No created events found for user " + userId);
                            }
                        } else {
                            Log.d(TAG, "User document " + userId + " does not exist.");
                        }
                    } else {
                        Log.e(TAG, "Error getting user document " + userId + ": ", task.getException());
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

}
