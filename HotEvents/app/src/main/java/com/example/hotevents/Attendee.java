package com.example.hotevents;

/**
 * Attendee Class meant to represent an attendee for an event
 */
public class Attendee {
    private String name;
    private int checkinCount;
    private String profileImageUrl;

    public Attendee(String name, int checkinCount, String profileImageUrl) {
        this.name = name;
        this.checkinCount = checkinCount;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public int getCheckinCount() {
        return checkinCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
