package com.example.storyplanningtool;


import android.content.Context;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapterForScriptComponents_edit extends RecyclerView.Adapter<CustomAdapterForScriptComponents_edit.MyViewHolder> {
    private final DButtonClickListener bc;
    ArrayList<String> s1;

    public ArrayList<String> segmentIDs;
    ArrayList<String> segmentTypes;
    ArrayList<String> selectedCharIDs;

    ArrayList<String> segmentContents;


    public ArrayList<String> newSegmentContents;
    public ArrayList<String> newSegmentTypes;
    public ArrayList<String> newSelectedIDs;

   ArrayList<String> characterIDs;
   ArrayList<String> characterNames;

    Context ctx;


    //constructor
    public CustomAdapterForScriptComponents_edit
    (Context ctx,
     ArrayList<String> selectedTypes,
     ArrayList<String> ids,
     ArrayList<String> contents,
     ArrayList<String> characterIDs,
     ArrayList<String> characters,
     ArrayList<String> selectedIds,
     DButtonClickListener bc) {

        this.segmentTypes = selectedTypes;
        this.segmentIDs = ids;
        this.segmentContents = contents;

        this.selectedCharIDs = selectedIds;
        this.characterIDs = characterIDs;
        this.characterNames = characters;

       if(!characterIDs.contains("")){
           this.characterIDs.add("");
           this.characterNames.add("Placeholder Character");
       }



        s1=new ArrayList<>();
        s1.add("Narration");
        s1.add("Dialog");

        newSegmentContents=this.segmentContents;
        newSegmentTypes=this.segmentTypes;
        newSelectedIDs=this.selectedCharIDs;

        this.ctx = ctx;
        this.bc = bc;
    }


    //functions
    @NonNull
    @Override
    public CustomAdapterForScriptComponents_edit.MyViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_script_item_edit, parent, false);
        return new MyViewHolder(v, bc);
    }

    @Override
    public void
    onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ArrayAdapter<String> a1=new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item,s1);
        holder.s1.setAdapter(a1);
        ArrayAdapter<String> a2=new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item,characterNames);
        holder.s2.setAdapter(a2);

        holder.s1.setSelection(s1.indexOf(newSegmentTypes.get(holder.getBindingAdapterPosition())));


        if(characterIDs.contains(selectedCharIDs.get(holder.getBindingAdapterPosition()))){
            holder.s2.setSelection(characterIDs.indexOf(newSelectedIDs.get(holder.getBindingAdapterPosition())));
        } else{
            holder.s2.setSelection(characterIDs.indexOf(""));
        }


        holder.s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {
              String ss=holder.s1.getItemAtPosition(p).toString();


              newSegmentTypes.set(holder.getBindingAdapterPosition(),ss);

              if(ss.equals("Narration")){
                  holder.s2.setVisibility(View.GONE);
              }else{
                  holder.s2.setVisibility(View.VISIBLE);
              }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        holder.s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int p ,long id) {
                String sd=holder.s2.getItemAtPosition(p).toString();


                newSelectedIDs.set(holder.getBindingAdapterPosition(),characterIDs.get(characterNames.indexOf(sd)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });







        holder.desc.setText(segmentContents.get(position));

        holder.desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                newSegmentContents.set(holder.getBindingAdapterPosition(), String.valueOf(holder.desc.getText()));
            }
        });

    }

    @Override
    public int
    getItemCount() {
        return segmentIDs.size();
    }


    public static class
    MyViewHolder extends RecyclerView.ViewHolder {
        Spinner s1;
        Spinner s2;
        EditText desc;
        ImageButton deleteButton;






        public MyViewHolder(@NonNull View itemView, DButtonClickListener bb) {
            super(itemView);
            s1 = itemView.findViewById(R.id.spinner);
            s2 = itemView.findViewById(R.id.spinner2);

            desc = itemView.findViewById(R.id.editContent);
            deleteButton = itemView.findViewById(R.id.deleteButton);
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
