package com.urrecliner.savehere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.savehere.Vars.CameraMapBoth;
import static com.urrecliner.savehere.Vars.mActivity;

public class CameraActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        String logID = "cameraAct";
        BuildBitMap buildBitMap = new BuildBitMap();
        buildBitMap.makeOutMap();
        if (CameraMapBoth) {
            Intent intent = new Intent(getApplicationContext(), LandActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    mActivity.finishAffinity();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 500);    // allow some delay to finish write file
        }
    }
}
