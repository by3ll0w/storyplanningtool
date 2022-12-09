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

import java.io.FileInputStream;
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
                builder.setTitle("Delete this note?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // delete selected script


                                updateFile();
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
        return s;
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

    private void updateFile() {

    }

    private void loadData() {
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