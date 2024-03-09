package com.example.hotevents;

import androidx.annotation.NonNull;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

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
    ArrayList<Event> eventDataArray;
    //ListView eventList;
    RecyclerView myEventView;
    RecyclerView.LayoutManager myEventViewLayoutManager;
    LinearLayoutManager myEventHorizantleManager;
    MyEventsAdapter myEventsAdapter;
    private String UserName = "";
    ArrayList<String> SignedUpEvent;
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout profile, signedUpEvents, publishedEvents, notifications, organizeEvent, admin;
    Switch toggleGeo;
    private static final String TAG = "MainActivity";
    private ListenerRegistration userListener;
    private TextView textViewName;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventDataArray = new ArrayList<Event>();
        myEventView = (RecyclerView) findViewById(R.id.event_list);
        myEventHorizantleManager = new LinearLayoutManager(this);
        myEventHorizantleManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        myEventView.setHasFixedSize(false);
        myEventView.setLayoutManager(myEventHorizantleManager);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        myEventsAdapter = new MyEventsAdapter(eventDataArray, this);

        myEventView.setAdapter(myEventsAdapter);


        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        TextView UserName = findViewById(R.id.userName);
        textViewName = findViewById(R.id.userName);

        db.collection("Users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, user "logged in"
//                    UserName = task.getResult().getString(("Name"));
                    UserName.setText(document.getString("Name"));
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
                        eventDataArray.add(newEvent);

                    }
                    myEventsAdapter.notifyDataSetChanged();
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

        /**
         * Initializes and sets up the toggle switch for location access.
         *
         * This method initializes the toggle switch for location access, sets it to true by default,
         * and sets up a listener to handle changes to the toggle state. When the toggle state changes,
         * it updates the corresponding field in the Firestore database.
         *
         * @param context The context of the activity.
         * @param db The instance of the Firestore database.
         * @param TAG The tag used for logging.
         * @param toggleGeo The toggle switch for location access.
         */
        toggleGeo = findViewById(R.id.toggleGeo);
        toggleGeo.setChecked(true); // Set the toggle switch to true by default
        toggleGeo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the field in Firestore based on the toggle state
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                DocumentReference userRef = db.collection("Users").document(deviceId);
                userRef.update("geo", isChecked)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Geo toggle state updated successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error updating geo toggle state", e);
                                // Handle failure
                            }
                        });
            }
        });


        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, AdminOptionsActivity.class);
            }
        });

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
        newUser.put("geo",true);

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

    protected void onResume() {
        super.onResume();
        fetchUserDataFromFirestore();
    }
    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }
    /**
     * Fetches user data from Firestore and updates the Navigation drawer UI with the user's name.
     * This method listens for changes in the user's profile data and automatically
     * updates the UI when changes occur.
     */
    private void fetchUserDataFromFirestore() {
        // Get device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Listen for changes in user profile data
        userListener = db.collection("Users").document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("ProfileActivity", "Listen failed.", e);
                return;
            }

            // Check if document exists and update UI with user data
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");

                textViewName.setText(name);

                // You can also handle profile photo here if it's stored in Firestore
            } else {
                Log.d("ProfileActivity", "No such document");
            }
        });
    }



}