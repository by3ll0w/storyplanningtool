package com.example.storyplanningtool;

import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;

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

public class EventEditorActivity extends AppCompatActivity {

    //variables
    int selectedIndex;

    String mainFolder;
    String projectName;
    String projectID;

    //ArrayLists
    ArrayList<String> noteTitles=new ArrayList<>();
    ArrayList<String> noteContents=new ArrayList<>();
    ArrayList<String> noteIDs=new ArrayList<>();

    //Widgets
    Toolbar toolbar;
    TextView titleView;
    EditText field1;
    EditText field2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        field1=findViewById(R.id.note_title);
        field2=findViewById(R.id.note_content);

        selectedIndex= getIntent().getIntExtra("index",0);
        mainFolder=getIntent().getStringExtra("Folder");
        projectName=getIntent().getStringExtra("Project");
        projectID=getIntent().getStringExtra("ProjectID");

        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        String tt="Edit Note";
        titleView.setText(tt);

        loadData();
        field1.setText(noteTitles.get(selectedIndex));
        field2.setText(noteContents.get(selectedIndex));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        save();
        finish();
    }

    public void save(){
        String s1=field1.getText().toString();
        String s2=field2.getText().toString();
        if (!s1.isEmpty()){
            if(!checkIfNameExist(s1)){
                //Code if valid
                noteTitles.set(selectedIndex,s1);
            }
        }
        noteContents.set(selectedIndex,s2);
        updateList();
    }


    private boolean checkIfNameExist(String n){
        int s=noteTitles.size();
        boolean x=false;
        if(s==0){
            return false;
        }else{
            for(int i=0;i<s;i++){
                if( n.equals( noteTitles.get(i) ) ){
                    x=true;
                    break;
                }
            }

            return x;
        }
    }

    private void updateList(){
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