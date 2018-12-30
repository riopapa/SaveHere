package com.urrecliner.andriod.savehere;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import static com.urrecliner.andriod.savehere.Vars.delayValue;
import static com.urrecliner.andriod.savehere.Vars.latitude;
import static com.urrecliner.andriod.savehere.Vars.longitude;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.mMap;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.utils;
import static com.urrecliner.andriod.savehere.Vars.zoomValue;

public class LandActivity extends AppCompatActivity implements OnMapReadyCallback {

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
        utils.appendText("land #3333");
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

        LatLng here = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
        mMap.addMarker(new MarkerOptions().position(here)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_here)));

//        mapScaleView.setColor(@ColorInt int color)
//        mapScaleView.setTextSize(float textSize)
//        mapScaleView.setStrokeWidth(float strokeWidth)

// enable/disable white outline
//        mapScaleView.setOutlineEnabled(boolean enabled)
//
//        mapScaleView.metersAndMiles() // default
//        mapScaleView.milesOnly()

//        final MapScaleView scaleMapView;
//        scaleMapView = findViewById(R.id.scaleView);
//        CameraPosition cp = mMap.getCameraPosition();
//        scaleMapView.metersOnly();
//        mMap.setMinZoomPreference(zoomValue);
        mMap.setMaxZoomPreference(zoomValue);
        mMap.setMinZoomPreference(zoomValue);

//        scaleMapView.update(cp.zoom, cp.target.latitude);

// expand scale bar from right to left
//        mapScaleView.setExpandRtlEnabled(true);

        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                drawScale(zoomValue);
//                Log.e("#", "onSnapshotReady");
                ImageView scaleImageView = findViewById(R.id.scaleImage);
                Bitmap scaleMap = createBitmapFromView (scaleImageView);
//                utils.bitMap2File(scaleMap,"scale");
                Bitmap mergedMap = mergeTwoBitmaps(snapshot, scaleMap);
//                utils.bitMap2File(mergedMap,"merged");
                ImageView mapImageView = findViewById(R.id.mapImage);
                mapImageView.setImageBitmap(mergedMap);
//                Log.e("#", "google mapped");
                View rootView = getWindow().getDecorView();
                takeScreenShot(rootView);
            }
        };
        int delayTime = (20 - zoomValue) * 200 + delayValue;
//        Toast.makeText(getApplicationContext(),"zoom: " + zoomValue + ", delay: " + delayTime,Toast.LENGTH_LONG).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mMap.snapshot(callback);
            }
        }, delayTime);
    }

    private Bitmap mergeTwoBitmaps(Bitmap firstImage, Bitmap secondImage){

        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage, firstImage.getWidth() - 240, firstImage.getHeight() - 100, null);
        return result;
    }
    private Bitmap createBitmapFromView(View v) {
//        utils.appendText("view > map");
        v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(),
                v.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return bitmap;
    }
//    private void drawScale(Bitmap gMap) {
//        Bitmap dMap = gMap;
//        ImageView iV = findViewById(R.id.mapImage);
//        Canvas canvas = new Canvas (dMap);
//        float scale = getResources().getDisplayMetrics().density;
//        Paint p = new Paint();
//        p.setColor(Color.BLACK);
//        p.setTextSize(24*scale);
//        canvas.drawText("Hello", iV.getWidth()/2,iV.getHeight()/2,p);
//        iV.setImageBitmap(dMap);
//    }


    private void drawScale (int zoom) {
        Paint paint = new Paint();
                                //      20,      19,     18,    17,     16,     15,     14,     13,     12,     11,     10,     9
        final int xPixels[] =   {  0,   113,	 113,    90,    90,		90,	    113,	113,	113,	90,	    90,	    90,     113, };
        final String xUnits[] = {  "",  "10 m",	"20 m", "50 m", "100 m","200 m","500 m","1 Km",	"2 Km",	"5 Km", "10 Km","20 Km","50 Km"};
        ImageView sIV = findViewById(R.id.scaleImage);
//        int vHeight = 80;
//        int vWidth = 200;
//        utils.appendText("H: " + vHeight + " W: "+ vWidth);
        Bitmap bitmap = Bitmap.createBitmap(200, 80, Bitmap.Config.ARGB_8888);
        int baseX, baseY, startX, startY, stopX, stopY, yPixel;
        Canvas canvas = new Canvas(bitmap);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5f);
        int xPixel = (int) ((float) (xPixels[20 - zoom]) * 1.4f);
        String xUnit = xUnits[20 - zoom];
        yPixel = 10;
        baseX = 15; baseY = 60;
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
        sIV.setImageBitmap(bitmap);
        sIV.invalidate();
    }

    public void takeScreenShot(View view) {

        final View rootView = view;

        Log.e("take","shot");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
//                utils.appendText("rootView made");
                Log.e("take1","shot");
                strPlace += " ";
                File screenShot = utils.captureScreen(rootView, "");
                if (screenShot != null) {
                    mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
//                    utils.appendText(("All Task completed"));
                    mActivity.finishAffinity();
//                    ActivityCompat.finishAffinity(mActivity);
//            System.runFinalizersOnExit(true);
                    System.exit(0);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } else {
                    utils.appendText("Screenshot is NULL");
                }
            }
        }, 300);
    }

}
