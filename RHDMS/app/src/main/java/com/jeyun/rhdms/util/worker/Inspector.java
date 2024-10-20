package com.jeyun.rhdms.util.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jeyun.rhdms.handler.DataHandler;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.NotificationUtils;
import com.jeyun.rhdms.util.SettingsManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Inspector extends Worker
{
    private Handler handler;
    private Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String, Integer> counter;

    public Inspector(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
        handler = new Handler(Looper.getMainLooper());
        counter = new HashMap<>();
        NotificationUtils.createNotificationChannel(getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork()
    {
        //test 현재시간 출력
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더해야 함
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String currentDateTime = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
        Log.d("Inspector", currentDateTime);
        //

        handler.post(() ->
        {
            Context context = getApplicationContext();
            executor.execute(() ->
            {
                Log.d("Inspector", "doWork실행");
                LocalDate todayDate = LocalDate.now();
                setAlarm(todayDate, context);
                setAlarm(todayDate.plusDays(1), context);
            });

        });
        return Result.success();
    }

    private void setAlarm(LocalDate date, Context context){
        //오늘 Pill 객체 가져오기
        DataHandler<Pill, LocalDate> dataHandler = new PillHandler();
        Optional<Pill> optionalPill = dataHandler.getData(PillHandler.dateToString(date));

        //intent세팅
        int dateAsNumber = Integer.parseInt(date.format(DateTimeFormatter.BASIC_ISO_DATE));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        SettingsManager settingsManager = SettingsManager.getInstance(context);
        int repeatCount = settingsManager.getAlarmCount(); //알람 반복 횟수
        int repeatDelay = settingsManager.getAlarmDelay(); //알람 딜레이(분)
        PendingIntent[] pendingIntents = new PendingIntent[10];

        for (int i = 0; i < 10; i++) {
            int requestCode = dateAsNumber*10 + i;
            pendingIntents[i] = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntents[i]);
        }

        optionalPill.ifPresent(pill ->
        {
            //Pill 데이터 가져오기
            String ST = pill.ARM_ST_TM;
            int flag = Integer.parseInt(pill.ARM_TP);
            int startHour = syncTime(flag, Integer.parseInt(ST.substring(0, 2)));
            int startMinute = Integer.parseInt(ST.substring(2, 4));

            //알람시간세팅
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, date.getYear());
            calendar.set(Calendar.MONTH, date.getMonthValue()-1); // 0부터 시작하므로 1월은 0
            calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, 0); // 초를 0으로 설정

            Calendar time = (Calendar) calendar.clone();
            for(int i = 0; i < repeatCount; i++){
                if(Calendar.getInstance().compareTo(time) < 0) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntents[i]);
                }
                time.add(Calendar.MINUTE, repeatDelay);
            }

            Log.d("Inspector", "새 알람이 설정되었습니다: " + calendar.getTime() + " 횟수:" + repeatCount + " 주기: " + repeatDelay);
        });
    }

    private int syncTime(int flag, int hour)
    {
        if(hour == 12) return flag == 1 ? 0 : 12;
        return flag == 1 ? hour : hour + 12;
    }
}
