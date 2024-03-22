package com.example.hotevents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;

public class AdminBrowseEventsActivity extends Activity {

    private FirebaseFirestore db;
    private CollectionReference adminEventsRef;
    ArrayList<Event> adminEventDataArray;
    //ListView eventList;
    RecyclerView adminEventView;
    LinearLayoutManager adminEventHorizantleManager;
    AdminEventsAdapter adminEventsAdapter;
    private ImageView backButton;
    private TextView backButton2;
    private static final String TAG = "AdminBrowseActivity";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_events);

        adminEventDataArray = new ArrayList<Event>();
        adminEventView = (RecyclerView) findViewById(R.id.admin_event_list);
        adminEventHorizantleManager = new LinearLayoutManager(this);
        adminEventHorizantleManager.setOrientation(LinearLayoutManager.VERTICAL);
        adminEventView.setHasFixedSize(false);
        adminEventView.setLayoutManager(adminEventHorizantleManager);

        db = FirebaseFirestore.getInstance();
        adminEventsRef = db.collection("Events");

        adminEventsAdapter = new AdminEventsAdapter(adminEventDataArray, this);

        adminEventView.setAdapter(adminEventsAdapter);

        // Adds listener to event reference. Populates Event Array with Event data from DB
        adminEventsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
//                    Log.e("FireStore", error.toString());
                    return;
                }
                if (value != null){
                    adminEventDataArray.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String eventId = doc.getId();
                        String title = doc.getString("Title");
                        Date startDate = doc.getDate("StartDateTime");
                        Date endDate = doc.getDate("EndDateTime");
                        String description = doc.getString("Description");
                        String organizerId = doc.getString("Organizer Id");
                        String posterStr = doc.getString("Poster");
                        String location = doc.getString("Location");
//                        Log.d("Firestore: ", String.format("Event (%s) fetched", title));

                        Event newEvent = new Event(title);
                        newEvent.setEventId(eventId);
                        newEvent.setStartDateTime(startDate);
                        newEvent.setEndDateTime(endDate);
                        newEvent.setDescription(description);
                        newEvent.setOrganiserId(organizerId);
                        newEvent.setPosterStr(posterStr);
                        newEvent.setLocation(location);
                        adminEventDataArray.add(newEvent);

                    }
                    adminEventsAdapter.notifyDataSetChanged();
                }
            }
        });
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        backButton2 = findViewById(R.id.backButton2);
        backButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
