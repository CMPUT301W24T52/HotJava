package com.example.hotevents;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        return new QRCodes("hotevent:checkin:AdJdkjsdSDJKDSG");
    }

    private Event mockEvent(Date startDate, Date endDate) {
        Event event = new Event (startDate, endDate, 50, "sdgwioegsjdgjsk",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                mockQRCode(), "this is the description", "Event title", "sdSDGJKSDsdjsjdk", "Address");
        return event;
    }

    //Testing the QR Code model
    @Test
    public void testCreateQRCode(){
        String encodedStr = "hotevent:checkin:AdJdkjsdSDJKDSG";
        QRCodes code = mockQRCode();
        assertEquals(encodedStr, code.getEncodedStr());
        assertEquals("checkin", code.getType());
        assertEquals("AdJdkjsdSDJKDSG", code.getEventId());
    }

    //Testing QR Code Encoder method through getBitmap method
    //Validating whether it correctly returns a bitmap
    @Test
    public void testGetBitmap(){
        QRCodes code = mockQRCode();
        Bitmap bitmap = code.getBitmap();
        assertNotNull(bitmap);
    }

    //Testing QR Code validation method
    @Test
    public void testCodeValidationPass(){
        //First, testing when it should return as correct
        String encodedStr = "hotevent:checkin:AdJdkjsdSDJKDSG";
        QRCodes code = mockQRCode();
        assertEquals(true, code.validateQRCode(encodedStr));
    }

    //Testing QR Code validation method when it should fail
    @Test
    public void testCodeValidationFail(){
        String encodedStr = "hotevent:checkin:sdjgsdgslkdjgjsd";
        QRCodes code = mockQRCode();
        assertEquals(false, code.validateQRCode(encodedStr));
    }

    //Testing that the Event model can be created successfully
    @Test
    public void testCreateEvent(){
        Date startDate = new Date();
        Date endDate = new Date();
        Event event = mockEvent(startDate, endDate);
        assertEquals(startDate, event.getStartDateTime());
        assertEquals(endDate, event.getEndDateTime());
        assertEquals("50", Integer.toString(event.getMaxAttendees()));
        assertEquals("sdgwioegsjdgjsk", event.getOrganiserId());
        assertEquals("gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                event.getPosterStr());
        assertEquals(mockQRCode().getEncodedStr(), event.getQrCode().getEncodedStr());
        assertEquals("this is the description", event.getDescription());
        assertEquals("sdSDGJKSDsdjsjdk", event.getEventId());
    }

//    //Test downloading a poster from firebase
//    @Test
//    public void testDownloadPoster(){
//
//    }

    //Testing start & end date & time string returns
    @Test
    public void testDateTimeStr(){
        String startDateStr = "04/04/2024";
        String timeStr = "12:30";
        String endDateStr = "04/05/2024";
        Date startDate;
        Date endDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            startDate = dateFormat.parse(startDateStr + " " + timeStr);
            endDate = dateFormat.parse(endDateStr + " " + timeStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Event event = mockEvent(startDate, endDate);

        assertEquals(startDateStr, event.getStartDateStr());
        assertEquals(timeStr, event.getStartTimeStr());
        assertEquals(endDateStr, event.getEndDateStr());
        assertEquals(timeStr, event.getEndTimeStr());
    }
}
