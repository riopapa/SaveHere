package com.urrecliner.andriod.savehere;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;

public class Vars {
    static String strPlace;
    static String strAddress;
    static String strMapPlace;
    static String strMapAddress;
    static String strPosition;
    static double dblAltitude;
    static double dblLatitude;
    static double dblLongitude;
    static String strDateTime;


    static Camera mCamera;
    static Bitmap bitMap;
    static String tempPNGName;
    static boolean doubleRun = false;

    static boolean isRUNNING = false;
    static Utils utils = new Utils();
    static Activity mActivity;
}
