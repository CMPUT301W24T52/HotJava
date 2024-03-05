package com.example.hotevents;

import android.widget.ImageView;

import com.google.type.DateTime;

/**
 * Represents an Event object. Contains attributes and methods regarding Event data
 */
public class Event {

    private DateTime dateTime;
    private Integer maxAttendees;
    private String organiserId;
    private ImageView poster;
    private String qrCode;
    private String description;
    private String title;

    /**
     * Constructor for Event Object
     * @param dateTime Date and time of the Event
     * @param maxAttendees Maximum number of Event Attendees [null if no max]?
     * @param organiserId Reference to Event organizer(user)
     * @param poster Reference to profile poster/banner imagee
     * @param qrCode Reference to Unique QR Code
     * @param description Description of Event
     * @param title Event Tile
     */
    Event(DateTime dateTime, Integer maxAttendees, String organiserId, ImageView poster, String qrCode, String description, String title){
        this.dateTime = dateTime;
        this.maxAttendees = maxAttendees;
        this.organiserId=organiserId;
        this.poster = poster;
        this.qrCode = qrCode;
        this.description = description;
        this.title = title;
    }

    /**
     * Optional Constructor
     * @param title Event Title
     */
    Event(String title){
        this.title = title;
    }       // Unsure whether necessary or if theres a better way

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public void setOrganiserId(String organiserId) {
        this.organiserId = organiserId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setPoster(ImageView poster) {
        this.poster = poster;
    }

    public DateTime getDateTime(){
        return this.dateTime;
    }
    public Integer getMaxAttendees(){
        return this.maxAttendees;
    }
    public String getOrganiserId(){
        return this.organiserId;
    }

    public ImageView getPoster() {
        return poster;
    }

    public String getDescription() {
        return description;
    }

    public String getQrCode() {
        return qrCode;
    }
}
