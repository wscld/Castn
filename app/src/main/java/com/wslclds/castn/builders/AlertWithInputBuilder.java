package com.wslclds.castn.builders;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

import com.rengwuxian.materialedittext.MaterialEditText;

import com.wslclds.castn.helpers.Helper;

public class AlertWithInputBuilder {
    AlertDialog alertDialog;
    OnAction onAction;

    public AlertWithInputBuilder(Context context, String title, String description, String hint, String text ,OnAction onAction){
        this.onAction = onAction;

        int margin = (int)Helper.pxFromDp(context,20);

        final MaterialEditText input = new MaterialEditText(context);
        input.setHint(hint);
        input.setText(text);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setView(input,margin,margin,margin,margin);
        alertDialog.setTitle(title);
        alertDialog.setMessage(description);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onAction.OnSubmit(input.getText().toString());
                        dialog.dismiss();
                    }
                });
    }
    public void show(){
        alertDialog.show();
    }

    public interface OnAction{
        void OnSubmit(String string);
    }
}
