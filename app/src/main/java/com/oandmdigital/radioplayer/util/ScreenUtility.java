package com.oandmdigital.radioplayer.util;

import android.app.Activity;
import android.app.Notification;
import android.util.DisplayMetrics;
import android.view.Display;

public class ScreenUtility {

    private Activity activity;
    private float width;
    private float height;


    // determine the size of the current device screen
    public ScreenUtility(Activity activity) {
        this.activity = activity;

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        float density = activity.getResources().getDisplayMetrics().density;
        height = metrics.heightPixels / density;
        width = metrics.widthPixels / density;

    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

}
