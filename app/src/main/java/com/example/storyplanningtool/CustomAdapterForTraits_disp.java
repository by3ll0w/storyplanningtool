package com.example.storyplanningtool;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterForTraits_disp extends RecyclerView.Adapter<CustomAdapterForTraits_disp.MyViewHolder>{




    ArrayList<String> traitName;
    ArrayList<String> traitContent;

    Context ctx;


    //constructor
    public CustomAdapterForTraits_disp(Context ctx,ArrayList<String> itemNames,ArrayList<String> itemDesc) {
        this.traitName = itemNames;
        this.traitContent=itemDesc;
        this.ctx = ctx;

    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForTraits_disp.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_trait_item_display, parent , false);
        return new MyViewHolder(v);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(traitName.get(position));
        holder.desc.setText(traitContent.get(position));
    }

    @Override
    public int
    getItemCount() {
        return traitName.size();
    }


    public static class
    MyViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView desc;
        CardView card;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card=itemView.findViewById(R.id.card_view);
            name=itemView.findViewById(R.id.trait_name);
            desc=itemView.findViewById(R.id.trait_desc);
        }
    }




}
