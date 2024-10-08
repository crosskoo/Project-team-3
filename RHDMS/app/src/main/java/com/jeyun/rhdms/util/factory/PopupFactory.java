package com.jeyun.rhdms.util.factory;

import android.content.DialogInterface;

public interface PopupFactory<R>
{
    R createPopup(String title, String message, DialogInterface.OnClickListener listener);
    R createPopupYN(String title, String message, DialogInterface.OnClickListener... listeners);
}