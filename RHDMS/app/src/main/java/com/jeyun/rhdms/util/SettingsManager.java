package com.jeyun.rhdms.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsManager {
    private static SettingsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setDisabledAlarmDate(LocalDate date) {
        editor.putString("disabledAlarmDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE)).apply();
    }

    public LocalDate getDisabledAlarmDate() {
        String dateString = sharedPreferences.getString("disabledAlarmDate", null);
        return dateString != null ? LocalDate.parse(dateString) : LocalDate.of(2000, 1, 1);
    }
}