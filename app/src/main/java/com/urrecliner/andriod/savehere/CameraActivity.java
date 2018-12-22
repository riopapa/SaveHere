package com.urrecliner.andriod.savehere;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import static com.urrecliner.andriod.savehere.Vars.utils;

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
        View rootView = getWindow().getDecorView();
        utils.takeScreenShot(rootView);
    }
}
