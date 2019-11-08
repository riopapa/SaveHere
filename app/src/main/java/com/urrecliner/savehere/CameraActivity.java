package com.urrecliner.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static com.urrecliner.savehere.Vars.CameraMapBoth;
import static com.urrecliner.savehere.Vars.currActivity;
import static com.urrecliner.savehere.Vars.mActivity;
import static com.urrecliner.savehere.Vars.xPixel;
import static com.urrecliner.savehere.Vars.yPixel;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_camera);
        currActivity =  this.getClass().getSimpleName();
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else {
            xPixel = Resources.getSystem().getDisplayMetrics().widthPixels;     // 2094, 2960
            yPixel = Resources.getSystem().getDisplayMetrics().heightPixels;    // 1080, 1440

            BuildImage buildImage = new BuildImage();
            buildImage.makeOutMap();
            if (CameraMapBoth) {
                Intent intent = new Intent(getApplicationContext(), LandActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                mActivity.finishAffinity();
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }
}
