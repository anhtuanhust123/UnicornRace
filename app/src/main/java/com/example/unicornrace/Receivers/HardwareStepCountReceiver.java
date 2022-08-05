package com.example.unicornrace.Receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.unicornrace.Services.HardwareStepCounterService;
import com.google.android.gms.stats.GCoreWakefulBroadcastReceiver;


public class HardwareStepCountReceiver extends GCoreWakefulBroadcastReceiver {
    private static final String LOG_CLASS = HardwareStepCountReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_CLASS, "Received hardware step count alarm");
        Intent serviceIntent = new Intent(context, HardwareStepCounterService.class);
        context.startService(serviceIntent);
    }
}
