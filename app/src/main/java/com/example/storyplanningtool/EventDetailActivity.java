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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EventDetailActivity extends AppCompatActivity {

    //variables
    int selectedIndex;

    String mainFolder;
    String projectName;
    String projectID;

    //Arraylists
    ArrayList<String> noteTitles = new ArrayList<>();
    ArrayList<String> noteContents = new ArrayList<>();
    ArrayList<String> noteIDs = new ArrayList<>();

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

                                noteTitles.remove(noteTitles.get(selectedIndex));
                                noteIDs.remove(noteIDs.get(selectedIndex));
                                noteContents.remove(noteContents.get(selectedIndex));

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
        noteTitles.clear();
        noteIDs.clear();
        noteContents.clear();
        loadData();
        titleView.setText(noteTitles.get(selectedIndex));
        contentDisplay.setText(noteContents.get(selectedIndex));
    }

    protected void setupViews() {
        titleView.setText(noteTitles.get(selectedIndex));
        contentDisplay.setText(noteContents.get(selectedIndex));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected void openEditor() {
        Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("Folder",mainFolder);
        intent.putExtra("Project",projectName);
        intent.putExtra("ProjectID",projectID);
        intent.putExtra("index",selectedIndex);
        startActivity(intent);
    }

    private void updateFile() {
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                "/notes.json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"notes\": [");
            for (int i = 0; i < noteIDs.size(); i++) {
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\"" + noteIDs.get(i) + "\"" + "," +
                        "\n\t\t\"title\":\"" + noteTitles.get(i) + "\"" + "," +
                        "\n\t\t\"content\":\"" + noteContents.get(i) + "\"" +
                        "\n\t\t}");
                if (i < (noteIDs.size() - 1)) {
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


        try {
            JSONObject o = new JSONObject(loadJSONFile());

            JSONArray jsonArray = o.getJSONArray("notes");
            int availableItems = jsonArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = jsonArray.getJSONObject(i);
                noteTitles.add(attributes.getString("title"));
                noteIDs.add(attributes.getString("id"));
                noteContents.add(attributes.getString("content"));
            }
        } catch (
                JSONException e) {
            e.printStackTrace();

        }


    }

    private String loadJSONFile() {
        String j;
        try {
            String fileDir =
                    Environment.getExternalStorageDirectory() +
                            "/" + mainFolder +
                            "/" + projectID +
                            "/notes.json";
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