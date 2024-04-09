package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        // Create a mock QRCodes object
        QRCodes mockQRCode = mock(QRCodes.class);

        // Create an Event object with mock data
        event = new Event(
                new Date(), // startDateTime
                new Date(), // endDateTime
                100, // maxAttendees
                "organiserId123", // organiserId
                null, // poster
                null, // posterStr
                mockQRCode, // qrCode
                "Test description", // description
                "Test title", // title
                "eventId123", // eventId
                "Test location" // location
        );
    }

    @Test
    public void testGetMaxAttendees() {
        // Assert that getMaxAttendees returns the correct value
        assertEquals(Integer.valueOf(100), event.getMaxAttendees());
    }

    @Test
    public void testGetOrganiserId() {
        // Assert that getOrganiserId returns the correct value
        assertEquals("organiserId123", event.getOrganiserId());
    }

    @Test
    public void testGetDescription() {
        // Assert that getDescription returns the correct value
        assertEquals("Test description", event.getDescription());
    }

    @Test
    public void testGetTitle() {
        // Assert that getTitle returns the correct value
        assertEquals("Test title", event.getTitle());
    }

    @Test
    public void testGetEventId() {
        // Assert that getEventId returns the correct value
        assertEquals("eventId123", event.getEventId());
    }

    @Test
    public void testGetLocation() {
        // Assert that getLocation returns the correct value
        assertEquals("Test location", event.getLocation());
    }
}
