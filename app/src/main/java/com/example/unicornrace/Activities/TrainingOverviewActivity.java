package com.example.unicornrace.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.unicornrace.R;
import com.example.unicornrace.Persistence.WalkingModePersistenceHelper;
import com.example.unicornrace.Persistence.TrainingPersistenceHelper;
import com.example.unicornrace.Models.Training;
import com.example.unicornrace.Models.WalkingMode;
import com.example.unicornrace.Adapter.TrainingOverviewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingOverviewActivity extends AppCompatActivity implements TrainingOverviewAdapter.OnItemClickListener {
    public static final String LOG_CLASS = TrainingOverviewActivity.class.getName();

    private Map<Integer, WalkingMode> menuWalkingModes;

    private TrainingOverviewAdapter mAdapter;
    private RelativeLayout mEmptyView;

    private List<Training> trainings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_overview);

        if(TrainingPersistenceHelper.getActiveItem(this) != null){
            // show current training session if there is one.
            Log.w(LOG_CLASS, "Found active training session");
            startTrainingActivity();
        }

        mEmptyView = (RelativeLayout) findViewById(R.id.empty_view);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.training_overview_list);
        if (mRecyclerView == null) {
            Log.e(LOG_CLASS, "Cannot find recycler view");
            return;
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // init fab
        FloatingActionButton mStartTrainingFAB = (FloatingActionButton) findViewById(R.id.start_training);
        if (mStartTrainingFAB == null) {
            Log.e(LOG_CLASS, "Cannot find fab.");
            return;
        }
        mStartTrainingFAB.setOnClickListener(v -> {
            // start new session
            startTrainingActivity();
        });

        // init recycler view
        // specify the adapter
        mAdapter = new TrainingOverviewAdapter(new ArrayList<>());
        mAdapter.setOnItemClickListener(this);
        mAdapter.setRecyclerView(mRecyclerView);
        showTrainings();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh of trainings.
        showTrainings();
    }

    protected void startTrainingActivity(){
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }


    protected void showTrainings() {
        // Load training sessions
        List<Training> trainingsLoadFromDatabase = TrainingPersistenceHelper.getAllItems(this);
        trainings = new ArrayList<>();
        int steps = 0;
        double distance = 0;
        double duration = 0;
        double calories = 0;

        // Add month labels
        Calendar cal = Calendar.getInstance();
        int month = -1;
        for(int i = 0; i < trainingsLoadFromDatabase.size(); i++){
            Training training = trainingsLoadFromDatabase.get(i);
            cal.setTimeInMillis(training.getStart());
            if(month != cal.get(Calendar.MONTH)){
                month = cal.get(Calendar.MONTH);
                SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy", getResources().getConfiguration().locale);
                // create dummy training entry to display the new month
                Training monthHeadline = new Training();
                monthHeadline.setName(df.format(cal.getTime()));
                monthHeadline.setViewType(TrainingOverviewAdapter.VIEW_TYPE_MONTH_HEADLINE);
                trainings.add(monthHeadline);
            }
            steps += training.getSteps();
            distance += training.getDistance();
            duration += training.getDuration();
            calories += training.getCalories();
            trainings.add(training);
        }

        // Add summary
        Training summary = new Training();
        summary.setEnd(-1);
        summary.setViewType(TrainingOverviewAdapter.VIEW_TYPE_SUMMARY);
        if(trainings.size() > 0) {
            summary.setStart(trainings.get(trainings.size()-1).getStart());
            summary.setEnd(summary.getStart() + (Double.valueOf(duration * 1000)).longValue());
        }
        summary.setSteps(steps);
        summary.setDistance(distance);
        summary.setCalories(calories);
        trainings.add(0, summary);
        this.mAdapter.setItems(trainings);
        if (trainings.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }


    protected void showEditDialog(final Integer position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(TrainingOverviewActivity.this, R.style.AppTheme_Dialog);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_training, null);
        final EditText edittext = (EditText) dialogLayout.findViewById(R.id.input_name);
        final EditText descriptionEditText = (EditText) dialogLayout.findViewById(R.id.input_description);
        final RatingBar feelingBar = (RatingBar) dialogLayout.findViewById(R.id.input_feeling);
        if (position != null) {
            edittext.setText(trainings.get(position).getName());
            descriptionEditText.setText(String.valueOf(trainings.get(position).getDescription()));
            feelingBar.setRating(trainings.get(position).getFeeling());
        }
        alert.setMessage(getString(R.string.training_input_message));
        alert.setTitle(getString(R.string.training_input_title));
        alert.setView(dialogLayout);
        alert.setPositiveButton(R.string.save, (dialog, which) -> {

        });
        alert.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {/* nothing to do here */});
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = edittext.getText().toString();
            String description = descriptionEditText.getText().toString();
            float feeling = feelingBar.getRating();
            Training training;
            if (position == null) {
                training = new Training();
            } else {
                training = trainings.get(position);
            }
            training.setName(name);
            training.setDescription(description);
            training.setFeeling(feeling);
            training = TrainingPersistenceHelper.save(training, getApplicationContext());
            if (position == null) {
                trainings.add(training);
                mAdapter.setItems(trainings);
                mAdapter.notifyItemInserted(trainings.size() - 1);
            } else {
                mAdapter.notifyItemChanged(position);
            }
            if (trainings.size() == 1 && position == null) {
                // force view update to hide "empty"-message
                showTrainings();
            }
            alertDialog.dismiss();
        });
    }


    protected void removeTrainingSession(int position) {
        Training training = trainings.get(position);
        if (!TrainingPersistenceHelper.delete(training, this)) {
            Toast.makeText(this, R.string.operation_failed, Toast.LENGTH_SHORT).show();
            showTrainings();
            return;
        }
        mAdapter.removeItem(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyItemRangeChanged(position, trainings.size() - 1);
        if (trainings.size() == 0) {
            // if no text exists, show default view.
            showTrainings();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        showEditDialog(position);
    }

    @Override
    public void onEditClick(View view, int position) {
        showEditDialog(position);
    }

    @Override
    public void onRemoveClick(View view, int position) {
        removeTrainingSession(position);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Add the walking modes to option menu
        menu.clear();
        menuWalkingModes = new HashMap<>();
        List<WalkingMode> walkingModes = WalkingModePersistenceHelper.getAllItems(this);
        int i = 0;
        for (WalkingMode walkingMode : walkingModes) {
            int id = Menu.FIRST + (i++);
            menuWalkingModes.put(id, walkingMode);
            menu.add(0, id, Menu.NONE, walkingMode.getName()).setChecked(walkingMode.isActive());
        }
        menu.setGroupCheckable(0, true, true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!menuWalkingModes.containsKey(item.getItemId())) {
            return false;
        }
        // update active walking mode
        WalkingMode walkingMode = menuWalkingModes.get(item.getItemId());
        WalkingModePersistenceHelper.setActiveMode(walkingMode, this);
        return true;
    }

}