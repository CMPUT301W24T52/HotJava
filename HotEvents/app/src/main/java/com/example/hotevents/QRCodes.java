package com.example.hotevents;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

import androidmads.library.qrgenearator.QRGContents.Type;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodes implements Serializable {
    private String eventId;
    private String type;
    private final String app = "hotevents";
    private String encodedStr;
    private QRGEncoder qrgEncoder;

    /**
     * Constructor for the QR Codes class
     * Also runs the method to generate the QR Code
     * @param eventId Unique event ID
     * @param type Either [checkin] or [promotional], will define how the system handles the QR Code
     * @param dimensions dimensions of the screen to determine the Bitmap size of the QR Code
     */

    QRCodes(String eventId, String type){
        this.eventId = eventId;
        this.type = type;
        this.encodedStr = app + ":" + type + ":" + eventId;
    }

    QRCodes(String qrCodeStr){
        this.encodedStr = qrCodeStr;

        String[] parts = encodedStr.split(":");
        this.eventId = parts[2];
        this.type = parts[1];
    }


    /**
     * Creates an instance of the QRGEncoded class based on the string to be encoded and the dimensions
     * of the device
     * @param data Full string to be encoded
     * @param dimensions Dimensions of the device to determine size of the bitmap
     */
    private Bitmap generateQRCode(String data, int dimensions){
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(data, null, Type.TEXT, dimensions);

        // getting our qrcode in the form of bitmap.
        qrgEncoder.setColorWhite(0xFFFFFFFF);
        qrgEncoder.setColorBlack(0xFF000000);
        return qrgEncoder.getBitmap();
    }

    /**
     * Used to compare the QR code scanner output with the correct QR code for the event
     * @param output Output from QR code scanner
     * @return Boolean representing whether the QR code scanned was the correct one or not
     */
    public Boolean validateQRCode(String output){
        return encodedStr.equals(output);
    }

    public Bitmap getBitmap(){
        return generateQRCode(encodedStr, 512);
    }

    public String getEventId(){
        return eventId;
    }

    public String getType(){
        return type;
    }

    public String getEncodedStr(){
        return encodedStr;
    }

    /**
     * Shares the QR code bitmap and event URL
     * @param context Context of the activity or application
     */
    public void shareQRCodeAndURL(Context context) {
        // Upload the QR code image to Firebase Cloud Storage
        uploadQRCodeToFirebase(context);
    }

    /**
     * Uploads the QR code image to Firebase Cloud Storage
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
     * @param context Context of the activity or application
     * @param downloadUrl Download URL of the QR code image
     */
    private void shareUrl(Context context, String downloadUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, downloadUrl);
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
    }

}
