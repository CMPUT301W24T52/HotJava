package com.example.hotevents;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import java.util.Random;

/**
 *Page for creating and updating an event
 * TODO:
 * - Set up choose QR button
 * - Fix location picker from the map
 * - Set up the update event components
 */

public class CreateEventActivity extends AppCompatActivity {
    //Firebase objects
    FirebaseFirestore db;
    FirebaseStorage sref;
    CollectionReference eventsRef;

    //Defining UI objects
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
    Button qrChooseButton;
    Button createButton;
    Switch maxAttendeeSwitch;
    View maxAttendeeContainer;

    //Defining variables used in creating the event
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


    /**
     * Started on creation of the activity. Assigns the views to variables, assigns the click events
     * for all of the buttons, grabs argument from main page intent, and sets up the required
     * firebase references
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //Setting firebase instance
        db = FirebaseFirestore.getInstance();
        sref = FirebaseStorage.getInstance();
        eventsRef = db.collection("Events");

        //Creating the random eventId
        eventId = generateRandomStr();

        //Calling a function that sets the UI elements to variables
        setViews();

        //Getting the arguments from the Intent
        Intent myIntent = getIntent();
        organiserId = myIntent.getStringExtra("organiser");

        backButton.setOnClickListener(v-> {
            returnPreviousActivity();
        });

        //Add Image button listener
        //https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/
        //https://stackoverflow.com/questions/29588124/how-to-add-an-image-to-the-emulator-gallery-in-android-studio
        addImageButton.setOnClickListener(v -> {
            //Open image dialog window
            imageChooser();
        });

        //Date input events
        //https://www.geeksforgeeks.org/how-to-popup-datepicker-while-clicking-on-edittext-in-android/
        //https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
        //https://developer.android.com/reference/android/app/DatePickerDialog
        //https://developer.android.com/reference/android/app/TimePickerDialog
        startCalButton.setOnClickListener(v -> {
            //Opening the date picker and time picker dialogs and saving them to the start texts
            openDateTimeDialog(startDateText, startTimeText);
        });

        endCalButton.setOnClickListener(v -> {
            //Opening the date picker and time picker dialogs and saving them to the end texts
            openDateTimeDialog(endDateText, endTimeText);
        });

        //Location input events
        //https://developers.google.com/codelabs/maps-platform/location-places-android#4
        //https://www.geeksforgeeks.org/how-to-implement-google-map-inside-fragment-in-android/
        mapButton.setOnClickListener(v -> {
//            Intent i = new Intent(CreateEvent.this,MapsActivity.class);
//            startActivity(i);
        });

        maxAttendeeSwitch.setOnClickListener(v->{
            maxAttendeeSwitchClick();
        });

        qrCreateButton.setOnClickListener(v-> {
            qrCreateClick();
        });

        qrChooseButton.setOnClickListener(v-> {
            qrChooseClick();
        });

        createButton.setOnClickListener(v->{
            createButtonClick();
        });
    }

    /**
     * Returns to the previous activity when event is created or back button is pressed
     * TODO
     * - Currently only returns back to MainActivity. Will be fixed once edit functionality is corrected
     */
    protected void returnPreviousActivity(){
        Intent myIntent = new Intent(CreateEventActivity.this, MainActivity.class);
        startActivity(myIntent);
    }

