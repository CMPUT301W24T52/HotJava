# HotJava
Group Project for CMPUT 301

## Summary
HotJava is an event-organizing mobile platform for android, written in Java. Users can act as either an attendee (by default) or as an event organizer (when a user creates their first event). HotJava utilizes a Firebase (noSQL) document-store database to manage cloud data on a per-user and per-event basis. *Details can be viewed on Wiki pages*

#### Members:
- Matthew
- Mahek
- Harsh
- Bryan
- Sergio
- Deepish

### Instructions/How-To:
Below is a list of general instructions to begin proper operation of the HotEvents organizing platform from both an attendee and event organizer role.
##### Getting Started:
When first launching this application, HotEvents will check if the current user previously exists within the database or if the user is new. This is done by verifying if a certain device identifier exists inside the `User` collection of the database. Assuming the user is new (in their first use of the app), HotEvents will display a toast notification to confirm the new user has been added to the database. From here, the user is presented with the homepage and has access to view, sign-up, and create events.
##### Signup for an Event

##### Create Event

##### View/Customize Profile

## Platform Format:
The following is a high-level overview of this projects code formatting and structure. More in depth details can be found *here* in the HotEvents Wiki.

### Frontend
The frontend consists of all displays and media including displaying images, page/activity-view formatting, color preferences, app splash-screen, and all other UI elements. All UI formatting done in XML files.

### Backend
The backend is responsible for handling the flow of data, making calls to the noSQL database whenever any `User`, `Event`, or `Notification` data is needed, and supporting underlying functionality presented to the user.

### Database
The database maintains 2 primary collections (Users and Events) and 1 secondary collection (Notifications) in a noSQL Document-Store format, Firebase. The following is a list of attributes for each document contained in each event.
##### User Collection:
Contains documents corresponding to each User.
- Name (String)
- Usertype (String)
- UID (String)
- FCM Token (String- from Firebase)
- geographic permissions toggle (Boolean)
- profilePicture-default (link)
- profilePicture-custom (link)
- signups (Array of Event ID strings)
##### Events Collection:
Contains documents corresponding to each Event. In addition to the attributes below, each events document also contains collections that house user signups and user checkins.
- Event Title (string)
- Event ID (string)
- Start Date/Time (Date)
- End Date/Time (Date)
- Poster (image Reference)
- QRCode-Signup (QR string)
- QRCode-CheckIn (QR string)
- Location (String/Address)
- MaxAttendees (Optional- Null if not set, otherwise Int)
##### Notifications Collection:
Contains documents corresponding to Notifications regarding events. These notifications are created by the organizer of the respective Event.
- Event ID (String- reference to a certain Event)
- FCM token (String - from Firebase)
- Notification type (String)
- Notification message (String- description of notification content)
- Timestamp (Date/Time)

