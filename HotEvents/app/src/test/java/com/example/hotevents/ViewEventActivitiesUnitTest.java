package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class ViewEventActivitiesUnitTest {

    final private ArrayList<String> mockUserSignedUpEvents = new ArrayList<>();
    private ArrayList<Event> mockEventList = new ArrayList<>();


    private void setMockUserSignedUpEvents(){
        for (int i = 0; i < 10; i++){
            String mockEventId = new Random().toString();
            mockUserSignedUpEvents.add(mockEventId);
        }
    }

    private void setMockList(){
        for (int i = 0; i < 10; i++){
            Event mockEvent = new Event("Event" + i);
            String id = new Random().toString();
            mockEvent.setEventId(id);
            mockEventList.add(mockEvent);
        }
    }
    private void clearMockList(){
        mockEventList.clear();
    }

    @Test
    public void testPopulateEvents(){
        setMockList();
        assertEquals(mockEventList.size(), 10);
        clearMockList();
    }

    @Test
    public void testSearch(){
        setMockList();
        String searchMsg = "Event1";
        boolean eventFound = false;
        assertEquals(mockEventList.size(), 10);
        for (Event event : mockEventList){
            if (searchMsg.equals(event.getTitle())){
                eventFound = true;
                break;
            }
        }
        assertTrue(eventFound);
        clearMockList();
    }

    @Test
    public void testUserSignedUpEvents(){
        // Eventlist contains event
        setMockUserSignedUpEvents();
        setMockList();
        boolean hasEvent = false;
        mockEventList.get(0).setEventId(mockUserSignedUpEvents.get(1));

        for (Event event : mockEventList){
            if (mockUserSignedUpEvents.contains(event.getEventId())){
                hasEvent = true;
                break;
            }
        }
        assertTrue(hasEvent);
        mockEventList.clear();
        mockUserSignedUpEvents.clear();

        // Eventlist does not contain any event
        setMockUserSignedUpEvents();
        setMockList();

        boolean hasEventNot = false;
        for (Event event:mockEventList){
            if (mockUserSignedUpEvents.contains(event.getEventId())){
                hasEventNot = true;
                break;
            }
        }
        assertFalse(hasEventNot);

        mockUserSignedUpEvents.clear();
        mockEventList.clear();

    }
}
