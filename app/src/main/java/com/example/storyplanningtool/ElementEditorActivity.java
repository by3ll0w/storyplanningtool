package com.example.storyplanningtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class ElementEditorActivity extends AppCompatActivity implements DButtonClickListener{


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
    ArrayList<String> traitIDs=new ArrayList<>();
    ArrayList<String> traitNames=new ArrayList<>();
    ArrayList<String> traitDetails=new ArrayList<>();

    //Widgets
    Toolbar toolbar;
    TextView titleView;
    EditText nameView;
    RecyclerView rV;
    FloatingActionButton buttonAdd;
    CustomAdapterForTraits_edit cA;

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
               createNewTrait();

            }
        });

        nameView.setText(elementNames.get(selectedElementIndex));
        updateView();

    }

    private void createNewTrait(){
        String id="t"+df.format(Calendar.getInstance().getTime());
        String namePrefix="New Trait";
        int number=1;
        String n=namePrefix;

        if(!traitNames.isEmpty()){
            for(int i=0;i<traitNames.size();i++){
                if(n.equals(traitNames.get(i))){
                    number++;
                    n=namePrefix+" "+number;
                    i=-1;
                }

            }
        }


        traitNames.add(n);
        traitIDs.add(id);
        traitDetails.add("");

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

        //load traits with names
        try {
            JSONObject o = new JSONObject(loadJSONFile( subDirect + categoryName+"/"+"traits" ));

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

        //load trait description of selected element
        try {
            JSONObject o = new JSONObject(loadJSONFile( subDirect + categoryName+"/"+elementIDs.get(selectedElementIndex) ));

            JSONArray traitArray = o.getJSONArray("traits");
            int availableItems = traitArray.length();

            for (int i = 0; i < availableItems; i++) {
                JSONObject attributes = traitArray.getJSONObject(i);
                String id=attributes.getString("id");
               for (int j=0;j<traitIDs.size();j++){
                   if (id.equals(traitIDs.get(j))){
                       traitDetails.add(attributes.getString("detail"));
                   }
                }



            }

        } catch (
                JSONException e) {
            e.printStackTrace();

        }



    }

    private void updateView(){

    cA=new CustomAdapterForTraits_edit(ElementEditorActivity.this,traitNames,traitIDs,traitDetails,this);
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

        //update traits file.
        String directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
                categoryName + "/traits.json";
        try {
            FileWriter fw = new FileWriter(directory);
            fw.write("{\n\t\"traits\": [");
            for (int i = 0; i < traitIDs.size(); i++) {
                fw.append("\n\t\t{" +

                        "\n\t\t\"id\":\"" + traitIDs.get(i) + "\"" + "," +
                        "\n\t\t\"name\":\"" + traitNames.get(i) + "\"" +
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


    //update element file.
     directory = Environment.getExternalStorageDirectory() +
                "/" + mainFolder +
                "/" + projectID +
                subDirect +
                categoryName + "/"+elementIDs.get(selectedElementIndex)+".json";
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

    private boolean checkIfUnused(String id){

        boolean xx=true;
        for (int i=0;i<elementIDs.size();i++){
            if (i!=selectedElementIndex){
                try {
                    JSONObject o = new JSONObject(loadJSONFile( subDirect + categoryName+"/"+elementIDs.get(i) ));

                    JSONArray traitArray = o.getJSONArray("traits");
                    int availableItems = traitArray.length();

                    for (int j = 0; j < availableItems; j++) {
                        JSONObject attributes = traitArray.getJSONObject(j);
                        String dd=attributes.getString("id");
                        if(dd.equals(id)){
                            if(!attributes.getString("detail").isBlank()){
                                return false;
                            }
                        }


                    }

                } catch (
                        JSONException e) {
                    e.printStackTrace();

                }
            }

        }

        return xx;
    }

    private boolean checkIfEmptyOrDuplicate(String s){
        boolean x=false;
        if(s.isBlank()){
            return true;
        }else{
            for(int i=0;i<elementNames.size();i++){
                if(s.equals(elementNames.get(i))) {
                    return true;
                }
            }
        }

        return x;
    }

    private void deleteTrait(String id){
        traitDetails.remove(traitDetails.get(traitIDs.indexOf(id)));
        traitNames.remove(traitNames.get(traitIDs.indexOf(id)));
        traitIDs.remove(id);


        updateView();
    }


    @Override
    public void onDButtonClick(int pos) {
        obtainValues();
        //Toast.makeText(getApplicationContext(),String.valueOf(pos),Toast.LENGTH_SHORT).show();
        if(elementIDs.size()<2){
            deleteTrait(traitIDs.get(pos));
        }else{
            if(checkIfUnused(traitIDs.get(pos))){
                deleteTrait(traitIDs.get(pos));
            }else{
                Toast.makeText(getApplicationContext(),"Unable to delete trait. Other elements are using it.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String s= String.valueOf(nameView.getText());

        if(!checkIfEmptyOrDuplicate(s)){
            elementNames.set(selectedElementIndex,s);
        }

        obtainValues();
        updateFiles();
    }

    public void obtainValues(){
        traitNames=cA.newTraitNames;
        traitDetails=cA.newTraitContents;
    }




}