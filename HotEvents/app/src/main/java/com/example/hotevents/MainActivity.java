package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Home Page and control center of program
 * Contains routes to all other activities and is largely responsible for handling core
 * db functions such as User and Event updates
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    ArrayList<Event> myEventDataArray;      //Signed up events
    ArrayList<String> signedUpUIDs;
    ArrayList<Event> upcomingEventDataArray;    //upcoming events array

    RecyclerView myEventView;
    RecyclerView upcomingEventView;
    LinearLayoutManager myEventHorizantleManager;
    LinearLayoutManager upcomingEventManager;
    MyEventsAdapter myEventsAdapter;
    UpcomingEventAdapter upcomingEventsAdapter;
    private String UserName = "";
    ArrayList<String> SignedUpEvent;
    DrawerLayout drawerLayout;
    ImageView menu, notifications_toolbar;
    SearchView eventSearchView;
    LinearLayout upcomingEventList_button, signedUpEventList_button;
    LinearLayout profile, signedUpEvents, organizedEvents, notifications, organizeEvent, admin, contact;
    ImageView scannerButton;
    private static final String TAG = "MainActivity";
    private ListenerRegistration userListener;
    private TextView textViewName;
    private CircleImageView profilePhotoImageView;
    private FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventSearchView = findViewById(R.id.searchView);
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

        upcomingEventList_button = findViewById(R.id.upcomingEventsList_button);
        signedUpEventList_button = findViewById(R.id.signedUpEvents_button);


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
                    signedUpUIDs = (ArrayList<String>) document.get("mysignup");

                    // constructs initial signedup events
                    myEventDataArray.clear();
                    for (Event event: upcomingEventDataArray){
                        if (signedUpUIDs != null && signedUpUIDs.contains(event.getEventId())){
                            myEventDataArray.add(event);
                        }
                    }
                    myEventsAdapter.notifyDataSetChanged();

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
                                        handleNewUserInput(db, deviceId, token);
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

        // listener for signedup events, repopulate signedup array when user doc updates
        db.collection("Users").document(deviceId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("FireStore", error.toString());
                    return;
                }
                Log.d("signupListener", "listener called");
                signedUpUIDs = (ArrayList<String>) value.get("mysignup");
                myEventDataArray.clear();
                for (Event event: upcomingEventDataArray){
                    if (signedUpUIDs != null && signedUpUIDs.contains(event.getEventId())){
                        myEventDataArray.add(event);
                    }
                }
                myEventsAdapter.notifyDataSetChanged();
            }
        });

        // Adds listener to event reference. Populates upcoming Event Array with Event data from DB
        eventsRef.orderBy("StartDateTime", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("FireStore", error.toString());
                    return;
                }

                // Downloading the posters happens in an asynchronous manner
                // To get around this we can get a count of the query, and until this count is reached
                // we loop to give the data array enough time to receive every event
                Integer count = value.size();
                Log.e("Event", "Query size: " + count);
                if (value != null) {
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
                        String qrCodeStr = doc.getString("QRCode");
                        String qrCodePromoStr = doc.getString("QRCodePromo");
                        Long maxAttendeesLong = doc.getLong("Max Attendees");
                        Integer maxAttendees = null;
                        if (maxAttendeesLong != null){
                            maxAttendees = maxAttendeesLong.intValue();
                        }
                        Log.d("Firestore: ", String.format("Event (%s) fetched", title));

                        Event newEvent = new Event(title);
                        newEvent.setEventId(eventId);
                        newEvent.setStartDateTime(startDate);
                        newEvent.setEndDateTime(endDate);
                        newEvent.setDescription(description);
                        newEvent.setOrganiserId(organizerId);
                        newEvent.setLocation(location);
                        newEvent.setPosterStr(posterStr);

                        if (maxAttendees != null){
                            newEvent.setMaxAttendees(maxAttendees);
                        }

                        if (qrCodeStr != null){
                            newEvent.setQRCode(new QRCodes(qrCodeStr));
                        }
                        if (qrCodePromoStr != null){
                            newEvent.setQRCodePromo(new QRCodes(qrCodePromoStr));
                        }

                        //add event to upcoming event data array
                        upcomingEventDataArray.add(newEvent);
                    }
                    upcomingEventsAdapter.notifyDataSetChanged();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        profile = findViewById(R.id.profile);
        signedUpEvents = findViewById(R.id.signedUpEvents);
        organizedEvents = findViewById(R.id.publishedEvents);
        notifications = findViewById(R.id.notifications);
        notifications_toolbar = findViewById(R.id.notifications_toolbar);
        organizeEvent = findViewById(R.id.organizeEvent);
        admin = findViewById(R.id.admin);
        contact = findViewById(R.id.contact);
        profilePhotoImageView = findViewById(R.id.CImageView);

        eventSearchView.setOnClickListener(v -> {
            eventSearchView.setIconifiedByDefault(true);
        });
        eventSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                populateSearchEvents(query);
                eventSearchView.clearFocus();
                eventSearchView.setQuery("", false);
                eventSearchView.setFocusable(false);
                eventSearchView.setIconified(true);
                eventSearchView.setPressed(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        eventSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                eventSearchView.setPressed(false);
                eventSearchView.clearFocus();
                eventSearchView.setFocusable(false);
                return false;
            }
        });


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
        signedUpEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                redirectActivity(MainActivity.this, SignedUpEventsActivity.class);
                Intent intent = new Intent(MainActivity.this, SignedUpEventsActivity.class);
                intent.putExtra("events", myEventDataArray);
                startActivity(intent);
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
                // redirectActivity(MainActivity.this, CreateEventActivity.class);
                // Sending the user ID to the create event page to be able to save the organizer with their event
                Intent myIntent = new Intent(MainActivity.this, CreateEventActivity.class);
                //Stating that we are entering the activity in the create event state
                myIntent.putExtra("State", true);
                myIntent.putExtra("organiser", deviceId);
                startActivity(myIntent);
            }
        });

        signedUpEventList_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SignedUpEventsActivity.class);
                myIntent.putParcelableArrayListExtra("events", myEventDataArray);
                startActivity(myIntent);
            }
        });

        upcomingEventList_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, UpcomingEventsActivity.class);
                myIntent.putParcelableArrayListExtra("events", upcomingEventDataArray);
                startActivity(myIntent);
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, AdminOptionsActivity.class);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this, ContactUsActivity.class);
            }
        });

        scannerButton = findViewById(R.id.qr_scanner);
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize the scanner
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setPrompt("Scan the Promotional QR Code");
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
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
        newUser.put("ProfilePictureDefault", "gs://hotevents-hotjava.appspot.com/ProfilePictures/profilePictureDefault.png");
        newUser.put("ProfilePictureCustom", "");
        newUser.put("userType", "Normal");
        newUser.put("UID", deviceId);
        newUser.put("Name", UserName);
        newUser.put("Contact", "");
        newUser.put("Email ID", "");
        newUser.put("Location", "");
        newUser.put("SignedUpEvent", SignedUpEvent);
        newUser.put("CreatedEvents", new ArrayList<String>());
        newUser.put("geo", true);
        newUser.put("fcmToken", token);

        // Add a new document with the device ID as the document ID
        db.collection("Users").document(deviceId).set(newUser)
                .addOnSuccessListener(aVoid -> {

                });
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        //        activity.finish();

    }

    protected void onResume() {
        super.onResume();
        fetchUserDataFromFirestore();
    }

    protected void onPause() {
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

    /**
     * On execution of searchView query, this method will generate an array of
     * events that contain the query string in the event title. It will then
     * start UpcomingEventsActivity, passing the searched events array rather than
     * the default upcomingEvents array
     * @param msg Query that is used to compare against event titles
     * NOTE: Will create activity regardless of whether the event array contains
     *       any Events
     */
    void populateSearchEvents(String msg){
        ArrayList<Event> searchEvents = new ArrayList<>();
        for (Event event : upcomingEventDataArray){
            if (event.getTitle().toLowerCase().contains(msg.toLowerCase())){
                searchEvents.add(event);
            }
        }
        Intent intent = new Intent(MainActivity.this, UpcomingEventsActivity.class);
        intent.putParcelableArrayListExtra("events", searchEvents);
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get the result from the scanner
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String scannedText = result.getContents();
                // Extract event ID from scanned text
                String eventId = extractEventId(scannedText);
                if (eventId != null) {
                    // Retrieve the Event object using the eventId
                    Event event = findEventById(eventId);
                    if (event != null) {
                        // Open the event object with the retrieved Event object
                        openEventObject(event);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private String extractEventId(String scannedText) {
        String eventId = null;
        String[] parts = scannedText.split(":");
        if (parts.length >= 3 && "hotevents".equals(parts[0]) && "promo".equals(parts[1])) {
            eventId = parts[2]; // Document ID after "promo:"
        }
        return eventId;
    }
    private Event findEventById(String eventId) {
        for (Event event : upcomingEventDataArray) {
            if (eventId.equals(event.getEventId())) {
                return event;
            }
        }
        return null; // Event not found
    }

    private void openEventObject(Event event) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("event", (Parcelable) event);
        startActivity(intent);
        Log.d("Event", "Opening event object with ID: " + event.getEventId());
    }


}