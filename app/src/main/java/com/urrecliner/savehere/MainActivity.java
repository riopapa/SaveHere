package com.urrecliner.savehere;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.savehere.Vars.CameraMapBoth;
import static com.urrecliner.savehere.Vars.bitMapCamera;
import static com.urrecliner.savehere.Vars.currActivity;
import static com.urrecliner.savehere.Vars.isTimerOn;
import static com.urrecliner.savehere.Vars.latitude;
import static com.urrecliner.savehere.Vars.longitude;
import static com.urrecliner.savehere.Vars.mActivity;
import static com.urrecliner.savehere.Vars.mCamera;
import static com.urrecliner.savehere.Vars.mainContext;
import static com.urrecliner.savehere.Vars.nexus6P;
import static com.urrecliner.savehere.Vars.nowTime;
import static com.urrecliner.savehere.Vars.outFileName;
import static com.urrecliner.savehere.Vars.phoneMake;
import static com.urrecliner.savehere.Vars.phoneModel;
import static com.urrecliner.savehere.Vars.phonePrefix;
import static com.urrecliner.savehere.Vars.strAddress;
import static com.urrecliner.savehere.Vars.strDateTime;
import static com.urrecliner.savehere.Vars.strMapAddress;
import static com.urrecliner.savehere.Vars.strMapPlace;
import static com.urrecliner.savehere.Vars.strPlace;
import static com.urrecliner.savehere.Vars.strPosition;
import static com.urrecliner.savehere.Vars.utils;
import static com.urrecliner.savehere.Vars.zoomValue;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

    private final static int PLACE_PICKER_REQUEST = 1;
    private com.urrecliner.savehere.CameraPreview mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        currActivity =  this.getClass().getSimpleName();
        mainContext = getApplicationContext();
        if (!com.urrecliner.savehere.AccessPermission.isPermissionOK(getApplicationContext(), this))
            return;
        mActivity = this;
        phoneModel = Build.MODEL;           // SM-G965N             Nexus 6P
        phoneMake = Build.MANUFACTURER;     // samsung              Huawei
        if (phoneModel.equals(nexus6P))
            phonePrefix = "IMG_";

//        String hardware = Build.HARDWARE;   // samsungexynos9810    angler
//        utils.appendText("this phone model is " + phoneModel + " manu " + manufacturer + " hardware " + hardware);

        final Button btnCameraOnly = findViewById(R.id.btnCamera);
        btnCameraOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactClick(btnCameraOnly);
                take_Picture();
            }
        });

        final Button btnCameraMap = findViewById(R.id.btnCameraMap);
        btnCameraMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraMapBoth = true;
