package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.ActivityCompat;
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
import static com.urrecliner.andriod.savehere.Vars.zoomValue;

public class LandActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        utils.appendText("land #1");
        setContentView(R.layout.activity_land);
//        utils.appendText("land #2");
        currActivity =  this.getClass().getSimpleName();
        int screenOrientation = getResources().getConfiguration().orientation;
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            utils.appendText("land act PORTRAIT");
            return;
        }
//        utils.appendText("land #3333");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        utils.appendText("land act LANDSCAPE");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextView mPlaceTextView = findViewById(R.id.placeText);
        mPlaceTextView.setText(Vars.strPlace);
        TextView mAddressTextView = findViewById(R.id.addressText);
        mAddressTextView.setText(Vars.strAddress);
        TextView mDTV = findViewById(R.id.datetimeText);
        mDTV.setText(Vars.strDateTime);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LatLng here = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,zoomValue));
        mMap.addMarker(new MarkerOptions().position(here)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_here)));
        mMap.setOnMapLoadedCallback(this);  // wait till all map is displayed
   }

    @Override
    public void onMapLoaded() {     // if map is displayed then try snapshot
        if (mMap != null) {
            mMap.snapshot(callback);
        }
    }

    GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
        @Override
        public void onSnapshotReady(Bitmap snapshot) {
//            utils.appendText("callback started");
            Bitmap scaleMap = drawScale(zoomValue);
            Bitmap mergedMap = mergeTwoBitmaps(snapshot, scaleMap);
//                utils.bitMap2File(mergedMap,"merged");
            ImageView mapImageView = findViewById(R.id.mapImage);
            mapImageView.setImageBitmap(mergedMap);
//                Log.e("#", "google mapped");
            View rootView = getWindow().getDecorView();
            takeScreenShot(rootView);
        }
    };

    private Bitmap mergeTwoBitmaps(Bitmap firstImage, Bitmap secondImage){

        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage, firstImage.getWidth() - 360, firstImage.getHeight() - 100, null);
        return result;
    }

//    private Bitmap createBitmapFromView(View v) {
//        v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT));
//        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
//        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
//                v.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//
//        Canvas c = new Canvas(bitmap);
//        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//        v.draw(c);
//        return bitmap;
//    }

    private Bitmap drawScale (int zoom) {
        Paint paint = new Paint();
                                //      20,      19,     18,    17,     16,     15,     14,     13,     12,     11,     10,     9
        final int xPixels[] =   {  0,   113,	 113,    90,    90,		90,	    113,	113,	113,	90,	    90,	    90,     113, };
        final String xUnits[] = {  "",  "10 m",	"20 m", "50 m", "100 m","200 m","500 m","1 Km",	"2 Km",	"5 Km", "10 Km","20 Km","50 Km"};
        Bitmap bitmap = Bitmap.createBitmap(200, 80, Bitmap.Config.ARGB_8888);
        int baseX, baseY, startX, startY, stopX, stopY, yPixel;
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5f);
        int xPixel = (int) ((float) (xPixels[20 - zoom]) * 1.2f);
        String xUnit = xUnits[20 - zoom];
        yPixel = 10; baseX = 15; baseY = 60;
        startX = baseX; startY = baseY; stopX = baseX + xPixel; stopY = baseY;
        canvas.drawLine(startX, startY, stopX, stopY, paint);       // ____
        startX = baseX; startY = baseY + 5; stopX = startX ; stopY = baseY - yPixel;
        canvas.drawLine(startX, startY, stopX, stopY, paint);       // |_
        startX = baseX + xPixel; startY = baseY + 5; stopX = startX; stopY = baseY - yPixel;
        canvas.drawLine(startX, startY, stopX, stopY, paint);       //    _|
        paint.setTextSize(32);
        paint.setStrokeWidth(10f);
        paint.setColor(Color.BLACK);
        startX = baseX + xPixel / 4; startY = baseY - 20;
        canvas.drawText(xUnit, startX, startY, paint);
        return bitmap;
    }

    public void takeScreenShot(View view) {

        final View rootView = view;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
//                utils.appendText("rootView made");
                strPlace += " ";
                File screenShot = utils.captureScreen(rootView, "");
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
//                    utils.appendText(("All Task completed"));
                    mActivity.finishAffinity();
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    utils.appendText("Screenshot is NULL");
                }
            }
        }, 300);
    }

}
