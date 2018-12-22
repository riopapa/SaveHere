package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
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

import static com.urrecliner.andriod.savehere.Vars.doubleRun;
import static com.urrecliner.andriod.savehere.Vars.isRUNNING;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class Utils {

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.ENGLISH);
    final SimpleDateFormat timeLogFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.ENGLISH);
    int appendCount = 0;

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
            String outText = "\n" + timeLogFormat.format(new Date()) + " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber() + " [[" + textLine + "]]\n";
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

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
    }
    static SimpleDateFormat imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
    public String getIMGTimeText() {
        return imgDateFormat.format(new Date());
    }

    public void takeScreenShot(View view) {

        if (doubleRun)
            return;
        doubleRun = true;
        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                rootView.setDrawingCacheEnabled(true);
                utils.appendText("rootView made");

                File screenShot = captureScreen(rootView);
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));

                    mActivity.finish();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    appendText("Screenshot is NULL");
                }
            }
        }, 10);
    }

    private File captureScreen(View view) {

        Bitmap screenBitmap = view.getDrawingCache();
//        Bitmap screenRotated = rotateImage(screenBitmap, 90);

        String filename;
        String buildId = Build.ID;
        switch (buildId) {
            case "R16NW":  // galaxy s9+
                filename = getIMGTimeText() + "_" + strPlace + ".PNG";
                break;
            case "NMF26F":
                filename = "IMG_" + getIMGTimeText() + "_" + "lenovo" + strPlace + ".PNG";
                break;
            default:
                filename = "IMG_" + getIMGTimeText() + "_" + buildId + strPlace + ".PNG";
        }
        File directory = getPublicAlbumStorageDir("/Camera");
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
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();

        } catch (IOException e) {
            appendText("Screenshot ioException");
            return null;
        }
        return file;
    }
}
