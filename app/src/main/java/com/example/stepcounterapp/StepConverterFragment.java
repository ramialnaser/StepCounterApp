package com.example.stepcounterapp;

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

import org.json.JSONException;
import org.json.JSONObject;


// this class shows the total steps of the current date, and the corresponding burned calories and kilometers.
public class StepConverterFragment extends Fragment {

    private TextView totalStepsText,kmText,caloriesText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_step_converter, container, false);

        totalStepsText = view.findViewById(R.id.converter_totalsteps);
        kmText = view.findViewById(R.id.converter_km);
        caloriesText = view.findViewById(R.id.converter_calories);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient  mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // to check if the logged account is google or not, if it is google then load the report of this account
        GoogleSignInAccount  acct = GoogleSignIn.getLastSignedInAccount(getContext());
            if (acct != null) {
                String personId = acct.getId();
                getRequest(personId);
            }
        // to check if the logged account is facebook or not, if it is facebook then load the report of this account
           AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                String personId = accessToken.getUserId();
                getRequest(personId);

            }


        return view;
    }

    // get request to get the report values of a certain user.
    private void getRequest(String userId) {
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/api/todaystep/"+userId;
        System.out.println(url);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if(!response.has("Message")){
                        int steps = response.getInt("NumberOfSteps");
                        double distance = response.getDouble("Kilometers");
                        int calories = response.getInt("Calories");

                        totalStepsText.setText(String.valueOf(steps));
                        kmText.setText(String.format("%.4f",distance));
                        caloriesText.setText(String.valueOf(calories));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println(error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
}
