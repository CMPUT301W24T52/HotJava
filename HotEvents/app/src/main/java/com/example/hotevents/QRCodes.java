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

    //When QR Code is called, it automatically generates the QR Code based on the data that's been given
    //But in this case, how will validating the QR codes be a thing? Will probably just have to query a search of the database
    QRCodes(String eventId, String type, int dimensions) {
        this.eventId = eventId;
        this.type = type;
        this.encodedStr = app + ":" + type + ":" + eventId;
        generateQRCode(encodedStr, dimensions);
    }

    private void generateQRCode(String data, int dimensions){
        // encoder to generate our qr code.
        qrgEncoder = new QRGEncoder(data, null, Type.TEXT, dimensions);

        // getting our qrcode in the form of bitmap.
        qrgEncoder.setColorWhite(0xFFFFFFFF);
        qrgEncoder.setColorBlack(0xFF000000);
        bitmap = qrgEncoder.getBitmap();
    }

    //Used to pass the received data from scanning the QR code to the class to validate whether the data is correct
    //Will still have to workout how the scanning is going to work
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
