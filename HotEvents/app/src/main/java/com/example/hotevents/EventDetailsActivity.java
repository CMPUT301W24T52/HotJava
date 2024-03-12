package com.example.hotevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetailsActivity extends AppCompatActivity {

    Event myEvent;
    ImageButton backButton;
    ImageView eventImage;
    TextView eventTitle;
    TextView startDate;
    TextView endDate;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    EventPagerAdapter eventPagerAdapter;
    TextView organiserName;
    private static final String TAG = "EventDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        myEvent = (Event) getIntent().getSerializableExtra("event");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        setViews();

        if (myEvent.getTitle() != null) {
            eventTitle.setText(myEvent.getTitle());
        }


        if (myEvent.getStartDateTime() != null) {
            startDate.setText(myEvent.getStartDateTime().toString());
        }

        if (myEvent.getEndDateTime() != null) {
            endDate.setText(myEvent.getEndDateTime().toString());
        }

//        Location missing
        if (myEvent.getOrganiserId() != null && !myEvent.getOrganiserId().trim().isEmpty()) {
            db.collection("Users").document(myEvent.getOrganiserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    organiserName.setText(document.getString("Name"));
                }
            });
        }
//        Notifications not implemented yet



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

        Button signUpButton = findViewById(R.id.check_in_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpButtonClick();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!NotificationManagerCompat.from(EventDetailsActivity.this).areNotificationsEnabled()) {
                        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                    }
                }
            }
        });

    }
    /**
     * Handles the sign-up button click event.
     * Retrieves the device ID of the user and the FCM token from Firestore Users collection.
     * Stores the device ID and FCM token in Firestore under the signups collection for the specified event.
     */
    private void onSignUpButtonClick() {
        // Retrieve the device ID of the user
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Retrieve the FCM token from Firestore Users collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    }
    private void addToMySignupArray(String deviceId, String eventId) {
        // Get the reference to the document in the Users collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(deviceId)
                .update("mysignup", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event added to mysignup array");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating mysignup array", e);
                });
    }



    private void setViews() {
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        eventTitle = findViewById(R.id.event_title);
        startDate = findViewById(R.id.event_start_date);
        endDate = findViewById(R.id.event_end_date);
        organiserName = findViewById(R.id.organiser_name);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.view_pager);
        eventPagerAdapter = new EventPagerAdapter(this,myEvent.getEventId(), myEvent.getTitle());
        viewPager2.setAdapter(eventPagerAdapter);
    }
    private class EventPagerAdapter extends FragmentStateAdapter {
        private String eventId;
        private String eventTitle;
        public EventPagerAdapter(@NonNull FragmentActivity fragmentActivity,String eventId, String eventTitle) {
            super(fragmentActivity);
            this.eventId = eventId;
            this.eventTitle = eventTitle;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                return EventDetailsAnnouncementFragment.newInstance(eventId,eventTitle);
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