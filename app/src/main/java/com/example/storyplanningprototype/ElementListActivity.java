package com.example.storyplanningprototype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ElementListActivity extends AppCompatActivity implements RecyclerViewClickListener, InputDialog.DiaologListener {
    //variables
    String mainFolder;
    String projectName;
    String categoryName;


    //ArrayLists
    ArrayList<String> elementNames=new ArrayList<>();
    ArrayList<String> elementDetails=new ArrayList<>();

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
        projectName=getIntent().getStringExtra("Project");
        categoryName=getIntent().getStringExtra("Category");
        String msg="You have no "+categoryName+"s.";

        //setup views
        recyclerView=findViewById(R.id.recyclerView);
        emptyView=findViewById(R.id.empty_view);
        emptyView.setText(msg);
        buttonAdd=findViewById(R.id.button_add_project);
        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        titleView.setText(projectName);
        subTitleView=findViewById(R.id.toolbar_sub_title);
        String subtitle=categoryName+"s";
        subTitleView.setText(subtitle);
        subTitleView.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        checkDirectories();
        updateView();


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();

            }
        });


    }




    @Override
    public void onItemClick(int position) {
        openDetails(position);
    }




    protected void onResume() {
        super.onResume();
        elementNames.clear();
        elementDetails.clear();
        updateView();
        if(elementNames.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    //Dialog Box
    public void openDialog(){
        InputDialog d =new InputDialog(categoryName);
        d.show(getSupportFragmentManager(),"dialog");
    }


    @Override
    public void applyTexts(String name) {
        if (name.isEmpty()){
            Toast.makeText(getApplicationContext(),"Unable to make Project. No name is entered.",Toast.LENGTH_SHORT).show();
        }else{
            if(Character.isLetter(name.charAt(0))){
                if (checkIfNameExist(name)){
                    Toast.makeText(getApplicationContext(),"Unable to make Project. Name already exists.",Toast.LENGTH_SHORT).show();
                }else{
                    //Code if valid
                    elementNames.add(name);
                    elementDetails.add("");
                    updateList();

                    if(elementNames.isEmpty()){
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                  openEditor(elementNames.indexOf(name));

                }
            }else{
                Toast.makeText(getApplicationContext(),"Unable to make Project. Name needs to start with a letter.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkIfNameExist(String n){
        int s=elementNames.size();
        boolean x=false;
        if(s==0){
            return false;
        }else{
            for(int i=0;i<s;i++){
                if( n.equals( elementNames.get(i) ) ){
                    x=true;
                    break;
                }
            }

            return x;
        }
    }


    //Open Activities
    protected void openEditor(int pos){
        Intent intent=new Intent(getApplicationContext(),EditorActivity.class);
        addExtras(intent,pos);
    }
    protected void openDetails(int pos){
        Intent intent=new Intent(getApplicationContext(),DetailActivity.class);
        addExtras(intent,pos);
    }

    protected void addExtras(Intent intent,int pos){
        intent.putExtra("Folder",mainFolder);
        intent.putExtra("Project",projectName);
        intent.putExtra("Category",categoryName);
        intent.putExtra("index",pos);
        intent.putStringArrayListExtra("ElementName",elementNames);
        intent.putStringArrayListExtra("ElementDetail",elementDetails);
        startActivity(intent);
    }



    //other methods
    public void updateView(){
        try{
        JSONObject o = new JSONObject(loadElementJSONFile());

        JSONArray elementArray = o.getJSONArray("elements");
        int availableItems=elementArray.length();

        for(int i=0;i<availableItems; i++){
            JSONObject attributes = elementArray.getJSONObject(i);
            elementNames.add(attributes.getString("name"));
            elementDetails.add(attributes.getString("details"));
        }

    }catch (
    JSONException e) {
        e.printStackTrace();

    }

    CustomAdapterForProjects cA = new CustomAdapterForProjects(ElementListActivity.this,elementNames,this);
        recyclerView.setAdapter(cA);

        if(elementNames.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    //File Management
    private void updateList(){
    String directory=Environment.getExternalStorageDirectory()+
            "/"+ mainFolder +
            "/"+projectName+
            "/"+ categoryName +".json";

    try{
        FileWriter fw=new FileWriter(directory);
        fw.write("{\n\t\"elements\": [");
        for (int i=0;i<elementNames.size();i++){
            fw.append("\n\t\t{" +
                    "\n\t\t\"name\":\""+elementNames.get(i)+"\"" +","+
                    "\n\t\t\"details\":\""+elementDetails.get(i)+"\"" +
                    "\n\t\t}");
            if(i<(elementNames.size()-1)){
                fw.append(",");
            }
        }

        fw.append( "\n\t]\n}");
        fw.flush();
        fw.close();
    }catch(IOException e) {
        finish();
    }
}

    private String loadElementJSONFile(){
        String j = null;
        try {
            String fileDir=Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/"+projectName+"/"+ categoryName +".json";
            InputStream iStream = new FileInputStream(fileDir);

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

    private void checkDirectories(){
        String fileDir=Environment.getExternalStorageDirectory()+
                "/"+ mainFolder +
                "/"+projectName+
                "/"+ categoryName +".json";
        File f=new File(fileDir);

        if(!f.exists()){
            try {
                FileWriter fw=new FileWriter(fileDir);
                fw.write("{\n\t\"elements\": [\n\t]\n}");
                fw.flush();
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}