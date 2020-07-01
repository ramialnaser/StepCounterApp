package com.example.stepcounterapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONObject;

// this is the steps fragment (for registered users)
// it shows the step count detected as well as the kilometers and calories calculations
public class StepsFragment extends Fragment implements SensorEventListener {
    private View view;
    private TextView steps;
    private TextView km;
    private TextView kmPerMin;
    private SensorManager sensorManager;
    private boolean running = false;
    private int stepCounter = 0;
    private int stepsCountCalculationLimit = 10;
    private TextView stepsCalories;
    private final double walkingFactor = 0.73;
    private double CaloriesBurnedPerKm;
    private double stride;
    private double stepCountKm;
    private double conversationFactor;
    private double CaloriesBurned;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount acct;
    private AccessToken accessToken;

    private SharedPreferences preferences;
    private int heightPref, weightPref;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_steps, container, false);

        steps = view.findViewById(R.id.steps_steps);
        km = view.findViewById(R.id.steps_KM);
        kmPerMin = view.findViewById(R.id.steps_calories);
        stepsCalories = view.findViewById(R.id.steps_calories);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);


        preferences = this.getActivity().getSharedPreferences("com.stepcounterapp.heightandweight", Context.MODE_PRIVATE);
        heightPref = preferences.getInt("height",0);
        weightPref = preferences.getInt("weight",0);

        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        // here to initialize the sensor the sensor manager and use one of the android sensors(step counter sensor)
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        running = true;
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // if the phone has that sensor then it will be registered to be used
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        } else {
            // if not this will be shown to the user that the phone does not have this sensor
            Toast.makeText(getContext(), "Sensor is not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // when the user leaves the activity, the step count will be set back to zero and to stop detecting
        // and it will send the data to the API to be updated

        running = false;
        sensorManager.unregisterListener(this);

        if (stepCounter > 10) {

            // to check if the logged account is google or not, if it is google then send the steps of this account to the api
            acct = GoogleSignIn.getLastSignedInAccount(getContext());
            if (acct != null) {
                String personId = acct.getId();
                putRequest(personId, stepCounter);
            }

            // to check if the logged account is facebook or not, if it is facebook then send the steps of this account to the api
            accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                String personId = accessToken.getUserId();
                putRequest(personId, stepCounter);
            }
        }

        stepCounter = 0;
        stepsCalories.setText(String.valueOf(0));
        stepsCountCalculationLimit = 10;
    }

    @Override
    public void onStop() {
        super.onStop();
        // when the user leaves the app, the step count will be set back to zero and to stop detecting
        // and it will send the data to the API to be updated

        if (stepCounter > 10) {

            acct = GoogleSignIn.getLastSignedInAccount(getContext());
            if (acct != null) {
                String personId = acct.getId();
                putRequest(personId, stepCounter);
            }

            accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                String personId = accessToken.getUserId();
                putRequest(personId, stepCounter);
            }
        }

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
            caloriesCalculation(heightPref, weightPref);
            stepCounter = stepCounter + 1;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // to update the count of the steps in the database of the logged user
    private void putRequest(String userId, int stepsToAdd) {

        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/ClientSteps/" + userId;

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("GoogleId", userId);
            jsonObject.put("NumberOfSteps", stepsToAdd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    // this will convert steps to the corresponding kilometers
    @SuppressLint("DefaultLocale")
    private String convertStepsToKM(int steps) {

        double stepsInKm = steps * 0.0008;
        return String.format("%.3f", stepsInKm);

    }

    // this will convert steps to the corresponding burned calories
    private void caloriesCalculation(int height, int weight) {

        if (stepsCountCalculationLimit <= stepCounter) {
            CaloriesBurnedPerKm = walkingFactor * weight;

            stride = ((height) * 0.415);
            stepCountKm = 100000 / stride;
            conversationFactor = CaloriesBurnedPerKm / stepCountKm;
            CaloriesBurned = stepCounter * conversationFactor;
            stepsCalories.setText(String.format("%.1f", CaloriesBurned));
            stepsCountCalculationLimit = stepsCountCalculationLimit + 50;
        }
    }
}
