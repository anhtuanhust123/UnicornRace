package com.example.unicornrace.Services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.unicornrace.R;
import com.example.unicornrace.Activities.MainActivity;
import com.example.unicornrace.Activities.TrainingActivity;
import com.example.unicornrace.Models.StepCount;
import com.example.unicornrace.Persistence.StepCountPersistenceHelper;
import com.example.unicornrace.Persistence.WalkingModePersistenceHelper;
import com.example.unicornrace.Persistence.TrainingPersistenceHelper;
import com.example.unicornrace.Utils.StepDetectionServiceHelper;
import com.example.unicornrace.Utils.UnitHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public abstract class AbstractStepDetectorService extends IntentService implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String BROADCAST_ACTION_STEPS_DETECTED = "com.example.unicornracestepcounter.STEPS_DETECTED";
    /**
     * Extra key for new steps which were added since last broadcast.
     */
    public static final String EXTENDED_DATA_NEW_STEPS = "com.example.unicornracestepcounter.NEW_STEPS";
    /**
     * Extra key for total step count since service start
     */
    public static final String EXTENDED_DATA_TOTAL_STEPS = "com.example.unicornracestepcounter.TOTAL_STEPS";
    /**
     * The notification id used for permanent step count notification
     */
    public static final int NOTIFICATION_ID = 1;
    private static final String LOG_TAG = AbstractStepDetectorService.class.getName();
    private final IBinder mBinder = new StepDetectorBinder();
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver();
    private NotificationManager mNotifyManager;
    private PowerManager.WakeLock mWakeLock;
    /**
     * Number of steps the user wants to walk every day
     */
    private int dailyStepGoal = 0;
    /**
     * Number of in-database-saved steps.
     */
    private int totalStepsAtLastSave = 0;
    /**
     * Number of in-database-saved calories;
     */
    private double totalCaloriesAtLastSave = 0;
    /**
     * Distance of in-database-saved steps
     */
    private double totalDistanceAtLastSave = 0;

    /**
     * Number of steps counted since service start
     */
    private int total_steps = 0;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractStepDetectorService(String name) {
        super(name);
    }

    /**
     * Notifies any subscriber about the detected amount of steps
     *
     * @param count The number of detected steps (greater zero)
     */
    protected void onStepDetected(int count) {
        if (count <= 0) {
            return;
        }
        this.total_steps += count;
        Log.i(LOG_TAG, count + " Step(s) detected. Steps since service start: " + this.total_steps);
        // broadcast the new steps
        Intent localIntent = new Intent(BROADCAST_ACTION_STEPS_DETECTED)
                // Add new step count
                .putExtra(EXTENDED_DATA_NEW_STEPS, count)
                .putExtra(EXTENDED_DATA_TOTAL_STEPS, total_steps);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        // Update notification
        updateNotification();
    }

    /**
     * Builds the permanent step count notification
     *
     * @param additionalStepCount The number of steps since last save
     * @return the new notification
     */
    @SuppressLint("StringFormatMatches")
    protected Notification buildNotification(StepCount additionalStepCount) {
        int totalSteps = this.totalStepsAtLastSave + additionalStepCount.getStepCount();
        double totalDistance = this.totalDistanceAtLastSave + additionalStepCount.getDistance();
        double totalCalories = this.totalCaloriesAtLastSave + additionalStepCount.getCalories(getApplicationContext());
        // Get user preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showSteps = sharedPref.getBoolean(this.getString(R.string.pref_notification_permanent_show_steps), true);
        boolean showDistance = sharedPref.getBoolean(this.getString(R.string.pref_notification_permanent_show_distance), false);
        boolean showCalories = sharedPref.getBoolean(this.getString(R.string.pref_notification_permanent_show_calories), false);
        String message = "";
        if (showSteps) {
            message = String.format(getString(R.string.notification_text_steps), totalSteps, this.dailyStepGoal);
        }
        if (showDistance) {
            message += (!message.isEmpty()) ? "\n" : "";
            message += String.format(getString(R.string.notification_text_distance), UnitHelper.kilometerToUsersLengthUnit(UnitHelper.metersToKilometers(totalDistance), this), UnitHelper.usersLengthDescriptionShort(this));
        }
        if (showCalories) {
            message += (!message.isEmpty()) ? "\n" : "";
            message += String.format(getString(R.string.notification_text_calories), totalCalories);
        }
        if(message.isEmpty()){
            message = getString(R.string.notification_text_default);
        }
        Intent intent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setTicker(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.app_name)))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_directions_walk_65black_30dp);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setProgress(this.dailyStepGoal, totalSteps, false);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        return mBuilder.build();
    }

    // has to be implemented by subclasses
    @Override
    public abstract void onSensorChanged(SensorEvent event);

    /**
     * The sensor type(s) on which the step detection service should listen
     *
     * @return Type of sensors requested
     * @see SensorManager#getDefaultSensor
     */
    public abstract int getSensorType();

    /**
     * Whether the notification should be canceled when service dies.
     * @return true if notification should be canceled else false
     */
    protected boolean cancelNotificationOnDestroy(){
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // currently doing nothing here.
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Creating service.");
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "Destroying service.");
        // release wake lock if any
        acquireOrReleaseWakeLock();
        // Unregister sensor listeners
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Objects.requireNonNull(sensorManager).unregisterListener(this);
        // Cancel notification
        if (mNotifyManager != null && cancelNotificationOnDestroy()) {
            mNotifyManager.cancel(NOTIFICATION_ID);
        }
        // Unregister shared preferences listeners
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        // Force save of step count
        // StepDetectionServiceHelper.startPersistenceService(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Starting service.");
        startForeground(NOTIFICATION_ID, buildNotification(this.stepCountFromTotalSteps()));
        acquireOrReleaseWakeLock();

        if(!StepDetectionServiceHelper.isStepDetectionEnabled(getApplicationContext())){
            stopSelf();
        }
        // register for sensors
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = Objects.requireNonNull(sensorManager).getDefaultSensor(this.getSensorType());
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Get daily goal(s) from preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String d = sharedPref.getString(getString(R.string.pref_daily_step_goal), "10000");
        this.dailyStepGoal = Integer.parseInt(d);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // register for steps-saved-event
        IntentFilter filterRefreshUpdate = new IntentFilter();
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED);
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_INSERTED);
        filterRefreshUpdate.addAction(StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_UPDATED );
        filterRefreshUpdate.addAction(TrainingActivity.BROADCAST_ACTION_TRAINING_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filterRefreshUpdate);
        // load step count from database
        getStepsAtLastSave();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // currently doing nothing here.
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Detect changes on preferences and update our internal variable
        if (key.equals(getString(R.string.pref_daily_step_goal))) {
            dailyStepGoal = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_daily_step_goal), "10000"));
            updateNotification();
        } else if (key.equals(getString(R.string.pref_notification_permanent_show_steps)) ||
                key.equals(getString(R.string.pref_notification_permanent_show_distance)) ||
                key.equals(getString(R.string.pref_notification_permanent_show_calories))) {
            updateNotification();
        } else if(key.equals(getString(R.string.pref_use_wake_lock))){
            acquireOrReleaseWakeLock();
        }
    }

    /**
     * Fetches the step count for this day from database
     */
    private void getStepsAtLastSave() {
        List<StepCount> stepCounts = StepCountPersistenceHelper.getStepCountsForDay(Calendar.getInstance(), getApplicationContext());
        totalStepsAtLastSave = 0;
        totalDistanceAtLastSave = 0;
        totalCaloriesAtLastSave = 0;
        for (StepCount stepCount : stepCounts) {
            totalStepsAtLastSave += stepCount.getStepCount();
            totalDistanceAtLastSave += stepCount.getDistance();
            totalCaloriesAtLastSave += stepCount.getCalories(getApplicationContext());
        }
    }

    /**
     * Transforms the current total_steps (total steps since last save) in an @{see StepCount} object
     *
     * @return total_steps since last save as stepCount object
     */
    private StepCount stepCountFromTotalSteps() {
        StepCount stepCount = new StepCount();
        stepCount.setStepCount(total_steps);
        stepCount.setWalkingMode(WalkingModePersistenceHelper.getActiveMode(getApplicationContext())); // use current walking mode
        return stepCount;
    }

    /**
     * Updates or creates the progress notification
     */
    protected void updateNotification() {
        Notification notification = buildNotification(this.stepCountFromTotalSteps());
        mNotifyManager.notify(NOTIFICATION_ID, notification);
    }

    private void acquireOrReleaseWakeLock(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useWakeLock = sharedPref.getBoolean(getString(R.string.pref_use_wake_lock), false);
        boolean useWakeLockDuringTraining = sharedPref.getBoolean(getString(R.string.pref_use_wake_lock_during_training), true);
        boolean isTrainingActive = TrainingPersistenceHelper.getActiveItem(getApplicationContext()) != null;
        if(mWakeLock == null && (useWakeLock || (useWakeLockDuringTraining && isTrainingActive))) {
            acquireWakeLock();
        }
        if(mWakeLock != null && !(useWakeLock || (useWakeLockDuringTraining && isTrainingActive))){
            releaseWakeLock();
        }
    }

    /**
     * Acquires a wakelock
     */
    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    private void acquireWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if(mWakeLock == null || !mWakeLock.isHeld()) {
            mWakeLock = Objects.requireNonNull(powerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepDetectorWakeLock");
            mWakeLock.acquire();
        }
    }

    /**
     * Releases the wake lock if there is any.
     */
    private void releaseWakeLock(){
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    /**
     * Class used for the client Binder.
     *
     * @author Tobias Neidig
     * @version 20160611
     */
    public class StepDetectorBinder extends Binder {
        /**
         * Get the number of steps which were taken since service starts.
         *
         * @return Step count since service start
         */
        public int stepsSinceLastSave() {
            return total_steps;
        }

        /**
         * Resets the step count since last save
         * Is usually called when we saved the steps.
         */
        public void resetStepCount() {
            total_steps = 0;
        }

        public AbstractStepDetectorService getService() {
            return AbstractStepDetectorService.this;
        }
    }

    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w(LOG_TAG, "Received intent which is null.");
                return;
            }
            switch (Objects.requireNonNull(intent.getAction())) {
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_INSERTED:
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_UPDATED:
                case StepCountPersistenceHelper.BROADCAST_ACTION_STEPS_SAVED:
                    // Steps were saved, reload step count from database
                    getStepsAtLastSave();
                    updateNotification();
                    break;
                case TrainingActivity.BROADCAST_ACTION_TRAINING_STOPPED:
                    acquireOrReleaseWakeLock();
                default:
            }
        }
    }
}
