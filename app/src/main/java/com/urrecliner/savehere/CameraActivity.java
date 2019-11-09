package com.urrecliner.savehere;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static com.urrecliner.savehere.Vars.CameraMapBoth;
import static com.urrecliner.savehere.Vars.bitMapCamera;
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
        int width = bitMapCamera.getWidth();
        int height = bitMapCamera.getHeight();
        xPixel = Resources.getSystem().getDisplayMetrics().widthPixels;     // 2094, 2960
        yPixel = Resources.getSystem().getDisplayMetrics().heightPixels;    // 1080, 1440
        if (width < height) {
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            bitMapCamera = Bitmap.createBitmap(bitMapCamera, 0, 0, width, height, matrix, false);
            int temp = xPixel; xPixel = yPixel; yPixel = temp;
        }

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
