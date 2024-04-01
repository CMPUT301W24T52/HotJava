package com.example.hotevents;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Page for creating and updating an event
 * TODO:
 * - Set up choose QR button
 * - Fix location picker from the map
 * - Set up the update event components
 */

public class CreateEventActivity extends AppCompatActivity {
    // Firebase objects
    FirebaseFirestore db;
    FirebaseStorage sref;
    CollectionReference eventsRef;

    // Defining UI objects
    EditText titleText;
    TextView startDateText;
    TextView startTimeText;
    TextView endDateText;
    TextView endTimeText;
    EditText locationText;
    EditText descriptionText;
    EditText maxAttendeeText;
    ImageView posterImage;

    ImageButton backButton;
    ImageButton startCalButton;
    ImageButton endCalButton;
    FloatingActionButton addImageButton;
    ImageButton mapButton;
    Button qrCreateButton;
    Spinner qrChooseSpinner;
    Button createButton;
    Switch maxAttendeeSwitch;
    View maxAttendeeContainer;

    // Defining variables used in creating the event
    String title;
    String eventId = null;
    Date startDate;
    Date endDate;
    String description;
    String location;
    QRCodes qrCode = null;
    QRCodes qrCodePromo = null;
    Bitmap poster = null;
    Integer maxAttendees = null;
    Uri posterUri = null;
    String storageUri = null;
    String organiserId;
    Event updateEvent = null;
    Event newEvent = null;

    // Variables for the Choose QR Code Spinner
    ArrayList<String> qrCodeArray = new ArrayList<String>();
    ArrayAdapter<String> qrCodeAdapter;

    // Defining an enum that will be used to specify whether the QR code will be getting created
    // or chosen from the list of previously used QR codes

    protected enum QRCodeState {
        CREATE,
        CHOOSE
    }

    // Defining an enum that will be used to will change how this activity is handled depending
    // Either in create mode or update mode. Default is create mode
    protected enum ActivityState {
        CREATE,
        UPDATE
    }

    // Setting default state to CREATE
    QRCodeState codeState = QRCodeState.CREATE;
    ActivityState activityState = ActivityState.CREATE;


    /**
     * Started on creation of the activity. Assigns the views to variables, assigns the click events
     * for all of the buttons, grabs argument from main page intent, and sets up the required
     * firebase references
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Setting firebase instance
        db = FirebaseFirestore.getInstance();
        sref = FirebaseStorage.getInstance();
        eventsRef = db.collection("Events");

        // Calling a function that sets the UI elements to variables
        setViews();

        // Getting the event. Might be null or on edit it will populate the fields
        // Pass a boolean inside instead, will make it a lot easier to implement
        Boolean createEvent = getIntent().getBooleanExtra("State", true);

        if (!createEvent) {
            updateEvent = (Event) getIntent().getParcelableExtra("event");
            if (updateEvent.getEventId() != null) {
                activityState = ActivityState.UPDATE;
                eventId = updateEvent.getEventId();
                setFields(updateEvent);
            }
        }

        // Getting the arguments from the Intent
        Intent myIntent = getIntent();
        organiserId = myIntent.getStringExtra("organiser");

        // Querying the current user and using their info to set up the QR Choose string array
        getUserData();

        backButton.setOnClickListener(v -> {
            returnPreviousActivity();
        });

        // Add Image button listener
        // https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/
        // https://stackoverflow.com/questions/29588124/how-to-add-an-image-to-the-emulator-gallery-in-android-studio
        addImageButton.setOnClickListener(v -> {
            // Open image dialog window
            imageChooser();
        });

        // Date input events
        // https://www.geeksforgeeks.org/how-to-popup-datepicker-while-clicking-on-edittext-in-android/
        // https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
        // https://developer.android.com/reference/android/app/DatePickerDialog
        // https://developer.android.com/reference/android/app/TimePickerDialog
        startCalButton.setOnClickListener(v -> {
            // Opening the date picker and time picker dialogs and saving them to the start texts
            openDateTimeDialog(startDateText, startTimeText);
        });

        endCalButton.setOnClickListener(v -> {
            // Opening the date picker and time picker dialogs and saving them to the end texts
            openDateTimeDialog(endDateText, endTimeText);
        });

        // Location input events
        // https://developers.google.com/codelabs/maps-platform/location-places-android#4
        // https://www.geeksforgeeks.org/how-to-implement-google-map-inside-fragment-in-android/

        maxAttendeeSwitch.setOnClickListener(v -> {
            maxAttendeeSwitchClick();
        });

        qrCreateButton.setOnClickListener(v -> {
            qrCreateClick();
        });


        qrChooseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Won't do anything unless "Choose QR Code: " was not selected
                if (i != 0) {
                    // Setting qrCode variable to the chosen QR Code
                    String chooseQrStr = qrCodeArray.get(i);
                    qrCode = new QRCodes(chooseQrStr);
                    codeState = QRCodeState.CHOOSE;
                    makeToast("QR Code Chosen: " + chooseQrStr);
                } else {
                    qrCode = null;
                    codeState = QRCodeState.CREATE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing if nothing selected
            }
        });


        createButton.setOnClickListener(v -> {
            createButtonClick();
        });

        Log.d(TAG, "made it here");

    }

    /**
     * Returns to the previous activity when event is created or back button is pressed
     * TODO
     * - Currently only returns back to MainActivity. Will be fixed once edit functionality is corrected
     */
    protected void returnPreviousActivity() {
        if (activityState == ActivityState.CREATE) {
            Intent myIntent = new Intent(CreateEventActivity.this, MainActivity.class);
            startActivity(myIntent);
        } else if (activityState == ActivityState.UPDATE) {
            Intent myIntent = new Intent(CreateEventActivity.this, EventDetailsActivity.class);

            // Returning different events depending on whether we pressed the back button or completed the event update
            if (newEvent == null) {
                myIntent.putExtra("event", (Parcelable) updateEvent);
                myIntent.putExtra("Update", true);
            } else {
                myIntent.putExtra("event", (Parcelable) newEvent);
            }
            startActivity(myIntent);
        }

    }

