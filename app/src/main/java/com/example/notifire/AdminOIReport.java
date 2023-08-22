package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminOIReport extends AppCompatActivity {
    private RecyclerView reportRV;
    private ProgressBar progressBar;
    private ArrayList<ReportModal> reports;
    private ReportAdapter adapter;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_oireport);
        reportRV = findViewById(R.id.reportRV);
        progressBar = findViewById(R.id.idPB);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        reports = new ArrayList<>();
        getAllReports();
        buildRecyclerView();
    }

    private void getAllReports() {
        String url = endpoint + "/api/reports?Status=on investigation";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                reportRV.setVisibility(View.VISIBLE);
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
                Toast.makeText(getApplicationContext(), "Fail to get data,please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private void buildRecyclerView() {
        adapter = new ReportAdapter(reports, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        reportRV.setHasFixedSize(true);
        reportRV.setLayoutManager(manager);
        reportRV.setAdapter(adapter);
    }
}