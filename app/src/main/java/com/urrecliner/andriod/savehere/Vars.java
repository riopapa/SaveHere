package com.urrecliner.andriod.savehere;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;

public class Vars {
    static String strPlace = null;
    static String strAddress = null;
    static String strMapPlace = null;
    static String strMapAddress = null;
    static String strPosition = null;
    static double dblAltitude;
    static double dblLatitude;
    static double dblLongitude;
    static String strDateTime;

    static Camera mCamera;
    static Bitmap bitMapScreen;
    static String tempPNGName;
    static boolean doubleRun = false;

    static boolean isRUNNING = false;
    static Utils utils = new Utils();
    static Activity mActivity;
}
