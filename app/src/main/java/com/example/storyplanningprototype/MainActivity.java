package com.example.storyplanningprototype;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
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

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener, InputDialog.DiaologListener {
    private int STORAGE_PERMISSION_CODE = 1;

    String mainFolder ="WorldbuildingPrototype";

    //Widget
    RecyclerView recyclerView;
    TextView emptyView;
    FloatingActionButton buttonAdd;
    Toolbar toolbar;
    TextView titleView;

    //ArrayList
    ArrayList<String> projectNames=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        recyclerView=findViewById(R.id.recyclerView);
        emptyView=findViewById(R.id.empty_view);
        buttonAdd=findViewById(R.id.button_add_project);
        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        requestPermission();
        checkMainDirectory();
        updateView();

        if(projectNames.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }


    buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        openDialog();

        }
    });

    }



    @Override
    public void onItemClick(int position) {
      openDashboard(projectNames.get(position));
    }



    public void updateView(){
        try{
            JSONObject o = new JSONObject(loadProjectJSONFile());

            JSONArray projectArray = o.getJSONArray("projects");
            int availableItems=projectArray.length();

            for(int i=0;i<availableItems; i++){
                JSONObject projectDetail = projectArray.getJSONObject(i);
                projectNames.add(projectDetail.getString("name"));
            }

        }catch (JSONException e) {
            e.printStackTrace();

        }

        CustomAdapterForProjects cA = new CustomAdapterForProjects(MainActivity.this,projectNames,this);
        recyclerView.setAdapter(cA);

    }

    public void openDashboard(String name){
        Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
        intent.putExtra("Name",name);
        intent.putExtra("Folder",mainFolder);
        intent.putStringArrayListExtra("ProjectList",projectNames);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        projectNames.clear();
        updateView();
        if(projectNames.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }


    //Dialog Box
    public void openDialog(){
        InputDialog d =new InputDialog("Project");
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
                    projectNames.add(name);

                    updateProjectList();
                    if(projectNames.isEmpty()){
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                    openDashboard(name);

                }
            }else{
                Toast.makeText(getApplicationContext(),"Unable to make Project. Name needs to start with a letter.",Toast.LENGTH_SHORT).show();
            }



        }

    }

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
    private void updateProjectList(){
        String directory=Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/projects.json";

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

    private String loadProjectJSONFile(){
        String j = null;
        try {
            InputStream iStream = new FileInputStream(Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/projects.json");

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

    private void checkMainDirectory(){
        String directory=Environment.getExternalStorageDirectory()+"/"+ mainFolder;

        File f=new File(directory);
        if(!f.exists()){
            f.mkdirs();

            importAssets(directory);
            checkMainDirectory();

        }else{
            String projectSubdirctory = directory+"/projects.json";
            f=new File(projectSubdirctory);
            if(!f.exists()){
                try{
                    FileWriter fw=new FileWriter(projectSubdirctory);
                    fw.write("{\n\t\"projects\": [\n\t]\n}");
                    fw.flush();
                    fw.close();
                }catch(IOException e){
                        finish();
                }

            }
        }


    }

    private void importAssets(String directory){
        try{

            InputStream iStream_asset=getAssets().open("categories.json");
            int size= iStream_asset.available();
            byte[] buffer =new byte[size];
            iStream_asset.read(buffer);
            iStream_asset.close();
            String j=new String(buffer, "UTF-8");

            FileWriter fw=new FileWriter(directory+"/categories.json");
            fw.write(j);
            fw.flush();
            fw.close();

        }catch (IOException e){
            e.printStackTrace();
        }

        try{

            InputStream iStream_asset=getAssets().open("categories2.json");
            int size= iStream_asset.available();
            byte[] buffer =new byte[size];
            iStream_asset.read(buffer);
            iStream_asset.close();
            String j=new String(buffer, "UTF-8");

            FileWriter fw=new FileWriter(directory+"/categories2.json");
            fw.write(j);
            fw.flush();
            fw.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }



    //Permissions
    private void requestPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){

            try{
                Log.d(TAG, "requestPermission: try");
                Intent intent=new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri u = Uri.fromParts("package",this.getPackageName(),null);
                intent.setData(u);
                storageActivityResultLauncher.launch(intent);

            }catch (Exception e){
                Log.e(TAG, "requestPermission: catch",e);
                Intent intent=new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }

        }else{
            ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_CODE);

        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.d(TAG,"onActivityResult:");

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                    if(Environment.isExternalStorageManager()){
                            Log.d(TAG,"onActivityResult: External storage permission granted.");
                        //check folder
                    }
                }else{

                }
            }
        }
        );

    public boolean checkPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }else{
            int write = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
            return write== PackageManager.PERMISSION_GRANTED && read== PackageManager.PERMISSION_GRANTED ;

        }
}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0){
                boolean write=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                boolean read=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                if(write&&read){
                    Log.d(TAG,"onRequestPermissionResult:  External storage permission granted.");
                    //check folder
                }else{

                }
            }

        }
    }
}