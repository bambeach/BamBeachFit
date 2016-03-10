package com.bambeachstudios.bambeachfit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

public class WorkoutTrackingActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                   ActivityCompat.OnRequestPermissionsResultCallback, LocationListener, OnMapReadyCallback {

    private Button endWorkoutButton;
    private Button pauseWorkoutButton;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private Handler handler;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private TextView totalTime;
    private TextView totalDistance;
    private TextView averagePace;
    private UiSettings uiSettings;

    private double currentLatitude;
    private double currentLongitude;
    private float currentSpeed;
    private float workoutDistance;
    private long milliseconds;
    private long startTime;
    private long timeBuffer;
    private long workoutTime;

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

            if (hours > 0) {
                totalTime.setText(
                        getString(R.string.workout_time_with_hours, hours, minutes, seconds));
            }
            else {
                totalTime.setText(
                        getString(R.string.workout_time_without_hours, minutes, seconds));
            }


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

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.workout_tracking_container);
        View bottomSheet = coordinatorLayout.findViewById(R.id.workout_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });

        behavior.setPeekHeight(200);

        MapFragment googleMapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        googleMapFrag.getMapAsync(this);

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
                intent.putExtra("time", workoutTime);
                intent.putExtra("distance", workoutDistance);
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
                    stopLocationUpdates();
                }
                else {
                    timeBuffer = (SystemClock.uptimeMillis() - startTime) - timeAtPause;
                    pauseWorkoutButton.setText(R.string.pause_workout);
                    handler.removeCallbacks(updateTimeTask);
                    handler.postDelayed(updateTimeTask, 100);
                    startLocationUpdates();
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
    public void onBackPressed() {
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
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

            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            startLocationUpdates();

            if (currentLocation != null){
                currentLatitude = currentLocation.getLatitude();
                currentLongitude = currentLocation.getLongitude();
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 20));
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
            currentSpeed = lastLocation.getSpeed();
            double distance = metersToMiles(workoutDistance);
            double speed = metersPerSecondToMPH(currentSpeed);
            double avgPace = calculateAveragePace((float) distance, workoutTime);
            int paceMinutes = (int)Math.floor(avgPace);//int)avgPace % 60;
            int paceSeconds = (int)((avgPace - paceMinutes) * 60);
            totalDistance.setText(String.format("%.2f", distance));
            //averagePace.setText(String.format("%.2f", pace));
            averagePace.setText(getString(R.string.average_pace, paceMinutes, paceSeconds));
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

    protected double metersToMiles(float distanceInMeters) {
        return distanceInMeters * .000621371;
    }

    protected double metersPerSecondToMPH(float speedInMetersPerSecond) {
        return speedInMetersPerSecond * 2.23694;
    }

    protected double calculateAveragePace (float totalDistance, float workoutTimeInMilliseconds) {
        int seconds = (int) (workoutTimeInMilliseconds / 1000);
        double secondsPerMile = seconds / totalDistance;
        double minutesPerMile = secondsPerMile / 60;
        return minutesPerMile;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            uiSettings = googleMap.getUiSettings();
            uiSettings.setMyLocationButtonEnabled(false);
        } else {
            // Show rationale and request permission.
        }
    }
}
