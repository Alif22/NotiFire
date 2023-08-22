package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminSelectReport extends AppCompatActivity {
    private TextView reportIDTV;
    private TextView reportedOnTV;
    private TextView reportCategoryTV;
    private TextView commentTV;
    private TextView burningAddressTV;
    private TextView userNameTV;
    private TextView userIDNumTV;
    private TextView userAddressTV;
    private TextView userPhoneNumTV;
    private TextView reportStatusTV;
    private TextView actionTakenTV;
    private TextView officerRemarkTV;
    private TextView officerAssignedTV;
    private Button assignOfficerBttn;
    private ProgressBar progressBar;
    private LinearLayout reportLL;
    private int reportID;
    private String endpoint;
    String[] stateArray ;
    String[] categoryArray ;
    TextView attachmentTV;
    LinearLayout attachmentLL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_report);
        reportIDTV = findViewById(R.id.reportIDTV);
        reportedOnTV = findViewById(R.id.reportTimeAndDateTV);
        reportCategoryTV = findViewById(R.id.categoryTV);
        commentTV = findViewById(R.id.usercommentTV);
        burningAddressTV = findViewById(R.id.burningAddressTV);
        userNameTV = findViewById(R.id.userNameTV);
        userIDNumTV = findViewById(R.id.userIDNumTV);
        userAddressTV = findViewById(R.id.userAddressTV);
        userPhoneNumTV = findViewById(R.id.userPhoneNumTV);
        reportStatusTV = findViewById(R.id.reportStatusTV);
        assignOfficerBttn = findViewById(R.id.buttonAssignOfficer);
        actionTakenTV = findViewById(R.id.actionTakenTV);
        officerRemarkTV = findViewById(R.id.officerRemarkTV);
        progressBar = findViewById(R.id.idPB);
        reportLL = findViewById(R.id.reportInfoLL);
        reportID = getIntent().getIntExtra("ReportID",0);
        stateArray = getResources().getStringArray(R.array.state);
        categoryArray = getResources().getStringArray(R.array.category);
        attachmentTV = findViewById(R.id.attachmentTV);
        attachmentLL = findViewById(R.id.attachmentLL);
        officerAssignedTV = findViewById(R.id.officerAssignedTV);
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
        assignOfficerBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AdminAssignOfficer.class);
                intent.putExtra("ReportID",reportID);
                startActivity(intent);
            }
        });
        getReport(reportID);
    }
    public void getReport(int reportID){
        String url = endpoint + "/api/reports/" + reportID ;
        Log.d("URL: ",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int addressID;
                int userID;
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    reportIDTV.setText(reportIDTV.getText() + responseObj.getString("ReportID"));
                    reportedOnTV.setText(reportedOnTV.getText() + responseObj.getString("created_at").toString());
                    reportStatusTV.setText(reportStatusTV.getText() + responseObj.getString("ReportStatus"));
                    commentTV.setText(commentTV.getText() + responseObj.getString("Comment"));
                    reportCategoryTV.setText(reportCategoryTV.getText() + categoryArray[responseObj.getInt("CategoryID")-1]);
                    getAssignedOfficerName();
                    getResponse();
                    String fileName = responseObj.getString("MediaAttachment");
                    if(!fileName.equals("")){
                        attachmentLL.setVisibility(View.VISIBLE);
                        attachmentTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String fileUrl = endpoint + "/files/"+fileName;
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                                startActivity(browserIntent);
                            }
                        });
                        attachmentTV.setText(responseObj.getString("MediaAttachment"));
                    }
                    addressID = responseObj.getInt("AddressID");
                    userID = responseObj.getInt("UserID");
                    getBurnAddress(addressID);
                    getUser(userID);

                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getUser(int id){
        String url = endpoint + "/api/user_/" + id ;
        Log.d("URL: ",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int addressID;
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    userNameTV.setText(userNameTV.getText() + responseObj.getString("UserName"));
                    userIDNumTV.setText(userIDNumTV.getText() + responseObj.getString("UserIDNumber"));
                    userPhoneNumTV.setText(userPhoneNumTV.getText() + responseObj.getString("UserPhoneNumber"));
                    addressID = responseObj.getInt("AddressID");
                    getUserAddress(addressID);
                    Log.d("AddressID: ", String.valueOf(responseObj.getInt("AddressID")));
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }
    public void getUserAddress(int id){
        String url = endpoint + "/api/addresses/" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    String fullAddress = responseObj.getString("AddressLine") + ", " +
                            responseObj.getInt("PostalCode") + ", " +
                            responseObj.getString("City") + ", " +
                            stateArray[responseObj.getInt("StateID")-1];
                    userAddressTV.setText(userAddressTV.getText() + fullAddress);
                    progressBar.setVisibility(View.GONE);
                    reportLL.setVisibility(View.VISIBLE);
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getBurnAddress(int id){
        String url = endpoint + "/api/addresses/" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    String fullAddress = responseObj.getString("AddressLine") + ", " +
                            responseObj.getInt("PostalCode") + ", " +
                            responseObj.getString("City") + ", " +
                            stateArray[responseObj.getInt("StateID")-1];
                    burningAddressTV.setText(burningAddressTV.getText() + fullAddress);
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);

    }
    private void getResponse(){
        String url = endpoint + "/api/report/responses?ReportID=" + reportID;
        Log.d("response URL: ",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseObj) {
                try {
                    actionTakenTV.setText(actionTakenTV.getText() + responseObj.getString("ActionTaken"));
                    officerRemarkTV.setText(officerRemarkTV.getText() + responseObj.getString("Remark"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "no response", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
    private void getAssignedOfficerName(){
        String url = endpoint+ "/api/officer/report/" + reportID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseObj) {
                try {
                    officerAssignedTV.setText(officerAssignedTV.getText() + responseObj.getString("OfficerName"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            //if does not have response
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "get officer name error", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}