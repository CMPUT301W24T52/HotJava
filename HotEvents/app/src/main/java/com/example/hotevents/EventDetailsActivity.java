package com.example.hotevents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventDetailsActivity extends AppCompatActivity {

    private static final int SCAN_QR_CODE_REQUEST_CODE = 1;
    Event myEvent;
    ImageButton backButton;
    Button editButton;
    ImageView eventImage;
    TextView eventTitle;
    TextView startDate;
    TextView endDate;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    EventPagerAdapter eventPagerAdapter;
    TextView organiserName;
    TextView eventLocation;
    String eventId;
    String myeventTitle;
    String orgfcmToken;
    ImageButton optionsButton;
    ImageButton shareButton;
    ImageButton deleteButton;
    private FirebaseFirestore db;
    private static final String TAG = "EventDetailsActivity";

    String deviceId;
    Button signUpButton;
    String notiType = "Milestone";
    Button checkInGenerateButton;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Grab the passed event, db, and deviceId
        db = FirebaseFirestore.getInstance();
        myEvent = getIntent().getParcelableExtra("event");
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set the views and event details
        setViews();
        setEventDetails();

        // Tab layout listeners
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Switch tabs
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        // Find the remove poster button
        ImageButton removePosterButton = findViewById(R.id.remove_poster_button);
        // Set OnClickListener for the button
        signUpButton = findViewById(R.id.check_in_button);
        if (Objects.equals(deviceId, myEvent.getOrganiserId())) {
            // Hide signup button
            signUpButton.setVisibility(View.GONE);

            // Set optionButton to open popup menu
            optionsButton.setVisibility(View.VISIBLE);
            optionsButton.setOnClickListener(this::showPopupMenu);

            deleteButton = findViewById(R.id.delete_button);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> deleteEvent());
            checkInGenerateButton.setVisibility(View.VISIBLE);
        } else {
            handleButtonBehaviour();
            signUpButton.setOnClickListener(v -> {
                // Change behaviour based on button type/text (sign-up or check-in)
                if (signUpButton.getText() == "Sign Up") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!NotificationManagerCompat.from(EventDetailsActivity.this).areNotificationsEnabled()) {
                            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                        }
                    }
                    onSignUpButtonClick();
                    Log.d(TAG, "Attempting to send milestone notifications");
                    fetchFCMTokenForOrganizer(myEvent.getOrganiserId());
                } else {
                    onCheckInButtonClick();
                }

            });
        }
    }


    /**
     * Handles the sign-up button click event.
     * Retrieves the device ID of the user and the FCM token from Firestore Users collection.
     * Stores the device ID and FCM token in Firestore under the signups collection for the specified event.
     */
    private void onSignUpButtonClick() {
        // Retrieve the FCM token from Firestore Users collection
        db.collection("Users").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve FCM token from the document
                            String fcmToken = document.getString("fcmToken");

                            // Store the device ID and FCM token in Firestore under the signups collection
                            String eventId = myEvent.getEventId();
                            String eventName = myEvent.getTitle();
                            Map<String, Object> signupData = new HashMap<>();
                            signupData.put("UID", deviceId);
                            signupData.put("fcmToken", fcmToken);


                            // Add the device ID to the signups collection under the specific event's document
                            // Reference: https://firebase.google.com/docs/firestore/query-data/aggregation-queries#java
                            CollectionReference colRef = db.collection("Events").document(eventId).collection("signups");
                            AggregateQuery countQuery = colRef.count();

                            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // Count fetched successfully
                                        AggregateQuerySnapshot snapshot = task.getResult();
                                        // Checking whether the count is smaller than the MaxAttendees field, but only if it's not empty
                                        Log.d("Aggregate Query Count", "Count: " + snapshot.getCount());

                                        Integer maxAttendees;
                                        maxAttendees = myEvent.getMaxAttendees();
                                        if (myEvent.getMaxAttendees() != null) {
                                            if (snapshot.getCount() >= maxAttendees) {
                                                Toast.makeText(getBaseContext(), "Unable to Sign-up: Max attendee limit has been reached!", Toast.LENGTH_LONG).show();
                                            } else {
                                                colRef.document(deviceId).set(signupData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Successfully stored the device ID in Firestore
                                                            Log.d(TAG, "Device ID stored in Firestore for event: " + eventName);
                                                            // You can add further logic here if needed
                                                            addToMySignupArray(deviceId, eventId);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Failed to store the device ID
                                                            Log.e(TAG, "Error storing device ID in Firestore", e);
                                                            // You can handle the failure here
                                                        });
                                            }
                                        } else {
                                            colRef.document(deviceId).set(signupData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Successfully stored the device ID in Firestore
                                                        Log.d(TAG, "Device ID stored in Firestore for event: " + eventName);
                                                        // You can add further logic here if needed
                                                        addToMySignupArray(deviceId, eventId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Failed to store the device ID
                                                        Log.e(TAG, "Error storing device ID in Firestore", e);
                                                        // You can handle the failure here
                                                    });
                                        }

                                    } else {
                                        Log.d("Aggregate Query Count", "Count failed: ", task.getException());
                                    }
                                }
                            });

                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });

        handleButtonBehaviour();
    }

    /**
     * Handles the check-in button click event.
     * Stores the device ID and check-in count in Firestore under the checkins collection for the specified event.
     */
    private void onCheckInButtonClick() {
        launchScanner();
    }

    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
        }
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Double latitude = location.getLatitude();
                        Double longitude = location.getLongitude();
                        updateCheckinData(latitude, longitude);
                    } else {
                        updateCheckinData(null, null);
                    }
                });
    }

    private void updateCheckinData(Double latitude, Double longitude) {
        CollectionReference colRef = db.collection("Events").document(eventId).collection("checkins");
        DocumentReference docRef = colRef.document(deviceId);
        Map<String, Object> checkinData = new HashMap<>();
        checkinData.put("UID", deviceId);
        checkinData.put("latitude", latitude);
        checkinData.put("longitude", longitude);


        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    checkinData.put("count", document.getLong("count").intValue() + 1);
                } else {
                    checkinData.put("count", 1);
                }

                docRef.set(checkinData).addOnSuccessListener(unused -> {
                    signUpButton.setEnabled(true);
                    Log.d(TAG, "Checked in successfully");
                    Toast.makeText(getBaseContext(), "Check-in was successful!", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    signUpButton.setEnabled(true);
                    Log.d(TAG, "Error checking in", e);
                    Toast.makeText(getBaseContext(), "Error checking in", Toast.LENGTH_LONG).show();
                });
            }
        });
        Log.d(TAG, "CheckIn Button Working");
    }

    private void launchScanner() {
        // Code to open the QR Code scanner
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan the Check-In QR Code");
        //        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        //        intentIntegrator.setCameraId(0);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    // This is what returns once a QR code has been scanned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll handle validating the QR Code
                // Toast.makeText(getBaseContext(), intentResult.getContents() + ":" + intentResult.getFormatName(), Toast.LENGTH_SHORT).show();
                if (myEvent.getQrCode().validateQRCode(intentResult.getContents())) {
                    // QR Code successfully scanned
                    signUpButton.setEnabled(false);
                    getLocation();
                } else {
                    Toast.makeText(getBaseContext(), "Incorrect QR Code", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Handles the sign-up/check-in button behaviour
     * Switches between sign-up and check-in based on if user has signed up
     */
    private void handleButtonBehaviour() {
        db.collection("Events")
                .document(eventId)
                .collection("signups")
                .document(deviceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "");
                            signUpButton.setText("Check In");
                        } else {
                            signUpButton.setText("Sign Up");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
    }

    /**
     * Adds the event ID to the mysignup array in the Firestore Users collection.
     *
     * @param deviceId The device ID of the user.
     * @param eventId  The ID of the event to be added to the mysignup array.
     */
    private void addToMySignupArray(String deviceId, String eventId) {
        // Get the reference to the document in the Users collection
        db.collection("Users").document(deviceId)
                .update("mysignup", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event added to mysignup array");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating mysignup array", e);
                });
    }


    /**
     * Fetches the FCM token for the event organizer from the Firestore Users collection.
     *
     * @param organizerId The ID of the event organizer.
     */
    public void fetchFCMTokenForOrganizer(String organizerId) {
        db.collection("Users").document(organizerId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            orgfcmToken = document.getString("fcmToken");
                            if (orgfcmToken != null) {
                                // FCM token found, you can use it here
                                sendMilestoneIfConditionMet();
                                Log.d(TAG, "FCM Token for organizer " + organizerId + ": " + orgfcmToken);
                                // Now you can perform any actions with the FCM token
                            } else {
                                // FCM token not found for the organizer
                                Log.d(TAG, "FCM token not found for organizer ID: " + organizerId);
                            }
                        } else {
                            // Document not found
                            Log.d(TAG, "No document found for organizer ID: " + organizerId);
                        }
                    } else {
                        // Error fetching document
                        Log.e(TAG, "Error fetching document: ", task.getException());
                    }
                });
    }

    /**
     * Checks if the condition for sending milestone notifications is met.
     * If the condition is met (e.g., signups count equals 1 or 3), sends the milestone notification to the organizer.
     */
    void sendMilestoneIfConditionMet() {
        db.collection("Events").document(eventId).collection("signups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    long signupsCount = queryDocumentSnapshots.size();
                    Log.d(TAG, "signupsCount = " + signupsCount);

                    // Check if conditions for sending notifications are met
                    if (signupsCount == 1) {
                        // Create a notification message based on the signups count
                        String notificationMessage = "Milestone: Signups count for event '" + myeventTitle + "' is " + signupsCount + ".";
                        // Send the notification to the organizer
                        sendPushNotification(orgfcmToken, notificationMessage, eventId);
                        //                        Toast.makeText(, "Milestone sent", Toast.LENGTH_SHORT).show();
                    }
                    if (signupsCount == 3) {
                        // Create a notification message based on the signups count
                        String notificationMessage = "Milestone: Signups count for event '" + myeventTitle + "' is " + signupsCount + ".";
                        // Send the notification to the organizer
                        sendPushNotification(orgfcmToken, notificationMessage, eventId);
                        //                        Toast.makeText(, "Milestone sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve signups count", e);
                    //                    Toast.makeText(new AnnouncementsAndMilestones(), "Failed to retrieve signups count", Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * Sends a push notification to the specified FCM token with the given message.
     *
     * @param fcmToken    The FCM token of the device.
     * @param messageText The message to be sent.
     */
    private void sendPushNotification(String fcmToken, String messageText, String eventId) {

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonNotif = new JSONObject();
            JSONObject wholeObj = new JSONObject();
            try {
                jsonNotif.put("title", "ANNOUNCEMENT - " + myeventTitle);
                jsonNotif.put("body", messageText);
                wholeObj.put("to", fcmToken);
                wholeObj.put("notification", jsonNotif);
            } catch (JSONException e) {
                Log.d(TAG, e.toString());
            }
            RequestBody rBody = RequestBody.create(mediaType, wholeObj.toString());
            Request request = new Request.Builder().url("https://fcm.googleapis.com/fcm/send")
                    .post(rBody)
                    .addHeader("Authorization", "key=AAAAlidLyZE:APA91bF1suXeK0OgT_eZIP9uEPOCarD4zMfUyLWvo5-ljaXKQp4wuZhU2Ik2C63QLZKsvKGnOuzNIh_56WCIl1R8-rENZFlPPrwAB8Corgtnba5w8pMpknuhzp7_q1dTyshB37uTu4EN")
                    .addHeader("Content-Type", "application/json").build();
            Log.d(TAG, "think working");
            //            Toast.makeText(getActivity(), "Announcement sent successfully", Toast.LENGTH_SHORT).show();

            try {
                // Execute the request synchronously
                Response response = client.newCall(request).execute();
                // Check if the notification was sent successfully
                if (response.isSuccessful()) {
                    // Call the helper method to store the notification data
                    NotificationStorer.storeNotification(fcmToken, eventId, messageText, notiType);
                    Log.d(TAG, "Notification sent successfully");
                } else {
                    // Handle the case where notification sending failed
                    Log.e(TAG, "Failed to send notification: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            }
        }).start();
    }


    /**
     * Sets all the views and adapters
     */
    private void setViews() {
        backButton = findViewById(R.id.back_button);
        // Making sure that if the event is updated we don't go back to the create event page
        // We will return back to the home activity in this case
        backButton.setOnClickListener(v -> {
            Boolean updateEvent = getIntent().getBooleanExtra("Update", false);
            if (updateEvent) {
                Intent myIntent = new Intent(EventDetailsActivity.this, MainActivity.class);
                startActivity(myIntent);
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        eventTitle = findViewById(R.id.event_title);
        startDate = findViewById(R.id.event_start_date);
        eventImage = findViewById(R.id.eventImage);
        endDate = findViewById(R.id.event_end_date);
        organiserName = findViewById(R.id.organiser_name);
        eventLocation = findViewById(R.id.event_location);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.view_pager);
        eventPagerAdapter = new EventPagerAdapter(this, myEvent.getEventId());
        viewPager2.setAdapter(eventPagerAdapter);
        optionsButton = findViewById(R.id.options_button);
        signUpButton = findViewById(R.id.check_in_button);
        shareButton = findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> onShareButtonClick(false));
        checkInGenerateButton = findViewById(R.id.checkin_generate_button);
        checkInGenerateButton.setOnClickListener(v -> onShareButtonClick(true));
    }

    /**
     * Sets all the event details based on myEvent
     */
    private void setEventDetails() {
        // Set title
        if (myEvent.getTitle() != null) {
            myeventTitle = myEvent.getTitle();
            eventTitle.setText(myeventTitle);
        }

        // Set eventId
        if (myEvent.getEventId() != null) {
            eventId = myEvent.getEventId();
        }

        // Set startDateTime
        if (myEvent.getStartDateTime() != null) {
            startDate.setText(myEvent.getStartDateTime().toString());
        }

        // Set endDateTime
        if (myEvent.getEndDateTime() != null) {
            endDate.setText(myEvent.getEndDateTime().toString());
        }

        // Set location
        if (myEvent.getLocation() != null) {
            eventLocation.setText(myEvent.getLocation());
        }

        if (myEvent.getPosterStr() != null) {
            myEvent.assignPoster(eventImage);
        }

        // Set organiser name if organiserId exists
        if (myEvent.getOrganiserId() != null && !myEvent.getOrganiserId().trim().isEmpty()) {
            db.collection("Users").document(myEvent.getOrganiserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    organiserName.setText(document.getString("Name"));
                }
            });
        }
    }

    /**
     * Opens the popup menu with extra options
     *
     * @param v Current view
     */
    private void showPopupMenu(View v) {
        // Create the PopupMenu and inflate it with the appropriate menu layout
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.event_details_menu, popupMenu.getMenu());

        // Set onclick listener for each specific menu option
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemID = item.getItemId();
            if (itemID == R.id.event_details_option_announce) {
                MakeAnnouncementFragment.newInstance(eventId, myeventTitle).show(getSupportFragmentManager(), "Make Announcement");
            }
            if (itemID == R.id.event_details_option_attendees) {
                Intent intent = new Intent(this, AttendeeList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("eventId", eventId);
                this.startActivity(intent);
            }
            if (itemID == R.id.remove_poster_button) {
                // Call method to remove the poster
                db.collection("Events").document(eventId).update("Poster", "")
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Remove Poster", "Remove Poster updated successfully");
                            // Handle success, if needed
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Remove Poster", "Error updating Remove Poster ", e);
                            // Handle failure, if needed
                        });
            }
            return false;
        });

        Menu menu = popupMenu.getMenu();
        boolean showItems = true;
        // Hide options if not organiser (set boolean to true to see all options for testing)
        if (Objects.equals(deviceId, myEvent.getOrganiserId())) {
            menu.findItem(R.id.event_details_option_announce).setVisible(showItems);
            menu.findItem(R.id.event_details_option_edit).setVisible(showItems);
            menu.findItem(R.id.event_details_option_attendees).setVisible(showItems);
            menu.findItem(R.id.remove_poster_button).setVisible(showItems);

            // Setting the editButton and the click event for the newly displayed button
            // Reference: https://stackoverflow.com/questions/19652848/how-to-cast-menuitem-to-button-or-imagebutton
            MenuItem editButton = menu.findItem(R.id.event_details_option_edit);
            editButton.setOnMenuItemClickListener(x -> {
                Intent myIntent = new Intent(EventDetailsActivity.this, CreateEventActivity.class);
                // Stating that we are entering the activity in the create event state
                myIntent.putExtra("State", false);
                myIntent.putExtra("organiser", deviceId);
                myIntent.putExtra("event", (Parcelable) myEvent);
                Log.d("Event Details", "Starting update event");
                startActivity(myIntent);
                return true;
            });
        }

        popupMenu.show();
    }

    private class EventPagerAdapter extends FragmentStateAdapter {
        private final String eventId;

        public EventPagerAdapter(@NonNull FragmentActivity fragmentActivity, String eventId) {
            super(fragmentActivity);
            this.eventId = eventId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                return EventDetailsAnnouncementFragment.newInstance(eventId);
            }
            return EventDetailsAboutFragment.newInstance(myEvent.getDescription());
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (Objects.equals(permissions[0], Manifest.permission.POST_NOTIFICATIONS)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications not allowed", Toast.LENGTH_SHORT).show();
                }
            } else if (Objects.equals(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    updateCheckinData(null, null);
                }
            }

        }
    }

    public void onShareButtonClick(Boolean checkin) {
        // Get the QR code bitmap
        Bitmap qrBitmap = null;
        if (checkin) {
            qrBitmap = myEvent.getQrCode().getBitmap();
        } else {
            qrBitmap = myEvent.getQrCodePromo().getBitmap();
        }


        // Share the QR code bitmap
        if (qrBitmap != null) {
            shareBitmap(qrBitmap);
        } else {
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to share the QR code bitmap
    private void shareBitmap(Bitmap qrBitmap) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");

        // Save the QR code bitmap to external storage
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), qrBitmap, "QR Code", null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        // Set the bitmap URI as the intent extra
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);

        // Start the share activity
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
    }
    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //
    //        // Check if the result is from scanning the QR code
    //        if (requestCode == SCAN_QR_CODE_REQUEST_CODE && resultCode == RESULT_OK) {
    //            if (data != null && data.getData() != null) {
    //                String scannedUrl = data.getData().toString();
    //                // Parse the event ID from the scanned URL
    //                String eventId = parseEventIdFromUrl(scannedUrl);
    //                if (eventId != null) {
    //                    // Open the event using the parsed event ID
    //                    openEvent(eventId);
    //                } else {
    //                    // Show an error message if the event ID cannot be parsed
    //                    Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
    //                }
    //            }
    //        }
    //    }

    //    private String parseEventIdFromUrl(String url) {
    //        // Parse the event ID from the URL
    //        // Example: hotjava:checkin:eventId
    //        String[] parts = url.split(":");
    //        if (parts.length == 3 && parts[0].equals("hotjava") && parts[1].equals("checkin")) {
    //            return parts[2]; // Return the event ID
    //        }
    //        return null; // Return null if the URL format is invalid
    //    }
    //
    //    private void openEvent(String eventId) {
    //        Intent intent = new Intent(this, EventDetailsActivity.class);
    //        intent.putExtra("event", (Parcelable) myEvent);
    ////        Log.d("UpcomingEventAdapter", String.format("Event %s clicked", myEvent.getTitle()));
    //        startActivity(intent);
    //    }
    private void deleteEvent() {
        if (eventId != null) {
            // Delete the event from Firestore
            db.collection("Events").document(eventId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Event deleted successfully
                        Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        // Finish the activity or navigate back to the previous screen
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Error deleting the event
                        Toast.makeText(EventDetailsActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting event", e);
                    });
        } else {
            // Event ID is null, show an error message
            Toast.makeText(EventDetailsActivity.this, "Event ID is null", Toast.LENGTH_SHORT).show();
        }
    }
}