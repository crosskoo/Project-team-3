package com.jeyun.rhdms.util.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jeyun.rhdms.handler.DataHandler;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.NotificationUtils;
import com.jeyun.rhdms.util.SettingsManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        LocalDate disabledAlarmDate = settingsManager.getDisabledAlarmDate();
        if(disabledAlarmDate.isEqual(LocalDate.now())) {
            Log.d("AlarmReceiver", "오늘 알람은 중지됨");
            return;
        }

        int doNotDisturbStartTime = settingsManager.getDoNotDisturbStartTime();
        int doNotDisturbEndTime = settingsManager.getDoNotDisturbEndTime();
        int currntTime = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        if(doNotDisturbStartTime <= doNotDisturbEndTime){
            if(doNotDisturbStartTime <= currntTime && currntTime <= doNotDisturbEndTime){
                Log.d("AlarmReceiver", "알람금지시간1");
                return;
            }
        }else if(doNotDisturbStartTime <= currntTime || currntTime <= doNotDisturbEndTime) {
            Log.d("AlarmReceiver", "알람금지시간2");
            return;
        }

        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
                Log.d("AlarmReceiver", "복약 상태 확인");

                NotificationUtils.sendNotificationWithAction(context, "복약 알림", "복약 시간입니다! 지금 복용해 주세요.", CancelTodayAlarmReceiver.class);
        });
    }
}