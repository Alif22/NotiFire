package com.example.notifire;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class OfficerSelectReport extends AppCompatActivity {
    private TextView reportIDTV;
    private TextView reportDateAndTimeTV;
    private TextView reportUserNameTV;
    private TextView commentTV;
    private TextView categoryTV;
    private TextView reportAddressTV;
    private TextView userAddressTV;
    private TextView userContactTV;
    private EditText actionTakenET;
    private EditText remarkET;
    private TextView attachmentTV;
    private String endpoint;
    private LinearLayout attachmentLL;
    private Button updateResponseButton;
    private int reportID ;
    String[] stateArray ;
    String[] categoryArray ;
    private LinearLayout reportLL;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_select_report);
        reportID = getIntent().getIntExtra("ReportID",0);
        categoryArray = getResources().getStringArray(R.array.category);
        stateArray = getResources().getStringArray(R.array.state);
        reportLL = findViewById(R.id.reportInfoLL);
        progressBar = findViewById(R.id.idPB);
        reportIDTV = findViewById(R.id.reportIDTV);
        reportDateAndTimeTV = findViewById(R.id.reportDateAndTimeTV);
        reportUserNameTV = findViewById(R.id.reportUserNameTV);
        commentTV = findViewById(R.id.commentTV);
        categoryTV = findViewById(R.id.categoryTV);
        reportAddressTV = findViewById(R.id.burningAddressTV);
        userAddressTV = findViewById(R.id.userAddressTV);
        userContactTV = findViewById(R.id.userPhoneNumTV);
        actionTakenET = findViewById(R.id.actionTakenET);
        remarkET = findViewById(R.id.remarkET);
        attachmentTV = findViewById(R.id.attachmentTV);
        attachmentLL = findViewById(R.id.attachmentLL);
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
        updateResponseButton = (Button)findViewById(R.id.buttonUpdateResponse);
        updateResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateResponse();
            }
        });
        getReport(reportID);
    }
    public void getReport(int id){
        String url = endpoint + "/api/reports/" + id ;
        Log.d("URL: ",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int addressID;
                int userID;
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    reportIDTV.setText(reportIDTV.getText() + responseObj.getString("ReportID"));
                    reportDateAndTimeTV.setText(reportDateAndTimeTV.getText() + responseObj.getString("created_at").toString());
                    commentTV.setText(commentTV.getText() + responseObj.getString("Comment"));
                    categoryTV.setText(categoryTV.getText() + categoryArray[responseObj.getInt("CategoryID")-1]);
                    String fileName = responseObj.getString("MediaAttachment");
                    if(!fileName.equals("")){
                        attachmentLL.setVisibility(View.VISIBLE);
                        attachmentTV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String fileUrl = endpoint +"/files/"+fileName;
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
                Toast.makeText(getApplicationContext(), "Get report list error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getUser(int id){
        String url = endpoint+ "/api/user_/" + id ;
        Log.d("URL: ",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int addressID;
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    reportUserNameTV.setText(reportUserNameTV.getText() + responseObj.getString("UserName"));
                    userContactTV.setText(userContactTV.getText() + responseObj.getString("UserPhoneNumber"));
                    addressID = responseObj.getInt("AddressID");
                    getUserAddress(addressID);
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Unable to get user info. Please try again", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Error please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getBurnAddress(int id){
        String url = endpoint+ "/api/addresses/" + id;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    String fullAddress = responseObj.getString("AddressLine") + ", " +
                            responseObj.getInt("PostalCode") + ", " +
                            responseObj.getString("City") + ", " +
                            stateArray[responseObj.getInt("StateID")-1];
                    reportAddressTV.setText(reportAddressTV.getText() + fullAddress);
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
    private void updateResponse(){
        JSONObject PUTData = new JSONObject();
        try {
            PUTData.put("ActionTaken",actionTakenET.getText());
            PUTData.put("Remark",remarkET.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //get the response for this report
        String url = endpoint+ "/api/responses?ReportID=" + reportID;
        //update the response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, PUTData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //update report
                updateReportStatus();
                Toast.makeText(getApplicationContext(), "Response updated successfully", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Response update failed. Please try again", Toast.LENGTH_LONG).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }
    public void updateReportStatus(){
        String url = endpoint + "/api/reports/"+ reportID;
        JSONObject PUTData = new JSONObject();
        try {
            PUTData.put("ReportStatus","resolved");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, PUTData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Report updated successfully", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Report update failed. Please try again", Toast.LENGTH_LONG).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}