package com.example.storyplanningtool;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class ScriptDetailActivity extends AppCompatActivity {

    //variables
    int selectedIndex;

    String mainFolder;
    String projectName;
    String projectID;
    String categoryName;
    String categoryID;
    String subDirect;

    //Arraylists
    ArrayList<String> elementNames = new ArrayList<>();
    ArrayList<String> elementIDs = new ArrayList<>();

    ArrayList<String> segmentIDs = new ArrayList<>();
    ArrayList<String> segmentTypes = new ArrayList<>();
    ArrayList<String> segmentContents = new ArrayList<>();

    ArrayList<String> selectedCharacterIDs = new ArrayList<>();

    ArrayList<String> characterIDs = new ArrayList<>();
    ArrayList<String> characterNames = new ArrayList<>();


    //Widgets
    Toolbar toolbar;
    TextView titleView;
    TextView contentDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_fulltext);
        toolbar = findViewById(R.id.menu_toolbar);
        titleView = findViewById(R.id.toolbar_title);
        contentDisplay = findViewById(R.id.txt_content);


        selectedIndex = getIntent().getIntExtra("index", 0);
        mainFolder = getIntent().getStringExtra("Folder");
        projectName = getIntent().getStringExtra("Project");
        projectID = getIntent().getStringExtra("ProjectID");
        categoryName = getIntent().getStringExtra("Category");
        categoryID = getIntent().getStringExtra("CatID");
        subDirect = getIntent().getStringExtra("SubFolder");

        loadCharacters();
        loadData();
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
                builder.setTitle("Delete this script?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete selected script
                                deleteStuff();

                                updateListFile();
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
        //clear stuff
        elementIDs.clear();
        elementNames.clear();
        segmentIDs.clear();
        segmentContents.clear();
        segmentTypes.clear();
        selectedCharacterIDs.clear();


        loadData();
        titleView.setText(elementNames.get(selectedIndex));
        contentDisplay.setText(setScript());
    }

    protected void setupViews() {
        titleView.setText(elementNames.get(selectedIndex));
        contentDisplay.setText(setScript());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected String setScript() {
        String s = "";

        for (int ii=0;ii<segmentIDs.size();ii++){
            String charName="";
            String content;

            //Check if Dialog
            if(segmentTypes.get(ii).equals("Dialog")){
                if(selectedCharacterIDs.get(ii).equals("")){
                        charName="???";
                }else{
                        charName=characterNames.get(characterIDs.indexOf(selectedCharacterIDs.get(ii)));
                }
                charName+=": \t";

            }

            //check if content is empty
            if(segmentContents.get(ii).isBlank()){
                content="[...]";
            }else{
                content=segmentContents.get(ii);
            }


            s+=charName+content;

            s+="\n\n";
        }






        return s;
    }

    protected void deleteStuff(){
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

    }





    protected void openEditor() {
        Intent intent = new Intent(getApplicationContext(), ScriptEditorActivity.class);

        intent.putExtra("Folder", mainFolder);
        intent.putExtra("Project", projectName);
        intent.putExtra("ProjectID", projectID);
        intent.putExtra("Category", categoryName);
        intent.putExtra("CategoryID", categoryID);
        intent.putExtra("index", selectedIndex);
        intent.putExtra("SubFolder", subDirect);


        startActivity(intent);
    }

    private void updateScriptFile() {
        String  directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
                categoryName + "/"+elementIDs.get(selectedIndex)+".json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"segments\": [");
            for (int i = 0; i < segmentIDs.size(); i++) {
                fw.append("\n\t\t{" +

                        "\n\t\t\"id\":\"" + segmentIDs.get(i) + "\"" + "," +
                        "\n\t\t\"type\":\"" + segmentTypes.get(i) + "\"" + "," +
                        "\n\t\t\"selectedCharID\":\"" + selectedCharacterIDs.get(i) + "\"" + "," +
                        "\n\t\t\"content\":\"" + segmentContents.get(i) + "\"" +
                        "\n\t\t}");
                if (i < (segmentIDs.size() - 1)) {
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

    private void updateListFile(){
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



    private void loadData() {

        //load all scripts from list
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

        //load segments from selected script
        try {
            JSONObject o = new JSONObject(loadJSONFile(subDirect + categoryName + "/" + elementIDs.get(selectedIndex)));

            JSONArray traitArray = o.getJSONArray("segments");
            int availableItems = traitArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = traitArray.getJSONObject(i);
                segmentIDs.add(attributes.getString("id"));
                segmentTypes.add(attributes.getString("type"));
                selectedCharacterIDs.add(attributes.getString("selectedCharID"));
                segmentContents.add(attributes.getString("content"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }


    }


    private void loadCharacters() {
        //load characters
        try {
            JSONObject o = new JSONObject(loadJSONFile(subDirect + "Character"));

            JSONArray elementArray = o.getJSONArray("elements");
            int availableItems = elementArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = elementArray.getJSONObject(i);
                characterIDs.add(attributes.getString("id"));
                characterNames.add(attributes.getString("name"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }
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