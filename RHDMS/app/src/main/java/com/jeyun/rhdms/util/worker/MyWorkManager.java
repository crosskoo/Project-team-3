package com.jeyun.rhdms.util.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import java.util.concurrent.TimeUnit;

public class MyWorkManager
{
    private static final String WORKER_TAG = "JEYUN";
    public static <T extends  Worker> void schedulePeriodicWork(Class<T> cls, Context context)
    {
        PeriodicWorkRequest.Builder myWorkBuilder =  new PeriodicWorkRequest.Builder(cls, 15, TimeUnit.MINUTES);
        myWorkBuilder.addTag(WORKER_TAG);

        PeriodicWorkRequest myWork = myWorkBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork
                (
                        WORKER_TAG,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork
                );
        Log.v("MyWorkManager", "백그라운드 반복 작업 등록");
    }

    public static <T extends  Worker> void OneTimeWork(Class<T> cls, Context context){
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(cls).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }

    //알림 예약 작업 취소
    public static void cancelPeriodicWork(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORKER_TAG);
        Log.v("MyWorkManager", "알림 예약 작업이 취소되었습니다.");
    }
}