    /**
     * Generates a random string to get a different event id for each created event
     * Reference: <a href="https://www.baeldung.com/java-random-string">...</a>
     * @return the random string
     */
    protected String generateRandomStr(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
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
        mapButton = findViewById(R.id.map_button);
        qrCreateButton = findViewById(R.id.qrcode_create_button);
        qrChooseButton = findViewById(R.id.qrcode_choose_button);
        createButton = findViewById(R.id.create_event_button);
        maxAttendeeSwitch = findViewById(R.id.max_attendee_switch);
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
                        }
                        catch (IOException e) {
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
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        int day = LocalDate.now().getDayOfMonth();

        //Opening the date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        String monthStr = String.format("%02d", monthOfYear);
                        String dayStr = String.format("%02d", dayOfMonth);
                        dateText.setText(monthStr + "/" + dayStr + "/" + year);
                        openTimeDialog(timeText);
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    /**
     * Opens a time dialog picker and sets the text of the associated TextView after selection of time was made
     * @param timeText Reference to the textview that will contain the time returned
     */
    protected void openTimeDialog(TextView timeText) {
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();

        //Opening the time picker dialog
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
    protected void maxAttendeeSwitchClick(){
        boolean on = maxAttendeeSwitch.isChecked();
        if (on){
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
    protected void qrCreateClick(){
        String type = "checkin";

        //https://www.geeksforgeeks.org/how-to-generate-qr-code-in-android/
        //Need to generate dimensions for the QR code bitmap
        // below line is for getting
        // the windowmanager service.
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // initializing a variable for default display.
        Display display = manager.getDefaultDisplay();

        // creating a variable for point which
        // is to be displayed in QR Code.
        Point point = new Point();
        display.getSize(point);

        // getting width and
        // height of a point
        int width = point.x;
        int height = point.y;

        // generating dimension from width and height.
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        //Creating the check in QR Code
        qrCode = new QRCodes(eventId, type, dimen);

        //Creating the promotional QR Code
        qrCodePromo = new QRCodes(eventId, "promo", dimen);

        makeToast("QR Code with data [" + qrCode.getEncodedStr() + "] created!");
    }

    /**
     * QR choose button click
     * Enables the user to select from their previously generated QR codes and selected one for the current task
     * TODO
     * - Add functionality
     */
    protected void qrChooseClick(){
        //Code to open the QR Code scanner
//        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
//        intentIntegrator.setPrompt("Scan a barcode or QR Code");
//        intentIntegrator.setOrientationLocked(false);
//        intentIntegrator.initiateScan();
    }

    /**
     * Starts the process of creating the event and uploading the data to firebase.
     * Data is validated, retrieved from UI elements, and prepared for firebase upload.
     * Once all of this is complete, calls a method to upload the poster to firebase
     * and post the rest of the information once this upload is complete.
     */
    protected void createButtonClick() {
        //Validating input
        if (!isValid()) {
            return;
        }

        //Getting the strings from all of the fields on the page
        //Setting text fields
        title = titleText.getText().toString();
        description = descriptionText.getText().toString();
        location = locationText.getText().toString();

        //Converting date to Date variable for easy passing to firebase
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

        //Checking whether the end date is after the start date
        if (startDate.after(endDate) == true) {
            makeToast("Start date cannot be after end date");
            return;
        }

        //Getting the firebase storage URL and saving it to a global variabl

        //Getting the maxAttendees field
        if (maxAttendeeSwitch.isChecked()) {
            maxAttendees = Integer.parseInt(maxAttendeeText.getText().toString());
        }

        Event event = new Event(startDate, endDate, maxAttendees, organiserId, poster, null, qrCode, description, title, eventId, location);
        //Uploading the event to firebase
        firebaseEventUpload(event);

    }

    /**
     * From the recently created event class, creates a new document in firebase and uploads all of the
     * data collected thus far
     * @param event The class instance containing all of the input data
     */

    protected void firebaseEventUpload(Event event){
        //Creating the map to set the data to the event
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("StartDateTime", event.getStartDateTime());
        eventMap.put("EndDateTime", event.getStartDateTime());
        eventMap.put("Description", event.getDescription());
        eventMap.put("Poster", storageUri);
        eventMap.put("Max Attendees", event.getMaxAttendees());
        eventMap.put("Organizer Id", event.getOrganiserId());
        eventMap.put("SignedUpUsers", new ArrayList<String>());
        eventMap.put("Title", event.getTitle());
        eventMap.put("QRCode", event.getQrCode().getEncodedStr());
        eventMap.put("QRCodePromo", qrCodePromo.getEncodedStr());
        eventMap.put("Location", event.getLocation());

        //Creating the document reference first so the document id can be retrieved later when uploading the photo
        DocumentReference docRef = eventsRef.document();

        //Uploading to the document
        docRef.set(eventMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        //Add event to organiser array
                        db.collection("Users").document(event.getOrganiserId())
                                .update("CreatedEvents", FieldValue.arrayUnion(docRef.getId()));

                        //Case where the poster isn't submitted, return to previous activity
                        if (posterImage.getDrawable() == null){
                            makeToast("Event successfully created!");
                            returnPreviousActivity();
                        }
                        else {
                            //Saving the poster image to the database
                            //https://stackoverflow.com/questions/40885860/how-to-save-bitmap-to-firebase
                            poster = ((BitmapDrawable) posterImage.getDrawable()).getBitmap();
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
     * Retrieves the reference of the photo on the device. Uploads the photo from this reference
     * to the database. Once this process is complete, calls the method to upload the rest of the
     * data to the database
     */
    protected void savePoster(DocumentReference docRef){
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
                //Add to the newly created document
                docRef.update("Poster", storageUri);

                //Return to the previous activity
                makeToast("Event successfully created!");
                returnPreviousActivity();
            }
        });
    }

    /**
     * Used to check for errors with any of the values input into the UI elements
     */
    protected Boolean isValid() {
        //Checking whether any of the fields are empty
        if (titleText.getText().toString().equals("") ||
                startDateText.getText().toString().equals("") ||
                endDateText.getText().toString().equals("") ||
                locationText.getText().toString().equals("") ||
                descriptionText.getText().toString().equals("")) {
            makeToast("Please ensure no fields are empty");
            return false;
        }

        //Checking whether MaxAttendee is empty even though it was checked
        if (maxAttendeeText.getText().toString().equals("") && maxAttendeeSwitch.isChecked() == true){
            makeToast("Please ensure no fields are empty");
            return false;
        }

        //Checking whether a QR code was generated
        if (qrCode == null){
            makeToast("Must generate a QR code for the event");
            return false;
        }

        return true;
    }

    /**
     * Generate the toast for saying what error with the input is
     * @param errStr The string detailing the error to report to the user
     */
    protected void makeToast(String errStr){
        Toast.makeText(getBaseContext(), errStr, Toast.LENGTH_LONG).show();
    }

    /**
     * On return from the Scan QR Code IntentIntegrator after a code has been scanned or the
     * activity has been cancelled.
     * Not necessary for this activity, will be implemented on the main page and the check in button
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                //messageText.setText(intentResult.getContents());
                //messageFormat.setText(intentResult.getFormatName());
                Toast.makeText(getBaseContext(), intentResult.getContents() + ":" + intentResult.getFormatName(), Toast.LENGTH_SHORT).show();
                //Here is where we would validate the text to check whether the QR code was scanned successfully
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}