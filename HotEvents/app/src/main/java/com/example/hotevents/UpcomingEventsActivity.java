package com.example.hotevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents the display for activity that contains all upcoming activities
 * Is also used when a user searches for an event, the array of events is passed
 * to the activity
 */
public class UpcomingEventsActivity extends AppCompatActivity {
    private static final String TAG = "UpcomingEventsActivity";

    //Firebase
    FirebaseFirestore db;
    CollectionReference eventsRef;

    //Events
    ArrayList<Event> eventArray;
    UpcomingEventActivityAdapter uEventAdapter;

    //Views and Components
    RecyclerView upcomingEventView;
    LinearLayoutManager upcomingEventLayoutManager;
    ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_events);
        Intent myIntent = getIntent();
        eventArray =  myIntent.getParcelableArrayListExtra("events");

//        eventArray = new ArrayList<Event>();
        upcomingEventView = (RecyclerView) findViewById(R.id.upcoming_event_list);
        upcomingEventLayoutManager = new LinearLayoutManager(this);
        upcomingEventLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        upcomingEventView.setHasFixedSize(false);
        upcomingEventView.setLayoutManager(upcomingEventLayoutManager);

        uEventAdapter = new UpcomingEventActivityAdapter(eventArray, this);


//        db = FirebaseFirestore.getInstance();
//        eventsRef = db.collection("Events");

        upcomingEventView.setAdapter(uEventAdapter);

        //get upcoming events
//        displayEvents();
        // back button functionality
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}