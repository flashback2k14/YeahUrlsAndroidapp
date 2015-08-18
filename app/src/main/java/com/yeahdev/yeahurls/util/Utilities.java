package com.yeahdev.yeahurls.util;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.yeahdev.yeahurls.R;

public class Utilities {

    public static void buildSnackbar(Activity activity, String text) {
        Snackbar
            .make(activity.findViewById(R.id.coordinatorLayout), text, Snackbar.LENGTH_LONG)
            .show();
    }

    public static void buildSnackbar(View view, String text) {
        Snackbar
            .make(view, text, Snackbar.LENGTH_LONG)
            .show();
    }
}
