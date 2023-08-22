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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class UserRegistrationPage extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText phoneNumEditText;
    private EditText IDNumEditText;
    private EditText emailEditText;
    private EditText pwEditText;
    private EditText addressEditText;
    private EditText postcodeEditText;
    private EditText cityEditText;
    private Spinner stateSpinner;
    private Button registerButton;
    public String userAddressID;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Registration");
        setContentView(R.layout.activity_user_registration_page);
        usernameEditText = findViewById(R.id.editTextUserName);
        addressEditText = findViewById(R.id.editTextAddress);
        phoneNumEditText = findViewById(R.id.editTextPhoneNumber);
        IDNumEditText = findViewById(R.id.editTextIDNumber);
        emailEditText = findViewById(R.id.editTextEmail);
        registerButton = findViewById(R.id.buttonRegister);
        postcodeEditText = findViewById(R.id.editTextPostcode);
        cityEditText = findViewById(R.id.editTextCity);
        stateSpinner = findViewById(R.id.spinnerState);
        pwEditText = findViewById(R.id.passwordTextField);
        ArrayAdapter<CharSequence> adapterStateArray = ArrayAdapter.createFromResource(this,
                R.array.state, android.R.layout.simple_spinner_item);
        adapterStateArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapterStateArray);
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
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    public void registerUser(){
        String url = endpoint + "/api/user_/register";
        JSONObject postData = new JSONObject();
        try {
            postData.put("AddressLine",addressEditText.getText().toString());
            postData.put("City",cityEditText.getText().toString());
            postData.put("PostalCode", postcodeEditText.getText().toString());
            postData.put("StateID",stateSpinner.getSelectedItemPosition()+1);
            postData.put("UserName",usernameEditText.getText().toString());
            postData.put("UserPhoneNumber",phoneNumEditText.getText().toString());
            postData.put("UserIDNumber",IDNumEditText.getText().toString());
            postData.put("UserType","User");
            postData.put("Email",emailEditText.getText().toString());
            postData.put("Password",pwEditText.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UserRegistrationPage.this, LoginActivity.class);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "There is some error. Please try again", Toast.LENGTH_LONG).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}