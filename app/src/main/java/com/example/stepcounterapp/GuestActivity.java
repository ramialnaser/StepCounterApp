package com.example.stepcounterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

// this is the guest activity (unregistered users)
// it shows the step count detected as well as the kilometers and calories calculations
public class GuestActivity extends AppCompatActivity implements SensorEventListener {

    private Toolbar toolbar;
    private TextView steps;
    private TextView km;
    private SensorManager sensorManager;
    private boolean running = false;
    private TextView calories;
    private int stepCounter = 0;
    private final int totalStepsKm=1312;
    private final int caloriesInKm=55;
    private int stepsCountCalculationLimit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        steps = findViewById(R.id.guest_steps_steps);
        km = findViewById(R.id.guest_steps_KM);

        toolbar = findViewById(R.id.guest_toolbar);
        calories = findViewById(R.id.guest_steps_calories);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Guest");
    }


    @Override
    public void onResume() {
        super.onResume();
        // here to initialize the sensor the sensor manager and use one of the android sensors(step counter sensor)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        running = true;
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // if the phone has that sensor then it will be registered to be used
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        } else {
            // if not this will be shown to the user that the phone does not have this sensor
            Toast.makeText(getApplicationContext(), "Sensor is not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // when the user leaves the activity, the step count will be set back to zero and to stop detecting
        running = false;
        sensorManager.unregisterListener(this);
        stepCounter = 0;
        calories.setText(String.valueOf(0));
        stepsCountCalculationLimit = 10;
    }

    @Override
    public void onStop() {
        super.onStop();
        stepCounter = 0;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // when the sensor detects changes it will start counting the steps by incrementing the step counting values as well as
        // passing the counter to the calories and kilometers calculations.
        if (running) {

            steps.setText(String.valueOf(stepCounter));
            km.setText(convertStepsToKM(stepCounter));
            caloriesCalculation();
            stepCounter = stepCounter + 1;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // this will convert steps to the corresponding kilometers
    @SuppressLint("DefaultLocale")
    private String convertStepsToKM(int steps) {

        double stepsInKm = steps * 0.0008;
        return String.format("%.3f",stepsInKm);

    }
    // this will convert steps to the corresponding burned calories
    private void caloriesCalculation() {
        if (stepsCountCalculationLimit <= stepCounter) {
            float stepsToKm = (float)stepCounter / totalStepsKm;
            double CaloriesBurned = stepsToKm * caloriesInKm;
            calories.setText(String.format("%.1f", CaloriesBurned));
            stepsCountCalculationLimit = stepsCountCalculationLimit + 50;

        }
    }
}
