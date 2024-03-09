package com.example.hotevents;

import android.graphics.Bitmap;
import android.location.Location;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.google.type.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an Event object. Contains attributes and methods regarding Event data
 */
public class Event implements Serializable {

    private Date startDateTime;
    private Date endDateTime;
    private Location location;
    private Integer maxAttendees;
    private String organiserId;
    private Bitmap poster;
    private QRCodes qrCode;
    private String description;
    private String title;
    private String eventId;

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
          @Nullable String organiserId, Bitmap poster, QRCodes qrCode, String description,
          String title, String eventId){
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.maxAttendees = maxAttendees;
        this.organiserId=organiserId;
        this.poster = poster;
        this.qrCode = qrCode;
        this.description = description;
        this.title = title;
        this.eventId = eventId;
    }

    /**
     * Optional Constructor
     * @param title Event Title
     */
    Event(String title){
        this.title = title;
    }       // Unsure whether necessary or if theres a better way

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
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

    public void setQrCode(QRCodes qrCode) {
        this.qrCode = qrCode;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public Date getStartDateTime(){
        return this.startDateTime;
    }
    public Date getEndDateTime(){
        return this.endDateTime;
    }
    public Integer getMaxAttendees(){
        return this.maxAttendees;
    }
    public String getOrganiserId(){
        return this.organiserId;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public String getDescription() {
        return description;
    }

    public QRCodes getQrCode() {
        return qrCode;
    }
    public String getEventId(){return eventId;}
}
