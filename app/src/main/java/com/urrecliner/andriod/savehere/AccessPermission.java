package com.urrecliner.andriod.savehere;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

class AccessPermission {
    private final static int MY_PERMISSIONS_WRITE_FILE = 1001;
    private final static int MY_PERMISSIONS_INTERNET = 1002;
    private final static int MY_PERMISSIONS_LOCATION = 1003;
    private final static int MY_PERMISSIONS_CAMERA = 1004;

    static boolean isPermissionOK(Context context, Activity activity) {

        return externalWrite(context, activity) != 0 &&
                accessInternet(context, activity) != 0 &&
                accessCamera(context, activity) != 0 &&
                accessFineLocation(context, activity) != 0;
    }

    private static int externalWrite(Context c, Activity a) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_WRITE_FILE);
            if (ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return MY_PERMISSIONS_WRITE_FILE;
            } else {
                Toast.makeText(c,"파일을 읽고 쓸 수 있도록 허락되어야 사용할 수 있습니다.", Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        else return MY_PERMISSIONS_WRITE_FILE;
    }

    private static int accessInternet(Context c, Activity a) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.INTERNET}, MY_PERMISSIONS_INTERNET);
            if (ContextCompat.checkSelfPermission(c, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                return MY_PERMISSIONS_INTERNET;
            } else {
                Toast.makeText(c,"인터넷에 접근할 수 있도록 허락하고 다시 실행해 주세요.", Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        else return MY_PERMISSIONS_INTERNET;
    }

    private static int accessFineLocation(Context c, Activity a) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_LOCATION);
            if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return MY_PERMISSIONS_LOCATION;
            } else {
                Toast.makeText(c,"GPS에 접근할 수 있도록 허락하고 다시 실행해 주세요.", Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        else return MY_PERMISSIONS_LOCATION;
    }

    private static int accessCamera(Context c, Activity a) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_CAMERA);
            if (ContextCompat.checkSelfPermission(c, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return MY_PERMISSIONS_CAMERA;
            } else {
                Toast.makeText(c,"카메라에 접근할 수 있도록 허락하고 다시 실행해 주세요.", Toast.LENGTH_LONG).show();
                return 0;
            }
        }
        else return MY_PERMISSIONS_CAMERA;
    }
}
