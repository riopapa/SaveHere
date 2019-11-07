package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import static com.urrecliner.andriod.savehere.Vars.CameraMapBoth;
import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.xPixel;
import static com.urrecliner.andriod.savehere.Vars.yPixel;

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
//            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//            int height = displayMetrics.heightPixels;
//            int width = displayMetrics.widthPixels;
//            s9    : h: 1080, w: 2094
//            nxs6p : h: 1440, w: 2392

//            TextView tV;
//            tV = findViewById(R.id.datetimeCText1); tV.setText(Vars.strDateTime);
//            tV = findViewById(R.id.datetimeCText2); tV.setText(Vars.strDateTime);
//            tV = findViewById(R.id.datetimeCText3); tV.setText(Vars.strDateTime);
//            tV = findViewById(R.id.datetimeCText4); tV.setText(Vars.strDateTime);
//            tV = findViewById(R.id.datetimeCText0); tV.setText(Vars.strDateTime);
//            tV = findViewById(R.id.placeCText1); tV.setText(Vars.strPlace);
//            tV = findViewById(R.id.placeCText2); tV.setText(Vars.strPlace);
//            tV = findViewById(R.id.placeCText3); tV.setText(Vars.strPlace);
//            tV = findViewById(R.id.placeCText4); tV.setText(Vars.strPlace);
//            tV = findViewById(R.id.placeCText0); tV.setText(Vars.strPlace);
//            tV = findViewById(R.id.address1); tV.setText(Vars.strAddress);
//            tV = findViewById(R.id.address2); tV.setText(Vars.strAddress);
//            tV = findViewById(R.id.address3); tV.setText(Vars.strAddress);
//            tV = findViewById(R.id.GPSText1); tV.setText(Vars.strPosition);
//            tV = findViewById(R.id.GPSText2); tV.setText(Vars.strPosition);
//            tV = findViewById(R.id.GPSText0); tV.setText(Vars.strPosition);

            xPixel = Resources.getSystem().getDisplayMetrics().widthPixels;     // 2094, 2960
            yPixel = Resources.getSystem().getDisplayMetrics().heightPixels;    // 1080, 1440
//            ImageView iV = findViewById(R.id.photoImage);
//            Bitmap bm = utils.getResizedBitmap(bitMapScreen, xPixel, yPixel);
//            iV.setImageBitmap(bm);
            View rootView = getWindow().getDecorView();
            takeCameraShot(rootView);
        }
    }

    public void takeCameraShot(View view) {


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

//        final View rootView = view;
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                rootView.setDrawingCacheEnabled(true);
//                File screenShot = utils.captureScreen(rootView, " ");
//                if (screenShot != null) {
//                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
//                    utils.setPhotoTag(screenShot);
//                    if (CameraMapBoth) {
//                        Intent intent = new Intent(getApplicationContext(), LandActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                    else {
//                        mActivity.finishAffinity();
//                        System.exit(0);
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                    }
//                } else {
//                    utils.appendText("Screenshot is NULL");
//                }
//            }
//        }, 800);
    }
}
