package com.jeyun.rhdms.util.factory;

import android.app.TimePickerDialog;
import android.content.Context;
import java.util.Locale;

// 시간 선택 다이얼로그를 띄우는 팩토리 클래스
public class TimePickerDialogFactory
{
    public interface OnTimeSetListener
    {
        void onTimeSet(String time);
    }

    public static void showTimePickerDialog(Context context, OnTimeSetListener listener)
    {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, selectedHour, selectedMinute) ->
                {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    listener.onTimeSet(time);
                },
                0, 0, true);
        timePickerDialog.show();
    }
}
