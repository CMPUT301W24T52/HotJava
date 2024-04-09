package com.example.hotevents;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Date;

public class NotificationTest {

    @Test
    public void testNotificationConstructorAndGetters() {
        // Mock data
        String fcmToken = "mockedToken";
        String eventId = "mockedEventId";
        String notificationMessage = "Test message";
        Date timestamp = new Date();

        // Create a new Notification object
        Notification notification = new Notification(fcmToken, eventId, notificationMessage, timestamp);

        // Verify that the getters return the correct values
        assertEquals("FCM token should match", fcmToken, notification.getFcmToken());
        assertEquals("Event ID should match", eventId, notification.getEventId());
        assertEquals("Notification message should match", notificationMessage, notification.getNotificationMessage());
        assertEquals("Timestamp should match", timestamp, notification.getTimestamp());
    }

    @Test
    public void testNotificationSetters() {
        // Mock data
        String fcmToken = "mockedToken";
        String eventId = "mockedEventId";
        String notificationMessage = "Test message";
        Date timestamp = new Date();

        // Create a new Notification object
        Notification notification = new Notification("", "", "", new Date());

        // Set mock data using setters
        notification.setFcmToken(fcmToken);
        notification.setEventId(eventId);
        notification.setNotificationMessage(notificationMessage);
        notification.setTimestamp(timestamp);

        // Verify that the getters return the correct values after setting
        assertEquals("FCM token should match", fcmToken, notification.getFcmToken());
        assertEquals("Event ID should match", eventId, notification.getEventId());
        assertEquals("Notification message should match", notificationMessage, notification.getNotificationMessage());
        assertEquals("Timestamp should match", timestamp, notification.getTimestamp());
    }
}
