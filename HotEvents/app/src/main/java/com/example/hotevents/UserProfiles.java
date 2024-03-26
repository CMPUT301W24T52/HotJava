// UserProfiles.java

package com.example.hotevents;

public class UserProfiles {
    private String profileImageUrl; // New field for profile picture URL
    private String username;
    private String uid;

    public UserProfiles(String profileImageUrl, String username, String uid) {
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.uid = uid;
    }

    // Getters and setters for the fields
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
