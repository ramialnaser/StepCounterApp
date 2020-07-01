package com.example.stepcounterapp;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

// this is the first screen that shows to the user, and it presents the user with the options login with facebook, google or as a guest.
public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private int RC_SIGN_IN = 0;
    private LoginButton faceLog;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());

        //to be redirected to the guest activity when clicked on the guest button
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this,GuestActivity.class);
            startActivity(intent);


            }
        });

        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        faceLog = findViewById(R.id.facelog);

        // is called when the google button pressed to be redirected to the google's own login activity
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;

                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //FaceBook login
        callbackManager = CallbackManager.Factory.create();
        // when the facebook button pressed to redirect the user to facebook's login activity
        faceLog.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                String userId = AccessToken.getCurrentAccessToken().getUserId();
                // if the user is authorized and is a valid facebook user then he will be allowed in
                if (userId != null) {
                    updateUI(userId);
                }
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    // this method to automatically redirect the user to the MainActivity and to the skip the login process
    // in case the user has already login in with either facebook or google before, until the user signs out then
    // the user will have to go through the login process again.
    @Override
    protected void onStart() {
        super.onStart();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            String userId = accessToken.getUserId();
            updateUI(userId);
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            updateUI(account.getId());
        }

    }

    // to move the user to the google's own login activity to enter the login credentials and if the user is authorized and a valid google user

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // the results of the user if s/he a valid user on google if true then the user will be redirected to the application
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // this will handle the valid users from google who are allowed to proceed
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            if (account != null) {
                updateUI(account.getId());
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // this method makes a get request to the api, to check if the user already exist in the database or not
    //if the user exists, then the user will be redirected to the main activity, and set the height and weight of that user.
    // if the user does no exist, then the user will be redirected to the profile activity
    public void updateUI(final String userId) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Validating...");
        progressDialog.show();
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/isclientexist/" + userId;

        System.out.println(url);

        // the request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            // this will handle the successful response
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    boolean isExist = response.getBoolean("IsClientExist");

                    if (isExist) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        getRequest(userId);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(intent);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // this will handle the error response
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println(error.getMessage());
            }
        });
        // to add the request to the queue.
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    //this method will get the existing user from the database and fetch his/her height and weight and save it in the shared pref to be consumed in other
    //activities
    public void getRequest(String userId) {
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/ClientInformations/"+userId;
        System.out.println(url);
        final SharedPreferences preferences = getSharedPreferences("com.stepcounterapp.heightandweight", Context.MODE_PRIVATE);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int weight = response.getInt("WeightKG");
                    int height = response.getInt("HeightCM");

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("height",height);
                    editor.putInt("weight",weight);
                    editor.apply();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println(error.getMessage());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
}
