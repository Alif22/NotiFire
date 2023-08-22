package com.example.notifire;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.notifire.map.MapActivity;
import com.example.notifire.map.MapInterface;
import com.example.notifire.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserFileReport extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText addressEditText;
    private EditText postcodeEditText;
    private EditText cityEditText;
    private EditText phoneNumberEditText;
    private EditText IDNumberEditText;
    private EditText addressBEditText;
    private EditText postcodeBEditText;
    private EditText cityBEditText;
    private EditText commentEditText;
    private TextView filenameTextView;
    private Button saveAsDraftButton;
    private Button submitButton;
    private Button uploadButton;
    private ImageButton locationButton;
    private Spinner categorySpinner;
    private Spinner stateSpinner;
    private Spinner stateBSpinner;
    public Uri uri;
    private String fileName = null;
    private ProgressBar progressbar;
    private TextView successTV;
    private LinearLayout fileReportLL;
    private int userAddressID;
    private int userID;
    private double latitude;
    private double longitude;
    private String[] stateArray;
    SharedPreferences userSharedPreferences;
    SharedPreferences reportSharedPreferences;
    private String userType;
    private String endpoint;
    private String gmapApiKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_file_report);
        setTitle("File Report");
        usernameEditText = findViewById(R.id.editTextPersonName);
        addressEditText = findViewById(R.id.editTextAddress);
        postcodeEditText = findViewById(R.id.editTextPostcode);
        cityEditText = findViewById(R.id.editTextCity);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        IDNumberEditText = findViewById(R.id.editTextIDNumber);
        addressBEditText = findViewById(R.id.editTextAddressB);
        postcodeBEditText = findViewById(R.id.editTextPostcodeB);
        cityBEditText = findViewById(R.id.editTextCityB);
        commentEditText = findViewById(R.id.editTextComment);
        saveAsDraftButton = findViewById(R.id.buttonSaveAsDraft);
        submitButton = findViewById(R.id.buttonSubmit);
        uploadButton = findViewById(R.id.buttonUpload);
        locationButton = findViewById(R.id.imageButtonLocator);
        filenameTextView = findViewById(R.id.textViewFileName);
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null) {
                endpoint = bundle.getString("com.example.notifire.ROOT_URL");
                gmapApiKey = bundle.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(this.getClass().getSimpleName(), "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.d(this.getClass().getSimpleName(), "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        //init spinner
        categorySpinner = findViewById(R.id.spinnerCategory);
        stateSpinner = findViewById(R.id.spinnerState);
        stateBSpinner = findViewById(R.id.spinnerStateB);
        //category spinner
        ArrayAdapter<CharSequence> adapterCategoryArray = ArrayAdapter.createFromResource(this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapterCategoryArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapterCategoryArray);
        //state spinner
        ArrayAdapter<CharSequence> adapterStateArray = ArrayAdapter.createFromResource(this,
                R.array.state, android.R.layout.simple_spinner_item);
        adapterStateArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapterStateArray);
        stateBSpinner.setAdapter(adapterStateArray);
        stateArray = getResources().getStringArray(R.array.state);
        progressbar = findViewById(R.id.idPB);
        successTV = findViewById(R.id.successTV);
        fileReportLL = findViewById(R.id.fileReportLL);
        reportSharedPreferences = getSharedPreferences("draftReportPref",
                Context.MODE_PRIVATE);
        userSharedPreferences = getSharedPreferences("UserInfoPref",
                Context.MODE_PRIVATE);

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermissionInManifest(UserFileReport.this,1, Manifest.permission.ACCESS_FINE_LOCATION))
                    selectLocationOnMap();
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        saveAsDraftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = reportSharedPreferences.edit();
                editor.putString("AddressLine",addressEditText.getText().toString());
                editor.putString("City",cityEditText.getText().toString());
                editor.putString("PostalCode", postcodeEditText.getText().toString());
                editor.putInt("StateID",stateSpinner.getSelectedItemPosition());
                editor.putString("AddressLineB",addressBEditText.getText().toString());
                editor.putString("CityB",cityBEditText.getText().toString());
                editor.putString("PostalCodeB", postcodeBEditText.getText().toString());
                editor.putInt("StateIDB",stateBSpinner.getSelectedItemPosition());
                editor.putString("Comment",commentEditText.getText().toString());
                editor.putInt("CategoryID",categorySpinner.getSelectedItemPosition());
                editor.putString("UserName",usernameEditText.getText().toString());
                editor.putString("UserPhoneNumber",phoneNumberEditText.getText().toString());
                editor.putString("UserIdNumber",IDNumberEditText.getText().toString());
                editor.apply();
                Toast.makeText(getApplicationContext(), "Draft of the report saved successfully", Toast.LENGTH_LONG).show();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clear draft
                SharedPreferences.Editor editor = reportSharedPreferences.edit();
                editor.clear();
                editor.apply();
                if(userType.equals("Guest")){
                    submitReportAsGuest();
                }else{
                    submitReport(userID);
                }

            }
        });
        getDraftReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userSharedPreferences.contains("UserID")) {
            userID = userSharedPreferences.getInt("UserID", 0);
        }
        if (userSharedPreferences.contains("AddressID")) {
            userAddressID = userSharedPreferences.getInt("AddressID", 0);
        }
        if (userSharedPreferences.contains("UserType")) {
            userType = userSharedPreferences.getString("UserType", "");
            if(!userType.equals("Guest")){
                if(userAddressID != 0) {
                    getUserAddressInfo(userAddressID);
                }
                if(userID != 0){
                    getUserInfo(userID);
                }
            }
        }
    }

    public void getDraftReport(){
        addressEditText.setText(reportSharedPreferences.getString("AddressLine", ""));
        cityEditText.setText(reportSharedPreferences.getString("City", ""));
        postcodeEditText.setText(reportSharedPreferences.getString("PostalCode", ""));
        stateSpinner.setSelection(reportSharedPreferences.getInt("StateID", 1));
        addressBEditText.setText(reportSharedPreferences.getString("AddressLineB", ""));
        cityBEditText.setText(reportSharedPreferences.getString("CityB", ""));
        postcodeBEditText.setText(reportSharedPreferences.getString("PostalCodeB", ""));
        stateBSpinner.setSelection(reportSharedPreferences.getInt("StateIDB", 1));
        categorySpinner.setSelection(reportSharedPreferences.getInt("CategoryID", 1));
        commentEditText.setText(reportSharedPreferences.getString("Comment", ""));
        usernameEditText.setText(reportSharedPreferences.getString("UserName", ""));
        phoneNumberEditText.setText(reportSharedPreferences.getString("UserPhoneNumber", ""));
        IDNumberEditText.setText(reportSharedPreferences.getString("UserIdNumber", ""));

    }
    public void submitReport(int userID){
        fileReportLL.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
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
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }else{
            //update user address if not new
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

        if(uri!=null){
            //submitting report with file
            String url = endpoint + "/api/reports";
            InputStream iStream = null;
            try {

                iStream = getContentResolver().openInputStream(uri);
                final byte[] inputData = getBytes(iStream);

                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                progressbar.setVisibility(View.GONE);
                                successTV.setVisibility(View.VISIBLE);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressbar.setVisibility(View.GONE);
                                fileReportLL.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "There is some error, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        }) {

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("AddressLineB",addressBEditText.getText().toString());
                        params.put("CityB",cityBEditText.getText().toString());
                        params.put("LatitudeB", String.valueOf(latitude));
                        params.put("LongitudeB", String.valueOf(longitude));
                        params.put("PostalCodeB", postcodeBEditText.getText().toString());
                        params.put("StateIDB", String.valueOf(stateBSpinner.getSelectedItemPosition()+1));
                        params.put("UserID", String.valueOf(userID));
                        params.put("Comment",commentEditText.getText().toString());
                        params.put("ReportStatus","New");
                        params.put("CategoryID", String.valueOf(categorySpinner.getSelectedItemPosition()+1));
                        return params;
                    }
                    //uploading file
                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        params.put("MediaAttachment", new DataPart(fileName ,inputData));
                        return params;
                    }
                };


                volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                QueueSingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{

        String url = endpoint + "/api/reports";
        JSONObject postData = new JSONObject();
        try {
            postData.put("AddressLineB",addressBEditText.getText().toString());
            postData.put("CityB",cityBEditText.getText().toString());
            postData.put("LatitudeB",latitude);
            postData.put("LongitudeB",longitude);
            postData.put("PostalCodeB", postcodeBEditText.getText().toString());
            postData.put("StateIDB",stateBSpinner.getSelectedItemPosition()+1);
            postData.put("UserID",userID);
            postData.put("Comment",commentEditText.getText().toString());
            postData.put("MediaAttachment","");
            postData.put("ReportStatus","New");
            postData.put("CategoryID",categorySpinner.getSelectedItemPosition()+1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressbar.setVisibility(View.GONE);
                successTV.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressbar.setVisibility(View.GONE);
                fileReportLL.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "There is some error please try again", Toast.LENGTH_LONG).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }

    public void submitReportAsGuest(){
        fileReportLL.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
        String url = endpoint + "/api/address/user";
        JSONObject postData = new JSONObject();
        try {
            postData.put("AddressLine",addressEditText.getText().toString());
            postData.put("City",cityEditText.getText().toString());
            postData.put("PostalCode", postcodeEditText.getText().toString());
            postData.put("StateID",stateSpinner.getSelectedItemPosition()+1);
            postData.put("UserName",usernameEditText.getText());
            postData.put("UserIDNumber",IDNumberEditText.getText());
            postData.put("UserPhoneNumber",phoneNumberEditText.getText());
            postData.put("UserType","Guest");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                int userid = 0;
                int addressID = 0;
                try {
                    userid = response.getInt("UserID");
                    addressID = response.getInt("AddressID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                savePreferenceGuest(userid,addressID);
                reportAsGuestSecondPart(userid);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }
    public void reportAsGuestSecondPart(int UserID){
        String url = endpoint + "/api/reports";
        if(uri!=null){
            InputStream iStream = null;

            try {

                iStream = getContentResolver().openInputStream(uri);
                final byte[] inputData = getBytes(iStream);

                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                progressbar.setVisibility(View.GONE);
                                successTV.setVisibility(View.VISIBLE);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressbar.setVisibility(View.GONE);
                                fileReportLL.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "There is some error, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        }) {

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("AddressLineB", addressBEditText.getText().toString());
                        params.put("CityB", cityBEditText.getText().toString());
                        params.put("LatitudeB", String.valueOf(latitude));
                        params.put("LongitudeB", String.valueOf(longitude));
                        params.put("PostalCodeB", postcodeBEditText.getText().toString());
                        params.put("StateIDB", String.valueOf(stateBSpinner.getSelectedItemPosition() + 1));
                        params.put("UserID", String.valueOf(UserID));
                        params.put("Comment", commentEditText.getText().toString());
                        params.put("ReportStatus", "New");
                        params.put("CategoryID", String.valueOf(categorySpinner.getSelectedItemPosition() + 1));
                        return params;
                    }

                    //uploading file
                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        params.put("MediaAttachment", new DataPart(fileName, inputData));
                        return params;
                    }
                };


                volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                QueueSingleton.getInstance(this).addToRequestQueue(volleyMultipartRequest);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            JSONObject postData = new JSONObject();
            try {
                postData.put("AddressLineB",addressBEditText.getText().toString());
                postData.put("CityB",cityBEditText.getText().toString());
                postData.put("LatitudeB",latitude);
                postData.put("LongitudeB",longitude);
                postData.put("PostalCodeB", postcodeBEditText.getText().toString());
                postData.put("StateIDB",stateBSpinner.getSelectedItemPosition()+1);
                postData.put("UserID", String.valueOf(UserID));
                postData.put("Comment",commentEditText.getText().toString());
                postData.put("MediaAttachment","");
                postData.put("ReportStatus","New");
                postData.put("CategoryID",categorySpinner.getSelectedItemPosition()+1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    progressbar.setVisibility(View.GONE);
                    successTV.setVisibility(View.VISIBLE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressbar.setVisibility(View.GONE);
                    fileReportLL.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "There is some error please try again", Toast.LENGTH_LONG).show();
                }
            });
            QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }

    public void getUserAddressInfo(int addressID){
        String url = endpoint+ "/api/addresses/" + addressID ;
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
                Toast.makeText(getApplicationContext(), "Get address error. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getUserInfo(int userID){
        String url = endpoint + "/api/user_/" + userID ;
        Log.d("Url",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    usernameEditText.setText(responseObj.getString("UserName"));
                    phoneNumberEditText.setText(responseObj.getString("UserPhoneNumber"));
                    IDNumberEditText.setText(responseObj.getString("UserIDNumber"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Get user info error. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    private void startMapActivity(String apiKey, String country, String language, String[]supportedAreas){
        Intent intent = new Intent(this, MapActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(MapInterface.API_KEY,apiKey);
        bundle.putString(MapInterface.COUNTRY,country);
        bundle.putString(MapInterface.LANGUAGE,language);
        bundle.putStringArray(MapInterface.SUPPORTED_AREAS,supportedAreas);

        intent.putExtras(bundle);
        startActivityForResult(intent, MapInterface.SELECT_LOCATION_REQUEST_CODE);
    }

    private void selectLocationOnMap() {
        String apiKey = gmapApiKey;
        String mCountry = "my";
        String mLanguage = "en";
        String [] mSupportedAreas = {""};
        startMapActivity(apiKey,mCountry,mLanguage,mSupportedAreas);
    }

    private void updateUi(Intent data){
        String state = data.getStringExtra(MapInterface.ADDRESS_STATE);
        stateBSpinner.setSelection(Arrays.asList(stateArray).indexOf(state));
        postcodeBEditText.setText(data.getStringExtra(MapInterface.ADDRESS_POSTCODE));
        cityBEditText.setText(data.getStringExtra(MapInterface.ADDRESS_CITY));
        addressBEditText.setText(data.getStringExtra(MapInterface.ADDRESS_LINE));
        latitude = data.getDoubleExtra(MapInterface.LOCATION_LAT_EXTRA,-1);
        longitude = data.getDoubleExtra(MapInterface.LOCATION_LNG_EXTRA,-1);
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MapInterface.SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null){
                updateUi(data);
            }
        }
        //file request code
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            if (data != null){
                uri = data.getData();
                // Get the Uri of the selected file
                String uriString = uri.toString();
                File myFile = new File(uriString);
                String path = myFile.getAbsolutePath();

                String displayName = null;

                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = this.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                        }
                    } finally {
                        cursor.close();
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.getName();

                }
                fileName = displayName;
                filenameTextView.setVisibility(View.VISIBLE);
                filenameTextView.setText(displayName);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectLocationOnMap();
        }
    }

    //check for location permission
    public static boolean hasPermissionInManifest(Activity activity, int requestCode, String permissionName) {
        if (ContextCompat.checkSelfPermission(activity, permissionName) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permissionName},
                    requestCode);
        } else {
            return true;
        }
        return false;
    }
    public void openFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent,1);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void savePreferenceGuest(int UID,int addressID){
        SharedPreferences.Editor editor = userSharedPreferences.edit();
        editor.putString("UserName", usernameEditText.getText().toString());
        editor.putInt("UserID", UID);
        editor.putString("UserIdNumber",IDNumberEditText.getText().toString());
        editor.putString("UserPhone",phoneNumberEditText.getText().toString());
        editor.putString("UserType","Guest");
        editor.putInt("AddressID", addressID);
        editor.apply();
    }
}
