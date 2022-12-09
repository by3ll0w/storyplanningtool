package com.example.storyplanningtool;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ElementDetailActivity extends AppCompatActivity {

    //variables
    int selectedIndex;

    String mainFolder;
    String projectName;
    String projectID;
    String categoryName;
    String categoryID;
    String subDirect;

    //ArrayLists
    ArrayList<String> elementNames = new ArrayList<>();
    ArrayList<String> elementIDs = new ArrayList<>();

    ArrayList<String> traitIDs = new ArrayList<>();
    ArrayList<String> traitNames = new ArrayList<>();
    ArrayList<String> traitDetails = new ArrayList<>();


    //Widgets
    Toolbar toolbar;
    TextView titleView;
    RecyclerView rV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        rV = findViewById(R.id.detaillist);
        rV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        toolbar = findViewById(R.id.menu_toolbar);
        titleView = findViewById(R.id.toolbar_title);


        selectedIndex = getIntent().getIntExtra("index", 0);
        mainFolder = getIntent().getStringExtra("Folder");
        projectName = getIntent().getStringExtra("Project");
        projectID = getIntent().getStringExtra("ProjectID");
        categoryName = getIntent().getStringExtra("Category");
        categoryID = getIntent().getStringExtra("CatID");
        subDirect = getIntent().getStringExtra("SubFolder");

        loadStuff();

if(!traitIDs.isEmpty()){
    updateElementFile();
}
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setupViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        menu.getItem(0).setTitle("Edit");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete this " + categoryName + "?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteElement();
                                finish();

                            }
                        }).show();
                break;

            }
            default: {
                openEditor();
                break;
            }

        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        elementIDs.clear();
        elementNames.clear();
        traitIDs.clear();
        traitNames.clear();
        traitDetails.clear();
        loadStuff();
        setupViews();
    }

    protected void setupViews() {
        ArrayList<String>visibleNames=new ArrayList<>();
        ArrayList<String>visibleDetails=new ArrayList<>();
        for(int i=0;i<traitIDs.size();i++){
            if(!traitDetails.get(i).isBlank()){
                visibleNames.add(traitNames.get(i));
                visibleDetails.add(traitDetails.get(i));
            }
        }


        titleView.setText(elementNames.get(selectedIndex));
        CustomAdapterForTraits_disp cA=new CustomAdapterForTraits_disp(ElementDetailActivity.this,visibleNames,visibleDetails);
        rV.setAdapter(cA);
    }


    protected void openEditor() {
        Intent intent = new Intent(getApplicationContext(), ElementEditorActivity.class);
        intent.putExtra("Folder", mainFolder);
        intent.putExtra("Project", projectName);
        intent.putExtra("ProjectID", projectID);
        intent.putExtra("Category", categoryName);
        intent.putExtra("CategoryID", categoryID);
        intent.putExtra("index", selectedIndex);
        intent.putExtra("SubFolder", subDirect);

        startActivity(intent);
    }

    private void updateElementListFile() {
        //update all elements
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
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


    private void loadStuff() {

        //Load all elements
        try {
            JSONObject o = new JSONObject(loadJSONFile(subDirect + categoryName));

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

        //load traits with names
        try {
            JSONObject o = new JSONObject(loadJSONFile(subDirect + categoryName + "/" + "traits"));

            JSONArray traitArray = o.getJSONArray("traits");
            int availableItems = traitArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = traitArray.getJSONObject(i);
                traitIDs.add(attributes.getString("id"));
                traitNames.add(attributes.getString("name"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }


        ArrayList<String> tmpArray = new ArrayList<>();



            for (int i = 0; i < traitIDs.size(); i++) {
                tmpArray.add("");
            }

            //load trait description of selected element
            try {
                JSONObject o = new JSONObject(loadJSONFile(subDirect + categoryName + "/" + elementIDs.get(selectedIndex)));

                JSONArray traitArray = o.getJSONArray("traits");
                int availableItems = traitArray.length();
                if (availableItems > 0) {
                    for (int i = 0; i < availableItems; i++) {
                        JSONObject attributes = traitArray.getJSONObject(i);
                        String id = attributes.getString("id");
                        for (int j = 0; j < traitIDs.size(); j++) {
                            if (id.equals(traitIDs.get(j))) {
                                tmpArray.set(j, attributes.getString("detail"));
                            }
                        }

                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();

            }







        traitDetails = tmpArray;

    }

    private void updateElementFile(){
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
                categoryName + "/" +elementIDs.get(selectedIndex)+".json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"traits\": [");
            for (int i = 0; i < traitIDs.size(); i++) {
                fw.append("\n\t\t{" +

                        "\n\t\t\"id\":\"" + traitIDs.get(i) + "\"" + "," +
                        "\n\t\t\"detail\":\"" + traitDetails.get(i) + "\"" +
                        "\n\t\t}");
                if (i < (traitIDs.size() - 1)) {
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


    private void deleteElement() {
        String fileDir =
                Environment.getExternalStorageDirectory() +
                        "/" + mainFolder +
                        "/" + projectID +
                        subDirect + categoryName +
                        "/" + elementIDs.get(selectedIndex) + ".json";
        File f = new File(fileDir);
        f.delete();
        elementIDs.remove(selectedIndex);
        elementNames.remove(selectedIndex);
        updateElementListFile();

    }

    private String loadJSONFile(String n) {
        String j;
        try {
            String fileDir = Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + n + ".json";

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

}