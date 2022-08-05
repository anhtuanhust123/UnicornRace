package com.example.unicornrace.Receivers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.unicornrace.Factory;
import com.example.unicornrace.Models.WalkingMode;
import com.example.unicornrace.Persistence.StepCountPersistenceHelper;
import com.example.unicornrace.Persistence.WalkingModePersistenceHelper;
import com.example.unicornrace.Utils.StepDetectionServiceHelper;
import com.google.android.gms.stats.GCoreWakefulBroadcastReceiver;

public class StepCountPersistenceReceiver extends GCoreWakefulBroadcastReceiver {
    private static final String LOG_CLASS = StepCountPersistenceReceiver.class.getName();
    private WalkingMode oldWalkingMode;

    private Context context;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepCountPersistenceHelper.storeStepCounts(service, context, oldWalkingMode);
            context.getApplicationContext().unbindService(mServiceConnection);
            StepDetectionServiceHelper.stopAllIfNotRequired(false, context);
            WidgetReceiver.forceWidgetUpdate(context);
        }
    };
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_CLASS, "Storing the steps");
        this.context = context.getApplicationContext();
        if (intent.hasExtra(WalkingModePersistenceHelper.BROADCAST_EXTRA_OLD_WALKING_MODE)) {
            oldWalkingMode = WalkingModePersistenceHelper.getItem(intent.getLongExtra(WalkingModePersistenceHelper.BROADCAST_EXTRA_OLD_WALKING_MODE, -1), context);
        }
        if(oldWalkingMode == null){
            oldWalkingMode = WalkingModePersistenceHelper.getActiveMode(context);
        }
        // bind to service
        Intent serviceIntent = new Intent(context, Factory.getStepDetectorServiceClass(context.getPackageManager()));
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }
    }

