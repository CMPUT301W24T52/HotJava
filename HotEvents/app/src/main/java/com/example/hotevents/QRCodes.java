package com.example.hotevents;

import android.graphics.Bitmap;

import androidmads.library.qrgenearator.QRGContents.Type;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodes {
    private String eventId;
    private String type;
    private final String app = "hotjava";
    private Bitmap bitmap;
    private String encodedStr;
    private QRGEncoder qrgEncoder;

    /**
     * Constructor for the QR Codes class
     * Also runs the method to generate the QR Code
     * @param eventId Unique event ID
     * @param type Either [checkin] or [promotional], will define how the system handles the QR Code
     * @param dimensions dimensions of the screen to determine the Bitmap size of the QR Code
     */
    QRCodes(String eventId, String type, int dimensions) {
        this.eventId = eventId;
        this.type = type;
        this.encodedStr = app + ":" + type + ":" + eventId;
        generateQRCode(encodedStr, dimensions);
    }

    /**
     * Creates an instance of the QRGEncoded class based on the string to be encoded and the dimensions
     * of the device
     * @param data Full string to be encoded
     * @param dimensions Dimensions of the device to determine size of the bitmap
     */
    private void generateQRCode(String data, int dimensions){
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(data, null, Type.TEXT, dimensions);

        // getting our qrcode in the form of bitmap.
        qrgEncoder.setColorWhite(0xFFFFFFFF);
        qrgEncoder.setColorBlack(0xFF000000);
        bitmap = qrgEncoder.getBitmap();
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
        return bitmap;
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

}
