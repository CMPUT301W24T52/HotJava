package com.example.hotevents;

import static android.content.ContentValues.TAG;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class that handles the activity for creating and updating the events
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
    LinearLayout maxAttendeeContainer;

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
    String organiserId = "4a2d37f1b5970890"; //Default user for espresso testing
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
     * This method is started on creation of the activity. Assigns the views to variables,
     * assigns the click events for all of the buttons, grabs argument from main page intent,
     * and sets up the required firebase references
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_new);

        // Setting firebase instance
        db = FirebaseFirestore.getInstance();
        sref = FirebaseStorage.getInstance();
        eventsRef = db.collection("Events");

        // Initialize UI elements
        titleText = findViewById(R.id.title_text);
        startDateText = findViewById(R.id.start_date_text);
        startTimeText = findViewById(R.id.start_time_text);
        endDateText = findViewById(R.id.end_date_text);
        endTimeText = findViewById(R.id.end_time_text);
        locationText = findViewById(R.id.location_input_text);
        descriptionText = findViewById(R.id.description_input_text);
        posterImage = findViewById(R.id.poster_image);
        qrChooseSpinner = findViewById(R.id.qrcode_choose_spinner);
        createButton = findViewById(R.id.create_event_button);
        maxAttendeeSwitch = findViewById(R.id.max_attendee_switch);
        maxAttendeeContainer = findViewById(R.id.max_attendee_container);
        backButton = findViewById(R.id.backButton);
        addImageButton = findViewById(R.id.add_image_button);
        maxAttendeeText = findViewById(R.id.max_attendee_input_text);
        qrCreateButton = findViewById(R.id.qrcode_create_button);


        startDateText.setFocusable(false);
        startTimeText.setFocusable(false);
        endDateText.setFocusable(false);
        endTimeText.setFocusable(false);
        // Set listeners
        startDateText.setOnClickListener(v -> openDateTimeDialog(startDateText, startTimeText));
        endDateText.setOnClickListener(v -> openDateTimeDialog(endDateText, endTimeText));
        createButton.setOnClickListener(v -> createButtonClick());
        maxAttendeeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> maxAttendeeSwitchClick());

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
        String temp = myIntent.getStringExtra("organiser");
        if (temp != null){
            organiserId = temp;
        }

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




        // Location input events
        // https://developers.google.com/codelabs/maps-platform/location-places-android#4
        // https://www.geeksforgeeks.org/how-to-implement-google-map-inside-fragment-in-android/



        qrCreateButton.setOnClickListener(v -> {
            qrCreateClick();
        });

        //Querying the user's QR codes from previously created events and adding them to the
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




        Log.d(TAG, "made it here");

    }

    /**
     * Returns to the previous activity when event is created or back button is pressed
     * Different logic based on whether the CreateEventActivity was accessed from EventDetailsActivity
     * or MainActivity
     */
    protected void returnPreviousActivity() {
        if (activityState == ActivityState.UPDATE) {
            Intent myIntent = new Intent();

            // Returning different events depending on whether we pressed the back button or completed the event update
            if (newEvent == null) {
                myIntent.putExtra("event", (Parcelable) updateEvent);
            } else {
                myIntent.putExtra("event", (Parcelable) newEvent);
            }
            setResult(Activity.RESULT_OK, myIntent);
        }
        finish();
    }

    /**
     * Querying the Created Events array from the user and creating the spinner list based on
     * the QR codes that can be retrieved
     * Reference: https://stackoverflow.com/questions/13377361/how-to-create-a-drop-down-list
     * @param createdEvents
     */
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

    /**
     * Retrieving the database user data based on the organiser ID/device ID
     */
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
                        Log.e("Getting User Data", e.toString());
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
        backButton = findViewById(R.id.backButton);
        startCalButton = findViewById(R.id.start_cal_button);
        endCalButton = findViewById(R.id.end_cal_button);
        addImageButton = findViewById(R.id.add_image_button);
        qrCreateButton = findViewById(R.id.qrcode_create_button);
        qrChooseSpinner = findViewById(R.id.qrcode_choose_spinner);
        createButton = findViewById(R.id.create_event_button);
        maxAttendeeSwitch = findViewById(R.id.max_attendee_switch);
    }

    /**
     * Triggers when user tries to update the event from EventDetailsActivity
     * Takes the passed in event to be updated and populates the UI fields with the data
     * @param myEvent The event to be updated, passed in by EventDetailsActivity
     */
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
     * @param dateText Reference to the textview which will contain the date returned
     * @param timeText Reference to the textview that will contain the time returned
     */
    protected void openDateTimeDialog(TextView dateText, TextView timeText) {
        // Create a MaterialDatePicker for date selection
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Date");
        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert the selected date to a formatted string
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            String selectedDate = sdf.format(new Date(selection + TimeUnit.DAYS.toMillis(1)));
            dateText.setText(selectedDate);
            openTimeDialog(timeText);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }


    /**
     * Opens a time dialog picker and sets the text of the associated TextView after selection of time was made
     * @param timeText Reference to the textview that will contain the time returned
     */
    protected void openTimeDialog(TextView timeText) {
        // Create a MaterialTimePicker for time selection
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
        builder.setTitleText("Select Time");
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
        MaterialTimePicker timePicker = builder.build();

        timePicker.addOnPositiveButtonClickListener(view -> {
            // Convert the selected time to a formatted string
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
            timeText.setText(selectedTime);
        });

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }



    /**
     * Handles the click of the switch button that enables the max attendee field to appear/disappear
     */
    protected void maxAttendeeSwitchClick() {
        boolean on = maxAttendeeSwitch.isChecked();
        if (on) {
            maxAttendeeText.setVisibility(View.VISIBLE);
        } else {
            maxAttendeeText.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the QRCodeState enum to the CREATE state
     * Ensures that when the event gets created, the QR code gets created as well
     * Since the QR Code string includes the event ID that gets randomly generated by Firebase,
     * the actual creation of the code must be pushed until after the event gets created.
     */
    protected void qrCreateClick() {
        codeState = QRCodeState.CREATE;
        // Resetting the qrCodes in case they were chosen already before this button was clicked
        qrCode = null;
        qrCodePromo = null;
        makeToast("QR Code created!");
    }

    /**
     * Creates the checkin and promo QR Codes based on the event ID that gets passed in as an argument
     * @param event Event ID of recently generated event
     * @return Strings of both QR codes to be saved on firebase
     */
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
        eventMap.put("EndDateTime", event.getEndDateTime());
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

    /**
     * Now that the event is created, create the QR codes or store the chosen QR Code
     * This happens last due to needed the eventID, however this event ID gets created by firebase
     * @param docRef DocumentReference of event in Firebase
     */
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

        return true;
    }

    /**
     * Method to generate toasts based on a string passed in
     *
     * @param errStr The string detailing the error to report to the user
     */
    protected void makeToast(String errStr) {
        Toast.makeText(getBaseContext(), errStr, Toast.LENGTH_LONG).show();
    }
}