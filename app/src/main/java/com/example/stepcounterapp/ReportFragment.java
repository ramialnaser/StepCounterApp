package com.example.stepcounterapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;

// report fragment where it shows the list of reports in a recycler view.
public class ReportFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.reports_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);


        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

// Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // to check if the logged account is google or not, if it is google then load the list of reports of this account
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            String personId = acct.getId();
            loadReports(personId);
        }

        // to check if the logged account is facebook or not, if it is facebook then load the list of reports of this account
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            String personId = accessToken.getUserId();
            loadReports(personId);


        }

        return view;
    }

    // this is a get request to get the list of reports of a specific user, it will also populate the recycler view with reports
    private void loadReports(String userId) {

        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/api/clientsteps/" + userId;
        final List<DailyReport> reports = new ArrayList<>();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = response.length() - 1; i >= 0; i--) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);

                        String date = jsonObject.getString("StepsDate");
                        int steps = jsonObject.getInt("NumberOfSteps");
                        double distance = jsonObject.getDouble("Kilometers");
                        int calories = jsonObject.getInt("Calories");

                        String nDate = editDateFormat(date);

                        DailyReport report = new DailyReport(steps, calories, nDate, distance);

                        reports.add(report);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                // it pass the fetched list of reports to the adapter to be passed to the recycler view in order to be displayed
                mAdapter = new StepReportAdapter(reports);
                recyclerView.setAdapter(mAdapter);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);
    }

    // this method to edit the date to only show the data in this formate (yyyy-MM-dd)
    private String editDateFormat(String receivedDate) {
        String date;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= receivedDate.length() - 1; i++) {
            if (i != 10) {
                sb.append(receivedDate.charAt(i));
            } else {

                break;
            }
        }
        date = sb.toString();
        return date;
    }

}
