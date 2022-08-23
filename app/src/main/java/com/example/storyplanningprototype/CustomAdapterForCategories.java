package com.example.storyplanningprototype;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterForCategories extends RecyclerView.Adapter<CustomAdapterForCategories.MyViewHolder>{
    private final RecyclerViewClickListener cc;



    ArrayList<String> categoryNames;
    Context ctx;


    //constructor
    public CustomAdapterForCategories(Context ctx, ArrayList<String> catNames, RecyclerViewClickListener cc) {
        this.categoryNames = catNames;
        this.ctx = ctx;
        this.cc=cc;
    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForCategories.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent , false);
        return new MyViewHolder(v,cc);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(categoryNames.get(position));
    }

    @Override
    public int
    getItemCount() {
        return categoryNames.size();
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
