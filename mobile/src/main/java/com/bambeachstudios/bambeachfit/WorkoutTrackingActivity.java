package com.bambeachstudios.bambeachfit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.Format;

public class WorkoutTrackingActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener{

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

    private float workoutDistance;
    private double currentLatitude;
    private double currentLongitude;
    private Location currentLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

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

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            startLocationUpdates();
            if (currentLocation != null){
                currentLatitude = currentLocation.getLatitude();
                currentLongitude = currentLocation.getLongitude();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentLocation != null){
            Location lastLocation = currentLocation;
            currentLocation = location;
            workoutDistance += lastLocation.distanceTo(currentLocation);
            totalDistance.setText(Float.toString(workoutDistance));
        }
        else {
            currentLocation = location;
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
}
