/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV
 */

package com.udacity.stockhawk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Contains various static methods needed by the Application
 *  to perform certain calculations
 */
public class Tools {

    private static final int PHONE_NORMAL_SCREEN_DENSITY = 3;

    /**
     * Converts a given density (dp) in pixels
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts a given size in Px to dp
     *
     * @param px
     * @return
     */
    public int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Checks internet availability
     *
     * @param context The context of the Activity ( or attached Fragment) that requests
     *                the online state verification
     * @Retuns true if the device is online.
     */
    public static boolean isInternetAvailaible(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the device is a Tablet
     *
     * @param context The context of the Activity ( or attached Fragment)
     * @Returns true if the device is a tablet.
     */
    public static boolean isTabletScreen(Context context) {
        return PHONE_NORMAL_SCREEN_DENSITY <= (context.getResources().getConfiguration().screenLayout & 0xF);
    }
}