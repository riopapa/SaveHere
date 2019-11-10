package com.urrecliner.savehere;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.google.android.gms.maps.GoogleMap;

public class Vars {

    static String currActivity = null;
    static Context mainContext = null;
    static String strPlace = null;
    static String strAddress = null;
    static String strMapPlace = null;
    static String strMapAddress = null;
    static String strPosition = null;
    static String strDateTime = null;

    static Camera mCamera;
    static Bitmap bitMapCamera;
    static double latitude = 0;
    static double longitude = 0;
    static GoogleMap mGoogleMap = null;
    static int zoomValue = 17;
    static boolean CameraMapBoth = false;
    static boolean isTimerOn = false;

    static Utils utils = new Utils();
    static Activity mActivity;
    static String phoneModel = null;
    static String phoneMake = null;
    static String phonePrefix = "";
    static String galaxyS9 = "SM-G965N";
    static String nexus6P = "Nexus 6P";
    static int xPixel, yPixel;
    static String outFileName = null;
    static int cameraOrientation;
    static long nowTime;
    static boolean terrain = false;

}
