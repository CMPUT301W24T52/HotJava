package com.example.hotevents;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    ArrayList<Event> myEventDataArray;      //Signed up events
    ArrayList<Event> upcomingEventDataArray;
    //ListView eventList;
    RecyclerView myEventView;
    RecyclerView upcomingEventView;
    LinearLayoutManager myEventHorizantleManager;
    LinearLayoutManager upcomingEventManager;
    MyEventsAdapter myEventsAdapter;
    UpcomingEventAdapter upcomingEventsAdapter;
    private String UserName = "";
    ArrayList<String> SignedUpEvent; // <----- This does not need to be here, move elsewhere
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout profile, signedUpEvents, publishedEvents, notifications, organizeEvent, admin;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myEventDataArray = new ArrayList<Event>();
        upcomingEventDataArray = new ArrayList<Event>();
        myEventView = (RecyclerView) findViewById(R.id.signedupevent_list);
        myEventHorizantleManager = new LinearLayoutManager(this);
        myEventHorizantleManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        myEventView.setHasFixedSize(false);
        myEventView.setLayoutManager(myEventHorizantleManager);

        upcomingEventManager = new LinearLayoutManager(this);
        upcomingEventView = (RecyclerView) findViewById(R.id.upcoming_events_list);
        upcomingEventView.setHasFixedSize(false);
        upcomingEventView.setLayoutManager(upcomingEventManager);


        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        myEventsAdapter = new MyEventsAdapter(myEventDataArray, this);
        upcomingEventsAdapter = new UpcomingEventAdapter(upcomingEventDataArray, this);

        myEventView.setAdapter(myEventsAdapter);
        upcomingEventView.setAdapter(upcomingEventsAdapter);


        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


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
                    myEventDataArray.clear();
                    upcomingEventDataArray.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String eventId = doc.getId();
                        String title = doc.getString("Title");
                        Date startDate = doc.getDate("StartDateTime");
                        Date endDate = doc.getDate("EndDateTime");
                        String description = doc.getString("Description");
                        String organizerId = doc.getString("Organizer Id");
                        Log.d("Firestore: ", String.format("Event (%s) fetched", title) );

                        Event newEvent = new Event(title);
                        newEvent.setEventId(eventId);
                        newEvent.setStartDateTime(startDate);
                        newEvent.setEndDateTime(endDate);
                        newEvent.setDescription(description);
                        newEvent.setOrganiserId(organizerId);
                        myEventDataArray.add(newEvent);
                        upcomingEventDataArray.add(newEvent);

                    }
                    myEventsAdapter.notifyDataSetChanged();
                    upcomingEventsAdapter.notifyDataSetChanged();
                }
            }
        });


        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        profile = findViewById(R.id.profile);
//        signedUpEvents = findViewById(R.id.signedUpEvents);
//        publishedEvents = findViewById(R.id.publishedEvents);
//        notifications = findViewById(R.id.notifications);
        organizeEvent = findViewById(R.id.organizeEvent);
        admin = findViewById(R.id.admin);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, ProfileActivity.class);
            }
        });

        organizeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirectActivity(MainActivity.this, CreateEventActivity.class);
                //Sending the user ID to the create event page to be able to save the organizer with their event
                Intent myIntent = new Intent(MainActivity.this, CreateEventActivity.class);
                myIntent.putExtra("organiser", deviceId);
                startActivity(myIntent);
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, AdminOptionsActivity.class);
            }
        });


//        CreateEventButton = findViewById(R.id.floatingActionButton);
//        CreateEventButton.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, CreateEventActivity.class));
//        });






//        myEventsAdapter.setOnItemClickListener(new MyEventsAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                Intent myIntent = new Intent(MainActivity.this, EventDetailsActivity.class);
//                myIntent.putExtra("event", eventDataArray.get(position));
//                startActivity(myIntent);
//            }
//        });


//        eventList.setOnItemClickListener((parent, view, position, id) -> {
//            Intent myIntent = new Intent(MainActivity.this, EventDetailsActivity.class);
//            myIntent.putExtra("event", eventDataArray.get(position));
//            startActivity(myIntent);
//        });

//        profileButton = findViewById(R.id.profileButton);
//        profileButton.setOnClickListener(view -> {
//            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
//        });
//
//        NavButton = findViewById(R.id.NavButton);
//        NavButton.setOnClickListener(view -> {
//            startActivity(new Intent(MainActivity.this, NavigationMenu.class));
//        });

    }
    private void handleNewUserInput(FirebaseFirestore db, String deviceId) {
        SignedUpEvent = new ArrayList<String>();
        UserName = "Test User";
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("ProfilePicture", "");
        newUser.put("userType", "Normal");
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

    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }
    public static void closeDrawer(DrawerLayout drawerLayout){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    public static void redirectActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
//        activity.finish();

    }
    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }
}