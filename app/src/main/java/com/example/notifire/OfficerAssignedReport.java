package com.example.notifire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OfficerAssignedReport extends AppCompatActivity {
    private RecyclerView reportRV;
    private ProgressBar progressBar;
    private ArrayList<ReportModal> reports;
    private TextView noReportTV;
    private int userID;
    private String endpoint;
    private ReportAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Assigned reports");
        setContentView(R.layout.activity_officer_assigned_report);
        reportRV = findViewById(R.id.reportRV);
        progressBar = findViewById(R.id.idPB);
        noReportTV = findViewById(R.id.noReportTV);
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
        SharedPreferences userSharedPreferences = getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);
        if (userSharedPreferences.contains("UserID")) {
            userID = userSharedPreferences.getInt("UserID", 0);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        reports = new ArrayList<>();
        getReports(userID);
        buildRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.LogOutItem:
                //clear shared preferences
                SharedPreferences userSharedPref = getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSharedPref.edit();
                editor.clear();
                editor.apply();
                //go to login page
                intent = new Intent(OfficerAssignedReport.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void buildRecyclerView() {
        adapter = new ReportAdapter(reports, OfficerAssignedReport.this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        reportRV.setHasFixedSize(true);
        reportRV.setLayoutManager(manager);
        reportRV.setAdapter(adapter);
    }
    private void getReports(int userID) {
        String url = endpoint + "/api/assignedreport/" + userID;
        RequestQueue queue = Volley.newRequestQueue(OfficerAssignedReport.this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                if(response.length() == 0){
                    noReportTV.setVisibility(View.VISIBLE);
                }else{
                    reportRV.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject responseObj = response.getJSONObject(i);
                        int id = responseObj.getInt("ReportID");
                        int addressid = responseObj.getInt("AddressID");
                        int categoryid = responseObj.getInt("CategoryID");
                        String comment = responseObj.getString("Comment");
                        String status = responseObj.getString("ReportStatus");
                        String timeAndDate = responseObj.getString("created_at").toString();
                        Log.d("status: ",status);
                        Log.d("timeAndDate: ",timeAndDate);
                        Log.d("comment: ",comment);
                        reports.add(new ReportModal(id,addressid,categoryid,comment,status,timeAndDate) );
                        buildRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OfficerAssignedReport.this, "Fail to get the data. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
}