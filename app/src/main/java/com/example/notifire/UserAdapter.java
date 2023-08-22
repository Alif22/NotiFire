package com.example.notifire;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // creating a variable for array list and context.
    private ArrayList<UserModal> users;
    private Context context;


    // creating a constructor for our variables.
    public UserAdapter(ArrayList<UserModal> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        // setting data to our views of recycler view.
        UserModal modal = users.get(position);
        holder.UserID.setText("UserID: "+ modal.getId());
        holder.UserName.setText("User name: "+ modal.getName());
        holder.UserIDNum.setText("User IC/Passport number: "+ modal.getIDNumber());
    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our views.
        TextView UserID, UserName, UserIDNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // initializing our views with their ids.
            UserID = itemView.findViewById(R.id.UserIDTV);
            UserName = itemView.findViewById(R.id.UserNameTV);
            UserIDNum = itemView.findViewById(R.id.UserIDNumTV);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AdminSelectReport.class);
                    intent.putExtra("UserID",users.get(getAdapterPosition()).getId());
                    Log.d("UserID",Integer.toString(users.get(getAdapterPosition()).getId()));
                    context.startActivity(intent);
                    //go to a new select user
                }
            });

        }
    }
}

