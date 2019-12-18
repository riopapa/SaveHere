package com.urrecliner.savehere;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import static com.urrecliner.savehere.Vars.cameraOrientation;
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
import static com.urrecliner.savehere.Vars.terrain;
import static com.urrecliner.savehere.Vars.utils;
import static com.urrecliner.savehere.Vars.xPixel;
import static com.urrecliner.savehere.Vars.yPixel;
import static com.urrecliner.savehere.Vars.zoomValue;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;
    private final static int PLACE_PICKER_REQUEST = 1;
    private CameraPreview mCameraPreview;
    private String logID = "main";

    TextView zoomTextV;
    String [] zoomTables = {"9","10","11","12","13","14","15","16","17","18","19","20"};
    // Wheel scrolled flag
    private boolean wheelScrolled = false;
    SharedPreferences.Editor editor = null;

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorManager mSensorManager;
    private DeviceOrientation deviceOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        currActivity =  this.getClass().getSimpleName();
        mainContext = getApplicationContext();
        if (!AccessPermission.isPermissionOK(getApplicationContext(), this))
            return;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        deviceOrientation = new DeviceOrientation();


        mActivity = this;
        phoneModel = Build.MODEL;           // SM-G965N             Nexus 6P
        phoneMake = Build.MANUFACTURER;     // samsung              Huawei
        if (phoneModel.equals(nexus6P))
            phonePrefix = "IMG_";

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mSettings.edit();
        zoomValue = mSettings.getInt("Zoom", 16);

//        String hardware = Build.HARDWARE;   // samsungexynos9810    angler
//        utils.log(logID,"this phone model is " + phoneModel + " manu " + manufacturer + " hardware " + hardware);

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
                reactClick(btnCameraMap);
                take_Picture();
            }
        });

        CheckBox checkBox = findViewById(R.id.terrain);
        terrain = mSettings.getBoolean("terrain", false);
        checkBox.setChecked(terrain);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terrain = !terrain;
                editor.putBoolean("terrain", terrain).apply();
            }
        });

        buildWheelView();
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
                utils.log(logID,"#PP" + e.toString());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        utils.log(logID," new Config "+newConfig.orientation);
        Toast.makeText(mainContext,"curr orentation is "+newConfig.orientation,Toast.LENGTH_SHORT).show();
//        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
//        if (newConfig.orientation ==Configuration.ORIENTATION_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else if (newConfig.orientation ==Configuration.ORIENTATION_PORTRAIT){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
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

    /* WHEEL related start */
    private void buildWheelView() {

        WheelView wheel = findViewById(R.id.wheel_zoom);
        wheel.setViewAdapter(new ArrayWheelAdapter(mainContext,zoomTables));
        wheel.setVisibleItems(1);
        wheel.setCurrentItem(zoomValue-9);
        wheel.addChangingListener(changedListener);
//        wheel.addScrollingListener(scrolledListener);
        zoomTextV = findViewById(R.id.mapScale);
    }

    // Wheel changed listener
    private final OnWheelChangedListener changedListener = new OnWheelChangedListener()
    {
        public void onChanged(WheelView wheel, int oldValue, int newValue)
        {
            if (!wheelScrolled)
            {
                updateStatus();
            }
        }
    };

//    private OnWheelScrollListener scrolledListener = new OnWheelScrollListener()
//    {
//        public void onScrollStarts(WheelView wheel)
//        {
//            wheelScrolled = true;
//        }
//
//        public void onScrollEnds(WheelView wheel)
//        {
//            wheelScrolled = false;
//            updateStatus();
//        }
//
//        @Override
//        public void onScrollingStarted(WheelView wheel) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void onScrollingFinished(WheelView wheel) {
//            // TODO Auto-generated method stub
//
//        }
//    };

    /**
     * Updates entered PIN status
     */
    private void updateStatus()
    {
        int idx = getWheel(R.id.wheel_zoom).getCurrentItem();
        zoomValue = Integer.parseInt(zoomTables[idx]);
        editor.putInt("Zoom", zoomValue).apply();
    }

    /**
     * Returns wheel by Id
     *
     * @param id
     *          the wheel Id
     * @return the wheel with passed Id
     */
    private WheelView getWheel(int id)
    {
        return (WheelView) findViewById(id);
    }

    /* WHEEL related end */


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
                vTimerToggle.setImageResource((isTimerOn)? R.mipmap.icon_timer_on_min: R.mipmap.icon_timer_off_min);
            }
        });
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270);
    }

    private void reactClick(Button button) {


        button.setBackgroundColor(Color.parseColor("#205eaa"));
        xPixel = Resources.getSystem().getDisplayMetrics().widthPixels;     // 2094, 2960
        yPixel = Resources.getSystem().getDisplayMetrics().heightPixels;    // 1080, 1440

        int mDeviceRotation = ORIENTATIONS.get(deviceOrientation.getOrientation());
        utils.logE(logID, "*** rotation="+mDeviceRotation);
        if (mDeviceRotation == 0)
            cameraOrientation = 1;
        else if (mDeviceRotation == 180)
            cameraOrientation = 3;
        else if (mDeviceRotation == 90)
            cameraOrientation = 6;
        else
            cameraOrientation = 8;

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
//            utils.log(logID,"#oF");
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
            // camera cameraOrientation
            mCamera.setDisplayOrientation(90);

        } catch (RuntimeException ex) {
            Toast.makeText(getApplicationContext(), "camera cameraOrientation " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            utils.log(logID,"CAMERA not found " + ex.getMessage());
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        utils.log(logID,"#oP");
    }

    public void showCurrentLocation() {

        double altitude;
//        utils.log(logID,"#a geocoder");
        Location location = getGPSCord();
        if (location == null) {
//            utils.log(logID,"Location is null");
            strPosition = " ";
        }
        else {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            strPosition = String.format(Locale.ENGLISH,"%.5f ; %.5f ; %.2f", latitude, longitude, altitude);
        }
//        utils.log(logID,strPosition);

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
//        utils.log(logID,"#shown");
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
            utils.log(logID,"#IOE " + e.toString());
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

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(deviceOrientation.getEventListener(), mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(deviceOrientation.getEventListener(), mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }
}
