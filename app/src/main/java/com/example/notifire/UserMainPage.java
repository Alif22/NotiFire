package com.example.notifire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class UserMainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main_page);
        Button fileReportButton = findViewById(R.id.fileReportButton);
        Button checkReportButton = findViewById(R.id.checkReportButton);
        checkReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainPage.this, UserReportHistory.class);
                startActivity(intent);
            }
            }
        );
        fileReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainPage.this, UserFileReport.class);
                startActivity(intent);
            }
        }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.usermainpage_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.UpProfileItem:
                intent = new Intent(UserMainPage.this, UserUpdateInfo.class);
                startActivity(intent);
                return true;
            case R.id.LogOutItem:
                //clear shared preferences
                SharedPreferences userSharedPref = getSharedPreferences("UserInfoPref",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSharedPref.edit();
                editor.clear();
                editor.apply();

                SharedPreferences reportSharedPref = getSharedPreferences("draftReportPref",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editorReport = reportSharedPref.edit();
                editorReport.clear();
                editorReport.apply();
                //go to login page
                intent = new Intent(UserMainPage.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}