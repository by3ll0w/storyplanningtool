package com.example.storyplanningprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    //variables
    int selectedIndex;

    String mainFolder;
    String projectName;
    String categoryName;

    //ArrayLists
    ArrayList<String> elementNames=new ArrayList<>();
    ArrayList<String> elementDetails=new ArrayList<>();

    //Widgets
    Toolbar toolbar;
    TextView titleView;
    TextView field1;
    TextView field2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        selectedIndex= getIntent().getIntExtra("index",0);
        mainFolder=getIntent().getStringExtra("Folder");
        projectName=getIntent().getStringExtra("Project");
        categoryName=getIntent().getStringExtra("Category");

       // elementNames=getIntent().getExtras().getStringArrayList("ElementName");
      //  elementDetails=getIntent().getExtras().getStringArrayList("ElementDetail");
        loadFiles();
        setupViews();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        menu.getItem(0).setTitle("Edit");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete:{
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Delete this "+categoryName+"?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                elementNames.remove(selectedIndex);
                                elementDetails.remove(selectedIndex);
                                updateFile();
                                finish();

                            }
                        }).show();
                break;

            }
            default:{
          openEditor();
                break;
            }

        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFiles();
        String s1=elementNames.get(selectedIndex);
        String s2=elementDetails.get(selectedIndex);
        field1.setText(s1);
        field2.setText(s2);
    }

    protected void setupViews(){
        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        titleView.setText(elementNames.get(selectedIndex));
        String s1=elementNames.get(selectedIndex);
        field1=findViewById(R.id.field1);
        field1.setText(s1);
        String s2=elementDetails.get(selectedIndex);
        field2=findViewById(R.id.field2);
        field2.setText(s2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }



    protected void openEditor(){
        Intent intent=new Intent(getApplicationContext(),EditorActivity.class);
        intent.putExtra("Folder",mainFolder);
        intent.putExtra("Project",projectName);
        intent.putExtra("Category",categoryName);
        intent.putExtra("index",selectedIndex);
        intent.putStringArrayListExtra("ElementName",elementNames);
        intent.putStringArrayListExtra("ElementDetail",elementDetails);
        startActivity(intent);
        finish();
    }

    private void updateFile(){
        String directory= Environment.getExternalStorageDirectory()+
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

    private void loadFiles(){
        elementDetails.clear();
        elementNames.clear();
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

}