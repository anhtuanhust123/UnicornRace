package com.example.unicornrace.Utils;

import android.content.pm.PackageManager;



public class AndroidVersionHelper {

    public static boolean supportsStepDetector(PackageManager pm) {
        return pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }
    public static boolean isHardwareStepCounterEnabled(PackageManager pm){
        return supportsStepDetector(pm);
    }

}
