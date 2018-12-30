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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import static com.urrecliner.andriod.savehere.Vars.CameraMapBoth;
import static com.urrecliner.andriod.savehere.Vars.bitMapScreen;
import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        currActivity =  this.getClass().getSimpleName();
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            utils.appendText("camera PORTRAIT");
        }
        else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            TextView pV = findViewById(R.id.placeCText1);
            pV.setText(Vars.strPlace);
            pV = findViewById(R.id.placeCText2);
            pV.setText(Vars.strPlace);
            pV = findViewById(R.id.placeCText3);
            pV.setText(Vars.strPlace);
            TextView pA = findViewById(R.id.placeAddress);
            String text = Vars.strPlace + "\n\n" + Vars.strAddress;
            pA.setText(text);
            TextView mPV = findViewById(R.id.positionCText);
            mPV.setText(Vars.strPosition);
            TextView mDV = findViewById(R.id.datetimeCText);
            mDV.setText(Vars.strDateTime);

            ImageView iV = findViewById(R.id.photoImage);
            //        imageView.setImageBitmap(BitmapFactory.decodeFile(tempPNGName));
            iV.setImageBitmap(bitMapScreen);
            //        ViewGroup vg = findViewById (R.id.cameraLayout);
            //        vg.invalidate();
            View rootView = getWindow().getDecorView();
            //        rootView.setVisibility(View.GONE);
            //        rootView.setVisibility(View.VISIBLE);

            takeScreenShot(rootView);
        }
    }

    public void takeScreenShot(View view) {

        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                rootView.setDrawingCacheEnabled(true);
//                utils.appendText("rootView made");

                File screenShot = utils.captureScreen(rootView, "");
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
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
                } else {
                    utils.appendText("Screenshot is NULL");
                }
            }
        }, 100);
    }
}
