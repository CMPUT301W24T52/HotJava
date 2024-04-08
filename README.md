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
- Dipesh

### Instructions/How-To:
Below is a list of general instructions to begin proper operation of the HotEvents organizing platform from both an attendee and event organizer role.
##### Getting Started:
When first launching this application, HotEvents will check if the current user previously exists within the database or if the user is new. This is done by verifying if a certain device identifier exists inside the `User` collection of the database. Assuming the user is new (in their first use of the app), HotEvents will display a toast notification to confirm the new user has been added to the database. From here, the user is presented with the homepage and has access to view, sign-up, and create events.

##### Signup for an Event
The main page of the app loads a number of upcoming events. To sign up for one of these events, click on the card corresponding to one of the events. This will open up another activity containing the details of the clicked event. At the bottom of the new activity, click the "Sign Up" button. The organizer for this event will be notified that another user has signed up. If you have opted in for geolocation tracking, the organizer will also be able to see where on the map you signed in. You will also now be able to access this event from the "Signed Up Events" activity, which is accessible from the main activity.

##### Checkin for an Event
Once signed up for an event, the "Sign Up" button will turn into the "Check In" button instead. Once this new button is clicked, you will be prompted to scan the check-in QR code. This QR code is attainable from the organiser of the event. Once checked in, the organiser will be able to view the location in which you checked in from. Checking in can also be done multiple times.

##### Create Event
To create an event, first open the side menu on the main activity using the hamburger button. The create event button is near the bottom of the side menu. This will open a separate activity where you can input all of the relevant event information. Once submitted, this event will be populated on the main activity for all users. As well, you can access this event through the "Organized Event" activity, which is also accessible through the side menu on the main activity. When accessing an event that you organized you will gain access to organiser specific functionalities. These functionalities include viewing the list of attendees signed up and checked in to the event, sending push notifications to the attendees, editing the event, and deleting the event.

##### View/Customize Profile
Your profile can also be accessed from the side menu on the main activity. A user profile is associated with the specific device that you are accessing the app from. From the profile activity you can edit the information associated with your profile, including the profile picture, email, phone number, and address.

##### Admin Functionalities
Admin users can have a high level access of all of the data present within the database. They can query all of the users, events, and pictures posted. From here, they can remove any of this input information. This functionality would be useful in the case of moderating innappropriate names and photos.

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


## Wiki Hyperlinks
[CRC Cards](https://github.com/CMPUT301W24T52/HotJava/wiki/CRC-Cards)\
[Database Structure](https://github.com/CMPUT301W24T52/HotJava/wiki/Database-Structure)\
[High-View UML](https://github.com/CMPUT301W24T52/HotJava/wiki/High%E2%80%90View-UML)\
[Overview, Staging, and Timeline](https://github.com/CMPUT301W24T52/HotJava/wiki/Overview,-Staging,-and-Timeline)\
[Sprint Planning](https://github.com/CMPUT301W24T52/HotJava/wiki/Sprint-Planning)\
[Storyboard - UI & Functionality](https://github.com/CMPUT301W24T52/HotJava/wiki/Storyboard-%E2%80%90-UI-&-Functionality)

