package com.example.notifire;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    // creating a variable for array list and context.
    private ArrayList<ReportModal> reports;
    private Context context;


    // creating a constructor for our variables.
    public ReportAdapter(ArrayList<ReportModal> reports, Context context) {
        this.reports = reports;
        this.context = context;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // below line is to inflate our layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        // setting data to our views of recycler view.
        ReportModal modal = reports.get(position);
        holder.status.setText("Status: "+ modal.getStatus());
        holder.dateAndTime.setText("Submitted on: "+ modal.getDateAndTime());
        holder.reportID.setText("ReportID: "+ Integer.toString(modal.getID()));
    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return reports.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our views.
        TextView reportID, status, dateAndTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // initializing our views with their ids.
            status = itemView.findViewById(R.id.statusTV);
            reportID = itemView.findViewById(R.id.reportIDTV);
            dateAndTime = itemView.findViewById(R.id.dateAndTimeTV);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(context,UserSelectReport.class);
                    Intent intent;
                    SharedPreferences userSharedPreferences = context.getSharedPreferences("UserInfoPref", Context.MODE_PRIVATE);
                    String userType = "";

                    if(userSharedPreferences.contains("UserType")){
                        userType = userSharedPreferences.getString("UserType","");
                    }
                    switch(userType) {
                        case "Officer":
                            intent = new Intent(context, OfficerSelectReport.class);
                            intent.putExtra("ReportID",reports.get(getAdapterPosition()).getID());
                            Log.d("ReportID",Integer.toString(reports.get(getAdapterPosition()).getID()));
                            context.startActivity(intent);
                            break;
                        case "Admin":
                            intent = new Intent(context, AdminSelectReport.class);
                            intent.putExtra("ReportID",reports.get(getAdapterPosition()).getID());
                            Log.d("ReportID",Integer.toString(reports.get(getAdapterPosition()).getID()));
                            context.startActivity(intent);
                            break;
                        case "User":
                        case "Guest":
                            intent = new Intent(context, UserSelectReport.class);
                            intent.putExtra("ReportID",reports.get(getAdapterPosition()).getID());
                            Log.d("ReportID",Integer.toString(reports.get(getAdapterPosition()).getID()));
                            context.startActivity(intent);
                            break;
                        default:
                            //intent = new Intent(context, LoginActivity.class);
                            //context.startActivity(intent);
                            Log.d("user type: ","no type");
                            break;
                    }
                    //go to UserSelectReport
                    //send reportID using put extra
                    //once you click the item
                }
            });
        }
    }
}
