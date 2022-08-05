
package com.example.unicornrace.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.unicornrace.R;
import com.example.unicornrace.Utils.StepDetectionServiceHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        StepDetectionServiceHelper.startAllIfEnabled(true, requireActivity().getApplicationContext());

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setSubtitle(R.string.action_main);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        container.removeAllViews();

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onDetach(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext());
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        StepDetectionServiceHelper.stopAllIfNotRequired(requireActivity().getApplicationContext());
        super.onDetach();
    }

    @Override
    public void onPause(){
        StepDetectionServiceHelper.stopAllIfNotRequired(requireActivity().getApplicationContext());
        super.onPause();
    }

    private void setupViewPager(ViewPager viewPager) {
        new ViewPagerAdapter(null);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(DailyReportFragment.newInstance(), getString(R.string.day));
        adapter.addFragment(WeeklyReportFragment.newInstance(), getString(R.string.week));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_options_overview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        setPauseContinueMenuItemVisibility(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences sharedPref = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        }
        SharedPreferences.Editor editor = Objects.requireNonNull(sharedPref).edit();
        switch(item.getItemId()){
            case R.id.menu_pause_step_detection:
                editor.putBoolean(getString(R.string.pref_step_counter_enabled), false);
                editor.apply();
                StepDetectionServiceHelper.stopAllIfNotRequired(requireActivity().getApplicationContext());
                return true;
            case R.id.menu_continue_step_detection:
                editor.putBoolean(getString(R.string.pref_step_counter_enabled), true);
                editor.apply();
                StepDetectionServiceHelper.startAllIfEnabled(true, requireActivity().getApplicationContext());
                return true;
            default:
                return false;
        }
    }

    private void setPauseContinueMenuItemVisibility(Menu menu){
        SharedPreferences sharedPref = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        }
        boolean isStepCounterEnabled = Objects.requireNonNull(sharedPref).getBoolean(getString(R.string.pref_step_counter_enabled), true);
        MenuItem continueStepDetectionMenuItem = menu.findItem(R.id.menu_continue_step_detection);
        MenuItem pauseStepDetectionMenuItem = menu.findItem(R.id.menu_pause_step_detection);
        if(isStepCounterEnabled){
            continueStepDetectionMenuItem.setVisible(false);
            pauseStepDetectionMenuItem.setVisible(true);
        }else {
            continueStepDetectionMenuItem.setVisible(true);
            pauseStepDetectionMenuItem.setVisible(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_step_counter_enabled))){
            this.requireActivity().invalidateOptionsMenu();
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<androidx.fragment.app.Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }



        @NonNull
        @Override
        public androidx.fragment.app.Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(androidx.fragment.app.Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        StepDetectionServiceHelper.startAllIfEnabled(true, requireActivity().getApplicationContext());
        super.onAttach(activity);
    }
}
