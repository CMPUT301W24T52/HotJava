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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Home Page and control center of program
 * TODO:
 * - Change Menu button to Hamburger Icon
 * - Provide endpoints to integrate other components
 * - Make Event Icons functional
 * - [Optional] Refresh Functionality
 */
public class MainActivity extends AppCompatActivity{

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
    ImageView menu, notifications_toolbar;
    LinearLayout profile, signedUpEvents, organizedEvents, notifications, organizeEvent, admin;
    Switch toggleGeo;
    private static final String TAG = "MainActivity";
    private ListenerRegistration userListener;
    private TextView textViewName;
    private CircleImageView profilePhotoImageView;
    private FirebaseStorage storage;
    private Boolean success = false;


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
        storage = FirebaseStorage.getInstance();
        eventsRef = db.collection("Events");

        myEventsAdapter = new MyEventsAdapter(myEventDataArray, this);
        upcomingEventsAdapter = new UpcomingEventAdapter(upcomingEventDataArray, this);

        myEventView.setAdapter(myEventsAdapter);
        upcomingEventView.setAdapter(upcomingEventsAdapter);


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
                    // Retrieve the FCM registration token
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (task.isSuccessful()) {
                                        // Get the token
                                        String token = task.getResult();
                                        handleNewUserInput(db,deviceId,token);
                                        Log.d(TAG, "FCM Registration Token: " + token);
                                        Toast.makeText(MainActivity.this, "FCM Registration Token: " + token, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle the error
                                        Log.e(TAG, "Failed to retrieve FCM Registration Token", task.getException());
                                    }
                                }
                            });
                }
            }
        });

        // Adds listener to event reference. Populates upcoming Event Array with Event data from DB
        eventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.e("FireStore", error.toString());
                    return;
                }

                //Downloading the posters happens in an asynchronous manner
                //To get around this we can get a count of the query, and until this count is reached
                //we loop to give the data array enough time to receive every event
                Integer count = value.size();
                Log.e("Event", "Query size: " + count);

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
                        String location = doc.getString("Location");
                        String posterStr = doc.getString("Poster");
                        Log.d("Firestore: ", String.format("Event (%s) fetched", title));

                        Event newEvent = new Event(title);
                        newEvent.setEventId(eventId);
                        newEvent.setStartDateTime(startDate);
                        newEvent.setEndDateTime(endDate);
                        newEvent.setDescription(description);
                        newEvent.setOrganiserId(organizerId);
                        newEvent.setLocation(location);
                        newEvent.setPosterStr(posterStr);


                        //Downloading the poster and waiting for completion before adding event to array
                        if (posterStr != null){
                            //Thread thread = downloadAndSetPoster(posterStr, newEvent);
                            //thread.join();
                            newEvent.getPoster();
                            myEventDataArray.add(newEvent);
                            upcomingEventDataArray.add(newEvent);
                        }
                        else{
                            myEventDataArray.add(newEvent);
                            upcomingEventDataArray.add(newEvent);
                        }

                        // if user.id is in signed up events --> myEventDataArray.add(newEvent);
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
        organizedEvents = findViewById(R.id.publishedEvents);
        notifications = findViewById(R.id.notifications);
        notifications_toolbar = findViewById(R.id.notifications_toolbar);
        organizeEvent = findViewById(R.id.organizeEvent);
        admin = findViewById(R.id.admin);
        profilePhotoImageView = findViewById(R.id.CImageView);


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
        organizedEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, OrganizedEventsActivity.class);
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, NotificationDisplayActivity.class);
            }
        });

        notifications_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, NotificationDisplayActivity.class);
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

    /**
     * Downloads the profile picture from the given URL and sets it to the profile photo image view.
     *
     * @param profilePictureUrl The URL of the profile picture.
     */
    private void downloadAndSetProfilePicture(String profilePictureUrl) {
        // Create a reference to the Firebase Storage URL
        StorageReference photoRef = storage.getReferenceFromUrl(profilePictureUrl);

        // Download the image into a Bitmap
        final long FIVE_MEGABYTE = 5 * 1024 * 1024;
        photoRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Decode the byte array into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            // Set the downloaded profile picture to the image view
            profilePhotoImageView.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("ProfileActivity", "Failed to download profile picture: " + exception.getMessage());
        });
    }
    private void handleNewUserInput(FirebaseFirestore db, String deviceId, String token) {
        SignedUpEvent = new ArrayList<String>();
        UserName = "Test User";
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("ProfilePictureDefault", "");
        newUser.put("ProfilePictureCustom", "");
        newUser.put("userType", "Normal");
        newUser.put("UID", deviceId);
        newUser.put("Name", UserName);
        newUser.put("Contact", "");
        newUser.put("Email ID", "");
        newUser.put("Location", "");
        newUser.put("SignedUpEvent",SignedUpEvent);
        newUser.put("CreatedEvents", new ArrayList<String>());
        newUser.put("geo",true);
        newUser.put("fcmToken",token);

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

                // Check if ProfilePicture field is present
                // Check if ProfilePictureCustom field is present
                if (documentSnapshot.contains("ProfilePictureCustom")) {
                    String profilePicUrl = documentSnapshot.getString("ProfilePictureCustom");

                    // Check if profilePicUrl is not null or empty
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        // Download and set profile picture
                        downloadAndSetProfilePicture(profilePicUrl);
                    } else {
                        // Generate default profile photo based on the first letter of the name
                        char firstLetter = name.charAt(0);
                        String profilePicUrl1 = documentSnapshot.getString("ProfilePictureDefault");
                        if (profilePicUrl1 != null && !profilePicUrl1.isEmpty()) {
                            downloadAndSetProfilePicture(profilePicUrl1);
                        }

                    }
                } else {
                    // Handle the case where the ProfilePicture field is not present in the document
                    Log.d("ProfileActivity", "No ProfilePicture field in the document");
                }
            } else {
                Log.d("ProfileActivity", "No such document");
            }
        });
    }

}