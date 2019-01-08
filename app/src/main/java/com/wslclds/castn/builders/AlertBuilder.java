package com.wslclds.castn.builders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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
}
