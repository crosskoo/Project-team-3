package com.jeyun.rhdms.util.factory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertFactory implements PopupFactory<AlertDialog>
{
    private Context context;
    public AlertFactory(Context context)
    {
        this.context = context;
    }

    public AlertDialog createPopup(String title, String message, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title)
                .setCancelable(false)
                .setNeutralButton("확인", listener);
        return builder.create();
    }

    public AlertDialog createPopupYN(String title, String message, DialogInterface.OnClickListener... listeners)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title)
                .setCancelable(false)
                .setPositiveButton("확인", listeners[0])
                .setNegativeButton("취소", listeners[1]);
        return builder.create();
    }
}
