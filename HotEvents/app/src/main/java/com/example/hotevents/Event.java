package com.example.hotevents;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an Event object. Contains attributes and methods regarding Event data
 */
public class Event implements Serializable, Parcelable {

    private Date startDateTime;
    private Date endDateTime;
    private String location;
    private Integer maxAttendees;
    private String organiserId;
    private Bitmap poster;
    private String posterStr;
    private QRCodes qrCode;
    private QRCodes qrCodePromo;
    private String description;
    private String title;
    private String eventId;
    private MyEventsAdapter myEventsAdapter;
    private UpcomingEventAdapter upcomingEventAdapter;
    private AdminEventsAdapter adminEventsAdapter;
    private OrganizedEventsAdapter organizedEventsAdapter;
    /**
     * Constructor for Event Object
     * @param startDateTime start Date and time of the Event
     * @param endDateTime end Date and time of the Event
     * @param maxAttendees Maximum number of Event Attendees [null if no max]?
     * @param organiserId Reference to Event organizer(user)
     * @param poster Reference to profile poster/banner imagee
     * @param qrCode Reference to Unique QR Code
     * @param description Description of Event
     * @param title Event Tile
     * @param eventId Unique event ID
     */
    Event(Date startDateTime, Date endDateTime, @Nullable Integer maxAttendees,
          @Nullable String organiserId, @Nullable Bitmap poster, @Nullable String posterStr,
          QRCodes qrCode, @Nullable String description, String title, @Nullable String eventId, String location){
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.maxAttendees = maxAttendees;
        this.organiserId=organiserId;
        this.poster = poster;
        this.posterStr = posterStr;
        this.qrCode = qrCode;
        this.description = description;
        this.title = title;
        this.eventId = eventId;
        this.location = location;
    }


    /**
     * Optional Constructor
     * @param title Event Title
     */
    Event(String title){
        this.title = title;
    }       // Unsure whether necessary or if theres a better way

    //https://www.geeksforgeeks.org/android-pass-parcelable-object-from-one-activity-to-another-using-putextra/
    //Implementing parcelable interface
    protected Event(Parcel in){
        startDateTime = (java.util.Date) in.readSerializable();
        endDateTime = (java.util.Date) in.readSerializable();
        maxAttendees = (Integer) in.readValue(Integer.class.getClassLoader());
        organiserId = (String) in.readValue(String.class.getClassLoader());
        qrCode = (QRCodes) in.readSerializable();
        description = (String) in.readValue(String.class.getClassLoader());
        title = in.readString();
        eventId = (String) in.readValue(String.class.getClassLoader());
        location = in.readString();
        posterStr = in.readString();

        if (posterStr != null){
            try {
                Thread thread = downloadAndSetPoster(FirebaseStorage.getInstance());
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void setAdapter(MyEventsAdapter adapter) {
        this.myEventsAdapter = adapter;
    }
    public void setAdapterUpComingEvents(UpcomingEventAdapter upcomingEventAdapter) {
        this.upcomingEventAdapter = upcomingEventAdapter;
    }
    public void setAdapterAdminEvents(AdminEventsAdapter adminEventsAdapter) {
        this.adminEventsAdapter = adminEventsAdapter;
    }
    public void setAdapterOrganizedEvents(OrganizedEventsAdapter organizedEventsAdapter) {
        this.organizedEventsAdapter = organizedEventsAdapter;
    }
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeSerializable(startDateTime);
        parcel.writeSerializable(endDateTime);
        parcel.writeValue(maxAttendees);
        parcel.writeValue(organiserId);
        parcel.writeSerializable(qrCode);
        parcel.writeValue(description);
        parcel.writeString(title);
        parcel.writeValue(eventId);
        parcel.writeString(location);
        parcel.writeString(posterStr);
        poster = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>(){
        @Override
        public Event createFromParcel(Parcel parcel) {
            return new Event(parcel);
        }

        @Override
        public Event[] newArray(int i) {
            return new Event[i];
        }
    };

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }
    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }
    public void setEventId(String eventId){this.eventId = eventId;}

    public void setOrganiserId(String organiserId) {
        this.organiserId = organiserId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQRCode(QRCodes qrCode) {
        this.qrCode = qrCode;
    }
    public void setQRCodePromo(QRCodes qrCode) {this.qrCodePromo = qrCode;}

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public Date getStartDateTime(){
        return this.startDateTime;
    }
    public Date getEndDateTime(){
        return this.endDateTime;
    }

    //Reference: https://stackoverflow.com/questions/5683728/convert-java-util-date-to-string
    public String getStartDateStr() {
        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    public String getEndDateStr(){
        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    public String getStartTimeStr(){
        String pattern = "HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    public String getEndTimeStr(){
        String pattern = "HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }
    public Integer getMaxAttendees(){
        return this.maxAttendees;
    }
    public String getOrganiserId(){
        return this.organiserId;
    }

    //Used to download the poster from the posterStr and returns it
    public Bitmap getPoster() {
        if (posterStr == null) {
            return null;
        }
        if (poster == null){
            try {
                Thread thread = downloadAndSetPoster(FirebaseStorage.getInstance());
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return poster;

    }

    public String getPosterStr(){
        return posterStr;
    }
    public void setPosterStr(String posterStr){
        this.posterStr = posterStr;
    }

    public String getDescription() {
        return description;
    }

    public QRCodes getQrCode() {
        return qrCode;
    }
    public QRCodes getQrCodePromo() {return qrCodePromo; }
    public String getEventId(){return eventId;}

    private Thread downloadAndSetPoster(FirebaseStorage storage) throws InterruptedException {
        try{
            StorageReference photoRef = storage.getReferenceFromUrl(posterStr);
        }
        catch (Exception e){
            return new Thread();
        }
        Thread thread = new Thread(() -> {
            Log.d("Event", "PosterStr: " + posterStr);
            StorageReference photoRef = storage.getReferenceFromUrl(posterStr);
            final long FIVE_MEGABYTE = 5 * 1024 * 1024;
            photoRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(bytes -> {
                // Check if the byte array is not null
                if (bytes != null && bytes.length > 0) {
                    // Decode the byte array into a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // Check if the bitmap is not null
                    if (bitmap != null) {
                        // Set the downloaded profile picture to the image view
                        Log.e("Event", "Setting poster bitmap");
                        //Setting poster to the event
                        this.setPoster(bitmap);
                        if (myEventsAdapter != null) {
                            myEventsAdapter.notifyDataSetChanged();
                        }
                        if (upcomingEventAdapter != null) {
                            upcomingEventAdapter.notifyDataSetChanged();
                        }
                        if (adminEventsAdapter != null) {
                            adminEventsAdapter.notifyDataSetChanged();
                        }
                        if (organizedEventsAdapter != null) {
                            organizedEventsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Event", "Failed to decode byte array into Bitmap");
                    }
                } else {
                    Log.e("Event", "Downloaded byte array is null or empty");
                }
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("Event", "Failed to download profile picture: " + exception.getMessage());
            });
        });
        thread.start();
        //Looping until the poster is properly set before returning
        while(poster == null){}
        return thread;
    }
}
