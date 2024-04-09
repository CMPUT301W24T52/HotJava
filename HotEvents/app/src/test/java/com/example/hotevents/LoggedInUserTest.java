package com.example.hotevents;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoggedInUserTest {

    @Test
    public void testGetSetUID() {
        // Create mock data
        String mockUID = "mockUID";

        // Create a LoggedInUser instance with the mock UID
        LoggedInUser user = new LoggedInUser(mockUID);

        // Test the getuID method
        assertEquals("Mock UID should match", mockUID, user.getuID());

        // Change the UID using setuID method
        String newMockUID = "newMockUID";
        user.setuID(newMockUID);

        // Test the getuID method after changing the UID
        assertEquals("New mock UID should match", newMockUID, user.getuID());
    }
}

