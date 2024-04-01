package com.example.hotevents;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;

public class SignedUpEventsActivity extends AppCompatActivity {
    private static final String TAG = "SignedUpActivity";
    //Firebase
    FirebaseFirestore db;
    FirebaseStorage sRef;
    CollectionReference eventsRef;
    DocumentReference userRef;

    //Events
    ArrayList<Event> eventArray;
//    UpcomingEventActivityAdapter eventAdapter;


    //User
    LoggedInUser user;

    //Views and Components
    RecyclerView signedUpEventView;
    LinearLayoutManager signedUpEventLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_up_events);

        eventArray = new ArrayList<Event>();

        db = FirebaseFirestore.getInstance();
        Intent myIntent =  getIntent();
        user = (LoggedInUser) myIntent.getSerializableExtra("user");

        eventsRef = db.collection("Events");

        userRef = db.collection("Users").document(user.getuID());


        //check if user has signed up events, populate view
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    ArrayList<String> eventUIDList = (ArrayList<String>) doc.get("SignedUpEvent");
                    if (eventUIDList != null && eventUIDList.isEmpty()){
                        Log.d(TAG, "No Events to display");
                        //make toast notification, return to main activity

                        return;
                    } else {
                        Log.d(TAG, "User contains " + eventUIDList.size() + " Signed up events!");
                        eventsRef.whereIn(FieldPath.documentId(), eventUIDList)
                                .get()
                                .addOnCompleteListener(eventTask -> {
                                    if (eventTask.isSuccessful()){
                                        for (QueryDocumentSnapshot eventDoc: eventTask.getResult()) {
                                            String eventId = eventDoc.getId();
                                            String title = eventDoc.getString("Title");
                                            Date startDate = eventDoc.getDate("StartDateTime");
                                            Date endDate = eventDoc.getDate("EndDateTime");
                                            String description = eventDoc.getString("Description");
                                            String organizerId = eventDoc.getString("Organizer Id");
                                            String posterStr = eventDoc.getString("Poster");
                                            String location = eventDoc.getString("Location");

                                            Event newEvent = new Event(title);
                                            newEvent.setEventId(eventId);
                                            newEvent.setStartDateTime(startDate);
                                            newEvent.setEndDateTime(endDate);
                                            newEvent.setDescription(description);
                                            newEvent.setOrganiserId(organizerId);
                                            newEvent.setPosterStr(posterStr);
                                            newEvent.setLocation(location);
                                            eventArray.add(newEvent);

                                        }
                                        //add to adapter
                                        //someAdapter.notify...()
                                    }
                                });
                    }
                }
            }
        });
//        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                if (error != null){
//                    Log.e(TAG, error.toString());
//                    return;
//                }
//
//            }
//        });
    }

    public void displayEvents(){

    }
    public void setEventArray(ArrayList<String> eventUIDArray){
        eventsRef.whereIn(FieldPath.documentId(), eventUIDArray)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot eventDoc: task.getResult()){
                        String eventId = eventDoc.getId();
                        String title = eventDoc.getString("Title");
                        Date startDate = eventDoc.getDate("StartDateTime");
                        Date endDate = eventDoc.getDate("EndDateTime");
                        String description = eventDoc.getString("Description");
                        String organizerId = eventDoc.getString("Organizer Id");
                        String posterStr = eventDoc.getString("Poster");
                        String location = eventDoc.getString("Location");

                        Event newEvent = new Event(title);
                        newEvent.setEventId(eventId);
                        newEvent.setStartDateTime(startDate);
                        newEvent.setEndDateTime(endDate);
                        newEvent.setDescription(description);
                        newEvent.setOrganiserId(organizerId);
                        newEvent.setPosterStr(posterStr);
                        newEvent.setLocation(location);

                        //add to adapter
                        //someAdapter.notify...()
                };
        });
    }
    public void getEventSet(){

    }

}