package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;

import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class LandActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land);
        int display_mode = getResources().getConfiguration().orientation;
        if (display_mode == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            utils.appendText("land act PORTRAIT");
        }
        else {
            utils.appendText("land act LANDSCAPE");
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
            takeScreenShot(rootView);
        }
    }

    public void takeScreenShot(View view) {

        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                rootView.setDrawingCacheEnabled(true);
                utils.appendText("rootView made");

                File screenShot = utils.captureScreen(rootView);
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));

                    mActivity.finish();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    utils.appendText("Screenshot is NULL");
                }
            }
        }, 100);
    }

}
