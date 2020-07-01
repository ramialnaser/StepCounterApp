package com.example.stepcounterapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// this is the profile activity where the information of the logged user is displayed.
public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView username, email;
    private ImageView profilePictureView;
    private Button editButton;
    private TextInputLayout heightTxt, weightTxt,genderTxt;
    private GoogleSignInClient mGoogleSignInClient;
    private URL imgValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FacebookSdk.sdkInitialize(getApplicationContext());

        heightTxt = findViewById(R.id.profile_height);
        weightTxt = findViewById(R.id.profile_weight);
        genderTxt = findViewById(R.id.profile_gender);

        toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        username = findViewById(R.id.profile_username);
        email = findViewById(R.id.profile_email);
        profilePictureView = findViewById(R.id.profile_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        editButton = findViewById(R.id.profile_editButton);

        final SharedPreferences preferences = getSharedPreferences("com.stepcounterapp.heightandweight", Context.MODE_PRIVATE);



        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        // to check if the logged in user is a google account if yes, then get name, email, picture and userid
        // as well as fetch data from the API about this user (height,weight and gender)
        // when u get all these values sets them in the corresponding fields
        if (acct != null) {
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();


            getRequest(personId);
            username.setText(String.valueOf(personGivenName + " " + personFamilyName));
            email.setText(personEmail);
            // to check if the user has a profile picture, if yest then glide will convert the uri into a string and load it into the image view
            if (personPhoto!=null){
                Glide.with(this).load(String.valueOf(personPhoto)).into(profilePictureView);

            }
        }

        //String id;
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        // to check if the logged in user is a facebook account if yes, then get name, email, picture and userid
        // as well as fetch data from the API about this user (height,weight and gender)
        // when u get all these values sets them in the corresponding fields
        if (accessToken != null) {
            try {

                String id = AccessToken.getCurrentAccessToken().getUserId();

                getRequest(id);

                imgValue = new URL("https://graph.facebook.com/" + id + "/picture?type=large");

                // to check if the user has a profile picture, if yest then glide will convert the uri into a string and load it into the image view
                if (imgValue!=null){
                    Glide.with(this).load(String.valueOf(imgValue)).into(profilePictureView);
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {

                    try {

                        String first_name = object.getString("first_name");
                        String last_name = object.getString("last_name");
                        String email2 = object.getString("email");
                        username.setText((first_name + " " + last_name));
                        email.setText(String.valueOf(email2));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "first_name,last_name,email,id");
            graphRequest.setParameters(parameters);
            graphRequest.executeAsync();
        }


        // when the user tries to update the height, weight and gender or adds them
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (editButton.getText().toString()) {
                    case "Edit":

                        heightTxt.setEnabled(true);
                        weightTxt.setEnabled(true);
                        genderTxt.setEnabled(true);
                        editButton.setText("Save");
                        break;
                    case "Save":

                        User user;
                        int height = Integer.parseInt(Objects.requireNonNull(heightTxt.getEditText()).getText().toString());
                        int weight = Integer.parseInt(Objects.requireNonNull(weightTxt.getEditText()).getText().toString());
                        String gender = Objects.requireNonNull(genderTxt.getEditText()).getText().toString();

                        // to update the shared preferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("height",Integer.valueOf(heightTxt.getEditText().getText().toString()));
                        editor.putInt("weight",Integer.valueOf(weightTxt.getEditText().getText().toString()));
                        editor.apply();

                        // to check if the logged user google account then make an update to the api with the new values
                        if (acct!=null){
                            user = new User(acct.getId(),gender,height,weight);
                            updateUserInfo(user);
                        }
                        // to check if the logged user facebook account then make an update to the api with the new values
                        if (accessToken!=null){
                            user = new User(accessToken.getUserId(),gender,height,weight);
                            updateUserInfo(user);
                        }

                        heightTxt.setEnabled(false);
                        weightTxt.setEnabled(false);
                        genderTxt.setEnabled(false);
                        editButton.setText("Edit");

                        break;
                    default:
                        editButton.setText("Edit");
                }
            }
        });
    }

// this method checks if the user exists in the database or not, if the user exists in the database, then a PUT request will be made to update the values
    // if the user does not exist, then this user will be added to the database by POST request to the api
    public void updateUserInfo(final User user) {
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/isclientexist/" + user.getUserId();

        System.out.println(url);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean isExist = response.getBoolean("IsClientExist");

                   if (isExist){
                       putRequest(user);
                   }else {
                       postRequest(user);
                   }

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

    // post request to add a user to the API
    public void postRequest(User user){
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/ClientInformations";

        System.out.println(url);

        Map<String, Object> postParam= new HashMap<String, Object>();
        postParam.put("GoogleId", user.getUserId());
        postParam.put("Gender", user.getGender());
        postParam.put("HeightCM",user.getHeight());
        postParam.put("WeightKG",user.getWeight());


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(postParam), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response.toString());
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    //put request to update a user records in the database
    public void putRequest( final User user){
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/ClientInformations/"+user.getUserId();

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("GoogleId",user.getUserId());
            jsonObject.put("Gender",user.getGender());
            jsonObject.put("HeightCM",user.getHeight());
            jsonObject.put("WeightKG",user.getWeight());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // get request to get the information of a specific user.
    public void getRequest(String userId) {
        String url = "https://stepapplicationapi20200604195611.azurewebsites.net/Api/ClientInformations/"+userId;
        System.out.println(url);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    int weight = response.getInt("WeightKG");
                    int height = response.getInt("HeightCM");
                    String gender = response.getString("Gender");

                    Objects.requireNonNull(heightTxt.getEditText()).setText(String.valueOf(height));
                    Objects.requireNonNull(weightTxt.getEditText()).setText(String.valueOf(weight));
                    Objects.requireNonNull(genderTxt.getEditText()).setText(gender);


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
