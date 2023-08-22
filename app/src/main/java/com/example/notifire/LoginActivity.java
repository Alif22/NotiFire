package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {
    private UserSingleton user = UserSingleton.getInstance();
    private SharedPreferences userSharedPreferences;
    private EditText passwordField;
    private EditText loginField;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.login_activity);
        TextView registerWOLoginTextView = findViewById(R.id.withoutLoginTextView);
        Button signInButton = findViewById(R.id.signInButton);
        Button registerButton = findViewById(R.id.registerButton);
        loginField = findViewById(R.id.loginTextField);
        passwordField = findViewById(R.id.passwordTextField);
        userSharedPreferences = getSharedPreferences("UserInfoPref",Context.MODE_PRIVATE);
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null) {
                endpoint = bundle.getString("com.example.notifire.ROOT_URL");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(this.getClass().getSimpleName(), "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        precheck();
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(LoginActivity.this, "Logging in. Please wait.", Toast.LENGTH_SHORT).show();
                String loginFieldValue = loginField.getText().toString();
                String Email = loginFieldValue;
                getUserInformation(Email);
            }
        }
        );
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, UserRegistrationPage.class);
                startActivity(intent);
            }
        });
        registerWOLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = userSharedPreferences.edit();
                editor.putString("UserType","Guest");
                editor.apply();
                Intent intent = new Intent(LoginActivity.this, UserMainPage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        precheck();
    }

    public void getUserInformation(String email){
        String url = endpoint + "/api/user_/?Email=" + email;
        Log.d("url:  ", url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    user.setName(responseObj.getString("UserName"));
                    user.setType(responseObj.getString("UserType"));
                    user.setEmail(responseObj.getString("Email"));
                    user.setPassword(responseObj.getString("Password"));
                    user.setId(responseObj.getInt("UserID"));
                    user.setIDNumber(responseObj.getString("UserIDNumber"));
                    user.setPhoneNumber(responseObj.getString("UserPhoneNumber"));
                    user.setAddressID(responseObj.getInt("AddressID"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                login();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if not user, log in as officer
                getOfficerInformation(email);
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getOfficerInformation(String email){
        String url = endpoint + "/api/officers/?Email=" + email;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    user.setName(responseObj.getString("OfficerName"));
                    user.setType("Officer");
                    user.setEmail(responseObj.getString("Email"));
                    user.setPassword(responseObj.getString("Password"));
                    user.setId(responseObj.getInt("OfficerID"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                login();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }


    public void saveInUserPreferences(UserSingleton user){
        SharedPreferences.Editor editor = userSharedPreferences.edit();
        editor.putString("UserName", user.getName());
        editor.putInt("UserID", user.getId());
        editor.putString("UserIdNumber",user.getIDNumber());
        editor.putString("UserPhone",user.getPhoneNumber());
        editor.putString("UserPassword", user.getPassword());
        editor.putString("UserEmail",user.getEmail());
        editor.putString("UserType",user.getType());
        if(user.getAddressID()!=null){
            editor.putInt("AddressID",user.getAddressID());
        }else{
            editor.putInt("AddressID",0);
        }
        editor.apply();
    }
    private void login(){
        Intent intent;
        String passwordFieldValue = passwordField.getText().toString();
        if(user != null ) {
            if (passwordFieldValue.equals(user.getPassword())) {
                switch (user.getType()) {
                    case "Officer":
                        intent = new Intent(LoginActivity.this, OfficerAssignedReport.class);
                        startActivity(intent);
                        break;
                    case "Admin":
                        intent = new Intent(LoginActivity.this, AdminDashboard.class);
                        startActivity(intent);
                        break;
                    case "User":
                    case "Guest":
                        intent = new Intent(LoginActivity.this, UserMainPage.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                saveInUserPreferences(user);
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //check if user has logged in
    private void precheck(){
        SharedPreferences userSharedPreferences;
        String userType = "";
        Intent intent;
        userSharedPreferences = getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);

        if(userSharedPreferences.contains("UserType")){
            userType = userSharedPreferences.getString("UserType","");
        }
        switch(userType) {
            case "Officer":
                intent = new Intent(LoginActivity.this, OfficerAssignedReport.class);
                startActivity(intent);
                break;
            case "Admin":
                intent = new Intent(LoginActivity.this, AdminDashboard.class);
                startActivity(intent);
                break;
            case "User":
            case "Guest":
                intent = new Intent(LoginActivity.this, UserMainPage.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}