package com.example.unicornrace.login;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class AppFb extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.getSdkVersion();
        AppEventsLogger.activateApp(this);
    }
}
