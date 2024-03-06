package com.example.hotevents;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {
    //Does this work?
    @Rule
    public ActivityScenarioRule<CreateEventActivity> scenario =
            new ActivityScenarioRule<CreateEventActivity>(CreateEventActivity.class);

    //Test adding poster
    @Test
    public void testAddPoster(){

    }

    //Test start date
    @Test
    public void testStartDate(){

    }

    //Test end date
    @Test
    public void testEndDate(){

    }

    //Test max attendees
    @Test
    public void testMaxAttendees(){

    }

    //Test QR Code creation
    @Test
    public void testQRCodeCreation(){

    }

    //Test create event success
    @Test
    public void testCreateEvent(){

    }

}
