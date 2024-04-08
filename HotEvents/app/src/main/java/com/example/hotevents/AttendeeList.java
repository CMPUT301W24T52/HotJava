package com.example.hotevents;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;


/**
 * Attendee List page
 */
public class AttendeeList extends AppCompatActivity {
    ImageButton backButton;
    private FirebaseFirestore db;
    private String eventId;
    private static final String TAG = "AttendeeList";
    TextView signups_number;
    TextView checkins_number;

    private ArrayList<Attendee> attendeesArray = new ArrayList<>();
    AttendeeListAdapter attendeeListAdapter;

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    MapView map;
    private IMapController mapController;
    private GeoPoint startPoint;

    private List<Marker> attendeeMarkers = new ArrayList<>();
    Drawable attendeeMarkerDrawable;
    ListenerRegistration eventCheckinListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_list);

        // Get DB and eventId
        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getExtras().getString("eventId");

        setViews();
        setupMap();

        db.collection("Events")
                .document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                Geocoder geocoder = new Geocoder(this);
                                List<Address> addressList = geocoder.getFromLocationName(document.getString("Location"), 1);
                                startPoint = new GeoPoint(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                            } catch (Exception e) {
                                startPoint = new GeoPoint(53.521331248, -113.521331248);
                            }

                            mapController.setCenter(startPoint);
                        } else {
                            Log.d(TAG, "event does not exist");
                        }
                    } else {
                        Log.e(TAG, "failed to grab event data", task.getException());
                    }
                });

        // Listen for changes in event checkin data
        eventCheckinListener = db.collection("Events")
                .document(eventId)
                .collection("checkins")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AttendeeList", "Listen failed", error);
                        return;
                    }

                    // Clear attendee list
                    attendeesArray.clear();

                    // Clear existing markers
                    map.getOverlays().removeAll(attendeeMarkers);
                    attendeeMarkers.clear();

                    // Init signup and checkin counts
                    int signups = 0;
                    int checkins = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        // Add to signup count for every attendee
                        signups++;

                        // Check if attendee has checked in at least once
                        if (doc.getLong("count") > 0) checkins++;

                        attendeesArray.add(new Attendee(doc.getString("UID"), doc.getLong("count").intValue()));
                        
                        // Add markers if available
                        if (doc.get("latitude") != null && doc.get("latitude") != null) {
                            double latitude = doc.getDouble("latitude");
                            double longitude = doc.getDouble("longitude");
                            Log.d(TAG, String.valueOf(latitude));
                            Log.d(TAG, String.valueOf(longitude));
                            GeoPoint location = new GeoPoint(latitude, longitude);
                            Marker marker = new Marker(map);
                            marker.setPosition(location);
                            marker.setIcon(attendeeMarkerDrawable);
                            map.getOverlays().add(marker);
                            attendeeMarkers.add(marker);
                            Log.d(TAG, "marker added");
                        }
                    }

                    // Refresh map
                    map.invalidate();
                    attendeeListAdapter.notifyDataSetChanged();

                    // Set the text to the counts
                    signups_number.setText(String.valueOf(signups));
                    checkins_number.setText(String.valueOf(checkins));
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventCheckinListener.remove();
    }

    private void setViews() {
        signups_number = findViewById(R.id.signups_number);
        checkins_number = findViewById(R.id.checkins_number);
        RecyclerView recyclerView = findViewById(R.id.attendee_recycler_view);
        attendeeListAdapter = new AttendeeListAdapter(this, attendeesArray);
        recyclerView.setAdapter(attendeeListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        map = findViewById(R.id.map);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            eventCheckinListener.remove();
            finish();
        });
    }

    private void setupMap() {
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        mapController = map.getController();
        mapController.setZoom(18.0);

        attendeeMarkerDrawable = AppCompatResources.getDrawable(this, R.drawable.attendeemarker);
    }
}