    // Querying the Created Events array from the user and creating the spinner list based on
    // The QR codes that can be retrieved
    // Reference: https://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-list
    protected void settingUpSpinner(@Nullable ArrayList<String> createdEvents) {
        qrCodeArray.add("Select:");
        qrCodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, qrCodeArray);
        qrChooseSpinner.setAdapter(qrCodeAdapter);
        qrCodeAdapter.notifyDataSetChanged();
        if (createdEvents != null) {
            for (int i = 0; i < createdEvents.size(); i++) {
                db.collection("Events")
                        .document(createdEvents.get(i)).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String chooseQrStr = documentSnapshot.getString("QRCode");
                                    if (!qrCodeArray.contains(chooseQrStr)) {
                                        qrCodeArray.add(documentSnapshot.getString("QRCode"));
                                        qrCodeAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Spinner Creation", "EXCEPTION: " + e);
                            }
                        });
            }
        }
    }

    protected void getUserData() {
        db.collection("Users")
                .document(organiserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> createdEvents = (ArrayList<String>) documentSnapshot.get("CreatedEvents");
                        settingUpSpinner(createdEvents);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        settingUpSpinner(null);
                    }
                });
    }

    /**
     * Assigns the interactive UI elements to their associated variables for later reference
     */
    protected void setViews() {
        posterImage = findViewById(R.id.poster_image);
        titleText = findViewById(R.id.title_text);
        startDateText = findViewById(R.id.start_date_text);
        endDateText = findViewById(R.id.end_date_text);
        startTimeText = findViewById(R.id.start_time_text);
        endTimeText = findViewById(R.id.end_time_text);
        locationText = findViewById(R.id.location_input_text);
        descriptionText = findViewById(R.id.description_input_text);
        maxAttendeeText = findViewById(R.id.max_attendee_input_text);
        maxAttendeeContainer = findViewById(R.id.max_attendee_container);
        backButton = findViewById(R.id.back_button);
        startCalButton = findViewById(R.id.start_cal_button);
        endCalButton = findViewById(R.id.end_cal_button);
        addImageButton = findViewById(R.id.add_image_button);
        qrCreateButton = findViewById(R.id.qrcode_create_button);
        qrChooseSpinner = findViewById(R.id.qrcode_choose_spinner);
        createButton = findViewById(R.id.create_event_button);
        maxAttendeeSwitch = findViewById(R.id.max_attendee_switch);
    }

    protected void setFields(Event myEvent) {
        Log.d("Update event", "Setting the fields");
        titleText.setText(myEvent.getTitle());
        if (myEvent.getLocation() != null) {
            locationText.setText(myEvent.getLocation());
        }

        if (myEvent.getDescription() != null) {
            descriptionText.setText(myEvent.getDescription());
        }

        if (myEvent.getMaxAttendees() != null) {
            maxAttendeeSwitch.setChecked(true);
            maxAttendeeSwitchClick();
            maxAttendeeText.setText(myEvent.getMaxAttendees().toString());
        }

        // Setting QR Codes
        if (myEvent.getQrCode() != null) {
            qrCode = myEvent.getQrCode();
            Log.d("Create Event QR Code", "QR Code: " + myEvent.getQrCode().getEncodedStr());
        }

        if (myEvent.getQrCodePromo() != null) {
            qrCodePromo = myEvent.getQrCodePromo();
            Log.d("Create Event QR Code", "QR Code: " + myEvent.getQrCodePromo().getEncodedStr());
        }

        String posterStr = myEvent.getPosterStr();
        if (posterStr != null) {
            // Setting poster
            myEvent.assignPoster(posterImage);
        }

        // Setting dates
        startDateText.setText(myEvent.getStartDateStr());
        startTimeText.setText(myEvent.getStartTimeStr());
        endDateText.setText(myEvent.getEndDateStr());
        endTimeText.setText(myEvent.getEndTimeStr());

    }

    /**
     * Started when the Floating action button is pressed
     * Opens the android event to prompt the user to upload a photo from their device
     */
    void imageChooser() {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }

    /**
     * The result from the Intent return from the imageChooser() method
     * Gets the URI of the device's photo and converts it to a bitmap for display.
     * The URI is saved for upload to the database
     */
    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null
                            && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        posterImage.setImageBitmap(
                                selectedImageBitmap);
                        posterUri = selectedImageUri;
                    }
                }
            });

    /**
     * Starts the process of setting the date and time of either the start or end fields.
     * Once the date dialog picker is closed, this function opens the time dialog picker
     * and the date selected is assigned to the textview
     *
     * @param dateText Reference to the textview which will contain the date returned
     * @param timeText Reference to the textview that will contain the time returned
     */
    protected void openDateTimeDialog(TextView dateText, TextView timeText) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        int day = LocalDate.now().getDayOfMonth();

        // Opening the date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        String monthStr = String.format("%02d", monthOfYear + 1);
                        String dayStr = String.format("%02d", dayOfMonth);
                        dateText.setText(monthStr + "/" + dayStr + "/" + year);
                        openTimeDialog(timeText);
                    }
                },
                year, month - 1, day);
        datePickerDialog.show();
    }

    /**
     * Opens a time dialog picker and sets the text of the associated TextView after selection of time was made
     *
     * @param timeText Reference to the textview that will contain the time returned
     */
    protected void openTimeDialog(TextView timeText) {
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();

        // Opening the time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String hourStr = String.format("%02d", hour);
                        String minuteStr = String.format("%02d", minute);
                        timeText.setText(hourStr + ":" + minuteStr);
                    }
                },
                hour, minute, false);
        timePickerDialog.show();
    }

    /**
     * Handles the click of the switch button that enables the max attendee field to appear/disappear
     */
    protected void maxAttendeeSwitchClick() {
        boolean on = maxAttendeeSwitch.isChecked();
        if (on) {
            maxAttendeeContainer.setVisibility(View.VISIBLE);
        } else {
            maxAttendeeContainer.setVisibility(View.GONE);
        }
    }

    /**
     * When the QR Code create button is pressed, start the process of generating the QR Code.
     * The encoded string has the format [app:type:eventId] and the dimensions of the QR code come
     * from the size of the device.
     * Also creates the promotional QR code for later saving within the database
     */
    protected void qrCreateClick() {
        codeState = QRCodeState.CREATE;
        // Resetting the qrCodes in case they were chosen already before this button was clicked
        qrCode = null;
        qrCodePromo = null;
        makeToast("QR Code created!");
    }

    protected String[] QRCreate(String event) {
        String type = "checkin";

        // Creating the check in QR Code
        QRCodes qrCodeTemp = new QRCodes(event, type);

        // Creating the promotional QR Code
        QRCodes qrCodePromoTemp = new QRCodes(event, "promo");

        // Returning both recently created strings
        // If it's QR code choose though, the check in QR code is going to be different
        if (codeState == QRCodeState.CREATE) {
            return new String[]{qrCodeTemp.getEncodedStr(), qrCodePromoTemp.getEncodedStr()};
        }
        return new String[]{null, qrCodePromoTemp.getEncodedStr()};
    }

    /**
     * Starts the process of creating the event and uploading the data to firebase.
     * Data is validated, retrieved from UI elements, and prepared for firebase upload.
     * Once all of this is complete, calls a method to upload the poster to firebase
     * and post the rest of the information once this upload is complete.
     */
    protected void createButtonClick() {
        // Validating input
        if (!isValid()) {
            return;
        }

        // Getting the strings from all of the fields on the page
        // Setting text fields
        title = titleText.getText().toString();
        description = descriptionText.getText().toString();
        location = locationText.getText().toString();

        // Converting date to Date variable for easy passing to firebase
        String startDateStr = startDateText.getText().toString() + " " +
                startTimeText.getText().toString();
        String endDateStr = endDateText.getText().toString() + " " +
                endTimeText.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            startDate = dateFormat.parse(startDateStr);
            endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Checking whether the end date is after the start date
        if (startDate.after(endDate) == true) {
            makeToast("Start date cannot be after end date");
            return;
        }

        // Getting the firebase storage URL and saving it to a global variabl

        // Getting the maxAttendees field
        if (maxAttendeeSwitch.isChecked()) {
            maxAttendees = Integer.parseInt(maxAttendeeText.getText().toString());
        }

        newEvent = new Event(startDate, endDate, maxAttendees, organiserId, poster, null, qrCode, description, title, eventId, location);
        // Uploading the event to firebase
        firebaseEventUpload(newEvent);

    }

    /**
     * From the recently created event class, creates a new document in firebase and uploads all of the
     * data collected thus far
     *
     * @param event The class instance containing all of the input data
     */

    protected void firebaseEventUpload(Event event) {
        // Creating the map to set the data to the event
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("StartDateTime", event.getStartDateTime());
        eventMap.put("EndDateTime", event.getStartDateTime());
        eventMap.put("Description", event.getDescription());
        eventMap.put("Poster", storageUri);
        eventMap.put("Max Attendees", event.getMaxAttendees());
        eventMap.put("Organizer Id", event.getOrganiserId());
        eventMap.put("SignedUpUsers", new ArrayList<String>());
        eventMap.put("Title", event.getTitle());
        eventMap.put("QRCode", null);
        eventMap.put("QRCodePromo", null);
        eventMap.put("Location", event.getLocation());

        // Creating the document reference first so the document id can be retrieved later when uploading the photo
        // Case where we're creating the event for the first time
        DocumentReference docRef;
        if (eventId == null) {
            docRef = eventsRef.document();
        }
        // Case where we're in update
        else {
            docRef = eventsRef.document(eventId);
        }


        // Uploading to the document
        docRef.set(eventMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        // Add event to organiser array
                        db.collection("Users").document(event.getOrganiserId())
                                .update("CreatedEvents", FieldValue.arrayUnion(docRef.getId()));

                        // Case where the poster isn't submitted, return to previous activity
                        if (posterImage.getDrawable() == null) {
                            makeToast("Event successfully created!");

                            // Finishing saving the object by creating QR code and saving event ID
                            finishSaving(docRef);
                            returnPreviousActivity();
                        } else {
                            // Saving the poster image to the database
                            // https://stackoverflow.com/questions/40885860/how-to-save-bitmap-to-firebase
                            poster = ((BitmapDrawable) posterImage.getDrawable()).getBitmap();
                            finishSaving(docRef);
                            savePoster(docRef);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        makeToast("Failed to write to Firestore");
                    }
                });

    }

    protected void finishSaving(DocumentReference docRef) {
        // Case where both of the QR codes must be created
        // qrCode == null && qrCodePromo == null
        if (codeState == QRCodeState.CREATE) {
            // Adding the QR code to the document
            String[] qrCodeStrArr = QRCreate(docRef.getId());
            // Updating the QR code fields in the firebase document
            qrCode = new QRCodes(qrCodeStrArr[0]);
            qrCodePromo = new QRCodes(qrCodeStrArr[1]);
            docRef.update("QRCode", qrCodeStrArr[0]);
            docRef.update("QRCodePromo", qrCodeStrArr[1]);
        }
        // Case where the QR code was chosen, but the promo QR code still needs to be created
        // qrCode != null && qrCodePromo == null
        else if (codeState == QRCodeState.CHOOSE) {
            String[] qrCodeStrArr = QRCreate(docRef.getId());
            // Updating the QR code fields in the firebase document
            qrCodePromo = new QRCodes(qrCodeStrArr[1]);
            docRef.update("QRCode", qrCode.getEncodedStr());
            docRef.update("QRCodePromo", qrCodeStrArr[1]);
        }
        // Edit event case. Both the QR code and the promo QR code were fetched from firebase already
        //        else{
        //            docRef.update("QRCode", qrCode.getEncodedStr());
        //            docRef.update("QRCodePromo", qrCodePromo.getEncodedStr());
        //        }

        newEvent.setEventId(docRef.getId());
        newEvent.setQRCode(qrCode);
        newEvent.setQRCodePromo(qrCodePromo);
    }

    /**
     * Retrieves the reference of the photo on the device. Uploads the photo from this reference
     * to the database. Once this process is complete, calls the method to upload the rest of the
     * data to the database
     */
    protected void savePoster(DocumentReference docRef) {
        storageUri = "gs://hotevents-hotjava.appspot.com/Event Images/" + "poster_" + docRef.getId() + ".jpg";
        // Create a storage reference from our app
        StorageReference storageRef = sref.getReferenceFromUrl("gs://hotevents-hotjava.appspot.com/Event Images");

        // Create a reference to the photo
        StorageReference posterRef = storageRef.child("poster_" + docRef.getId() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        poster.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = posterRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.w(TAG, "Error writing poster to Firestore", exception);
                makeToast("Failed to write to Firestore");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Add to the newly created document
                docRef.update("Poster", storageUri);

                // Return to the previous activity
                makeToast("Event successfully created!");
                newEvent.setPosterStr(storageUri);
                returnPreviousActivity();
            }
        });
    }

    /**
     * Used to check for errors with any of the values input into the UI elements
     */
    protected Boolean isValid() {
        // Checking whether any of the fields are empty
        if (titleText.getText().toString().equals("") ||
                startDateText.getText().toString().equals("") ||
                endDateText.getText().toString().equals("") ||
                locationText.getText().toString().equals("") ||
                descriptionText.getText().toString().equals("")) {
            makeToast("Please ensure no fields are empty");
            return false;
        }

        // Checking whether MaxAttendee is empty even though it was checked
        if (maxAttendeeText.getText().toString().equals("") && maxAttendeeSwitch.isChecked() == true) {
            makeToast("Please ensure no fields are empty");
            return false;
        }

        // Checking whether the location is valid
        // Testing Geocode stuff
        //        Geocoder geocoder = new Geocoder(this);
        //        Log.d("Location Validation", locationText.getText().toString());
        //
        //        try {
        //            List<Address> addressList = geocoder.getFromLocationName(locationText.getText().toString(), 1);
        //            //Log.d("Location Validation", "Location: " + addressList.get(0).getAddressLine(0));
        //        } catch (IOException e) {
        //            Log.e("Location Validation", e.toString());
        //            makeToast("Please ensure the location is valid");
        //            return false;
        //        }

        return true;
    }

    /**
     * Generate the toast for saying what error with the input is
     *
     * @param errStr The string detailing the error to report to the user
     */
    protected void makeToast(String errStr) {
        Toast.makeText(getBaseContext(), errStr, Toast.LENGTH_LONG).show();
    }
}