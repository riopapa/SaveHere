package com.urrecliner.savehere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.savehere.Vars.bitMapCamera;
import static com.urrecliner.savehere.Vars.cameraOrientation;
import static com.urrecliner.savehere.Vars.latitude;
import static com.urrecliner.savehere.Vars.longitude;
import static com.urrecliner.savehere.Vars.mActivity;
import static com.urrecliner.savehere.Vars.mContext;
import static com.urrecliner.savehere.Vars.nowTime;
import static com.urrecliner.savehere.Vars.outFileName;
import static com.urrecliner.savehere.Vars.phoneMaker;
import static com.urrecliner.savehere.Vars.phoneModel;
import static com.urrecliner.savehere.Vars.phonePrefix;
import static com.urrecliner.savehere.Vars.signatureMap;
import static com.urrecliner.savehere.Vars.strAddress;
import static com.urrecliner.savehere.Vars.strPlace;
import static com.urrecliner.savehere.Vars.strPosition;
import static com.urrecliner.savehere.Vars.utils;

class BuildBitMap {

//    private String logID = "buildCameraImage";

    void makeOutMap() {

        String timeStamp;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("`yy/MM/dd HH:mm", Locale.ENGLISH);
        timeStamp =  dateTimeFormat.format(nowTime);
        int width = bitMapCamera.getWidth();
        int height = bitMapCamera.getHeight();

        if (cameraOrientation == 6 && width > height)
            bitMapCamera = utils.rotateBitMap(bitMapCamera, 90);
        if (cameraOrientation == 1 && width < height)
            bitMapCamera = utils.rotateBitMap(bitMapCamera, 90);
        if (cameraOrientation == 3)
            bitMapCamera = utils.rotateBitMap(bitMapCamera, 180);

//        width = bitMapCamera.getWidth();
//        height = bitMapCamera.getHeight();
//        utils.log(logID, "before Merge "+width+" x "+height+" orientation "+cameraOrientation+" ooooooo");
        Bitmap mergedMap = markDateLocSignature(bitMapCamera, timeStamp);

        File newFile = new File(utils.getPublicCameraDirectory(), phonePrefix + outFileName + " _ha.jpg");
        writeCameraFile(mergedMap, newFile);
        setNewFileExif(newFile);
    }

    static final private SimpleDateFormat sdfHourMinSec = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
//    static final private SimpleDateFormat sdfHourMin = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);

    private void setNewFileExif(File fileHa) {
        ExifInterface exifHa;
        try {
            exifHa = new ExifInterface(fileHa.getAbsolutePath());
            exifHa.setAttribute(ExifInterface.TAG_MAKE, phoneMaker);
            exifHa.setAttribute(ExifInterface.TAG_MODEL, phoneModel);
            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertGPS(latitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRefGPS(latitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertGPS(longitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRefGPS(longitude));
            exifHa.setAttribute(ExifInterface.TAG_ORIENTATION, "1");
            exifHa.setAttribute(ExifInterface.TAG_DATETIME,sdfHourMinSec.format(nowTime));
            exifHa.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "by riopapa");
            exifHa.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        private String latitudeRefGPS(double latitude) {
            return latitude<0.0d?"S":"N";
        }
        private String longitudeRefGPS(double longitude) {
            return longitude<0.0d?"W":"E";
        }

        private static String convertGPS(double latitude) {
            latitude = Math.abs(latitude);
            int degree = (int) latitude;
            latitude *= 60;
            latitude -= (degree * 60.0d);
            int minute = (int) latitude;
            latitude *= 60;
            latitude -= (minute * 60.0d);
            int second = (int) (latitude*10000.d);
            return degree+"/1,"+minute+"/1,"+second+"/10000";
        }

    private Bitmap markDateLocSignature(Bitmap photoMap, String dateTime) {

        int width = photoMap.getWidth();
        int height = photoMap.getHeight();
        Bitmap newMap = Bitmap.createBitmap(width, height, photoMap.getConfig());
        Canvas canvas = new Canvas(newMap);
        canvas.drawBitmap(photoMap, 0f, 0f, null);
        int fontSize = (cameraOrientation == 1) ? height/16 : width/16;
        int xPos = (cameraOrientation == 1) ? width/5 : width*3/10;
        int yPos = height/12;
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos);

        int sigSize = (width + height) / 14;
        Bitmap sigMap = Bitmap.createScaledBitmap(signatureMap, sigSize, sigSize, false);
        xPos = width - sigSize - width/20;
        yPos = height/20;
        canvas.drawBitmap(sigMap, xPos, yPos, null);

        if (strPlace.length() == 0) strPlace = " ";
        xPos = width / 2;
        fontSize = (height + width) / 60;  // gps
        yPos = height - fontSize - fontSize / 5;
        drawTextOnCanvas(canvas, strPosition, fontSize, xPos, yPos);
        fontSize = fontSize * 12 / 10;  // address
        yPos -= fontSize + fontSize / 5;
        drawTextOnCanvas(canvas, strAddress, fontSize, xPos, yPos);
        fontSize = fontSize * 14 / 10;  // Place
        yPos -= fontSize + fontSize / 5;
        drawTextOnCanvas(canvas, strPlace, fontSize, xPos, yPos);
        return newMap;
    }

    private void drawTextOnCanvas(Canvas canvas, String text, int fontSize, int xPos, int yPos) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(mContext.getResources().getFont(R.font.nanumbarungothic));
        int d = fontSize / 16;
        canvas.drawText(text, xPos-d, yPos-d, paint);
        canvas.drawText(text, xPos+d, yPos-d, paint);
        canvas.drawText(text, xPos-d, yPos+d, paint);
        canvas.drawText(text, xPos+d, yPos+d, paint);
        canvas.drawText(text, xPos-d, yPos, paint);
        canvas.drawText(text, xPos+d, yPos, paint);
        canvas.drawText(text, xPos, yPos-d, paint);
        canvas.drawText(text, xPos, yPos+d, paint);
        paint.setColor(ContextCompat.getColor(mContext, R.color.foreColor));
        canvas.drawText(text, xPos, yPos, paint);
    }

    private void writeCameraFile(Bitmap bitmap, File file) {
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
            mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (IOException e) {
            Log.e("ioException", e.toString());
            Toast.makeText(mContext, e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    static Bitmap buildSignatureMap() {
        Bitmap sigMap;
        File sigFile = new File (Environment.getExternalStorageDirectory(),"signature.png");
        if (sigFile.exists()) {
            sigMap = BitmapFactory.decodeFile(sigFile.toString(), null);
        }
        else
            sigMap = BitmapFactory.decodeResource(mContext.getResources(), R.raw.signature);
        Bitmap newBitmap = Bitmap.createBitmap(sigMap.getWidth(), sigMap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(120);
        canvas.drawBitmap(sigMap, 0, 0, alphaPaint);
        return newBitmap;
    }
}
