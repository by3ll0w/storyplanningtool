package com.example.storyplanningtool;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class WorldbuildingFragment extends Fragment implements RecyclerViewClickListener, RecyclerViewClickListener2, InputDialog2.DialogListener {

    public String mainFolder;
    public String projectName;
    public String projectID;
    public ArrayList<String> categoryNames = new ArrayList<>();
    public ArrayList<String> categoryNames_custom = new ArrayList<>();
    public ArrayList<String> categoryIDs = new ArrayList<>();
    public ArrayList<String> categoryIDs_custom = new ArrayList<>();
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    RecyclerView recyclerView1;
    RecyclerView recyclerView2;
    TextView customCatLabel;
    FloatingActionButton buttonAdd;


    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worldbuilding, container, false);
        recyclerView1 = view.findViewById(R.id.recyclerView);
        recyclerView1.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        customCatLabel = view.findViewById(R.id.fragSubTitleW);
        buttonAdd = view.findViewById(R.id.button_add_cat);

        Bundle b = this.getArguments();
        if (b != null) {
            projectName = b.getString("PName");
            projectID = b.getString("PID");
            mainFolder = b.getString("MainFolder");

        }
        updateView();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });


        return view;
    }

    public void openDialog() {
        FragmentManager fm = getFragmentManager();
        InputDialog2 d = new InputDialog2("Category");
        d.setTargetFragment(WorldbuildingFragment.this, 1);
        d.show(fm, "dialog");
    }


    @Override
    public void onItemClick(int position) {
        // Toast.makeText(getContext(),"A "+ Integer.toHexString(position),Toast.LENGTH_SHORT).show();

        openElementList(categoryNames.get(position), categoryIDs.get(position), true);
    }

    @Override
    public void onCustomItemClick(int position) {
        // Toast.makeText(getContext(),"B "+ Integer.toHexString(position),Toast.LENGTH_SHORT).show();
        openElementList(categoryNames_custom.get(position), categoryIDs_custom.get(position), false);
    }


    public void openElementList(String name, String id, boolean isDefault) {
        Intent intent = new Intent(getContext(), ElementListActivity.class);
        intent.putExtra("Folder", mainFolder);
        intent.putExtra("Project", projectName);
        intent.putExtra("ProjectID", projectID);
        intent.putExtra("Category", name);
        intent.putExtra("CatID", id);
        intent.putExtra("isdefault", !isDefault);
        startActivity(intent);
    }


    public void updateView() {

        categoryNames.clear();
        categoryNames_custom.clear();
        categoryIDs.clear();
        categoryIDs_custom.clear();

        try {
            JSONObject o = new JSONObject(loadJSONFile(projectID + "/categories"));

            JSONArray catArray = o.getJSONArray("categories");
            int availableItems = catArray.length();
            for (int i = 0; i < availableItems; i++) {
                JSONObject catDetail = catArray.getJSONObject(i);
                String id = catDetail.getString("id");
                String name = catDetail.getString("name");
                boolean isDefault = Boolean.parseBoolean(catDetail.getString("default"));
                if (isDefault) {
                    categoryIDs.add(id);
                    categoryNames.add(name);
                } else {
                    categoryIDs_custom.add(id);
                    categoryNames_custom.add(name);
                }


            }

        } catch (JSONException e) {
            e.printStackTrace();

        }

        CustomAdapterForCategories cA = new CustomAdapterForCategories(this.getContext(), categoryNames, this);
        recyclerView1.setAdapter(cA);
        CustomAdapterForCategories_custom cA2 = new CustomAdapterForCategories_custom(this.getContext(), categoryNames_custom, this);
        recyclerView2.setAdapter(cA2);

        if(categoryNames_custom.isEmpty()){
            customCatLabel.setVisibility(View.GONE);
        }else{
            customCatLabel.setVisibility(View.VISIBLE);
        }
    }


    private String loadJSONFile(String s) {
        String j;
        try {
            InputStream iStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + s + ".json");

            int size = iStream.available();
            byte[] buffer = new byte[size];
            iStream.read(buffer);
            iStream.close();

            j = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }

        return j;
    }


    private void updateJSONFile() {
        String directory = Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + "/categories.json";

        try {
            FileWriter fw = new FileWriter(directory);

            fw.write("{\n\t\"categories\": [");

            for (int i = 0; i < categoryIDs.size(); i++) {
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\"" + categoryIDs.get(i) + "\"" + "," +
                        "\n\t\t\"name\":\"" + categoryNames.get(i) + "\"" + "," +
                        "\n\t\t\"default\":\"" + "true" + "\"" +
                        "\n\t\t}");
                if (i < (categoryIDs.size())) {
                    fw.append(",");
                }

            }

            for (int i = 0; i < categoryIDs_custom.size(); i++) {
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\"" + categoryIDs_custom.get(i) + "\"" + "," +
                        "\n\t\t\"name\":\"" + categoryNames_custom.get(i) + "\"" + "," +
                        "\n\t\t\"default\":\"" + "false" + "\"" +
                        "\n\t\t}");
                if (i < (categoryIDs_custom.size() - 1)) {
                    fw.append(",");
                }
            }

            fw.append("\n\t]\n}");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Unable to Load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void applyTextToFragment(String name) {

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Unable to make Category. No name is entered.", Toast.LENGTH_SHORT).show();
        } else if (checkIfNameExist(name)) {
            Toast.makeText(getContext(), "Unable to make Category. Name already exists", Toast.LENGTH_SHORT).show();
        } else {
            //Code if valid
            String id = "cat" + f.format(Calendar.getInstance().getTime());

            categoryIDs_custom.add(id);
            // Toast.makeText(getContext(),id,Toast.LENGTH_SHORT).show();
            categoryNames_custom.add(name);
            //  Toast.makeText(getContext(),name,Toast.LENGTH_SHORT).show();
            updateJSONFile();

        }
        updateView();
    }

    public boolean checkIfNameExist(String n) {

        boolean x = false;

        ArrayList<String> pCatNames = new ArrayList<>();
        try {
            JSONObject o = new JSONObject(loadJSONFile("plannerCategories"));
            JSONArray elementArray = o.getJSONArray("categories");
            int availableItems = elementArray.length();
            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = elementArray.getJSONObject(i);
                pCatNames.add(attributes.getString("name"));
            }
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        int s = pCatNames.size();
        if (s > 0) {
            for (int i = 0; i < s; i++) {
                if (n.equals(pCatNames.get(i))) {
                    x = true;
                    break;
                }
            }

        }
        if (!x) {
            for (int i = 0; i < categoryNames.size(); i++) {
                if (n.equals(categoryNames.get(i))) {
                    x = true;
                    break;
                }
            }
            if (!categoryNames_custom.isEmpty()) {
                for (int i = 0; i < categoryNames_custom.size(); i++) {
                    if (n.equals(categoryNames_custom.get(i))) {
                        x = true;
                        break;
                    }
                }

            }

        }
        return x;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

}
