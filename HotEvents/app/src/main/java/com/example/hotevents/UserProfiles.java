package com.example.hotevents;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserProfiles implements Parcelable, Serializable {
    private final int profileImageRes; // Resource ID for the profile image
    private final String username;
    private String uid; // Add UID field

    public UserProfiles(int profileImageRes, String username, String uid) {
        if (username == null || uid == null) {
            throw new IllegalArgumentException("Username and deviceId cannot be null");
        }
        this.profileImageRes = profileImageRes;
        this.username = username;
        this.uid = uid;
    }

    protected UserProfiles(Parcel in) {
        profileImageRes = in.readInt();
        username = in.readString();
        uid = in.readString();
    }

    public static final Creator<UserProfiles> CREATOR = new Creator<UserProfiles>() {
        @Override
        public UserProfiles createFromParcel(Parcel in) {
            return new UserProfiles(in);
        }

        @Override
        public UserProfiles[] newArray(int size) {
            return new UserProfiles[size];
        }
    };

    public int getProfileImageRes() {
        return profileImageRes;
    }

    public String getUsername() {
        return username;
    }

    // Getter method for UID
    public String getUid() {
        return uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(profileImageRes);
        dest.writeString(username);
        dest.writeString(uid);
    }
}
