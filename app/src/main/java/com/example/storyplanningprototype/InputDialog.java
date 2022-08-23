package com.example.storyplanningprototype;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class InputDialog extends AppCompatDialogFragment {

    private EditText editText;
    private DiaologListener listener;
    public String s;
    public String msg;
    boolean rename = false;


    public InputDialog(String Element){
        this.rename=false;
        this.msg="Add new "+Element;

    }
    public InputDialog(String Element,String name){
        this.rename=true;
        this.msg="Rename "+Element;
        this.s=name;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle b) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.layout_dialog,null);
        builder.setView(view)
                .setTitle(msg)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name=editText.getText().toString();

                            listener.applyTexts(name);


                    }
                });
        editText=view.findViewById(R.id.edit_name);
        if(rename){
            editText.setText(s);
        }

        return builder.create();


    }

    public interface DiaologListener{
        void applyTexts(String name);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener =(DiaologListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+
                    "Must Implement listener");
        }
    }
}
