package com.wmwise.labelscannerwmwise.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

public class SimpleConfirmDialog {
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener positiveListener, negativeListener;

    //sin icono
    public SimpleConfirmDialog(Context c, String title, String message){
        builder = new AlertDialog.Builder(c);
        builder.setMessage(message)
               .setTitle(title);
    }

    //con icono
    public SimpleConfirmDialog(Context c, String title, String message, int ico){
        builder = new AlertDialog.Builder(c);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(ico);
    }

    public SimpleConfirmDialog(Context c, String title, String message, Drawable ico){
        builder = new AlertDialog.Builder(c);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(ico);
    }

    public void setPositive(String text,DialogInterface.OnClickListener listener){
        positiveListener= listener;
        builder.setPositiveButton(text, positiveListener);
    }

    public void setNegative(String text, DialogInterface.OnClickListener listener){
        negativeListener=listener;
        builder.setNegativeButton(text,negativeListener);
    }

    public void make(){
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
