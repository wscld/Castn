package com.wslclds.castn.builders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import com.wslclds.castn.helpers.Helper;

public class AlertBuilder {
    AlertDialog alertDialog;

    public AlertBuilder(Context context, String title, String description){
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public AlertBuilder(Context context, String title, String description, onButtonClick3 onButtonClick3){
        EditText edittext = new EditText(context);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);

        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);
        alertDialog.setView(edittext);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(edittext.getText().toString() != null && edittext.getText().toString().length() > 0 && Helper.isNumeric(edittext.getText().toString()))
                        onButtonClick3.onConfirm(Long.valueOf(edittext.getText().toString()));
                    }
                });
    }

    public AlertBuilder(Context context, String title, String description, onButtonClick onButtonClick){
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onButtonClick.onNeutral();
                        dialog.dismiss();
                    }
                });
    }

    public AlertBuilder(Context context, String title, String description, onButtonClick2 onButtonClick){
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onButtonClick.onConfirm();
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onButtonClick.onCancel();
                        dialog.dismiss();
                    }
                });
    }

    public void updateDescription(String description){
        alertDialog.setMessage(description);
    }

    public void show(){
        alertDialog.show();
    }

    public interface onButtonClick{
        void onNeutral();
    }

    public interface onButtonClick2{
        void onConfirm();
        void onCancel();
    }

    public interface onButtonClick3{
        void onConfirm(long l);
    }
}
