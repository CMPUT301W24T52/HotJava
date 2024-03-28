package com.example.hotevents;

import java.io.Serializable;

public class LoggedInUser implements Serializable {
    private String uID;

    LoggedInUser(String uID){
        this.uID = uID;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }
}
