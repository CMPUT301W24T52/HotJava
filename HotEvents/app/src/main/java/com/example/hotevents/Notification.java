package com.example.hotevents;

import java.util.Date;

/**
 * A class representing a notification.
 */
public class Notification {
    private String fcmToken;
    private String eventId;
    private String notificationMessage;
    private Date timestamp;

    /**
     * Constructs a new Notification object.
     *
     * @param fcmToken            The FCM token associated with the notification.
     * @param eventId             The event ID associated with the notification.
     * @param notificationMessage The message of the notification.
     * @param timestamp           The timestamp of the notification.
     */
    public Notification(String fcmToken, String eventId, String notificationMessage, Date timestamp) {
        this.fcmToken = fcmToken;
        this.eventId = eventId;
        this.notificationMessage = notificationMessage;
        this.timestamp = timestamp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
