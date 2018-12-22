package com.urrecliner.andriod.savehere;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.utils;

public class LandActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils.appendText("land act onCreate");
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
        View rootView = getWindow().getDecorView();
        utils.takeScreenShot(rootView);
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
