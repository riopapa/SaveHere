package com.urrecliner.andriod.savehere;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.galaxyS9;
import static com.urrecliner.andriod.savehere.Vars.isRUNNING;
import static com.urrecliner.andriod.savehere.Vars.latitude;
import static com.urrecliner.andriod.savehere.Vars.longitude;
import static com.urrecliner.andriod.savehere.Vars.mainContext;
import static com.urrecliner.andriod.savehere.Vars.phoneMake;
import static com.urrecliner.andriod.savehere.Vars.phoneModel;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class Utils {

    final String PREFIX = "log_";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.ENGLISH);
    private final SimpleDateFormat timeLogFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.ENGLISH);
    private final SimpleDateFormat jpegTimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
    private int appendCount = 0;

    public void appendText(String textLine) {
        if (isRUNNING)
            return;
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
            File file = new File(directory, PREFIX + dateFormat.format(new Date())+".txt");
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
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,(longitude > 0) ? "E":"W");
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "Created by riopapa");
            exif.setAttribute(ExifInterface.TAG_MAKE, phoneMake);
            exif.setAttribute(ExifInterface.TAG_MODEL, phoneModel);
            exif.setAttribute(ExifInterface.TAG_DATETIME, jpegTimeFormat.format(new Date()));
            exif.saveAttributes();
        } catch (IOException e) {
            utils.appendText("EXIF ioException\n"+e.toString());
        }
    }

    String utf2euc(String str) {
        Charset utf8charset = Charset.forName("UTF-8");
        Charset eucKr = Charset.forName("euc-kr");
        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());
        CharBuffer data = utf8charset.decode(inputBuffer);
        ByteBuffer outputBuffer = eucKr.encode(data);
        CharBuffer data2 = eucKr.decode(inputBuffer);
        ByteBuffer outputBuffer2 = utf8charset.encode(data);
        String x = str + ", "+data.toString()+" , "+data2.toString();
        Log.w("code",x);
        return x;
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

    void deleteOldFiles() {

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
        File[] files = fullPath.listFiles();
        return files;
    }

}
