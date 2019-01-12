package com.urrecliner.andriod.savehere;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import android.widget.FrameLayout;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.CameraMapBoth;
import static com.urrecliner.andriod.savehere.Vars.bitMapScreen;
import static com.urrecliner.andriod.savehere.Vars.currActivity;
import static com.urrecliner.andriod.savehere.Vars.latitude;
import static com.urrecliner.andriod.savehere.Vars.longitude;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.mCamera;
import static com.urrecliner.andriod.savehere.Vars.nexus6P;
import static com.urrecliner.andriod.savehere.Vars.phoneModel;
import static com.urrecliner.andriod.savehere.Vars.strAddress;
import static com.urrecliner.andriod.savehere.Vars.strDateTime;
import static com.urrecliner.andriod.savehere.Vars.strMapAddress;
import static com.urrecliner.andriod.savehere.Vars.strMapPlace;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.strPosition;
import static com.urrecliner.andriod.savehere.Vars.utils;
import static com.urrecliner.andriod.savehere.Vars.zoomValue;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

//    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 1;
    public int Permission_Write = 0;
    public int Permission_Internet = 0;
    public int Permission_Location = 0;
    private CameraPreview mCameraPreview;

    //    private FusedLocationProviderClient mFusedLocationClient;
    int screenOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        currActivity =  this.getClass().getSimpleName();
        screenOrientation = getResources().getConfiguration().orientation;
        Permission_Write = AccessPermission.externalWrite(getApplicationContext(), this);
        Permission_Internet = AccessPermission.accessInternet(getApplicationContext(), this);
        Permission_Location = AccessPermission.accessLocation(getApplicationContext(), this);
        if (Permission_Write == 0 || Permission_Internet == 0 || Permission_Location == 0) {
            Toast.makeText(getApplicationContext(),"안드로이드 허가 관계를 확인해 주세요",
                    Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        mActivity = this;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new MyConnectionCallBack())
                    .addOnConnectionFailedListener(new MyOnConnectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
        }

        phoneModel = Build.MODEL;                   // SM-G965N             Nexus 6P
        String manufacturer = Build.MANUFACTURER;   // samsung              Huawei
        String hardware = Build.HARDWARE;           // samsungexynos9810    angler
        utils.appendText("this phone model is " + phoneModel + " manu " + manufacturer + " hardware " + hardware);

        final Button btnCameraOnly = findViewById(R.id.btnCamera);
        btnCameraOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactClick(btnCameraOnly);
                mCamera.takePicture(null, null, rawCallback, jpegCallback); // null is for silent shot
            }
        });
        final Button btnMapOnly = findViewById(R.id.btnMap);
        btnMapOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactClick(btnMapOnly);
                Intent intent = new Intent(getApplicationContext(), LandActivity.class);
                startActivity(intent);
            }
        });
        final Button btnCameraMap = findViewById(R.id.btnCameraMap);
        btnCameraMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraMapBoth = true;
//                mCamera.enableShutterSound(false);
                reactClick(btnCameraMap);
                mCamera.takePicture(null, null, rawCallback, jpegCallback); // null is for silent shot
            }
        });

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = mSettings.edit();
        zoomValue = mSettings.getInt("Zoom", 16);
        TextView tV = findViewById(R.id.zoomText);
        tV.setText(""+ zoomValue);
        final SeekBar seekZoom = findViewById(R.id.seek_bar_zoom);
        seekZoom.setProgress(zoomValue);
        seekZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zoomValue = seekZoom.getProgress();
                TextView tV = findViewById(R.id.zoomText);
                tV.setText(""+ zoomValue);
                editor.putInt("Zoom", zoomValue).apply();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        utils.appendText("##step 0");
        startCamera();
//        getScreenSize(getApplicationContext());
//        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isNetworkAvailable()) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//                utils.appendText("##step 1");
                Intent intent = null;
                try {
                    intent = builder.build(MainActivity.this);
//                utils.appendText("##step 2");
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    utils.appendText("#PP" + e.toString());
                    e.printStackTrace();
                }
//                utils.appendText("##step 3");
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            }
            else {
                utils.appendText("##step NO NETWORK");
                showCurrentLocation();
            }
//        }
        utils.appendText("#ready ---");
    }
    private void reactClick(Button button) {

        button.setBackgroundColor(Color.parseColor("#205eaa"));
        TextView mAddressTextView = findViewById(R.id.addressText);
        strAddress = mAddressTextView.getText().toString();
        try {
            strPlace = strAddress.substring(0, strAddress.indexOf("\n"));
            if (strPlace.equals("")) {
                strPlace = "_";
            }
            strAddress = strAddress.substring(strAddress.indexOf("\n") + 1, strAddress.length());
        } catch (Exception e) {
            strPlace = strAddress;
            strAddress = "?";
        }

        final SeekBar seekZoom = findViewById(R.id.seek_bar_zoom);
        zoomValue = seekZoom.getProgress();

        mAddressTextView.setBackgroundColor(Color.MAGENTA);
//        int backColor = 0x106410;
//        for (int i = 0; i < 2; i++) {
//            String hexColor = String.format("#%06X", (0xFFFFFF & backColor));
//            mAddressTextView.setBackgroundColor(Color.parseColor(hexColor));
//            backColor += 0x070707;
//        }
//        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT)
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

        //byte array를 bitmap으로 변환
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (phoneModel.equals(nexus6P)) {
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bw, bh, matrix, true);
        }
