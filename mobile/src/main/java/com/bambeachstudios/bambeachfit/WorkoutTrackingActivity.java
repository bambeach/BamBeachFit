package com.bambeachstudios.bambeachfit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WorkoutTrackingActivity extends AppCompatActivity {

    private long startTime;
    private long timeBuffer;
    private long milliseconds;
    private long workoutTime;
    private TextView totalTime;
    private TextView totalDistance;
    private TextView averagePace;
    private Handler handler;
    private Button endWorkoutButton;
    private Button pauseWorkoutButton;

    private final Runnable updateTimeTask = new Runnable() {
        public void run() {
            final long start = startTime;
            milliseconds = SystemClock.uptimeMillis() - start;
            workoutTime = milliseconds - timeBuffer;
            int seconds = (int) (workoutTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            totalTime.setText(getString(R.string.workout_time, hours, minutes, seconds));

            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_tracking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        totalTime = (TextView) findViewById(R.id.workout_time);
        totalDistance = (TextView) findViewById(R.id.total_distance);
        averagePace = (TextView) findViewById(R.id.mile_pace);
        endWorkoutButton = (Button) findViewById(R.id.end_workout_button);
        pauseWorkoutButton = (Button) findViewById(R.id.pause_workout_button);
        handler = new Handler();
        if (startTime == 0L) {
            startTime = SystemClock.uptimeMillis();
            handler.removeCallbacks(updateTimeTask);
            handler.postDelayed(updateTimeTask, 100);
        }

        endWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(updateTimeTask);
                Intent intent = new Intent(getApplicationContext(), WorkoutDetailsActivity.class);
                startActivity(intent);
            }
        });

        pauseWorkoutButton.setOnClickListener(new View.OnClickListener() {
            long timeAtPause;
            @Override
            public void onClick(View v) {
                if (pauseWorkoutButton.getText().equals("Pause Workout")) {
                    timeAtPause = workoutTime;
                    handler.removeCallbacks(updateTimeTask);
                    pauseWorkoutButton.setText(R.string.resume_workout);
                }
                else {
                    timeBuffer = (SystemClock.uptimeMillis() - startTime) - timeAtPause;
                    pauseWorkoutButton.setText(R.string.pause_workout);
                    handler.removeCallbacks(updateTimeTask);
                    handler.postDelayed(updateTimeTask, 100);
                }

            }
        });
    }
}
