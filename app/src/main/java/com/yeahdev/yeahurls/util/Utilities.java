package com.yeahdev.yeahurls.util;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.yeahdev.yeahurls.R;

public class Utilities {

    public static void buildSnackbar(Activity activity, String text) {
        Snackbar
            .make(activity.findViewById(R.id.coordinatorLayout), text, Snackbar.LENGTH_LONG)
            .show();
    }

    public static void buildToast(Activity activity, String text, int duration) {
        Toast
            .makeText(activity, text, duration)
            .show();
    }
}
