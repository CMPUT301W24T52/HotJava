package com.example.hotevents;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import androidmads.library.qrgenearator.QRGContents.Type;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * Stores information related to a QR code and associated methods
 */

public class QRCodes implements Serializable {
    private String eventId;
    private String type;
    private final String app = "hotevents";
    private String encodedStr;
    private QRGEncoder qrgEncoder;

    /**
     * Constructor for the QR Code class
     * This constructor takes the event that it belongs to and the type of the QR code,
     * converting it into the encoded string that will be used to generate the QR code.
     * @param eventId ID representing the ID in which the code is associated with
     * @param type Type is either checkin or promotional
     */

    QRCodes(String eventId, String type) {
        this.eventId = eventId;
        this.type = type;
        this.encodedStr = app + ":" + type + ":" + eventId;
    }

    /**
     * Used to generate the QR Code class when the encoded string has already been created.
     * This method is used when pulling an existing QR code string from the database.
     * Also sets the eventId and type variables based on the string
     * @param qrCodeStr The string used to encode the QR code
     */

    QRCodes(String qrCodeStr){
        this.encodedStr = qrCodeStr;

        String[] parts = encodedStr.split(":");
        this.eventId = parts[2];
        this.type = parts[1];
    }


    /**
     * Generates the QR code and returns the associated bitmap
     * @param data Full string to be encoded
     * @param dimensions Dimensions of the device to determine size of the bitmap
     */
    private Bitmap generateQRCode(String data, int dimensions) {
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(data, null, Type.TEXT, dimensions);

        return qrgEncoder.getBitmap(3);
    }

    /**
     * Used to compare the QR code scanner output with the correct QR code for the event
     *
     * @param output Output from QR code scanner
     * @return Boolean representing whether the QR code scanned was the correct one or not
     */
    public Boolean validateQRCode(String output) {
        return encodedStr.equals(output);
    }

    /**
     * Public method to access the QR Code bitmap. Calls the private method that encodes the QR code
     * @return QR code bitmap
     */
    public Bitmap getBitmap(){
        return generateQRCode(encodedStr, 512);
    }

    /**
     * Getter for the eventId variable
     * @return eventId
     */
    public String getEventId(){
        return eventId;
    }

    /**
     * Getter for the type variable
     * @return type
     */
    public String getType(){
        return type;
    }

    /**
     * Getter for the encodedStr
     * @return encodedStr
     */
    public String getEncodedStr(){
        return encodedStr;
    }

    /**
     * Shares the QR code bitmap and event URL
     *
     * @param context Context of the activity or application
     */
    public void shareQRCodeAndURL(Context context) {
        // Upload the QR code image to Firebase Cloud Storage
        uploadQRCodeToFirebase(context);
    }

    /**
     * Uploads the QR code image to Firebase Cloud Storage
     *
     * @param context Context of the activity or application
     */
    private void uploadQRCodeToFirebase(final Context context) {
        Bitmap bitmap = generateQRCode(encodedStr, 512);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + eventId + ".png");

        UploadTask uploadTask = qrCodeRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Toast.makeText(context, "Failed to upload QR code image", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> {
            // Task completed successfully
            qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL for the QR code image
                String downloadUrl = uri.toString();

                // Share the download URL
                shareUrl(context, downloadUrl);
            });
        });
    }

    /**
     * Shares the download URL of the QR code image
     *
     * @param context     Context of the activity or application
     * @param downloadUrl Download URL of the QR code image
     */
    private void shareUrl(Context context, String downloadUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, downloadUrl);
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
    }

}
