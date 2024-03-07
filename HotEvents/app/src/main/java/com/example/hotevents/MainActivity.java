package com.example.hotevents;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Home Page and control center of program
 * TODO:
 * - Change Menu button to Hamburger Icon
 * - Provide endpoints to integrate other components
 * - Make Event Icons functional
 * - [Optional] Refresh Functionality
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    MyEventsAdapter myEventsAdapter;
    ArrayList<Event> eventDataArray;
    ListView eventList;
    Button menuButton;
    CircleImageView profileButton;
    Button NavButton;
    private String UserName = ""; //???
    ArrayList<String> SignedUpEvent;  //???

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        eventDataArray = new ArrayList<Event>();
        eventList = findViewById(R.id.event_list);
        myEventsAdapter = new MyEventsAdapter(this, eventDataArray);

        eventList.setAdapter(myEventsAdapter);

        String deviceId = "gyvygvghcg";//Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        db.collection("Users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, user "logged in"
                     UserName = task.getResult().getString(("Name"));
                } else {
                    // No such document, user not "logged in"
                    handleNewUserInput(db,deviceId);
                }
            }
        });

        // Adds listener to event reference. Populates Event Array with Event data from DB
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("FireStore", error.toString());
                    return;
                }
                if (value != null){
                    eventDataArray.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String title = doc.getString("Title");
                        Log.d("Firestore: ", String.format("Event (%s) fetched", title) );

                        eventDataArray.add(new Event(title));
                    }
                    myEventsAdapter.notifyDataSetChanged();
                }
            }
        });
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        NavButton = findViewById(R.id.NavButton);
        NavButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, NavigationMenu.class));
        });
    }
        private void handleNewUserInput(FirebaseFirestore db, String deviceId) {
        SignedUpEvent = new ArrayList<String>();
        UserName = "Harsh Patel";
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("ProfilePicture", "");
        newUser.put("UID", deviceId);
        newUser.put("Name", UserName);
        newUser.put("Contact", "");
        newUser.put("Email ID", "");
        newUser.put("Location", "");
        newUser.put("SignedUpEvent",SignedUpEvent);


            // Add a new document with the device ID as the document ID
        db.collection("Users").document(deviceId).set(newUser)
                .addOnSuccessListener(aVoid -> {

        });
    }
}