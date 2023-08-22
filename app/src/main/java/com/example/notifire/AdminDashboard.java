package com.example.notifire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AdminDashboard extends AppCompatActivity {
    private LinearLayout dashboardLL;
    private ProgressBar progressBar;
    private TextView newReportCountTV;
    private TextView OIReportCountTV;
    private Button generateReportButton;
    ArrayList<Integer> reportCountForState = new ArrayList<>();
    ArrayList<Integer> reportCountForCategories = new ArrayList<>();
    private BarChart caseOverStateChart;
    private PieChart caseOverCategoryChart;
    protected final String[] stateStringList = new String[]{
            "Johor","Kedah","Kelantan","Melaka","N.Sembilan","Pahang","Penang","Perak","Perlis","Sabah", "Sarawak"
            , "Selangor", "Terengganu"
    };
    protected final String[] Categories = new String[] {
            "Residential area", "Religious activity","Forest/Bushes","Plantation","Industry","Disposal site",
            "Construction site"
    };
    private String endpoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        generateReportButton = (Button)findViewById(R.id.generateReportButton);
        LinearLayout LLallReport = (LinearLayout)findViewById(R.id.allReportLL);
        LinearLayout LLnewReport = (LinearLayout)findViewById(R.id.newReportLL);
        LinearLayout LLonInvestigationReport = (LinearLayout)findViewById(R.id.onInvestigationLL);
        newReportCountTV = findViewById(R.id.newReportCountTV);
        OIReportCountTV = findViewById(R.id.OIreportCountTV);
        dashboardLL = findViewById(R.id.adminDashboardLL);
        progressBar = findViewById(R.id.idPB);
        caseOverStateChart = findViewById(R.id.stateBarChart);
        caseOverCategoryChart = findViewById(R.id.categoryPieChart);
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
        generateReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileUrl = endpoint+ "/api/export/report";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                startActivity(browserIntent);
            }
        });
        LLallReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminAllReport.class);
                startActivity(intent);
            }
        });
        LLnewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminNewReport.class);
                startActivity(intent);
            }
        });
        LLonInvestigationReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminOIReport.class);
                startActivity(intent);
            }
        });
        initBarChart(caseOverStateChart);
        initPieChart(caseOverCategoryChart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getReportCount();
        getReportCountForState();
        getReportCountForCategory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){

            case R.id.LogOutItem:
                //clear shared preferences
                SharedPreferences userSharedPref = getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSharedPref.edit();
                editor.clear();
                editor.apply();
                //go to login page
                Intent intent = new Intent(AdminDashboard.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void getReportCount() {
        String url = endpoint + "/api/count/reports";
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("message: ","reached on response");
                progressBar.setVisibility(View.GONE);
                dashboardLL.setVisibility(View.VISIBLE);
                    try {
                        int newReportCount = response.getInt("newReportCount");
                        int OIReportCount = response.getInt("OIReportCount");
                        newReportCountTV.setText(String.valueOf(newReportCount));
                        OIReportCountTV.setText(String.valueOf(OIReportCount));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                dashboardLL.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Fail to get data, please try again", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);
    }
    public void initBarChart(BarChart chart){
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.setMaxVisibleValueCount(15);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setWordWrapEnabled(true);
    }
    public void initPieChart(PieChart chart){
        chart.getDescription().setEnabled(false);
        chart.setEntryLabelColor(Color.BLACK);
        Legend l2 = chart.getLegend();
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l2.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l2.setDrawInside(false);
        l2.setForm(Legend.LegendForm.SQUARE);
        l2.setFormSize(9f);
        l2.setTextSize(11f);
        l2.setXEntrySpace(4f);
        l2.setWordWrapEnabled(true);
    }
    public void getReportCountForState(){
        String url = endpoint + "/api/count/reports/state";
        JsonArrayRequest jsonObjRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {
                    for (int i=0;i <= response.length();i++){
                        try {
                            reportCountForState.add(response.getInt(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setBarData(caseOverStateChart);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Fail to get chart data", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);
    }

    private void setBarData(BarChart chart) {

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < stateStringList.length; i++) {
            values.add(new BarEntry(i,  reportCountForState.get(i).floatValue()));
        }

        BarDataSet set1;

        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();

        } else {

            int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            List<GradientColor> gradientFills = new ArrayList<>();
            gradientFills.add(new GradientColor(endColor1, endColor1));

            set1 = new BarDataSet(values, "States");
            set1.setDrawIcons(false);
            set1.setGradientColors(gradientFills);


            XAxis xAxis = chart.getXAxis();
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(stateStringList)); //setting the state list

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            chart.setData(data);
        }
    }
    public void getReportCountForCategory(){
        String url = endpoint + "/api/count/reports/category";
        JsonArrayRequest jsonObjRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response != null) {
                    for (int i=0;i<response.length();i++){
                        try {
                            reportCountForCategories.add(response.getInt(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setPieData(caseOverCategoryChart);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Fail to get chart data", Toast.LENGTH_SHORT).show();
            }
        });
        QueueSingleton.getInstance(this).addToRequestQueue(jsonObjRequest);
    }
    private void setPieData(PieChart chart) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < Categories.length ; i++) {
            if(reportCountForCategories.get(i)>0) {
                entries.add(new PieEntry(reportCountForCategories.get(i).floatValue(), Categories[i]));
            }
        }


        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);



        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        //number inside the chart
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
    }
}