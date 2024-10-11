package com.jeyun.rhdms.util.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jeyun.rhdms.handler.DataHandler;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.NotificationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final int UNTIL = 2;

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
        handler.post(() ->
        {
            Context context = getApplicationContext();
            executor.execute(() ->
            {
                DataHandler<Pill, LocalDate> dataHandler = new PillHandler();
                Optional<Pill> optionalPill = dataHandler.getData(today());
                optionalPill.ifPresent(pill ->
                {
                    String ET = pill.ARM_ED_TM;
                    String ST = pill.TAKEN_ST;
                    int flag = Integer.parseInt(pill.ARM_TP);
                    int endHour = syncTime(flag, Integer.parseInt(ET.substring(0, 2)));
                    int endMinute = Integer.parseInt(ET.substring(2, 4));

//                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime now = LocalDateTime.of(2024, 7, 2, 14, 0);
                    LocalDateTime endTime = formatTime(endHour, endMinute);

                    int currentCount = counter.getOrDefault(pill.ARM_DT, 0);
                    if(now.isAfter(endTime) && ST.equals("UNTAKEN") && currentCount < UNTIL)
                    {
                        NotificationUtils.sendNotification(context, "복약 알림", "오늘의 복용 시간이 지났어요, 어서 복용하세요!");
                        counter.put(pill.ARM_DT, currentCount + 1);
                    }
                });

                if(!optionalPill.isPresent())
                {
                    NotificationUtils.sendNotification(context, "복약 정보", "[테스트] N/A");
//                    NotificationUtils.sendNotification(context, "복약 알림", "복용 시간이 지났어요, 어서 복용하세요!");
                }
            });

        });
        return Result.success();
    }

    private int syncTime(int flag, int hour)
    {
        if(hour == 12) return flag == 1 ? 0 : 12;
        return flag == 1 ? hour : hour + 12;
    }

    private LocalDateTime formatTime(int hour, int minutes)
    {
//        LocalDate date = LocalDate.now();
        LocalDate date = LocalDate.of(2024, 7, 2);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return LocalDateTime.of(year, month, day, hour, minutes);
    }
    private String today()
    {
//        LocalDate date = LocalDate.now();
        LocalDate date = LocalDate.of(2024, 7, 2);
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        return String.format("%d%02d%02d", year, month, day);
    }
}
