package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

public class AdminAllReport extends AppCompatActivity {
    private RecyclerView reportRV;
    private ProgressBar progressBar;
    private ArrayList<ReportModal> reports;
    private ReportAdapter adapter;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_report);
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
        String url = endpoint +"/api/reports";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                reportRV.setVisibility(View.VISIBLE);
                for (int i = 0; i < response.length(); i++) {
                    // creating a new json object and
                    // getting each object from our json array.
                    try {
                        // we are getting each json object.
                        JSONObject responseObj = response.getJSONObject(i);

                        // now we get our response from API in json object format.
                        // in below line we are extracting a string with
                        // its key value from our json object.
                        // similarly we are extracting all the strings from our json object.
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
                Toast.makeText(getApplicationContext(), "Fail to get the data..", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private void buildRecyclerView() {

        // initializing our adapter class.
        adapter = new ReportAdapter(reports, getApplicationContext());

        // adding layout manager
        // to our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(this);
        reportRV.setHasFixedSize(true);

        // setting layout manager
        // to our recycler view.
        reportRV.setLayoutManager(manager);

        // setting adapter to
        // our recycler view.
        reportRV.setAdapter(adapter);
    }
}