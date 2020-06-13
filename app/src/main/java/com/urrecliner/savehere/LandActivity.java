package com.urrecliner.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

import static com.urrecliner.savehere.Vars.cameraOrientation;
import static com.urrecliner.savehere.Vars.latitude;
import static com.urrecliner.savehere.Vars.longitude;
import static com.urrecliner.savehere.Vars.mActivity;
import static com.urrecliner.savehere.Vars.mGoogleMap;
import static com.urrecliner.savehere.Vars.terrain;
import static com.urrecliner.savehere.Vars.utils;
import static com.urrecliner.savehere.Vars.zoomValue;

public class LandActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private String logID = "LandAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_land);
       if (cameraOrientation == 1 || cameraOrientation == 3) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextView tV = findViewById(R.id.placeText1); tV.setText(Vars.strPlace);
        tV = findViewById(R.id.addressText1); tV.setText(Vars.strAddress);
        tV = findViewById(R.id.datetimeText1); tV.setText(Vars.strDateTime);
        tV = findViewById(R.id.GPSText1); tV.setText(Vars.strPosition);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        LatLng here = new LatLng(latitude, longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,zoomValue));
        mGoogleMap.addMarker(new MarkerOptions().position(here)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_face_marker)));
        int mapType = (terrain)? GoogleMap.MAP_TYPE_TERRAIN : GoogleMap.MAP_TYPE_NORMAL;
        mGoogleMap.setMapType(mapType);  // 지형 포함
//        mGoogleMap.setTrafficEnabled(false);
        mGoogleMap.setOnMapLoadedCallback(this);  // wait till all map is displayed
   }

    @Override
    public void onMapLoaded() {     // if map is displayed then try snapshot
        while (mGoogleMap == null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                }
            }, 300);        // delay till all views are displayed
        }
        mGoogleMap.snapshot(callback);
    }

    GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            Bitmap scaleMap = getScaleMap(zoomValue);
            Bitmap mergedMap = mergeScaleBitmap(snapshot, scaleMap);
            ImageView mapImageView = findViewById(R.id.mapImage);
            mapImageView.setImageBitmap(mergedMap);
//            utils.log(logID, "callback rootview ///");
            View rootView = getWindow().getDecorView();
            takeScreenShot(rootView);
        }
    };

    private Bitmap mergeScaleBitmap(Bitmap mapImage, Bitmap scaleMap){

        Canvas canvas = new Canvas(mapImage);
        Paint paint = new Paint();
        canvas.drawBitmap(mapImage, 0, 0, paint);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        int xPos = mapImage.getWidth() - mapImage.getWidth()/15 - scaleMap.getWidth();
        int yPos = mapImage.getHeight() - mapImage.getHeight()/20 - scaleMap.getHeight();
        canvas.drawBitmap(scaleMap, xPos, yPos, paint);
        return mapImage;
    }


    private Bitmap getScaleMap(int zoom) {
                                //      20,      19,     18,    17,     16,     15,     14,     13,     12,     11,     10,     9
        final int [] xWidths =   {  0,   113,	 113,    90,    90,		90,	    113,	113,	113,	90,	    90,	    90,     113, };
        final String [] xUnits = {  "",  "10 m",	"20 m", "50 m", "100 m","200 m","500 m","1 Km",	"2 Km",	"5 Km", "10 Km","20 Km","50 Km"};
        Bitmap bitmap = Bitmap.createBitmap(300, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int xWidth = (int) ((float) (xWidths[20 - zoom]) * 2.0f);   // if snapshot resolution changed 2.0f should be changed also
        int baseX = 10; int baseY = 60; int yHeight = 15;
        int []xPos = {baseX, baseX        , baseX+xWidth     , baseX+xWidth, baseX+(xWidth/2)     , baseX };
        int []yPos = {baseY, baseY-yHeight, baseY-yHeight    , baseY       , baseY-yHeight        , baseY };
        Path path = new Path();
        path.moveTo(baseX, baseY);
        for (int i = 0; i < xPos.length; i++) {
            path.lineTo(xPos[i], yPos[i]);
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(3f);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);

        paint.setTextSize(36);
        paint.setStrokeWidth(20f);
        paint.setColor(Color.BLACK);
        String xUnit = xUnits[20 - zoom];
        canvas.drawText(xUnit, baseX+ 20, baseY - yHeight - 10, paint);
        return bitmap;
    }

    public void takeScreenShot(View view) {

//        utils.log(logID, "callback rootview ///");

        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                File screenShot = utils.captureMapScreen(rootView);
                if (screenShot != null) {
                    utils.setPhotoTag(screenShot);
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                    mActivity.finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                } else {
                    utils.logE(logID,"Screenshot is NULL");
                }
            }
        }, 300);
    }
}
