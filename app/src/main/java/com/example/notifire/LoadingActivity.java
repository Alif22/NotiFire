package com.example.notifire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

public class LoadingActivity extends AppCompatActivity {
    SharedPreferences userSharedPreferences;
    Integer userID;
    String userType = "";
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        userSharedPreferences = getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);
        if(userSharedPreferences.contains("UserID")){
            userID = userSharedPreferences.getInt("UserId",0);
        }
        if(userSharedPreferences.contains("UserType")){
            userType = userSharedPreferences.getString("UserType","");
        }
        switch(userType) {
                case "Officer":
                    intent = new Intent(LoadingActivity.this, OfficerAssignedReport.class);
                    startActivity(intent);
                    break;
                case "Admin":
                    intent = new Intent(LoadingActivity.this, AdminDashboard.class);
                    startActivity(intent);
                    break;
                case "User":
                case "Guest":
                    intent = new Intent(LoadingActivity.this, UserMainPage.class);
                    startActivity(intent);
                    break;
                default:
                    intent = new Intent(LoadingActivity.this, LoginActivity.class);
                    startActivity(intent);
                    break;
        }
    }
}