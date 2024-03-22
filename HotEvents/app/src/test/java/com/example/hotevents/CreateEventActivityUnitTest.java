package com.example.hotevents;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;

import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateEventActivityUnitTest {
    FirebaseFirestore db;
    FirebaseStorage sref;
    CollectionReference eventsRef;

    private QRCodes mockQRCode(){
        return new QRCodes("asfowegiwdvslegjwks", "checkin", 5);
    }

    private Event mockEvent(Date startDate, Date endDate) {
        Event event = new Event (startDate, endDate, 50, "sdgwioegsjdgjsk",
                null, null,  mockQRCode(), "this is the description", "Event title", null, "Address");
        return event;
    }

    //Testing the QR Code model
    @Test
    public void testGenerateQRCode(){
        String encodedStr = "hotjava:checkin:asfowegiwdvslegjwks";
        QRCodes code = mockQRCode();
        assertEquals(encodedStr, code.getEncodedStr());
        assertEquals("checkin", code.getType());
        assertEquals("asfowegiwdvslegjwks", code.getEventId());
    }

    //Testing that the model can be created successfully
    @Test
    public void testCreateEvent(){
        Date startDate = new Date();
        Date endDate = new Date();
        Event event = mockEvent(startDate, endDate);
        assertEquals(startDate, event.getStartDateTime());
        assertEquals(endDate, event.getEndDateTime());
        assertEquals("50", Integer.toString(event.getMaxAttendees()));
        assertEquals("sdgwioegsjdgjsk", event.getOrganiserId());
        assertEquals(null, event.getPoster());
        assertEquals(mockQRCode().getEncodedStr(), event.getQrCode().getEncodedStr());
        assertEquals("this is the description", event.getDescription());
        assertEquals(null, event.getEventId());
    }
}
