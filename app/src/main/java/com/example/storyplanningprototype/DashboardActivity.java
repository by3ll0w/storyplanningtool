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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements InputDialog.DiaologListener {
    String mainFolder;
    String projectName;
    Bundle bundle=new Bundle();

    ArrayList<String> projectNames=new ArrayList<>();
    ArrayList<String> noteList=new ArrayList<>();


    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    TextView toolbarTitle;

    WorldbuildingFragment wFrag=new WorldbuildingFragment();
    StoryPlanningFragment sFrag=new StoryPlanningFragment();
    NotesFragment nFrag=new NotesFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //retrieve data
        projectName= getIntent().getStringExtra("Name");
        mainFolder=getIntent().getStringExtra("Folder");
        projectNames=getIntent().getExtras().getStringArrayList("ProjectList");

        //checkDirectory
        checkDirectories();

        //save data to bundles
        bundle.putString("Stuff",projectName);
        bundle.putString("MainFolder",mainFolder);

        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.sub_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(projectName);

        bottomNavigationView=findViewById(R.id.navi);
        wFrag.setArguments(bundle);
        sFrag.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_container,wFrag).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.worldbuilding:
                        getSupportFragmentManager().beginTransaction().replace(R.id.menu_container,wFrag).commit();
                        return true;
                    case R.id.storyplanning:
                        getSupportFragmentManager().beginTransaction().replace(R.id.menu_container,sFrag).commit();
                        return true;
                    case R.id.notes:
                        getSupportFragmentManager().beginTransaction().replace(R.id.menu_container,nFrag).commit();
                        return true;

                }
                return false;
            }
        });

    }


    //Enable Menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       switch(item.getItemId()){
           case R.id.action_delete:{
             AlertDialog.Builder builder=new AlertDialog.Builder(this);
             builder.setTitle("Delete this Project?")
                     .setNegativeButton("No", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {

                         }
                     }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                            deleteProject();
                            finish();

                         }
                     }).show();
             break;

           }
           default:{
                openDialog();
                break;
           }

       }
        return true;
    }




    //Dialog Box
    public void openDialog(){
        InputDialog d =new InputDialog("Project",projectName);
        d.show(getSupportFragmentManager(),"dialog");
    }

    @Override
    public void applyTexts(String name) {
        if (name.isEmpty()){
            Toast.makeText(getApplicationContext(),"Unable to rename Project. No name is entered.",Toast.LENGTH_SHORT).show();
        }else{
            if(Character.isLetter(name.charAt(0))){
                if (name==projectName){
                    Toast.makeText(getApplicationContext(),"No changes made.",Toast.LENGTH_SHORT).show();
                }else if(checkIfNameExist(name)){
                    Toast.makeText(getApplicationContext(),"Unable to make Project. Name already exists.",Toast.LENGTH_SHORT).show();
                }
                else{
                    //Code if valid
                    renameProject(name);
                }
            }else{
                Toast.makeText(getApplicationContext(),"Unable to make Project. Name needs to start with a letter.",Toast.LENGTH_SHORT).show();
            }

        }

    }


    //Check if input text exists
    private boolean checkIfNameExist(String n){
        int s=projectNames.size();
        boolean x=false;
        if(s==0){
            return false;
        }else{
            for(int i=0;i<s;i++){
                if( n.equals( projectNames.get(i) ) ){
                    x=true;
                    break;
                }
            }

            return x;
        }
    }

    //File Management

    private void checkDirectories(){
        File f=new File(Environment.getExternalStorageDirectory()+"/"+ mainFolder+"/"+projectName);
        if(!f.exists()){
            f.mkdirs();
        }


    }
    private void deleteProject(){
        File f=new File(Environment.getExternalStorageDirectory()+"/"+ mainFolder+"/"+projectName);
        deleteRecursive(f);
        projectNames.remove(projectName);
        updateProjectList();
    }
    private void deleteRecursive(File f){
            if (f.isDirectory()){
                for (File c: f.listFiles()){
                    deleteRecursive(c);
                }
            }
            f.delete();
    }

    private void renameProject(String name){

        File oldDir=new File(Environment.getExternalStorageDirectory()+"/"+ mainFolder+"/"+projectName);
        File newDir=new File(Environment.getExternalStorageDirectory()+"/"+ mainFolder+"/"+name);
        oldDir.renameTo(newDir);

        //
        projectNames.set(projectNames.indexOf(projectName), name);
        projectName=name;

        //update title
        toolbarTitle.setText(name);

        //update bundle
        bundle.remove("Stuff");
        bundle.putString("Stuff",projectName);

        updateProjectList();
    }

    private void updateProjectList(){
        String directory=Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/projects.json";
        File f=new File(directory);
        try{
            FileWriter fw=new FileWriter(directory);
            fw.write("{\n\t\"projects\": [");
            for (int i=0;i<projectNames.size();i++){
                fw.append("\n\t\t{" +
                        "\n\t\t\"name\":\""+projectNames.get(i)+"\"" +
                        "\n\t\t}");
                if(i<(projectNames.size()-1)){
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
}