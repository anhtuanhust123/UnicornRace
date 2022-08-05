package com.example.unicornrace.Activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.view.MenuItem;
import android.view.View;

import com.example.unicornrace.R;
import com.google.android.material.navigation.NavigationView;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // delay to launch nav drawer item, to allow close animation to play
    static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    // Helper
    private Handler mHandler;
    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler();

        //ActionBar ab = getSupportActionBar();
        //if (ab != null) {
        //    mActionBar = ab;
        //    ab.setDisplayHomeAsUpEnabled(true);
        //}

        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected int getNavigationDrawerID() {
        return 0;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        return goToNavigationItem(itemId);
    }

    protected boolean goToNavigationItem(final int itemId) {

        if (itemId == getNavigationDrawerID()) {
            // just close drawer because we are already in this activity
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // delay transition so the drawer can close
        mHandler.postDelayed(() -> callDrawerItem(itemId), NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawer(GravityCompat.START);

        selectNavigationItem(itemId);

        // fade out the active activity
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }
        return true;
    }

    // set active navigation item
    private void selectNavigationItem(int itemId) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            boolean b = itemId == mNavigationView.getMenu().getItem(i).getItemId();
            mNavigationView.getMenu().getItem(i).setChecked(b);
        }
    }


    private void createBackStack(Intent intent) {
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(intent);
        builder.startActivities();
    }

    @SuppressLint("NonConstantResourceId")
    private void callDrawerItem(final int itemId) {

        Intent intent;

        switch (itemId) {
            case R.id.menu_home:
                //Fragment fragment = new MainFragment();

                //FragmentManager fragmentManager = getSupportFragmentManager();
                //fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
            case R.id.menu_training:
                intent = new Intent(this, TrainingOverviewActivity.class);
                createBackStack(intent);
                break;
            case R.id.menu_distance_measurement:
                intent = new Intent(this, DistanceMeasureActivity.class);
                createBackStack(intent);
                break;

            case R.id.menu_settings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            default:
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null && mainContent.getAlpha() != 1.0f) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }

        selectNavigationItem(getNavigationDrawerID());

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        selectNavigationItem(getNavigationDrawerID());

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }


}