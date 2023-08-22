package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class UserSelectReport extends AppCompatActivity {
    private TextView reportIDTV;
    private TextView reportDateAndTimeTV;
    private TextView reportAddressTV;
    private TextView statusTV;
    private TextView commentTV;
    private TextView attachmentTV;
    private LinearLayout attachmentLL;
    private int reportID ;
    private String endpoint;
    private String[] stateArray ;
    private LinearLayout reportLL;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select_report);
        reportLL = findViewById(R.id.reportInfoLL);
        progressBar = findViewById(R.id.idPB);
        reportID = getIntent().getIntExtra("ReportID",0);
        stateArray = getResources().getStringArray(R.array.state);
        reportIDTV = findViewById(R.id.reportIDTV);
        reportDateAndTimeTV = findViewById(R.id.reportTimeAndDateTV);
        reportAddressTV = findViewById(R.id.reportAddressTV);
        statusTV = findViewById(R.id.statusTV);
        commentTV = findViewById(R.id.commentTV);
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
        getReport();
    }
    public void getReport(){
        String url = endpoint  +"/api/reports/" + reportID ;
        Log.d("URL: ",url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int addressID;
                String addressUrl = endpoint + "/api/addresses/";
                try {
                    JSONObject responseObj = response.getJSONObject(0);
                    reportIDTV.setText(reportIDTV.getText() + responseObj.getString("ReportID"));
                    reportDateAndTimeTV.setText(reportDateAndTimeTV.getText() + responseObj.getString("created_at").toString());
                    statusTV.setText(statusTV.getText() + responseObj.getString("ReportStatus"));
                    commentTV.setText(commentTV.getText() + responseObj.getString("Comment"));
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
                    addressUrl += addressID;
                    getAddress(addressUrl);
                    Log.d("AddressID: ", String.valueOf(responseObj.getInt("AddressID")));
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Get report details error. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }
    public void getAddress(String url){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url,null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    progressBar.setVisibility(View.GONE);
                    reportLL.setVisibility(View.VISIBLE);
                    JSONObject responseObj = response.getJSONObject(0);
                    String address = responseObj.getString("AddressLine") + ", " +
                            responseObj.getInt("PostalCode") + ", " +
                            responseObj.getString("City") + ", " +
                            stateArray[responseObj.getInt("StateID")-1];
                    reportAddressTV.setText(reportAddressTV.getText() + address);
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
}