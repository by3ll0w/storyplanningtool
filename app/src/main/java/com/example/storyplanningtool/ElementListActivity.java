package com.example.storyplanningtool;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ElementListActivity extends AppCompatActivity implements RecyclerViewClickListener, InputDialog.DialogListener, InputDialog3.DialogListener {
    //variables
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    String mainFolder;
    String projectName;
    String projectID;
    String categoryName;
    String categoryID;

    String subFolder;

    boolean bb;


    //ArrayLists
    ArrayList<String> elementNames = new ArrayList<>();
    ArrayList<String> elementIDs = new ArrayList<>();

    ArrayList<String> catNames = new ArrayList<>();
    ArrayList<String> catIDs = new ArrayList<>();
    ArrayList<Boolean> catDefaults = new ArrayList<>();

    //widgets
    RecyclerView recyclerView;
    TextView emptyView;
    FloatingActionButton buttonAdd;
    Toolbar toolbar;
    TextView titleView;
    TextView subTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //set variables
        mainFolder = getIntent().getStringExtra("Folder");
        projectName = getIntent().getStringExtra("Project");
        projectID = getIntent().getStringExtra("ProjectID");
        categoryName = getIntent().getStringExtra("Category");
        categoryID = getIntent().getStringExtra("CatID");
        if (categoryID.contains("Pcat")) {
            subFolder = "/";
        } else {
            subFolder = "/elements/";
        }

        updateCategories();
        bb = getIntent().getBooleanExtra("isdefault", false);

        String msg = "There is nothing here.";

        //setup views
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_view);
        emptyView.setText(msg);
        buttonAdd = findViewById(R.id.button_add_project);
        toolbar = findViewById(R.id.menu_toolbar);
        titleView = findViewById(R.id.toolbar_title);
        titleView.setText(projectName);
        subTitleView = findViewById(R.id.toolbar_sub_title);
        String subtitle = categoryName;
        subTitleView.setText(subtitle);
        subTitleView.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        checkDirectories();
        updateElements();


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDialog();

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean bool = getIntent().getBooleanExtra("isdefault", false);
        if (bool) {
            getMenuInflater().inflate(R.menu.option_menu, menu);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete this Category?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCategory();
                                finish();

                            }
                        }).show();
                break;

            }
            default: {
                openRenameDialog();
                break;
            }

        }
        return true;
    }


    @Override
    public void onItemClick(int position) {
        openDetails(position);
    }


    protected void onResume() {
        super.onResume();
        elementIDs.clear();
        elementNames.clear();
        updateElements();
        if (elementNames.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    //Dialog Box
    public void openAddDialog() {
        InputDialog d = new InputDialog(categoryName);
        d.show(getSupportFragmentManager(), "dialog");
    }

    public void openRenameDialog() {
        InputDialog3 d = new InputDialog3("Category", categoryName);
        d.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void applyTexts(String name) {
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Unable to make Item. No name is entered.", Toast.LENGTH_SHORT).show();
        } else {

                if (checkIfNameExist(name)) {
                    Toast.makeText(getApplicationContext(), "Unable to make Item. Name already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    //Code if valid

                    String id = "e" + f.format(Calendar.getInstance().getTime());
                    elementIDs.add(id);
                    elementNames.add(name);
                    updateElementListFile();
                    createFiles();

                    if (elementNames.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }


                }

        }
    }

    @Override
    public void catRenameApplyTexts(String name) {
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Unable to rename category. No name is entered.", Toast.LENGTH_SHORT).show();
        } else {
            if (Character.isLetter(name.charAt(0))) {
                if (name == categoryName) {
                    Toast.makeText(getApplicationContext(), "No changes made.", Toast.LENGTH_SHORT).show();
                } else if (checkOtherCategoryNames(name)) {
                    Toast.makeText(getApplicationContext(), "Unable to rename category. Name already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    //Code if valid
                    renameCategory(name);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Unable to rename category. Name needs to start with a letter.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private boolean checkIfNameExist(String n) {
        int s = elementNames.size();
        boolean x = false;
        if (s == 0) {
            return false;
        } else {
            for (int i = 0; i < s; i++) {
                if (n.equals(elementNames.get(i))) {
                    x = true;
                    break;
                }
            }

            return x;
        }
    }

    private boolean checkOtherCategoryNames(String n) {
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
        if (x == false) {
            for (int i = 0; i < catNames.size(); i++) {
                if (n.equals(catNames.get(i))) {
                    x = true;
                    break;
                }
            }

        }
        return x;
    }


    //Open Activities
    protected void openDetails(int pos) {
        Intent intent;
        switch (categoryID) {
            case "Pcat02":
                intent = new Intent(getApplicationContext(), EventDetailActivity.class);
                addExtras(intent, pos);
                break;
            case "Pcat03":
                intent = new Intent(getApplicationContext(), ScriptDetailActivity.class);
                addExtras(intent, pos);
                break;
            default:
                intent = new Intent(getApplicationContext(), ElementDetailActivity.class);
                addExtras(intent, pos);
                break;
        }



    }


    protected void addExtras(Intent intent, int pos) {
        intent.putExtra("Folder", mainFolder);
        intent.putExtra("Project", projectName);
        intent.putExtra("ProjectID",projectID);
        intent.putExtra("Category", categoryName);
        intent.putExtra("CatID",categoryID);
        intent.putExtra("SubFolder", subFolder);
        intent.putExtra("index", pos);

        startActivity(intent);
    }


    //other methods
    public void updateElements() {


        try {
            JSONObject o = new JSONObject(loadJSONFile(projectID + subFolder + categoryName));

            JSONArray elementArray = o.getJSONArray("elements");
            int availableItems = elementArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = elementArray.getJSONObject(i);
                elementIDs.add(attributes.getString("id"));
                elementNames.add(attributes.getString("name"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }
        CustomAdapterForItems cA = new CustomAdapterForItems(ElementListActivity.this, elementNames, this);
        recyclerView.setAdapter(cA);

        if (elementNames.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    public void updateCategories() {
        try {
            JSONObject o = new JSONObject(loadJSONFile(projectID + "/categories"));
            JSONArray elementArray = o.getJSONArray("categories");
            int availableItems = elementArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = elementArray.getJSONObject(i);
                catNames.add(attributes.getString("name"));
                catIDs.add(attributes.getString("id"));
                catDefaults.add(attributes.getBoolean("default"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }
    }

    public void renameCategory(String name) {

        File oldDir = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + categoryName + ".json");
        File newDir = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + name + ".json");

        oldDir.renameTo(newDir);

        File oldDirFolder = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + categoryName);
        File newDirFolder = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + name);

        oldDirFolder.renameTo(newDirFolder);


        catNames.set(catNames.indexOf(categoryName), name);
        categoryName = name;

        subTitleView.setText(name);
        updateCategoryListFile();
    }

    public void deleteCategory() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + categoryName + ".json");
        dir.delete();
        File dir2 = new File(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + subFolder + categoryName);
        deleteRecursive(dir2);
        catNames.remove(categoryName);
        updateCategoryListFile();

    }


    //File Management
    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursive(c);
            }
        }
        f.delete();
    }

    private void updateElementListFile() {
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subFolder +
                categoryName + ".json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"elements\": [");
            for (int i = 0; i < elementNames.size(); i++) {
                fw.append("\n\t\t{" +

                        "\n\t\t\"id\":\"" + elementIDs.get(i) + "\"" + "," +
                        "\n\t\t\"name\":\"" + elementNames.get(i) + "\"" +
                        "\n\t\t}");
                if (i < (elementNames.size() - 1)) {
                    fw.append(",");
                }
            }

            fw.append("\n\t]\n}");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            finish();
        }
    }

    private void updateCategoryListFile() {
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                "/categories.json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"categories\": [");
            for (int i = 0; i < catNames.size(); i++) {
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\"" + catIDs.get(i) + "\"" + "," +
                        "\n\t\t\"name\":\"" + catNames.get(i) + "\"" + "," +
                        "\n\t\t\"default\":\"" + catDefaults.get(i).toString() + "\"" +
                        "\n\t\t}");
                if (i < (catNames.size() - 1)) {
                    fw.append(",");
                }
            }

            fw.append("\n\t]\n}");
            fw.flush();
            fw.close();
        } catch (IOException e) {

            finish();
        }
    }

    private String loadJSONFile(String filename) {
        String j;
        try {
            String fileDir = Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + filename + ".json";
            InputStream iStream = new FileInputStream(fileDir);

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

    private void checkDirectories() {
        String fileDir = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subFolder + categoryName;

        File f = new File(fileDir);

        if (!f.exists()) {
            f.mkdirs();
            try {
                FileWriter fw = new FileWriter(fileDir + ".json");
                fw.write("{\n\t\"elements\": [\n\t]\n}");
                fw.flush();
                fw.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void createFiles() {
        String ss="";
        if(categoryID.equals("Pcat03")){
            ss="segments";
        }else{
            ss="traits";
        }


        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subFolder +
                categoryName +"/"+ elementIDs.get(elementIDs.size() - 1) + ".json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\""+ss+"\": [");
            fw.append("\n\t]\n}");
            fw.flush();
            fw.close();
        } catch (IOException e) {

            finish();
        }


        if(!(categoryID.equals("Pcat02")||categoryID.equals("Pcat03"))){
            directory = Environment.getExternalStorageDirectory() +
                    "/" + mainFolder +
                    "/" + projectID +
                    subFolder +
                    categoryName + "/traits.json";

            File f = new File(directory);
            if (!f.exists()) {
                try {
                    FileWriter fw = new FileWriter(directory);
                    fw.write("{\n\t\"traits\": [");
                    fw.append("\n\t]\n}");
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    finish();
                }
            }
        }


    }


}