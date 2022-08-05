package com.example.unicornrace.Receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

import android.util.Log;

import com.example.unicornrace.Utils.StepDetectionServiceHelper;


public class OnShutdownBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_CLASS = OnShutdownBroadcastReceiver.class.getName();

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_CLASS, "onReceive");
        StepDetectionServiceHelper.startPersistenceService(context);
    }
}
