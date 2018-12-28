package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.latitude;
import static com.urrecliner.andriod.savehere.Vars.longitude;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.mMap;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.utils;

public class LandActivity extends AppCompatActivity  implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land);
        currActivity =  this.getClass().getSimpleName();
        int screenOrientation = getResources().getConfiguration().orientation;
        utils.appendText("Orientation is " + screenOrientation);
        if (mMap == null) {
            utils.appendText("mMap is null");
        }
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            utils.appendText("land act PORTRAIT");
        }
        else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            utils.appendText("land act LANDSCAPE");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            TextView mPlaceTextView = findViewById(R.id.placeText);
            mPlaceTextView.setText(Vars.strPlace);
            TextView mAddressTextView = findViewById(R.id.addressText);
            mAddressTextView.setText(Vars.strAddress);
            TextView mDateTimeTextView = findViewById(R.id.datetimeText);
            mDateTimeTextView.setText(Vars.strDateTime);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng here = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
        mMap.setMinZoomPreference(18.0f);
        mMap.setMinZoomPreference(18.0f);
        mMap.addMarker(new MarkerOptions().position(here)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_here)));

        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                ImageView iV = findViewById(R.id.mapImage);
                iV.setImageBitmap(snapshot);
                View rootView = getWindow().getDecorView();
                takeScreenShot(rootView);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                utils.appendText("map snapshot");
                mMap.snapshot(callback);
            }
        }, 500);

    }

    public void takeScreenShot(View view) {

        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                utils.appendText("rootView made");
                strPlace += " ";
                File screenShot = utils.captureScreen(rootView);
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                    utils.appendText(("All Task completed"));
                    mActivity.finishAffinity();
//                    ActivityCompat.finishAffinity(mActivity);
//            System.runFinalizersOnExit(true);
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    utils.appendText("Screenshot is NULL");
                }
            }
        }, 200);
    }

}