//
//        //bitmap을 byte array로 변환
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitMapScreen = bitmap;

//        byte[] currentData = stream.toByteArray();
        //파일로 저장
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
        mGoogleApiClient.connect();

        //        getSupportActionBar().setDisplayShowHomeEnabled(true);  // icon set
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
//        utils.appendText("#oP");
    }

    public void startCamera() {

        if (mCameraPreview == null) {
            mCameraPreview = new CameraPreview(this, (SurfaceView) findViewById(R.id.camera_surface));
            mCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            ((FrameLayout) findViewById(R.id.frame)).addView(mCameraPreview);
            mCameraPreview.setKeepScreenOn(true);

            /* 프리뷰 화면 눌렀을 때  사진을 찍음
            preview.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                }
            });*/
        }

        mCameraPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open(0);
//        utils.appendText("Camera found");
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

        mCamera.setParameters(params);
        mCamera.startPreview();

//        mCameraPreview.setRotation(0);
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

        double altitude = 0;
//        utils.appendText("#a geocoder");
        Location location = getGPSCord();
        if (location == null) {
//            utils.appendText("Location is null");
            strPosition = "No Position";
        }
        else {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            strPosition = String.format("%s\n%s\n%s", latitude, longitude, altitude);
        }
//        utils.appendText(strPosition);
        strDateTime = getViewTimeText();
        String text;
        if (strMapPlace == null ) {
            if (isNetworkAvailable()) {
                Geocoder geocoder = new Geocoder(this, Locale.KOREA);
                strAddress = getAddressByGPSValue(geocoder, latitude, longitude);
            }
            else {
                strAddress = " ";
            }
//            utils.appendText("#strAddress " + strAddress);
            text = "\n" + strAddress;
        }
        else {
            text = strMapPlace + "\n" + strMapAddress;
        }
        TextView mAdV = findViewById(R.id.addressText);
        mAdV.setText(text);
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

//        utils.appendText("#c");
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
//                String Premises = address.getPremises();
//                String AdminArea = address.getAdminArea();
//                String SubAdminArea = address.getSubAdminArea();
                String Feature = address.getFeatureName();
                String Thorough = address.getThoroughfare();
                String Locality = address.getLocality();
                String SubLocality = address.getSubLocality();
                String Country = address.getCountryName();  // or getCountryName()
                String CountryCode = address.getCountryCode();
                String SState = address.getSubAdminArea();
                String State = address.getAdminArea();
//                String zip = address.getPostalCode();
//                utils.appendText("F: " + Feature + ", T: " + Thorough +  ", L: " + Locality + ", sL: " + SubLocality + ", C: " + CountryCode);
                Feature = (Feature == null) ? noInfo : Feature;
                Thorough = (Thorough == null) ? noInfo : Thorough;  // Kakakaua Avernue
                SubLocality = (SubLocality == null) ? noInfo : SubLocality; // 분당구
                Locality = (Locality == null) ? noInfo : Locality;  // Honolulu, 성남시
                SState = (SState == null) ? noInfo : SState;
                State = (State == null) ? noInfo : State;   // Hawaii, 경기도
                Country = (Country == null) ? noInfo : Country; // United States, 대한민국

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

//        utils.appendText("F: " + Feature + ", T: " + Thorough +  ", L: " + Locality + ", sL: " + SubLocality);

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

//        utils.appendText("#g1");

        if (resultCode == RESULT_OK) {  // user picked up place within the google map list
            Place place = PlacePicker.getPlace(this, data);
            strMapPlace = place.getName().toString();
            strMapAddress = place.getAddress().toString();
        } else if (resultCode == RESULT_CANCELED) {
            strMapPlace = null;
            strMapAddress = null;
        }
        mCamera.enableShutterSound(true);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        utils.appendText("#g2 before showCurrentLocation");
        showCurrentLocation();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final Button btnCameraMap = findViewById(R.id.btnCameraMap);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:      // assume camera and map
            case KeyEvent.KEYCODE_VOLUME_UP:
                CameraMapBoth = true;
                reactClick(btnCameraMap);
                mCamera.takePicture(null, null, rawCallback, jpegCallback); // null is for silent shot
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

    final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yy/MM/dd\nHH:mm:ss", Locale.ENGLISH);
    private String getViewTimeText() { return dateTimeFormat.format(new Date()); }

//    public void getScreenSize(Context context){
//        DisplayMetrics dm = new DisplayMetrics();
//        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//        windowManager.getDefaultDisplay().getMetrics(dm);
//        mWidthInDP = Math.round(dm.widthPixels / dm.density);
//        mHeightInDP = Math.round(dm.heightPixels / dm.density);
//    }

}
