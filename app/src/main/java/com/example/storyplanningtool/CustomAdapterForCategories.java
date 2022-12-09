package com.example.storyplanningtool;


import static com.example.storyplanningtool.R.drawable.ic_cat_char;
import static com.example.storyplanningtool.R.drawable.ic_cat_creature;
import static com.example.storyplanningtool.R.drawable.ic_cat_event;
import static com.example.storyplanningtool.R.drawable.ic_cat_item;
import static com.example.storyplanningtool.R.drawable.ic_cat_place;
import static com.example.storyplanningtool.R.drawable.ic_cat_race;
import static com.example.storyplanningtool.R.drawable.ic_cat_script;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterForCategories extends RecyclerView.Adapter<CustomAdapterForCategories.MyViewHolder> {
    private final RecyclerViewClickListener cc;


    ArrayList<String> categoryNames;

    Context ctx;


    //constructor
    public CustomAdapterForCategories(Context ctx, ArrayList<String> catNames, RecyclerViewClickListener cc) {
        this.categoryNames = catNames;
        this.ctx = ctx;
        this.cc = cc;
    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForCategories.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new MyViewHolder(v, cc);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String itemName = categoryNames.get(position);

        holder.name.setText(itemName);


        switch (itemName) {
            case "Race":
                holder.imageView.setImageResource(ic_cat_race);
                break;
            case "Place":
                holder.imageView.setImageResource(ic_cat_place);
                break;
            case "Creature":
                holder.imageView.setImageResource(ic_cat_creature);
                break;
            case "Item":
                holder.imageView.setImageResource(ic_cat_item);
                break;
            case "Character":
                holder.imageView.setImageResource(ic_cat_char);
                break;
            case "Event":
                holder.imageView.setImageResource(ic_cat_event);
                break;
            case "Script":
                holder.imageView.setImageResource(ic_cat_script);
                break;
        }


    }

    @Override
    public int
    getItemCount() {
        return categoryNames.size();
    }


    public static class
    MyViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        AppCompatImageView imageView;

        public MyViewHolder(@NonNull View itemView, RecyclerViewClickListener ccc) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.item_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ccc != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            ccc.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }


}
