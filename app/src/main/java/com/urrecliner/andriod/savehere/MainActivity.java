package com.urrecliner.andriod.savehere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.urrecliner.andriod.savehere.Vars.dblAltitude;
import static com.urrecliner.andriod.savehere.Vars.dblLatitude;
import static com.urrecliner.andriod.savehere.Vars.dblLongitude;
import static com.urrecliner.andriod.savehere.Vars.mActivity;
import static com.urrecliner.andriod.savehere.Vars.strAddress;
import static com.urrecliner.andriod.savehere.Vars.strDateTime;
import static com.urrecliner.andriod.savehere.Vars.strMapAddress;
import static com.urrecliner.andriod.savehere.Vars.strMapPlace;
import static com.urrecliner.andriod.savehere.Vars.strPlace;
import static com.urrecliner.andriod.savehere.Vars.strPosition;
import static com.urrecliner.andriod.savehere.Vars.utils;


public class MainActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

//    private final static int FINE_LOCATION = 100;
    private final static int PLACE_PICKER_REQUEST = 1;
    public int Permission_Write = 0;
    public int Permission_Internet = 0;
    public int Permission_Location = 0;

    //    private FusedLocationProviderClient mFusedLocationClient;
    long backKeyPressedTime;

//    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permission_Write = AccessPermission.externalWrite(getApplicationContext(), this);
        Permission_Internet = AccessPermission.accessInternet(getApplicationContext(), this);
        Permission_Location = AccessPermission.accessLocation(getApplicationContext(), this);
        if ( Permission_Write == 0 || Permission_Internet == 0 || Permission_Location == 0) {
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
        backKeyPressedTime = System.currentTimeMillis();

        final Button button = findViewById(R.id.btnCapture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView mAddressTextView = findViewById(R.id.addressText);
                strAddress = mAddressTextView.getText().toString();
                strPlace = strAddress.substring(0, strAddress.indexOf("\n"));
                if (strPlace.equals("")) {
                    strPlace = "no name";
                }
                int backColor = 0x106410;
                for (int i = 0; i < 2; i++) {
                    String hexColor = String.format("#%06X", (0xFFFFFF & backColor));
                    mAddressTextView.setBackgroundColor(Color.parseColor(hexColor));
                    backColor += 0x070707;
                }
                button.setBackgroundColor(Color.parseColor("#205eaa"));
                Intent intent = new Intent(getApplicationContext(), LandActivity.class);
                startActivity(intent);
            }
        });
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intent = null;
        try {
            intent = builder.build(MainActivity.this);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            appendText("#PP" + e.toString());
            e.printStackTrace();
        }
        startActivityForResult(intent, PLACE_PICKER_REQUEST);
        appendText("#ready ---");

    }

    private class MyConnectionCallBack implements GoogleApiClient.ConnectionCallbacks {
        public void onConnected(Bundle bundle) {}

        public void onConnectionSuspended(int i) {}
    }

    private class MyOnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            appendText("#oF");
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
        appendText("#oP");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        appendText("#oP");
    }

    public void showCurrentLocation() {
//        boolean isGrantStorage = grantExternalStoragePermission();
//        appendText("#oG");
//
//        if (!isGrantStorage) {
//            return;
//        }
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        appendText("#a geocoder");
        Location mCurrentLocation = getGPSCord();
        if (mCurrentLocation == null) {
            appendText("Location is null");
            return;
        }
        dblLatitude = mCurrentLocation.getLatitude();
        dblLongitude = mCurrentLocation.getLongitude();
        dblAltitude = mCurrentLocation.getAltitude();

        strPosition = String.format("%s\n%s\n%s",
                dblLatitude, dblLongitude, dblAltitude);
        strDateTime = getviewTimeText();

        TextView mPositionTextView = findViewById(R.id.positionText);
        mPositionTextView.setText(strPosition);
        TextView mDateTimeTextView = findViewById(R.id.datetimeText);
        mDateTimeTextView.setText(strDateTime);
        TextView mAddressTextView = findViewById(R.id.addressText);
        String text;
        if (strMapPlace == null ) {
            strAddress = getGPSAddress(geocoder);
            appendText("#strAddress " + strAddress);
            text = "\n" + strAddress;
        }
        else {
            text = strMapPlace + "\n" + strMapAddress;
        }
        mAddressTextView.setText(text);
        appendText("#shown");
    }

    public Location getGPSCord() {

        appendText("#oGPS");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "ACCESS FINE LOCATION not allowed", Toast.LENGTH_LONG).show();
            return null;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        appendText("#mLo");
        return mCurrentLocation;
    }

    final String noInfo = "No_Info";
    public String getGPSAddress(Geocoder geocoder) {

        utils.appendText("#c");
        try {
            List<Address> addresses = geocoder.getFromLocation(dblLatitude, dblLongitude, 1);
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
//                String zip = address.getPostalCode();
                String SState = address.getSubAdminArea();
                String State = address.getAdminArea();
                utils.appendText("#d address vars");

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
//        utils.appendText("#ma");

        if (Thorough.equals(Feature)) Feature = noInfo;
        if (SubLocality.equals(Feature)) Feature = noInfo;
        if (SubLocality.equals(Thorough)) Thorough = noInfo;
        if (Locality.equals(Thorough)) Thorough = noInfo;
        if (Locality.equals(SubLocality)) SubLocality = noInfo;
        if (SState.equals(Locality)) Locality = noInfo;
        if (State.equals(SState)) SState = noInfo;

        String addressMerged = "";
        if (CountryCode.equals("kr")) {
            if (State.equals(noInfo)) addressMerged += " " + State;
            if (SState.equals(noInfo)) addressMerged += " " + SState;
            if (Locality.equals(noInfo)) addressMerged += " " + Locality;
            if (SubLocality.equals(noInfo)) addressMerged += " " + SubLocality;
            if (Thorough.equals(noInfo)) addressMerged += " " + Thorough;
            if (Feature.equals(noInfo)) addressMerged += " " + Feature;
        }
        else {
            if (Feature.equals(noInfo)) addressMerged += " " + Feature;
            if (Thorough.equals(noInfo)) addressMerged += " " + Thorough;
            if (SubLocality.equals(noInfo)) addressMerged += " " + SubLocality;
            if (Locality.equals(noInfo)) addressMerged += " " + Locality;
            if (SState.equals(noInfo)) addressMerged += " " + SState;
            if (State.equals(noInfo)) addressMerged += " " + State;
            addressMerged += " " + Country;
        }
//        utils.appendText("#f2");

        return addressMerged;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        appendText("#g1");

        if (resultCode == RESULT_OK) {  // user picked up place within the google map list
            Place place = PlacePicker.getPlace(this, data);
            strMapPlace = place.getName().toString();
            strMapAddress = place.getAddress().toString();
        } else if (resultCode == RESULT_CANCELED) {
            strMapPlace = null;
            strMapAddress = null;
        }
        appendText("#g2 before showCurrentLocation");
        showCurrentLocation();
    }

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.ENGLISH);
    final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yy/MM/dd\nHH:mm:ss", Locale.ENGLISH);
    final SimpleDateFormat timeLogFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.ENGLISH);
    private String getviewTimeText() { return dateTimeFormat.format(new Date()); }

    public void appendText(String textLine) {
        File directory = new File(Environment.getExternalStorageDirectory(), "SaveHere");
        try {
            if (!directory.exists()) {
                boolean result = directory.mkdirs();
                Log.e("Directory",  directory.toString() + " created " + result);
            }
        } catch (Exception e) {
            Log.e("Directory", "Create error " + directory.toString() + "_" + e.toString());
        }

        File directoryDate = new File(directory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                if (directoryDate.mkdirs())
                    Log.e("Directory", directoryDate.toString() + " created ");
            }
        } catch (Exception e) {
            Log.e("creating Folder error", directoryDate + "_" + e.toString());
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directoryDate.toString() + "/" + "save_here.txt";

        try {
            File file = new File(fullName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("createFile", " Error");
                }
            }
            StackTraceElement[] traces;
            traces = Thread.currentThread().getStackTrace();
            String outText = "\n" + timeLogFormat.format(new Date()) + " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber() + " [[" + textLine + "]]\n";
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
