package com.example.storyplanningprototype;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity {

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
    EditText field1;
    EditText field2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        selectedIndex= getIntent().getIntExtra("index",0);
        mainFolder=getIntent().getStringExtra("Folder");
        projectName=getIntent().getStringExtra("Project");
        categoryName=getIntent().getStringExtra("Category");
        elementNames=getIntent().getExtras().getStringArrayList("ElementName");
        elementDetails=getIntent().getExtras().getStringArrayList("ElementDetail");

        toolbar=findViewById(R.id.menu_toolbar);
        titleView=findViewById(R.id.toolbar_title);
        String tt="Edit "+categoryName;
        titleView.setText(tt);

        String s1=elementNames.get(selectedIndex);
        field1=findViewById(R.id.field_trait1);
        field1.setText(s1);

        String s2=elementDetails.get(selectedIndex);
        field2=findViewById(R.id.field_trait2);
        field2.setText(s2);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //save
        String s1=field1.getText().toString();
        String s2=field2.getText().toString();
        if (!s1.isEmpty()){
            if(Character.isLetter(s1.charAt(0))){
                 if(!checkIfNameExist(s1)){
                    //Code if valid
                    elementNames.set(selectedIndex,s1);
                }
            }
        }
        elementDetails.set(selectedIndex,s2);

        updateList();
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

    private void updateList(){
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

}