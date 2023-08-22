package com.example.notifire;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class OfficerAdapter extends RecyclerView.Adapter<OfficerAdapter.ViewHolder> {

    // creating a variable for array list and context.
    private ArrayList<OfficerModal> officers;
    private ArrayList<Integer> checkedOfficer;
    private Context context;


    // creating a constructor for our variables.
    public OfficerAdapter(ArrayList<OfficerModal> officers,ArrayList<Integer> checkedOfficer, Context context) {
        this.officers = officers;
        this.context = context;
        this.checkedOfficer = checkedOfficer;
    }

    @NonNull
    @Override
    public OfficerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.officer_row, parent, false);
        return new OfficerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfficerAdapter.ViewHolder holder, int position) {
        // setting data to our views of recycler view.
        OfficerModal modal = officers.get(position);
        holder.OfficerID.setText("Officer ID: " + modal.getOfficerID());
        holder.OfficerName.setText("Officer Name: "+  modal.getOfficerName());
        holder.Department.setText("Department: "+ modal.getDepartment());
        holder.Availability.setText("Availability: "+ modal.getAvailibility());
    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return officers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our views.
        TextView OfficerID, OfficerName, Department, Availability;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            OfficerID = itemView.findViewById(R.id.OfficerIDTV);
            OfficerName = itemView.findViewById(R.id.OfficerNameTV);
            Department = itemView.findViewById(R.id.DepartmentTV);
            Availability = itemView.findViewById(R.id.AvailabilityTV);
            checkBox = itemView.findViewById(R.id.checkbox);
           //added to list
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        checkedOfficer.add(officers.get(getAdapterPosition()).getOfficerID());
                    }
                    else{
                        checkedOfficer.removeIf(officerID -> officerID.equals(officers.get(getAdapterPosition()).getOfficerID()) );
                    }
                }
            });

        }
    }
}
