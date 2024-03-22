package com.example.hotevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

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
    String organizerId;
    String orgfcmToken;
    ImageButton optionsButton;
    private FirebaseFirestore db;
    private static final String TAG = "EventDetailsActivity";

    String deviceId;
    Button signUpButton;
    String notiType = "Milestone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        myEvent = (Event) getIntent().getParcelableExtra("event");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        setViews();
        setEventDetails();

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

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        optionsButton.setOnClickListener(this::showPopupMenu);


        signUpButton = findViewById(R.id.check_in_button);
        // Hide
        if (deviceId == organizerId) {
            signUpButton.setVisibility(View.GONE);
        } else {
            handleButtonBehaviour();
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (signUpButton.getText() == "Sign Up") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (!NotificationManagerCompat.from(EventDetailsActivity.this).areNotificationsEnabled()) {
                                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                            }
                        }
                        onSignUpButtonClick();
                        Log.d(TAG, "Attempting to send milestone notifications");
                        fetchFCMTokenForOrganizer(organizerId);
                    } else {
                        onCheckInButtonClick();
                    }

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
        // Retrieve the device ID of the user

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
                            db.collection("Events").document(eventId).collection("signups")
                                    .document(deviceId)
                                    .set(signupData)
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
        DocumentReference docRef = db.collection("Events").document(eventId).collection("checkins")
                .document(deviceId);
        Map<String, Object> checkinData = new HashMap<>();
        checkinData.put("UID", deviceId);

        docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            checkinData.put("count", document.getLong("count").intValue() + 1);
                        } else {
                            checkinData.put("count", 1);
                        }

                        docRef.set(checkinData).addOnSuccessListener(unused -> {
                            Log.d(TAG, "Checked in successfully");
                        }).addOnFailureListener(e -> {
                            Log.d(TAG, "Error checking in", e);
                        });
                    }
                });

        Log.d(TAG, "CheckIn Button Working");
    }

    /**
     * Handles the sign-up/check-in button behaviour
     * Switches between sign-up and check-in based on if user has signed up
     * */
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
                    Log.d(TAG, "signupsCount = "+signupsCount);

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



    private void setViews() {
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        eventTitle = findViewById(R.id.event_title);
        startDate = findViewById(R.id.event_start_date);
        eventImage = findViewById(R.id.eventImage);
        endDate = findViewById(R.id.event_end_date);
        organiserName = findViewById(R.id.organiser_name);
        eventLocation = findViewById(R.id.event_location);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.view_pager);
        eventPagerAdapter = new EventPagerAdapter(this,myEvent.getEventId(), myEvent.getTitle());
        viewPager2.setAdapter(eventPagerAdapter);
        optionsButton = findViewById(R.id.options_button);
    }

    private void setEventDetails() {
        if (myEvent.getTitle() != null) {
            myeventTitle = myEvent.getTitle();
            eventTitle.setText(myeventTitle);
        }

        if (myEvent.getEventId() != null) {
            eventId = myEvent.getEventId();
        }

        if (myEvent.getOrganiserId() != null) {
            organizerId = myEvent.getOrganiserId();
        }


        if (myEvent.getStartDateTime() != null) {
            startDate.setText(myEvent.getStartDateTime().toString());
        }

        if (myEvent.getEndDateTime() != null) {
            endDate.setText(myEvent.getEndDateTime().toString());
        }

        if (myEvent.getLocation() != null) {
            eventLocation.setText(myEvent.getLocation());
        }

        if (myEvent.getOrganiserId() != null && !myEvent.getOrganiserId().trim().isEmpty()) {
            db.collection("Users").document(myEvent.getOrganiserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    organiserName.setText(document.getString("Name"));
                }
            });
        }
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.event_details_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.event_details_option_announce) {
                MakeAnnouncementFragment.newInstance(eventId, myeventTitle).show(getSupportFragmentManager(), "Make Announcement");
            }
            return false;
        });

        Menu menu = popupMenu.getMenu();
        boolean showItems = true;
        // Hide options if not organiser (set boolean to true to see all options for testing)
        if (!Objects.equals(deviceId, myEvent.getOrganiserId())) {
            menu.findItem(R.id.event_details_option_announce).setVisible(showItems);
            menu.findItem(R.id.event_details_option_edit).setVisible(showItems);
            menu.findItem(R.id.event_details_option_attendees).setVisible(showItems);
        }

        popupMenu.show();
    }
    private class EventPagerAdapter extends FragmentStateAdapter {
        private String eventId;
        public EventPagerAdapter(@NonNull FragmentActivity fragmentActivity,String eventId, String eventTitle) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications not allowed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}