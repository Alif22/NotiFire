package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

public class UserUpdateInfo extends AppCompatActivity {
    private  EditText usernameEditText;
    private EditText addressEditText;
    private EditText phoneNumEditText;
    private EditText IDNumEditText;
    private Button saveButton;
    private EditText postcodeEditText;
    private EditText cityEditText;
    private Spinner stateSpinner;
    SharedPreferences userSharedPreferences;
    private int userAddressID;
    private int userID;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Update profile information");
        setContentView(R.layout.activity_user_update_info);
        usernameEditText = findViewById(R.id.editTextUserName);
        addressEditText = findViewById(R.id.editTextAddress);
        phoneNumEditText = findViewById(R.id.editTextPhoneNumber);
        IDNumEditText = findViewById(R.id.editTextIDNumber);
        //emailEditText = findViewById(R.id.editTextEmail);
        saveButton = findViewById(R.id.saveButton);
        postcodeEditText = findViewById(R.id.editTextPostcode);
        cityEditText = findViewById(R.id.editTextCity);
        stateSpinner = findViewById(R.id.spinnerState);
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
        ArrayAdapter<CharSequence> adapterStateArray = ArrayAdapter.createFromResource(this,
                R.array.state, android.R.layout.simple_spinner_item);
        adapterStateArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapterStateArray);
        //button listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
                updateAddressInfo();
                updateUserPref();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserPref();
    }

    public void getUserPref() {
        userSharedPreferences = getSharedPreferences("UserInfoPref",
                Context.MODE_PRIVATE);
        if (userSharedPreferences.contains("UserID")) {
            userID = userSharedPreferences.getInt("UserID", 0);
            getUserAddressInfoByUserID(userID);
        }
        if (userSharedPreferences.contains("UserName")) {
            usernameEditText.setText(userSharedPreferences.getString("UserName", ""));
        }
        if (userSharedPreferences.contains("UserIdNumber")) {
            IDNumEditText.setText(userSharedPreferences.getString("UserIdNumber", ""));
        }
        if (userSharedPreferences.contains("UserPhone")) {
            phoneNumEditText.setText(userSharedPreferences.getString("UserPhone", ""));
        }

        if(userSharedPreferences.contains("AddressID")){
            userAddressID = userSharedPreferences.getInt("AddressID",0);
            if(userAddressID != 0){
               getUserAddressInfo(userAddressID);
            }
        }
    }
    //should have been get user address by userID
    public void getUserAddressInfoByUserID(int userID){
        String url = endpoint + "/api/addresses/user_/" + userID ;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    addressEditText.setText(response.getString("AddressLine"));
                    cityEditText.setText(response.getString("City"));
                    postcodeEditText.setText(response.getString("PostalCode"));
                    stateSpinner.setSelection(response.getInt("StateID")-1);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "get user address error", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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
                Toast.makeText(getApplicationContext(), "get user address error", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void updateUserInfo(){
        //get user id
        userSharedPreferences = getSharedPreferences("UserInfoPref",Context.MODE_PRIVATE);
        int UserID = userSharedPreferences.getInt("UserID", 0);

        String url = endpoint + "/api/user_/"+ UserID;
        JSONObject putData = new JSONObject();
        try {
            putData.put("UserName",usernameEditText.getText().toString());
            putData.put("UserPhoneNumber",phoneNumEditText.getText().toString());
            putData.put("UserIDNumber",IDNumEditText.getText().toString());

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
                Toast.makeText(getApplicationContext(), "There is some error with account request please try again", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }
    public void updateAddressInfo(){
        //create new address if user has no address
        userSharedPreferences = getSharedPreferences("UserInfoPref",Context.MODE_PRIVATE);
        int userID = userSharedPreferences.getInt("UserID", 0);
        Log.d("userAddressID in the function: ", String.valueOf(userAddressID));
        if(userAddressID == 0){
            //post address if user has no address yet
            String url = endpoint + "/api/addresses?UserID=" + userID;
            JSONObject postData = new JSONObject();
            try {
                postData.put("AddressLine",addressEditText.getText().toString());
                postData.put("City",cityEditText.getText().toString());
                postData.put("PostalCode", postcodeEditText.getText().toString());
                postData.put("StateID",stateSpinner.getSelectedItemPosition()+1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    userSharedPreferences = getSharedPreferences("UserInfoPref",
                            Context.MODE_PRIVATE);
                    //save addressID in user shared pref
                    SharedPreferences.Editor editor = userSharedPreferences.edit();
                    try {
                        editor.putInt("AddressID", response.getInt("AddressID"));
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }
    public void updateUserPref(){
        userSharedPreferences = getSharedPreferences("UserInfoPref",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userSharedPreferences.edit();
        editor.putString("UserName",usernameEditText.getText().toString());
        editor.putString("UserIdNumber",IDNumEditText.getText().toString());
        editor.putString("UserPhone",phoneNumEditText.getText().toString());
        editor.apply();
    }
}