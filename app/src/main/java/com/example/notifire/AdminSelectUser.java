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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminSelectUser extends AppCompatActivity {
    private int userID;
    private EditText usernameEditText;
    private EditText addressEditText;
    private EditText phoneNumEditText;
    private EditText IDNumEditText;
    private EditText emailEditText;
    private Button saveButton;
    private EditText postcodeEditText;
    private EditText cityEditText;
    private Spinner stateSpinner;
    private int addressID;
    private String endpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_user);
        usernameEditText = findViewById(R.id.editTextUserName);
        addressEditText = findViewById(R.id.editTextAddress);
        phoneNumEditText = findViewById(R.id.editTextPhoneNumber);
        IDNumEditText = findViewById(R.id.editTextIDNumber);
        emailEditText = findViewById(R.id.editTextEmail);
        saveButton = findViewById(R.id.saveButton);
        postcodeEditText = findViewById(R.id.editTextPostcode);
        cityEditText = findViewById(R.id.editTextCity);
        stateSpinner = findViewById(R.id.spinnerState);
        ArrayAdapter<CharSequence> adapterStateArray = ArrayAdapter.createFromResource(this,
                R.array.state, android.R.layout.simple_spinner_item);
        adapterStateArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapterStateArray);
        userID = getIntent().getIntExtra("UserID",0);
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
        initEditTexts();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
                updateAddressInfo();
            }
        });
    }
    public void initEditTexts(){
        String url = endpoint+ "/api/user_/" + userID ;
        Log.d("Url",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    usernameEditText.setText(responseObj.getString("UserName"));
                    phoneNumEditText.setText(responseObj.getString("UserPhoneNumber"));
                    IDNumEditText.setText(responseObj.getString("UserIDNumber"));
                    emailEditText.setText(responseObj.getString("Email"));
                    addressID = responseObj.getInt("AddressID");
                    Log.d("addressID: ", String.valueOf(addressID));
                    getUserAddressInfo(addressID);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getUserAddressInfo(int addressID){
        String url = endpoint + "/api/addresses/" + addressID ;
        Log.d("Url",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    addressEditText.setText(responseObj.getString("AddressLine"));
                    cityEditText.setText(responseObj.getString("City"));
                    postcodeEditText.setText(responseObj.getString("PostalCode"));
                    stateSpinner.setSelection(responseObj.getInt("StateID")-1);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void updateUserInfo(){
        String url = endpoint+ "/api/user_/"+ userID;
        JSONObject putData = new JSONObject();
        try {
            putData.put("UserName",usernameEditText.getText().toString());
            putData.put("UserPhoneNumber",phoneNumEditText.getText().toString());
            putData.put("UserIDNumber",IDNumEditText.getText().toString());
            putData.put("Email",emailEditText.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //send request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, putData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Account information updated successfully", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "There is some error. Please try again", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }
    public void updateAddressInfo(){
        if(addressID == 0){
            //post address if user has no address yet
            String url = endpoint + "/api/addresses?UserID=" + userID;
            JSONObject postData = new JSONObject();
            try {
                postData.put("AddressLine",addressEditText.getText().toString());
                postData.put("City",cityEditText.getText().toString());
                //postData.put("Latitude",);
                //postData.put("Longitude",);
                postData.put("PostalCode", postcodeEditText.getText().toString());
                postData.put("StateID",stateSpinner.getSelectedItemPosition()+1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Address update successfully", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Address update failed. Please try again", Toast.LENGTH_LONG).show();
                }
            });

            QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }else{
            //update user address
            String url = endpoint + "/api/addresses/user_/" + userID;

            JSONObject postData = new JSONObject();
            try {
                postData.put("AddressLine",addressEditText.getText().toString());
                postData.put("City",cityEditText.getText().toString());
                postData.put("PostalCode", postcodeEditText.getText().toString());
                postData.put("StateID",stateSpinner.getSelectedItemPosition()+1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Address update successfully", Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Address update failed. Please try again", Toast.LENGTH_LONG).show();
                }
            });
            QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }
}