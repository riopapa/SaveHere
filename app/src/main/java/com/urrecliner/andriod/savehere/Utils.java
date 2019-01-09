package com.urrecliner.andriod.savehere;

import android.graphics.Bitmap;
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
import static com.urrecliner.andriod.savehere.Vars.phoneModel;
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

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
    }

    static SimpleDateFormat imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
    public String getIMGTimeText() {
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
        if (phoneModel.equals(galaxyS9)) {
            width -= 240;
            screenBitmap = Bitmap.createBitmap(screenBitmap, 0, 0, width, screenBitmap.getHeight());
        }
//        appendText("width : " + width);
//        Bitmap screenRotated = rotateImage(screenBitmap, 90);
        File file = bitMap2File (screenBitmap, tag);
        appendText("Screen Captured.. " + file.getName());
        return file;
    }

    public File bitMap2File (Bitmap bitmap, String tag) {
        String filename;
        if (phoneModel.equals(galaxyS9)) {
            filename = getIMGTimeText() + "_" + strPlace + tag + ".PNG";
        } else {
            filename = "IMG_" + getIMGTimeText() + "_"  + strPlace + tag + ".PNG";
        }
        File directory = utils.getPublicAlbumStorageDir("/Camera");
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);   //비트맵을 PNG파일로 변환
            os.close();
        } catch (IOException e) {
            utils.appendText("Screenshot ioException");
            return null;
        }
        return file;
    }
}
