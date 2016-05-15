package com.bambeachstudios.bambeachfit;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class WorkoutDetailsFragment extends Fragment {

    float workoutDistance;
    long workoutTime;

    TextView averagePace;
    TextView totalDistance;
    TextView totalTime;

    public WorkoutDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_details, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            workoutDistance = intent.getFloatExtra("distance", 0);
            workoutTime = intent.getLongExtra("time", 0);
        }

        averagePace = (TextView) rootView.findViewById(R.id.mile_pace);
        totalDistance = (TextView) rootView.findViewById(R.id.total_distance);
        totalTime = (TextView) rootView.findViewById(R.id.workout_time);

        int seconds = (int) (workoutTime / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            totalTime.setText(
                    getString(R.string.workout_time_with_hours, hours, minutes, seconds));
        }
        else {
            totalTime.setText(
                    getString(R.string.workout_time_without_hours, minutes, seconds));
        }

        double distance = metersToMiles(workoutDistance);
        double pace = calculateAveragePace((float) distance, workoutTime);
        int paceMinutes = (int)Math.floor(pace);//int)avgPace % 60;
        int paceSeconds = (int)((pace - paceMinutes) * 60);
        totalDistance.setText(String.format("%.2f", distance));
        averagePace.setText(getString(R.string.average_pace, paceMinutes, paceSeconds));


        return rootView;
    }

    protected double metersToMiles(float distanceInMeters) {
        return distanceInMeters * .000621371;
    }

    protected double calculateAveragePace (float totalDistance, float workoutTimeInMilliseconds) {
        int seconds = (int) (workoutTimeInMilliseconds / 1000);
        double secondsPerMile = seconds / totalDistance;
        return secondsPerMile / 60;
    }
}
