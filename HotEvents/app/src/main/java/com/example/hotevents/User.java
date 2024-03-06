package com.example.hotevents;

import android.graphics.Bitmap;

import java.util.ArrayList;


/**
 * User object to represent user attributes
 */
public class User {
    private String userName;
    private Bitmap profilePicture;
    private String firebaseId;
    private String email;
    private ArrayList<String> signedUpEvents;       //array of eventId's the user is signed up for

    public String getUserName() {
        return userName;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<String> getSignedUpEvents() {
        return signedUpEvents;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSignedUpEvents(ArrayList<String> signedUpEvents) {
        this.signedUpEvents = signedUpEvents;
    }
}