//                mCamera.enableShutterSound(false);
                reactClick(btnCameraMap);
                take_Picture();
            }
        });

        buildZoomSeekBar();
        buildTimerToggle();
        buildCameraView();
        startCamera();

        ready_GoogleAPIClient();
        if (isNetworkAvailable()) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent intent = null;
            try {
                intent = builder.build(MainActivity.this);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                utils.appendText("#PP" + e.toString());
                e.printStackTrace();
            }
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        }
        else {
            Toast.makeText(mainContext,"No Network", Toast.LENGTH_LONG).show();;
            showCurrentLocation();
        }
        utils.deleteOldLogFiles();

    }

    private void ready_GoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new MyConnectionCallBack())
                    .addOnConnectionFailedListener(new MyOnConnectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void take_Picture() {
        if (isTimerOn) {
            delayCount = 100;
            delayTime = 1000;
            try {
                flashSeveralTimes();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            mCamera.takePicture(null, null, rawCallback, jpegCallback); // null is for silent shot
        }
    }

    private void buildZoomSeekBar() {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = mSettings.edit();
        final TextView tV = findViewById(R.id.zoomText);
        final SeekBar seekZoom = findViewById(R.id.seek_bar_zoom);
        zoomValue = mSettings.getInt("Zoom", 16);
        seekZoom.setProgress(zoomValue);
        showSeekBarValue(tV);
        seekZoom.setMax(20);
        seekZoom.setMin(9);
        seekZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zoomValue = seekZoom.getProgress();
                showSeekBarValue(tV);
                editor.putInt("Zoom", zoomValue).apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
//        seekZoom.post(new Runnable() {
//            @Override
//            public void run() {
//                Display display = getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//                int height = seekZoom.getHeight();
//                int seekZoomTop = seekZoom.getTop();
//                FrameLayout camera_surface = findViewById(R.id.frame);
//                int width = size.x - camera_surface.getWidth();
//                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(width, height);
//                layoutParams.setMargins(0,seekZoomTop,0,0);
//                seekZoom.setLayoutParams(layoutParams);
//            }
//        });
    }

    private void buildCameraView() {
//        final FrameLayout frame = findViewById(R.id.frame);
//        frame.post(new Runnable() {
//            @Override
//            public void run() {
//                int width = frame.getWidth();
//                int height = width * 160 / 100;
//                Log.w("Size"," "+width+" x "+height);
//                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(width, height);
////                frame.setLayoutParams(layoutParams);
//            }
//        });
    }

    private void buildTimerToggle () {
        final ImageButton vTimerToggle = findViewById(R.id.timer);
        vTimerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimerOn ^= true;
                vTimerToggle.setImageResource((isTimerOn)? R.mipmap.icon_timer_active_min: R.mipmap.icon_timer_off_min);
            }
        });
    }

    private void showSeekBarValue (TextView tV) {
        String ZoomValue = "" + zoomValue;
        tV.setText(ZoomValue);
    }
    private void reactClick(Button button) {

        button.setBackgroundColor(Color.parseColor("#205eaa"));
        TextView mAddressTextView = findViewById(R.id.addressText);
        strAddress = mAddressTextView.getText().toString();
        try {
            strPlace = strAddress.substring(0, strAddress.indexOf("\n"));
            if (strPlace.equals("")) {
                strPlace = " ";
            }
            strAddress = strAddress.substring(strAddress.indexOf("\n") + 1);
            final SimpleDateFormat imgDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
            outFileName  = imgDateFormat.format(nowTime) + "_" + strPlace;
        } catch (Exception e) {
            strPlace = strAddress;
            strAddress = "?";
        }

        final SeekBar seekZoom = findViewById(R.id.seek_bar_zoom);
        zoomValue = seekZoom.getProgress();

        mAddressTextView.setBackgroundColor(Color.MAGENTA);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitMapCamera = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            new SaveImageTask().execute("");
            }
        };

    private class SaveImageTask extends AsyncTask<String, String , String> {

        @Override
        protected String doInBackground(String ... data) {
            return "";
        }

        @Override
        protected void onPostExecute(String none) {
//            Log.w("post", "Executed");
            mCamera.stopPreview();
            mCamera.release();
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
        }
    }

    private class MyConnectionCallBack implements GoogleApiClient.ConnectionCallbacks {
        public void onConnected(Bundle bundle) {}

        public void onConnectionSuspended(int i) {}
    }

    private class MyOnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//            utils.appendText("#oF");
        }
    }

    protected void onStart() {
        super.onStart();
        ready_GoogleAPIClient();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void startCamera() {

        if (mCameraPreview == null) {
            mCameraPreview = new com.urrecliner.savehere.CameraPreview(this, (SurfaceView) findViewById(R.id.camera_surface));
            mCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            ((FrameLayout) findViewById(R.id.frame)).addView(mCameraPreview);
            mCameraPreview.setKeepScreenOn(true);
        }

        mCameraPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open(0);
        try {
            // camera orientation
            mCamera.setDisplayOrientation(90);

        } catch (RuntimeException ex) {
            Toast.makeText(getApplicationContext(), "camera orientation " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            utils.appendText("CAMERA not found " + ex.getMessage());
        }
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(90);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            float ratio= (float) size.width / (float) size.height;
            if (ratio > 1.7) {  // force ratio to wider screen
//                params.setPreviewSize(size.width, size.height);
                params.setPictureSize(size.width, size.height);
                break;
            }
        }

        mCamera.setParameters(params);
        mCamera.startPreview();
        mCameraPreview.setCamera(mCamera);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        utils.appendText("#oP");
    }

    public void showCurrentLocation() {

        double altitude;
//        utils.appendText("#a geocoder");
        Location location = getGPSCord();
        if (location == null) {
//            utils.appendText("Location is null");
            strPosition = " ";
        }
        else {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            strPosition = String.format(Locale.ENGLISH,"%.5f ; %.5f ; %.2f", latitude, longitude, altitude);
        }
//        utils.appendText(strPosition);

        if (isNetworkAvailable()) {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            strAddress = getAddressByGPSValue(geocoder, latitude, longitude);
        }
        else {
            strAddress = " ";
        }
        String text = ((strMapPlace == null) ? " ":strMapPlace) + "\n" + ((strMapAddress == null) ? strAddress:strMapAddress);
        EditText et = findViewById(R.id.addressText);
        et.setText(text);
        et.setSelection(text.indexOf("\n"));
//        utils.appendText("#shown");
    }

    public Location getGPSCord() {

//        Log.w("gpscord called", "here");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "ACCESS FINE LOCATION not allowed", Toast.LENGTH_LONG).show();
            return null;
        }
        mGoogleApiClient.connect();
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return lastLocation;
    }

    final String noInfo = "No_Info";
    public String getAddressByGPSValue(Geocoder geocoder, double latitude, double longitude) {

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String Feature = address.getFeatureName();
                String Thorough = address.getThoroughfare();
                String Locality = address.getLocality();
                String SubLocality = address.getSubLocality();
                String Country = address.getCountryName();  // or getCountryName()
                String CountryCode = address.getCountryCode();
                String SState = address.getSubAdminArea();
                String State = address.getAdminArea();
                Feature = (Feature == null) ? noInfo : Feature;
                Thorough = (Thorough == null) ? noInfo : Thorough;  // Kakakaua Avernue
                SubLocality = (SubLocality == null) ? noInfo : SubLocality; // 분당구
                Locality = (Locality == null) ? noInfo : Locality;  // Honolulu, 성남시
                SState = (SState == null) ? noInfo : SState;
                State = (State == null) ? noInfo : State;   // Hawaii, 경기도
                if (Country == null && CountryCode == "KR")
                    Country = noInfo; // United States, 대한민국

                return MergedAddress(Feature, Thorough, SubLocality, Locality, State, SState, Country, CountryCode);
            } else {
                return "\nnull address text";
            }
        } catch (IOException e) {
            Toast.makeText(this, "No Address List", Toast.LENGTH_LONG).show();
            utils.appendText("#IOE " + e.toString());
            return "\n" + strPosition;
        }
    }

    public String MergedAddress(String Feature, String Thorough, String SubLocality, String Locality, String SState, String State, String Country, String CountryCode) {

        if (Thorough.equals(Feature)) Feature = noInfo;
        if (SubLocality.equals(Feature)) Feature = noInfo;
        if (SubLocality.equals(Thorough)) Thorough = noInfo;
        if (Locality.equals(Thorough)) Thorough = noInfo;
        if (Locality.equals(SubLocality)) SubLocality = noInfo;
        if (SState.equals(Locality)) Locality = noInfo;
        if (State.equals(SState)) SState = noInfo;

        String addressMerged = "";
        if (CountryCode.equals("KR")) {
            if (!State.equals(noInfo)) addressMerged += " " + State;
            if (!SState.equals(noInfo)) addressMerged += " " + SState;
            if (!Locality.equals(noInfo)) addressMerged += " " + Locality;
            if (!SubLocality.equals(noInfo)) addressMerged += " " + SubLocality;
            if (!Thorough.equals(noInfo)) addressMerged += " " + Thorough;
            if (!Feature.equals(noInfo)) addressMerged += " " + Feature;
        }
        else {
            if (!Feature.equals(noInfo)) addressMerged += " " + Feature;
            if (!Thorough.equals(noInfo)) addressMerged += " " + Thorough;
            if (!SubLocality.equals(noInfo)) addressMerged += " " + SubLocality;
            if (!Locality.equals(noInfo)) addressMerged += " " + Locality;
            if (!SState.equals(noInfo)) addressMerged += " " + SState;
            if (!State.equals(noInfo)) addressMerged += " " + State;
            addressMerged += " " + Country;
        }
        return addressMerged;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {  // user picked up place within the google map list
            Place place = PlacePicker.getPlace(this, data);
            strMapPlace = place.getName().toString();
            String text = place.getAddress().toString();
            strMapAddress = (text.length() > 10) ? text : null;
        } else if (resultCode == RESULT_CANCELED) {
            strMapPlace = null;
            strMapAddress = null;
        }
        mCamera.enableShutterSound(true);
        showCurrentLocation();
        nowTime = System.currentTimeMillis();
        final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("`yy/MM/dd HH:mm", Locale.ENGLISH);
        strDateTime = dateTimeFormat.format(nowTime);
    }

    int delayTime = 1000;
    int delayCount = 100;
    private void flashSeveralTimes() throws InterruptedException {
//        Log.w("delayTime", delayTime+" delayCount "+delayCount);
        if (delayCount < 60) {
            int sleepTime = 10;
            if (delayCount < 10)
                sleepTime = 5;
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
            Thread.sleep(sleepTime);
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
        }
        if (delayCount > 10) {
            delayCount -= 10;
        }
        else if (delayCount == 10) {
            delayTime = 200;
            delayCount--;
        }
        else if (delayCount > 0) {
            delayCount--;
        }
        if (delayCount > 0) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    try {
                        flashSeveralTimes();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, delayTime);
        }
        else {
            mCamera.takePicture(null, null, rawCallback, jpegCallback);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final Button btnCameraMap = findViewById(R.id.btnCameraMap);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:      // assume camera and map
            case KeyEvent.KEYCODE_VOLUME_UP:
                CameraMapBoth = true;
                reactClick(btnCameraMap);
                take_Picture();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
