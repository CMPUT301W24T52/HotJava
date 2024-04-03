package com.example.hotevents;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

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
    private UpcomingEventActivityAdapter upcomingEventActivityAdapter;
    /**
     * Constructor for Event Object
     * Optional parameters are nullable and can be set by a Setter later
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
     * Utilized when pulling data from the database
     * @param title Event Title
     */
    Event(String title){
        this.title = title;
    }

    /**
     * Parcelable interface to pass between activities
     * @param in Parcel
     */
    //https://www.geeksforgeeks.org/android-pass-parcelable-object-from-one-activity-to-another-using-putextra/
    //Implementing parcelable interface
    protected Event(Parcel in){
        startDateTime = (java.util.Date) in.readSerializable();
        endDateTime = (java.util.Date) in.readSerializable();
        maxAttendees = (Integer) in.readValue(Integer.class.getClassLoader());
        organiserId = (String) in.readValue(String.class.getClassLoader());
        qrCode = (QRCodes) in.readSerializable();
        qrCodePromo = (QRCodes) in.readSerializable();
        description = (String) in.readValue(String.class.getClassLoader());
        title = in.readString();
        eventId = (String) in.readValue(String.class.getClassLoader());
        location = in.readString();
        posterStr = in.readString();
    }

    /**
     * Setting the MyEventsAdapter to be updated once the poster finishes downloading
     * @param adapter MyEventsAdapter
     */
    public void setAdapter(MyEventsAdapter adapter) {
        this.myEventsAdapter = adapter;
    }

    /**
     * Setting the UpcomingEventAdapter to be updated once the poster finishes downloading
     * @param upcomingEventAdapter UpcomingEventAdapter
     */
    public void setAdapterUpComingEvents(UpcomingEventAdapter upcomingEventAdapter) {
        this.upcomingEventAdapter = upcomingEventAdapter;
    }

    /**
     * Setting the AdminEventsAdapter to be updated once the poster finishes downloading
     * @param adminEventsAdapter AdminEventAdapter
     */
    public void setAdapterAdminEvents(AdminEventsAdapter adminEventsAdapter) {
        this.adminEventsAdapter = adminEventsAdapter;
    }

    /**
     * Setting the OrganizedEventsAdapter to be updated once the poster finishes downloading
     * @param organizedEventsAdapter OrganizedEventAdapter
     */
    public void setAdapterOrganizedEvents(OrganizedEventsAdapter organizedEventsAdapter) {
        this.organizedEventsAdapter = organizedEventsAdapter;
    }

    /**
     * Setting the UpcomingEventActivityAdapter to be updated once the poster finishes downloading
     * @param upcomingEventActivityAdapter UpcomingEventActivityAdapter
     */
    public void setAdapterUpcomingEventsActivity(UpcomingEventActivityAdapter upcomingEventActivityAdapter) {
        this.upcomingEventActivityAdapter = upcomingEventActivityAdapter;
    }

    /**
     * Part of the parcelable interface
     * Parcel is written to while getting ready to pass the event between activities
     * @param parcel Parcel
     * @param i
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeSerializable(startDateTime);
        parcel.writeSerializable(endDateTime);
        parcel.writeValue(maxAttendees);
        parcel.writeValue(organiserId);
        parcel.writeSerializable(qrCode);
        parcel.writeSerializable(qrCodePromo);
        parcel.writeValue(description);
        parcel.writeString(title);
        parcel.writeValue(eventId);
        parcel.writeString(location);
        parcel.writeString(posterStr);
        poster = null;
    }

    /**
     * Part of the parcelable interface
     * @return Hardcoded 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Part of the parcelable interface
     */
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

    /**
     * Setter for location
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for location
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter for title
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for title
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for the start date & time for the event
     * @param startDateTime
     */
    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    /**
     * Setter for the start date & time for the event
     * @param endDateTime
     */
    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * Setter for the max attendees
     * @param maxAttendees
     */
    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    /**
     * Setter for the eventId
     * @param eventId
     */
    public void setEventId(String eventId){this.eventId = eventId;}

    /**
     * Setter for the organiserId
     * @param organiserId
     */
    public void setOrganiserId(String organiserId) {
        this.organiserId = organiserId;
    }

    /**
     * Setter for the description
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Setter for the QR code
     * @param qrCode
     */
    public void setQRCode(QRCodes qrCode) {
        this.qrCode = qrCode;
    }

    /**
     * Setter for the promotional QR code
     * @param qrCode
     */
    public void setQRCodePromo(QRCodes qrCode) {this.qrCodePromo = qrCode;}

    /**
     * Setter for the poster
     * @param poster
     */
    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    /**
     * Getter for the start date & time of the event
     * @return startDateTime
     */
    public Date getStartDateTime(){
        return this.startDateTime;
    }

    /**
     * Getter for the end date & time of the event
     * @return
     */
    public Date getEndDateTime(){
        return this.endDateTime;
    }

    /**
     * Converts the start date into a usable string for display in the UI
     * @return Start date string
     */
    //Reference: https://stackoverflow.com/questions/5683728/convert-java-util-date-to-string
    public String getStartDateStr() {
        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    /**
     * Converts the end date into a usable string for display in the UI
     * @return end date string
     */
    public String getEndDateStr(){
        String pattern = "MM/dd/yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    /**
     * Converts the start time into a usable string for display in the UI
     * @return start time string
     */
    public String getStartTimeStr(){
        String pattern = "HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    /**
     * Converts the end time into a usable string for display in the UI
     * @return end time string
     */
    public String getEndTimeStr(){
        String pattern = "HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(startDateTime);
    }

    /**
     * Getter for the max attendees
     * @return maxAttendees
     */
    public Integer getMaxAttendees(){
        return this.maxAttendees;
    }

    /**
     * Getter for the organiser Id
     * @return organiserId
     */
    public String getOrganiserId(){
        return this.organiserId;
    }

    /**
     * If the poster database URL exists, start the asynchronous poster download and returns the bitmap once complete
     * Otherwise, returns null
     * If the poster has already been set earlier, returns the bitmap associated with the poster
     * @return
     */
    //Used to download the poster from the posterStr and returns it
    public Bitmap getPoster() {
        if (posterStr == null) {
            return null;
        }
        if (poster == null){
            downloadAndSetPoster(FirebaseStorage.getInstance(), null);
        }
        return poster;

    }

    /**
     * Used to assign the poster to a passed ImageView
     * Handles the asynchronous download before the image is set
     * @param imageView UI ImageView displaying the poster
     */
    public void assignPoster(ImageView imageView){
        //Once the poster download is complete, it's going to set the bitmap
        //of the image view to the recently downloaded bitmap, making asynchronicity a non-issue
        if (posterStr != null){
            if (poster != null){
                imageView.setImageBitmap(this.poster);
            }
            else{
                downloadAndSetPoster(FirebaseStorage.getInstance(), imageView);
            }
        }
    }

    /**
     * Getter for the poster database URL
     * @return posterStr
     */
    public String getPosterStr(){
        return posterStr;
    }

    /**
     * Setter for the poster database URL
     * @param posterStr
     */
    public void setPosterStr(String posterStr){
        this.posterStr = posterStr;
    }

    /**
     * Getter for the description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the QR code
     * @return qrCode
     */
    public QRCodes getQrCode() {
        return qrCode;
    }

    /**
     * Getter for the promotional QR Code
     * @return qrCodePromo
     */
    public QRCodes getQrCodePromo() {return qrCodePromo; }

    /**
     * Getter for the eventId
     * @return eventId
     */
    public String getEventId(){return eventId;}

    /**
     * Starts the asynchronous poster download process
     * Once completes, notifies the adapter that the dataset has changed to ensure the poster is displayed correctly
     * @param storage FirebaseStorage reference
     * @param imageView ImageView displaying the poster
     */
    private void downloadAndSetPoster(FirebaseStorage storage, @Nullable ImageView imageView){
        try{
            StorageReference photoRef = storage.getReferenceFromUrl(posterStr);
        }
        catch (Exception e){
            return;
        }

        //Log.d("Event", "PosterStr: " + posterStr);
        StorageReference photoRef = storage.getReferenceFromUrl(posterStr);
        final long FIVE_MEGABYTE = 5 * 1024 * 1024;

        Task<byte[]> downloadTask = photoRef.getBytes(FIVE_MEGABYTE);

        downloadTask.addOnSuccessListener(bytes -> {
            // Check if the byte array is not null
            if (bytes != null && bytes.length > 0) {
                // Decode the byte array into a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Check if the bitmap is not null
                if (bitmap != null) {
                    // Set the downloaded profile picture to the image view
                    //Log.d("Event", "Setting poster bitmap");
                    //Setting poster to the event
                    this.setPoster(bitmap);
                    if (imageView != null){
                        imageView.setImageBitmap(bitmap);
                    }
                    if (myEventsAdapter != null) {
                        myEventsAdapter.notifyDataSetChanged();
                        //Log.d("Poster Set", "myEventsAdapter was notified");
                    }
                    if (upcomingEventAdapter != null) {
                        upcomingEventAdapter.notifyDataSetChanged();
                        //Log.d("Poster Set", "myEventsAdapter was notified");
                    }
                    if (adminEventsAdapter != null) {
                        adminEventsAdapter.notifyDataSetChanged();
                        //Log.d("Poster Set", "myEventsAdapter was notified");
                    }
                    if (organizedEventsAdapter != null) {
                        organizedEventsAdapter.notifyDataSetChanged();
                        //Log.d("Poster Set", "myEventsAdapter was notified");
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
    }
}
