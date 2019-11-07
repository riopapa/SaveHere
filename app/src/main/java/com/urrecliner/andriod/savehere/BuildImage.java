package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.bitMapScreen;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.mainContext;
import static com.urrecliner.andriod.savehere.Vars.outFileName;
import static com.urrecliner.andriod.savehere.Vars.phoneMake;
import static com.urrecliner.andriod.savehere.Vars.phoneModel;
import static com.urrecliner.andriod.savehere.Vars.strAddress;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.strPosition;
import static com.urrecliner.andriod.savehere.Vars.utils;


public class BuildImage {

    String timeStamp;

    void makeOutMap() {

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("`yy/MM/dd HH:mm", Locale.ENGLISH);
        timeStamp =  dateTimeFormat.format(new Date());

        Bitmap mergedMap = addSignature2Bitmaps(bitMapScreen, timeStamp);

        File newFile = new File(utils.getPublicCameraDirectory(), outFileName + "_ha.jpg");
        bitMap2File(mergedMap, newFile);
        setNewFileExif(newFile);
    }

    static final private SimpleDateFormat sdfHourMinSec = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
    static final private SimpleDateFormat sdfHourMin = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.ENGLISH);

    private void setNewFileExif(File fileHa) {
        ExifInterface exifHa;
        try {
            exifHa = new ExifInterface(fileHa.getAbsolutePath());
            exifHa.setAttribute(ExifInterface.TAG_MAKE, phoneMake);
            exifHa.setAttribute(ExifInterface.TAG_MODEL, phoneModel);
//            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, exifOrg.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
//            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, exifOrg.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
//            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, exifOrg.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
//            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE, exifOrg.getAttribute(ExifInterface.TAG_GPS_LATITUDE));

            exifHa.setAttribute(ExifInterface.TAG_ORIENTATION, "1");
            exifHa.setAttribute(ExifInterface.TAG_DATETIME,sdfHourMinSec.format(new Date()));
            exifHa.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "by riopapa");
            exifHa.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap addSignature2Bitmaps(Bitmap photoMap, String dateTime) {

        int width = photoMap.getWidth();
        int height = photoMap.getHeight();
        boolean isBright;
        Bitmap newMap = Bitmap.createBitmap(width, height, photoMap.getConfig());
        Canvas canvas = new Canvas(newMap);
        canvas.drawBitmap(photoMap, 0f, 0f, null);
        int fontSize = width/27;
        int xPos = width/5;
        int yPos = height/12;
        isBright = checkBright(photoMap, xPos, yPos);
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos, false, isBright);

        int sigSize = (width > height) ? height/6 : width/4;
        Bitmap sigMap = BitmapFactory.decodeResource(mainContext.getResources(), R.raw.signature_yellow_min);
        sigMap = Bitmap.createScaledBitmap(sigMap, sigSize, sigSize, false);
        xPos = width - sigSize - width/20;
        yPos = height/20;
        Paint sigPaint = new Paint();
        sigPaint.setAlpha(60);
        canvas.drawBitmap(sigMap, xPos, yPos, sigPaint);

        if (strPlace.length() == 0) strPlace = " ";
        fontSize = (width+height)/36;
        xPos = width/2;
        yPos = height - height/24 - fontSize - fontSize;
        isBright = checkBright(photoMap, xPos, yPos);
        drawTextOnCanvas(canvas, strPlace, fontSize, xPos, yPos, true, isBright);
        yPos += fontSize;
        fontSize = width/32;
        drawTextOnCanvas(canvas, strAddress, fontSize, xPos, yPos,false, isBright);
        yPos += fontSize;
        fontSize = width/40;
        drawTextOnCanvas(canvas, strPosition, fontSize, xPos, yPos,false, isBright);
        return newMap;
    }

    private void drawTextOnCanvas(Canvas canvas, String text, int fontSize, int xPos, int yPos, boolean wide, boolean isBright) {
        Paint paint = new Paint();
        paint.setColor(isBright ? Color.YELLOW:Color.BLACK);

//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        paint.setTextSize(fontSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        if (wide) {
            canvas.drawText(text, xPos-4, yPos-4, paint);
            canvas.drawText(text, xPos+4, yPos-4, paint);
            canvas.drawText(text, xPos-4, yPos+4, paint);
            canvas.drawText(text, xPos+4, yPos+4, paint);
            canvas.drawText(text, xPos+6, yPos+6, paint);
        } else {
            canvas.drawText(text, xPos-2, yPos-2, paint);
            canvas.drawText(text, xPos+2, yPos-2, paint);
            canvas.drawText(text, xPos-2, yPos+2, paint);
            canvas.drawText(text, xPos+2, yPos+2, paint);
            canvas.drawText(text, xPos+4, yPos+4, paint);
        }
        paint.setColor(isBright ? Color.BLACK:Color.YELLOW);
        canvas.drawText(text, xPos, yPos, paint);
    }

    private boolean checkBright(Bitmap bitmap, int xPos, int yPos) {
        int brightness = 0;
        final int xMax = 120;
        final int yMax = 80;
        for (int x = -xMax; x < xMax; x+=4) {
            for (int y = -yMax; y < yMax; y+=4) {
                int color = bitmap.getPixel(xPos+x, yPos+y);
                int R = color & 0xff0000; int G = color & 0x00ff00; int B = color & 0xff;
                if (R > 0x6f0000 && G > 0x6f00 && B > 0x6f)
                    brightness++;
            }
        }
        return brightness >= (xMax/2) * (yMax/2) / 2;
    }

    private void bitMap2File(Bitmap bitmap, File file) {
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
            mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (IOException e) {
            Log.e("ioException", e.toString());
            Toast.makeText(mainContext, e.toString(),Toast.LENGTH_LONG).show();
        }
    }

}
