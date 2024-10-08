package com.jeyun.rhdms.util.factory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertFactory implements PopupFactory<AlertDialog>
{
    private Context context;
    public AlertFactory(Context context) { this.context = context; }

    @Override
    public AlertDialog createPopup(String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton("확인", listener);
        return builder.create();
    }

    @Override
    public AlertDialog createPopupYN(String title, String message, DialogInterface.OnClickListener... listeners) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("확인", listeners[0]);
        builder.setNegativeButton("취소", listeners[1]);
        return builder.create();
    }
}
