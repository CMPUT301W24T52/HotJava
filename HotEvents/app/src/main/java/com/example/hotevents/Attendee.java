package com.example.hotevents;

/**
 * Attendee Class meant to represent an attendee for an event
 */
public class Attendee {
    private String UID;
    private int checkinCount;

    public Attendee(String UID, int checkinCount) {
        this.UID = UID;
        this.checkinCount = checkinCount;
    }

    public String getUID() {
        return UID;
    }

    public int getCheckinCount() {
        return checkinCount;
    }
}
