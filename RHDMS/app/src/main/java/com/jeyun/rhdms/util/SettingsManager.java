package com.jeyun.rhdms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SettingsManager {
    private static SettingsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences("RHDMSAppSetting", Context.MODE_PRIVATE);
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

    public int increaseAlarmCount(Context context){
        int count = sharedPreferences.getInt("alarmCount", 1);

        if(count == 10) count = 1;
        else count++;

        editor.putInt("alarmCount", count).apply();
        MyWorkManager.OneTimeWork(Inspector.class, context);

        return count;
    }

    public int getAlarmCount(){
        return sharedPreferences.getInt("alarmCount", 1);
    }

    public int increaseAlarmDelay(Context context){
        int delay = sharedPreferences.getInt("alarmDelay", 10);

        if(delay >= 60) delay = 5;
        else delay += 5;

        editor.putInt("alarmDelay", delay).apply();
        MyWorkManager.OneTimeWork(Inspector.class, context);

        return delay;
    }

    public int getAlarmDelay(){
        return sharedPreferences.getInt("alarmDelay", 1);
    }

    public int increaseAlarmVibrationLevel(Context context){
        int AlarmVibrationLevel = sharedPreferences.getInt("AlarmVibrationLevel", 1);

        if(AlarmVibrationLevel >= 3) AlarmVibrationLevel = 0;
        else AlarmVibrationLevel += 1;

        editor.putInt("AlarmVibrationLevel", AlarmVibrationLevel).apply();
        NotificationUtils.vibrateWithIntensity(context, AlarmVibrationLevel, 300);

        return AlarmVibrationLevel;
    }

    public int getAlarmVibrationLevel(){
        return sharedPreferences.getInt("AlarmVibrationLevel", 1);
    }

    public void setDoNotDisturbStartTime(int startTimeHour, int startTimeMinute){
        Log.d("SettingsManager", "방해금지시작시간: " + startTimeHour + " " + startTimeMinute);
        editor.putInt("doNotDisturbStartTime", startTimeHour*60+startTimeMinute).apply();
    }

    public void setDoNotDisturbEndTime(int endTimeHour, int endTimeMinute){
        Log.d("SettingsManager", "방해금지종료시간: " + endTimeHour + " " + endTimeMinute);
        editor.putInt("doNotDisturbEndTime", endTimeHour*60+endTimeMinute).apply();
    }

    public int getDoNotDisturbStartTime(){
        return sharedPreferences.getInt("doNotDisturbStartTime", 0);
    }

    public int getDoNotDisturbEndTime(){
        return sharedPreferences.getInt("doNotDisturbEndTime", 0);
    }
}