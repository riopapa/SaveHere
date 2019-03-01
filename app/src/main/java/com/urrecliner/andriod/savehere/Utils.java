package com.urrecliner.andriod.savehere;

import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.galaxyS9;
import static com.urrecliner.andriod.savehere.Vars.isRUNNING;
import static com.urrecliner.andriod.savehere.Vars.latitude;
import static com.urrecliner.andriod.savehere.Vars.longitude;
import static com.urrecliner.andriod.savehere.Vars.phoneModel;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class Utils {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.ENGLISH);
    private final SimpleDateFormat timeLogFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.ENGLISH);
    private int appendCount = 0;

    public void appendText(String textLine) {
        if (isRUNNING)
            return;
        File directory = new File(Environment.getExternalStorageDirectory(), "SaveHere");
        try {
            if (!directory.exists()) {
                boolean result = directory.mkdirs();
                Log.e("Directory",  directory.toString() + " created " + result);
            }
        } catch (Exception e) {
            Log.e("Directory", "Create error " + directory.toString() + "_" + e.toString());
        }

        File directoryDate = new File(directory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                if (directoryDate.mkdirs())
                    Log.e("Directory", directoryDate.toString() + " created ");
            }
        } catch (Exception e) {
            Log.e("creating Folder error", directoryDate + "_" + e.toString());
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directoryDate.toString() + "/" + "save_here.txt";

        try {
            File file = new File(fullName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("createFile", " Error");
                }
            }
            StackTraceElement[] traces;
            traces = Thread.currentThread().getStackTrace();
            String outText = timeLogFormat.format(new Date()) + " " + currActivity + " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber() + " [[" + textLine + "]]\n";
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);
            Log.w("append " + appendCount++, outText);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getPublicCameraDirectory() {
        // Get the directory for the user's public pictures directory.
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),"/Camera");
    }

    static SimpleDateFormat imgDateFormat;

    static {
        imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
    }

    private String getIMGTimeText() {
        return imgDateFormat.format(new Date());
    }

    public File captureScreen(View view, String tag) {

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap screenBitmap = view.getDrawingCache();
        if (screenBitmap == null) {
            Log.e("screen"," bitmap null");
        }
        int width = screenBitmap.getWidth();
        int height = screenBitmap.getHeight();
        if (phoneModel.equals(galaxyS9)) {
            screenBitmap = Bitmap.createBitmap(screenBitmap, 200, 0, width-400, height);
        }
        File file = bitMap2File (screenBitmap, tag);
//        appendText("Screen Captured.. " + file.getName());
        return file;
    }

    public File bitMap2File (Bitmap bitmap, String tag) {
        String filename;
        if (phoneModel.equals(galaxyS9)) {
            filename = getIMGTimeText() + "_" + strPlace + tag + ".jpg";
        } else {
            filename = "IMG_" + getIMGTimeText() + "_"  + strPlace + tag + ".jpg";
        }
        File directory = getPublicCameraDirectory();
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } catch (Exception e) {
            Log.w("creating file error", e.toString());
        }
        File file = new File(directory, filename);
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        } catch (IOException e) {
            utils.appendText("Create ioException\n"+e);
            return null;
        }
        Log.w("photo","file created");
        return file;
    }

    void setPhotoTag(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,convertGpsToDMS(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,(latitude > 0) ? "N":"S");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertGpsToDMS(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,(longitude > 0) ? "W":"E");
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "riopapa");
            exif.saveAttributes();
        } catch (IOException e) {
            utils.appendText("EXIF ioException\n"+e);
        }
    }

    synchronized private static String convertGpsToDMS(double latitude) {
        StringBuilder sb = new StringBuilder(20);
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000");
        return sb.toString();
    }
}
