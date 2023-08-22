package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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

public class AdminManageUser extends AppCompatActivity {
    private RecyclerView userRV;
    private ProgressBar progressBar;
    private ArrayList<UserModal> users;
    private UserAdapter adapter;
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_user);
        userRV = findViewById(R.id.userRV);
        progressBar = findViewById(R.id.idPB);
        users = new ArrayList<>();
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
        getUsersData();
    }
    private void getUsersData() {
        String url = endpoint + "/api/user_/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                userRV.setVisibility(View.VISIBLE);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject responseObj = response.getJSONObject(i);

                        int id = responseObj.getInt("UserID");
                        String name = responseObj.getString("UserName");
                        String idnum = responseObj.getString("UserIDNumber");

                        users.add(new UserModal(id,name,idnum));
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
        adapter = new UserAdapter(users, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        userRV.setHasFixedSize(true);
        userRV.setLayoutManager(manager);
        userRV.setAdapter(adapter);
    }
}