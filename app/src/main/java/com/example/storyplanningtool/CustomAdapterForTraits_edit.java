package com.example.storyplanningtool;


import android.content.Context;
import android.sax.TextElementListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterForTraits_edit extends RecyclerView.Adapter<CustomAdapterForTraits_edit.MyViewHolder> {
    private final DButtonClickListener bc;


    ArrayList<String> traitNames;

    ArrayList<String> traitContents;
    public ArrayList<String> traitIDs;
    public ArrayList<String> newTraitNames;
    public ArrayList<String> newTraitContents;

    Context ctx;


    //constructor
    public CustomAdapterForTraits_edit
    (Context ctx, ArrayList<String> names, ArrayList<String> ids, ArrayList<String> contents, DButtonClickListener bc) {

        this.traitNames = names;
        this.traitIDs = ids;
        this.traitContents = contents;

        newTraitNames=this.traitNames;
        newTraitContents=this.traitContents;

        this.ctx = ctx;
        this.bc = bc;
    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForTraits_edit.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_trait_item_edit, parent, false);
        return new MyViewHolder(v, bc);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(traitNames.get(position));
        holder.desc.setText(traitContents.get(position));
        holder.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newTraitNames.set(holder.getBindingAdapterPosition(),String.valueOf(holder.name.getText()));
            }
        });

        holder.desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newTraitContents.set(holder.getBindingAdapterPosition(),String.valueOf(holder.desc.getText()));
            }
        });

    }

    @Override
    public int
    getItemCount() {
        return traitIDs.size();
    }


    public static class
    MyViewHolder extends RecyclerView.ViewHolder {

        EditText name;
        EditText desc;
        ImageButton deleteButton;

        public MyViewHolder(@NonNull View itemView, DButtonClickListener bb) {
            super(itemView);

            name = itemView.findViewById(R.id.edittrait_name);
            desc = itemView.findViewById(R.id.edittrait_desc);
            deleteButton = itemView.findViewById(R.id.dButton_trait);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bb != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            bb.onDButtonClick(pos);
                        }
                    }


                }
            });


        }
    }



}
