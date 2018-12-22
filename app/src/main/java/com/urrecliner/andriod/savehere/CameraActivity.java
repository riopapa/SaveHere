package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.strPlace;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_land);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        makeFullScreen();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextView mPlaceTextView = findViewById(R.id.placeText);
        mPlaceTextView.setText(Vars.strPlace);
        TextView mAddressTextView = findViewById(R.id.addressText);
        mAddressTextView.setText(Vars.strAddress);
        TextView mPositionTextView = findViewById(R.id.positionText);
        mPositionTextView.setText(Vars.strPosition);
        TextView mDateTimeTextView = findViewById(R.id.datetimeText);
        mDateTimeTextView.setText(Vars.strDateTime);
        takeScreenShot();
    }

//    private void makeFullScreen() {
//
//        View decorView = getWindow().getDecorView();
// Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
//    }

    private void takeScreenShot() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                View rootView = getWindow().getDecorView();
                rootView.setDrawingCacheEnabled(true);

                File screenShot = captureScreen(rootView);
                if (screenShot != null) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));

                    finish();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    Toast.makeText(getApplicationContext(), "\nScreenshot is NULL\n", Toast.LENGTH_LONG).show();
                }
            }
        }, 10);
    }

    public File captureScreen(View view) {

        Bitmap screenBitmap = view.getDrawingCache();
//        Bitmap screenRotated = rotateImage(screenBitmap, 90);

        String filename;
        String buildId = Build.ID;
        switch (buildId) {
            case "R16NW":  // galuxy s9+ ?
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
            Toast.makeText(getApplicationContext(), "\nScreenshot ERROR\n", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR to print " + e, Toast.LENGTH_LONG).show();
            return null;
        }
        return file;
    }

//    public void launchPhotoApp() {
//
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivity(cameraIntent);
//    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);
    }
    static SimpleDateFormat imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
    private String getIMGTimeText() {
        return imgDateFormat.format(new Date());
    }

}
