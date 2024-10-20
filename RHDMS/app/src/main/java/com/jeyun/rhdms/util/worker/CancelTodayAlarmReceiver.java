package com.jeyun.rhdms.util.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeyun.rhdms.util.SettingsManager;

import java.time.LocalDate;

public class CancelTodayAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsManager settingsManager = SettingsManager.getInstance(context);

        settingsManager.setDisabledAlarmDate(LocalDate.now());
        Log.d("CancelTodayAlarmReceiver", "오늘 알람 비활성화");
    }
}
