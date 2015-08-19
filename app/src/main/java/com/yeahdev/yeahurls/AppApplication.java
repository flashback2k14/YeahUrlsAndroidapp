package com.yeahdev.yeahurls;

import android.app.Application;
import com.firebase.client.Firebase;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
