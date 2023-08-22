package com.example.notifire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;

public class AdminAssignOfficer extends AppCompatActivity {
    private RecyclerView officerRV;
    private ProgressBar progressBar;
    private ArrayList<OfficerModal> officers;
    private OfficerAdapter adapter;
    private ArrayList<Integer> checkedOfficer;
    private int reportID;
    private int adminID;
    private TextView successTV;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Assign Officer");
        setContentView(R.layout.activity_admin_assign_officer);
        officerRV = findViewById(R.id.officerRV);
        progressBar = findViewById(R.id.idPB);
        officers = new ArrayList<>();
        checkedOfficer = new ArrayList<>();
        successTV = findViewById(R.id.successTV);
        //getreportID from extra
        reportID = getIntent().getIntExtra("ReportID",0);
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
        //getAdmin id from userpref
        SharedPreferences userSharedPreferences = getSharedPreferences("UserInfoPref",Context.MODE_PRIVATE);
        adminID = userSharedPreferences.getInt("UserID", 0);
        getOfficerData();
        buildRecyclerView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.assign_officer_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.confirmText:
                buildRecyclerView();
                progressBar.setVisibility(View.VISIBLE);
                officerRV.setVisibility(View.GONE);
                String url = endpoint + "/api/responses?ReportID="+reportID+"&AdminID="+adminID;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        updateReportStatus();
                        for (int officerID : checkedOfficer) {
                            postDesignation(officerID);
                        }
                        ActionBar actionBar = getSupportActionBar();
                        actionBar.hide();
                        progressBar.setVisibility(View.GONE);
                        successTV.setVisibility(View.VISIBLE);
                        //Toast.makeText(getApplicationContext(), "Assignment success", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Fail to assign. Please try again", Toast.LENGTH_LONG).show();
                    }
                });

                QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    public void updateReportStatus(){
        String url = endpoint + "/api/reports/"+ reportID;
        JSONObject PUTData = new JSONObject();
        try {
            PUTData.put("ReportStatus","on investigation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, PUTData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Toast.makeText(getApplicationContext(), "Response created successfully", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(), "Address update failed. Please try again", Toast.LENGTH_LONG).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
    private void buildRecyclerView() {
        adapter = new OfficerAdapter(officers,checkedOfficer, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        officerRV.setHasFixedSize(true);
        officerRV.setLayoutManager(manager);
        officerRV.setAdapter(adapter);
    }
    public void getOfficerData(){
        String url = endpoint + "/api/officers/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                officerRV.setVisibility(View.VISIBLE);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject responseObj = response.getJSONObject(i);

                        int id = responseObj.getInt("OfficerID");
                        String name = responseObj.getString("OfficerName");
                        String department = responseObj.getString("Department");
                        int availability = responseObj.getInt("Availability");
                        String available;
                        if(availability==1){ available = "Yes"; }else{available = "No";}
                        officers.add(new OfficerModal(id,name,department,available));
                        buildRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Fail to get the data", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void postDesignation(int officerID){
        String url = endpoint + "/api/designations?ReportID="+reportID+"&OfficerID="+officerID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
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