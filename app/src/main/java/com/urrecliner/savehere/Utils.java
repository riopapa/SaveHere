package com.urrecliner.savehere;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.savehere.Vars.currActivity;
import static com.urrecliner.savehere.Vars.latitude;
import static com.urrecliner.savehere.Vars.longitude;
import static com.urrecliner.savehere.Vars.mainContext;
import static com.urrecliner.savehere.Vars.nexus6P;
import static com.urrecliner.savehere.Vars.nowTime;
import static com.urrecliner.savehere.Vars.outFileName;
import static com.urrecliner.savehere.Vars.phoneMake;
import static com.urrecliner.savehere.Vars.phoneModel;
import static com.urrecliner.savehere.Vars.strPlace;
import static com.urrecliner.savehere.Vars.utils;
import static com.urrecliner.savehere.Vars.xPixel;
import static com.urrecliner.savehere.Vars.yPixel;

class Utils {

    final private String PREFIX = "log_";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.ENGLISH);
    private final SimpleDateFormat timeLogFormat = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.ENGLISH);
    private final SimpleDateFormat jpegTimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
    private int appendCount = 0;
    void appendText(String textLine) {
        File directory = getPackageDirectory();
        try {
            if (!directory.exists()) {
                boolean result = directory.mkdirs();
                Log.e("Directory",  directory.toString() + " created " + result);
            }
        } catch (Exception e) {
            Log.e("Directory", "Create error " + directory.toString() + "_" + e.toString());
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(directory, PREFIX + dateFormat.format(nowTime)+".txt");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("createFile", " Error");
                }
            }
            StackTraceElement[] traces;
            traces = Thread.currentThread().getStackTrace();
            String outText = timeLogFormat.format(nowTime) + " " + currActivity + " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber() + " [[" + textLine + "]]\n";
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);

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

    File getPublicCameraDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),"/Camera");
    }

    File captureScreen(View view, String tag) {

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap screenBitmap = view.getDrawingCache();
        assert screenBitmap != null;

//        bitMap2File (screenBitmap, tag+" a");
//        int width = screenBitmap.getWidth();
//        int height = screenBitmap.getHeight();
//        float ratio = (float) width / (float) height;
//        Log.w("size", screenBitmap.getWidth()+" x "+ screenBitmap.getHeight()+" before");
//        if (ratio > 1.5f) {
//            int width2 = (int) ((float) height * 1.5f);
//            screenBitmap = Bitmap.createBitmap(screenBitmap, (width - width2) / 2, 0, width2, height);

//            screenBitmap = getResizedBitmap(screenBitmap, (width * 80) / 100 , height);
//        }
//        Log.w("size", screenBitmap.getWidth()+" x "+ screenBitmap.getHeight()+" after");
        return bitMap2File(screenBitmap, tag);
    }

    private File bitMap2File (Bitmap bitmap, String tag) {

//        final SimpleDateFormat imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
        Bitmap outMap = Bitmap.createBitmap(bitmap, 0, 0, xPixel, yPixel);  // remove right actiobar white area
        outMap = getResizedBitmap(outMap, xPixel*85/100, yPixel);
//        String filename = imgDateFormat.format(nowTime) + "_" + strPlace + tag + "_ha.jpg";
        String filename = outFileName + "_" + tag + "_ha.jpg";
        if (phoneModel.equals(nexus6P))
            filename = "IMG_" + filename;
        File directory = getPublicCameraDirectory();
        try {
            if (!directory.exists()) {
                boolean mkdirs = directory.mkdirs();
                Log.w("mkdirs", ""+mkdirs);
            }
        } catch (Exception e) {
            Log.w("creating file error", e.toString());
        }
        File file = new File(directory, filename);
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            outMap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        } catch (IOException e) {
            utils.appendText("Create ioException\n"+e);
            return null;
        }
        return file;
    }

    void setPhotoTag(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,convertGpsToDMS(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,(latitude > 0) ? "N":"S");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertGpsToDMS(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,(longitude > 0) ? "E":"W");
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "Created by riopapa");
            exif.setAttribute(ExifInterface.TAG_MAKE, phoneMake);
            exif.setAttribute(ExifInterface.TAG_MODEL, phoneModel);
            exif.setAttribute(ExifInterface.TAG_DATETIME, jpegTimeFormat.format(nowTime));
            exif.saveAttributes();
        } catch (IOException e) {
            utils.appendText("EXIF ioException\n"+e.toString());
        }
    }

    synchronized private static String convertGpsToDMS(double latitude) {
        StringBuilder sb = new StringBuilder(30);
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        float second = (float) latitude;

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1");
        return sb.toString();
    }

    void deleteOldLogFiles() {

        String oldDate = PREFIX + dateFormat.format(System.currentTimeMillis() - 3*24*60*60*1000L);
        File packageDirectory = getPackageDirectory();
        File[] files = getFilesList(packageDirectory);
        Collator myCollator = Collator.getInstance();
        for (File file : files) {
            String shortFileName = file.getName();
            if (myCollator.compare(shortFileName, oldDate) < 0) {
                if (file.delete())
                    Log.e("file","Delete Error "+file);
            }
        }
    }

    private  String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    private File getPackageDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(), getAppLabel(mainContext));
        try {
            if (!directory.exists()) {
                if(directory.mkdirs()) {
                    Log.e("mkdirs","Failed "+directory);
                }
            }
        } catch (Exception e) {
            Log.e("creating Directory error", directory.toString() + "_" + e.toString());
        }
        return directory;
    }

    private File[] getFilesList(File fullPath) {
        return fullPath.listFiles();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
        return resizedBitmap;
    }
}
