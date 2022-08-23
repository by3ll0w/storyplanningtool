package com.example.storyplanningprototype;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class StoryPlanningFragment extends Fragment implements RecyclerViewClickListener {

    public String mainFolder;
    public String projectName;
    public ArrayList<String> categoryNames=new ArrayList<>();
    RecyclerView recyclerView;
    private int position;


    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_worldbuilding, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Bundle b=this.getArguments();
        if (b!=null){
            projectName =b.getString("Stuff");
            mainFolder=b.getString("MainFolder");
        }
        updateView();

        return view;
    }

    @Override
    public void onItemClick(int position) {
        openElementList(categoryNames.get(position));
    }
    public void openElementList(String strg){
        Intent intent=new Intent(getContext(),ElementListActivity.class);
        intent.putExtra("Folder",mainFolder);
        intent.putExtra("Project",projectName);
        intent.putExtra("Category",strg);
        startActivity(intent);
    }


    public void updateView(){
        categoryNames.clear();
        try{
            JSONObject o = new JSONObject(loadCategoryJSONFile());

            JSONArray catArray = o.getJSONArray("categories");
            int availableItems=catArray.length();
            for(int i=0;i<availableItems; i++){
                JSONObject catDetail = catArray.getJSONObject(i);
                categoryNames.add(catDetail.getString("name"));
            }

        }catch (JSONException e) {
            e.printStackTrace();

        }

        CustomAdapterForCategories cA = new CustomAdapterForCategories(this.getContext(),categoryNames,this);
        recyclerView.setAdapter(cA);

    }



    private String loadCategoryJSONFile(){
        String j = null;
        try {
            InputStream iStream = new FileInputStream(Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/categories2.json");

            int size= iStream.available();
            byte[] buffer =new byte[size];
            iStream.read(buffer);
            iStream.close();

            j=new String(buffer, "UTF-8");
        }catch(IOException e){
            return null;
        }

        return j;
    }


}