package com.example.storyplanningtool;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class ScriptEditorActivity extends AppCompatActivity implements DButtonClickListener{


    //variables
    int selectedElementIndex;
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    String mainFolder;
    String projectName;
    String projectID;
    String categoryName;
    String categoryID;
    String subDirect;

    //ArrayLists

    ArrayList<String> elementNames = new ArrayList<>();
    ArrayList<String> elementIDs=new ArrayList<>();

    ArrayList<String> segmentIDs=new ArrayList<>();
    ArrayList<String> segmentTypes=new ArrayList<>();
    ArrayList<String> segmentContents=new ArrayList<>();
    ArrayList<String> selectedCharacterIDs=new ArrayList<>();

    ArrayList<String> characterIDs=new ArrayList<>();
    ArrayList<String> characterNames=new ArrayList<>();



    //Widgets
    Toolbar toolbar;
    TextView titleView;
    EditText nameView;
    RecyclerView rV;
    FloatingActionButton buttonAdd;
    CustomAdapterForScriptComponents_edit cA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_editor);

        rV=findViewById(R.id.editorlist);
        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        nameView=findViewById(R.id.edit_element_name);
        buttonAdd = findViewById(R.id.button_add_trait);
        rV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        selectedElementIndex = getIntent().getIntExtra("index", 0);
        mainFolder = getIntent().getStringExtra("Folder");
        projectName = getIntent().getStringExtra("Project");
        projectID=getIntent().getStringExtra("ProjectID");
        categoryName = getIntent().getStringExtra("Category");
        categoryID = getIntent().getStringExtra("CatID");
        subDirect = getIntent().getStringExtra("SubFolder");

        loadStuff();
        String tt="Edit "+categoryName;
        titleView.setText(tt);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               createNewSegment();

            }
        });

        nameView.setText(elementNames.get(selectedElementIndex));
        updateView();

    }

    private void createNewSegment(){
        String id="sg"+df.format(Calendar.getInstance().getTime());

        segmentIDs.add(id);
        segmentContents.add("");

        // Toast.makeText(getApplicationContext(),traitNames.get(traitNames.size()-1),Toast.LENGTH_SHORT).show();

       updateView();
    }

    private void loadStuff() {
        //Load all elements
        try {
            JSONObject o = new JSONObject(loadJSONFile( subDirect + categoryName));

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
            JSONObject o = new JSONObject(loadJSONFile( subDirect + categoryName+"/"+elementIDs.get(selectedElementIndex)));

            JSONArray traitArray = o.getJSONArray("segments");
            int availableItems = traitArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = traitArray.getJSONObject(i);
                segmentIDs.add(attributes.getString("id"));
                segmentTypes.add(attributes.getString("type"));
                segmentTypes.add(attributes.getString("selectedCharID"));
                segmentContents.add(attributes.getString("content"));
            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }

        //load characters
        try {
            JSONObject o = new JSONObject(loadJSONFile( subDirect + "Character"));

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

    private void updateView(){

    cA=new CustomAdapterForScriptComponents_edit(ScriptEditorActivity.this,segmentTypes,segmentIDs,segmentContents,this);
        rV.setAdapter(cA);
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


    private void updateFiles(){

    //update element file.
   String  directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
                categoryName + "/"+elementIDs.get(selectedElementIndex)+".json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"segments\": [");
            for (int i = 0; i < segmentIDs.size(); i++) {
                fw.append("\n\t\t{" +

                        "\n\t\t\"id\":\"" + segmentIDs.get(i) + "\"" + "," +
                        "\n\t\t\"type\":\"" + segmentIDs.get(i) + "\"" + "," +
                        "\n\t\t\"selectedCharacterID\":\"" + segmentIDs.get(i) + "\"" + "," +
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

        //update element list file

         directory = Environment.getExternalStorageDirectory() +
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



    private void deleteTrait(String id){
        segmentContents.remove(segmentContents.get(segmentIDs.indexOf(id)));
        segmentTypes.remove(segmentTypes.get(segmentIDs.indexOf(id)));
        segmentIDs.remove(id);


        updateView();
    }


    @Override
    public void onDButtonClick(int pos) {
        obtainValues();
        //Toast.makeText(getApplicationContext(),String.valueOf(pos),Toast.LENGTH_SHORT).show();
        deleteTrait(segmentIDs.get(pos));


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String s= String.valueOf(nameView.getText());



        obtainValues();
        updateFiles();
    }

    public void obtainValues(){
        segmentTypes=cA.newTraitNames;
        segmentContents=cA.newTraitContents;
    }




}