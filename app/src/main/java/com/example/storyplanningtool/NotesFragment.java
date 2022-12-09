package com.example.storyplanningtool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class NotesFragment extends Fragment implements RecyclerViewClickListener, InputDialog2.DialogListener {

    //variables
    public String mainFolder;
    public String projectName;
    public String projectID;
    SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    //Arraylist
    ArrayList<String> noteTitles = new ArrayList<>();
    ArrayList<String> noteIDs=new ArrayList<>();
    ArrayList<String> noteContents=new ArrayList<>();

    //widgets
    RecyclerView recyclerView;
    TextView emptyView;
    FloatingActionButton buttonAdd;


    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView=view.findViewById(R.id.noteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyView=view.findViewById(R.id.noteEmpty_view);
        emptyView.setText("There is nothing here.");
        buttonAdd = view.findViewById(R.id.button_add_note);

        Bundle b=this.getArguments();

        if (b!=null){
            projectName =b.getString("PName");
            mainFolder=b.getString("MainFolder");
            projectID=b.getString("PID");
        }

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
        checkDirectories();
        updateView();
        return view;

    }

    private void openDialog() {
        FragmentManager fm = getFragmentManager();
        InputDialog2 d = new InputDialog2("Note");
        d.setTargetFragment(NotesFragment.this, 1);
        d.show(fm, "dialog");
    }


    @Override
    public void applyTextToFragment(String name) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Unable to make Category. No name is entered.", Toast.LENGTH_SHORT).show();
        } else if(checkIfNameExist(name)){
            Toast.makeText(getContext(), "Unable to make Category. Name already exists", Toast.LENGTH_SHORT).show();
        }
        else {
            //Code if valid
            String id = "note" + f.format(Calendar.getInstance().getTime());

            noteIDs.add(id);
            // Toast.makeText(getContext(),id,Toast.LENGTH_SHORT).show();
            noteTitles.add(name);
            //  Toast.makeText(getContext(),name,Toast.LENGTH_SHORT).show();
            noteContents.add("");
            updateJSONFile();
            updateView();

        }
    }

    private boolean checkIfNameExist(String name) {
        boolean x = false;
        if(!noteTitles.isEmpty()){
            for(int i=0;i<noteTitles.size();i++){
                if (name.equals(noteTitles.get(i))){
                    x=true;
                    break;
                }
            }
        }

        return x;

    }

    @Override
    public void onItemClick(int position) {
        //new intent
        Intent intent = new Intent(getContext(), NoteDetailActivity.class);
        //add stuff to bundle
        intent.putExtra("Folder", mainFolder);
        intent.putExtra("Project", projectName);
        intent.putExtra("ProjectID", projectID);
        intent.putExtra("index",position);

        //open activity
        startActivity(intent);

    }
    public void updateView() {

        noteTitles.clear();
        noteIDs.clear();
        noteContents.clear();
        try {
            JSONObject o = new JSONObject(loadJSONFile());

            JSONArray nArray = o.getJSONArray("notes");
            int availableItems = nArray.length();
            for (int i = 0; i < availableItems; i++) {
                JSONObject nDetail = nArray.getJSONObject(i);
                String id = nDetail.getString("id");
                String name = nDetail.getString("title");
                String content=nDetail.getString("content");
                noteTitles.add(name);
                noteIDs.add(id);
                noteContents.add(content);
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }

        CustomAdapterForItems cA = new CustomAdapterForItems(this.getContext(), noteTitles, this);
        recyclerView.setAdapter(cA);

        if(noteIDs.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

    }

    private String loadJSONFile() {
        String j;
        try {
            InputStream iStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + "/notes.json");
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

    private void updateJSONFile() {
        String directory = Environment.getExternalStorageDirectory() + "/" + mainFolder + "/" + projectID + "/notes.json";

        try {
            FileWriter fw = new FileWriter(directory);

            fw.write("{\n\t\"notes\": [");

            for (int i = 0; i < noteIDs.size(); i++) {
                fw.append("\n\t\t{" +
                        "\n\t\t\"id\":\"" + noteIDs.get(i) + "\"" + "," +
                        "\n\t\t\"title\":\"" + noteTitles.get(i) + "\"" +","+
                        "\n\t\t\"content\":\"" + noteContents.get(i) + "\""+
                        "\n\t\t}");
                if (i < (noteIDs.size() - 1)) {
                    fw.append(",");
                }
            }

            fw.append("\n\t]\n}");
            fw.flush();
            fw.close();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Unable to Load", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDirectories(){
        String directory=Environment.getExternalStorageDirectory()+"/"+ mainFolder+"/"+projectID+"/notes.json";
        File f=new File(directory);
        if(!f.exists()){
            try{
                FileWriter fw=new FileWriter(f);
                fw.write("{\n\t\"notes\": [\n\t]\n}");
                fw.flush();
                fw.close();
            }catch(IOException e){
              e.printStackTrace();
            }

        }


    }

    @Override
    public void onResume(){
        super.onResume();
        updateView();
    }


}