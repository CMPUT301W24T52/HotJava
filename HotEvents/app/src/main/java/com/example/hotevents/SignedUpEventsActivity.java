package com.example.hotevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class SignedUpEventsActivity extends AppCompatActivity {
    private static final String TAG = "SignedUpActivity";
    //Firebase
    FirebaseFirestore db;
    FirebaseStorage sRef;
    CollectionReference eventsRef;
    DocumentReference userRef;

    //Events
    ArrayList<Event> eventArray;
//    UpcomingEventActivityAdapter eventAdapter;
    SignedUpEventsActivityAdapter signedUpEventsActivityAdapter;


    //User
    LoggedInUser user;

    //Views and Components
    RecyclerView signedUpEventView;
    LinearLayoutManager signedUpEventLayoutManager;
    ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_up_events);

        Intent intent = getIntent();
        eventArray = intent.getParcelableArrayListExtra("events");

        signedUpEventView = findViewById(R.id.signedupevent_list);
        signedUpEventLayoutManager = new LinearLayoutManager(this);
        signedUpEventLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        signedUpEventView.setHasFixedSize(false);
        signedUpEventView.setLayoutManager(signedUpEventLayoutManager);

        signedUpEventsActivityAdapter = new SignedUpEventsActivityAdapter(eventArray, this);
        signedUpEventView.setAdapter(signedUpEventsActivityAdapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}