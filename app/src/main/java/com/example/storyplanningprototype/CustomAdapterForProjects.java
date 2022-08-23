package com.example.storyplanningprototype;


import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterForProjects extends RecyclerView.Adapter<CustomAdapterForProjects.MyViewHolder>{
    private final RecyclerViewClickListener cc;



    ArrayList<String> projectNames;
    Context ctx;


    //constructor
    public CustomAdapterForProjects(Context ctx, ArrayList<String> projectNames, RecyclerViewClickListener cc) {
        this.projectNames = projectNames;
        this.ctx = ctx;
        this.cc=cc;
    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForProjects.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent , false);
        return new MyViewHolder(v,cc);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(projectNames.get(position));
    }

    @Override
    public int
    getItemCount() {
        return projectNames.size();
    }


    public static class
    MyViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        public MyViewHolder(@NonNull View itemView,RecyclerViewClickListener ccc) {
            super(itemView);

            name=itemView.findViewById(R.id.name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ccc!=null){
                        int pos = getBindingAdapterPosition();
                        if (pos!=RecyclerView.NO_POSITION){
                            ccc.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }




}
