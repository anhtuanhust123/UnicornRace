package com.example.unicornrace.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.unicornrace.fragments.DailyReportFragment;
import com.example.unicornrace.fragments.MainFragment;
import com.example.unicornrace.fragments.MonthlyReportFragment;
import com.example.unicornrace.fragments.WeeklyReportFragment;
import com.example.unicornrace.R;
import com.example.unicornrace.Utils.StepDetectionServiceHelper;
import com.example.unicornrace.fragments.DailyReportFragment;
import com.example.unicornrace.fragments.WeeklyReportFragment;


public class MainActivity extends BaseActivity implements DailyReportFragment.OnFragmentInteractionListener, WeeklyReportFragment.OnFragmentInteractionListener, MonthlyReportFragment.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        // Load first view
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainFragment(), "MainFragment");
        fragmentTransaction.commit();

        // Start step detection if enabled and not yet started
        StepDetectionServiceHelper.startAllIfEnabled(this);
        //Log.i(LOG_TAG, "MainActivity initialized");
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.menu_home;
    }

}