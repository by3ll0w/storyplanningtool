package com.example.storyplanningtool;

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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener, InputDialog.DialogListener {

    SimpleDateFormat idformat =new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private final int STORAGE_PERMISSION_CODE = 1;



    //Strings
    String mainFolder ="WorldbuildingTool";

    //Widget
    RecyclerView recyclerView;
    TextView emptyView;
    FloatingActionButton buttonAdd;
    Toolbar toolbar;
    TextView titleView;

    //ArrayList
    ArrayList<String> projectNames=new ArrayList<>();
    ArrayList<String> projectIDs=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

       if(!checkPermission()){
           requestPermission();
       }


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
      openDashboard(projectNames.get(position),projectIDs.get(position));
    }



    public void updateView(){
        try{
            JSONObject o = new JSONObject(loadProjectJSONFile());

            JSONArray projectArray = o.getJSONArray("projects");
            int availableItems=projectArray.length();

            for(int i=0;i<availableItems; i++){
                JSONObject projectDetail = projectArray.getJSONObject(i);
                projectIDs.add(projectDetail.getString("id"));
                projectNames.add(projectDetail.getString("name"));
            }

        }catch (JSONException e) {
            e.printStackTrace();

        }

        CustomAdapterForItems cA = new CustomAdapterForItems(MainActivity.this,projectNames,this);
        recyclerView.setAdapter(cA);

    }

    public void openDashboard(String name, String id){
        Intent intent=new Intent(getApplicationContext(),DashboardActivity.class);
        intent.putExtra("ID",id);
        intent.putExtra("Name",name);
        intent.putExtra("Folder",mainFolder);

        intent.putStringArrayListExtra("IDList",projectIDs);
        intent.putStringArrayListExtra("ProjectList",projectNames);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        projectNames.clear();
        projectIDs.clear();
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
            //If valid

          /* if(Character.isLetter(name.charAt(0))){*/
                if (checkIfNameExist(name)){
                    Toast.makeText(getApplicationContext(),"Unable to make Project. Name already exists.",Toast.LENGTH_SHORT).show();
                }else{

                    //Code if valid
                    String id="proj"+ idformat.format(Calendar.getInstance().getTime());

                    projectIDs.add(id);
               // Toast.makeText(getApplicationContext(),id,Toast.LENGTH_SHORT).show();
                    projectNames.add(name);

                    updateProjectList();
                    if(projectNames.isEmpty()){
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }

                    openDashboard(name,id);

               }
         /*   }else{
                Toast.makeText(getApplicationContext(),"Unable to make Project. Name needs to start with a letter.",Toast.LENGTH_SHORT).show();
            }*/

           //

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
            for (int i=0;i<projectIDs.size();i++){
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\""+projectIDs.get(i)+"\"" +","+
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
        String j;
        try {
            InputStream iStream = new FileInputStream(Environment.getExternalStorageDirectory()+"/"+ mainFolder +"/projects.json");
            int size= iStream.available();
            byte[] buffer =new byte[size];
            iStream.read(buffer);
            iStream.close();
            j=new String(buffer, StandardCharsets.UTF_8);
        }catch(IOException e){
            return "";
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
    /*    try{

            InputStream iStream_asset=getAssets().open("categories.json");
            int size= iStream_asset.available();
            byte[] buffer =new byte[size];
            iStream_asset.read(buffer);
            iStream_asset.close();
            String j=new String(buffer, StandardCharsets.UTF_8);

            FileWriter fw=new FileWriter(directory+"/categories.json");
            fw.write(j);
            fw.flush();
            fw.close();

        }catch (IOException e){
            e.printStackTrace();
        }*/

        try{

            InputStream iStream_asset=getAssets().open("plannerCategories.json");
            int size= iStream_asset.available();
            byte[] buffer =new byte[size];
            iStream_asset.read(buffer);
            iStream_asset.close();
            String j=new String(buffer, StandardCharsets.UTF_8);

            FileWriter fw=new FileWriter(directory+ "/plannerCategories.json");
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

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =registerForActivityResult(
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