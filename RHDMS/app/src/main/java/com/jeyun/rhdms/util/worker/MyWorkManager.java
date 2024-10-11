package com.jeyun.rhdms.util.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import java.util.concurrent.TimeUnit;

public class MyWorkManager
{
    private static final String WORKER_TAG = "JEYUN";
    public static <T> void schedulePeriodicWork(T cls, Context context, int time)
    {
        PeriodicWorkRequest.Builder myWorkBuilder =
//                new PeriodicWorkRequest.Builder((Class<? extends Worker>) cls, time, TimeUnit.MINUTES);
        new PeriodicWorkRequest.Builder((Class<? extends Worker>) cls, 5, TimeUnit.SECONDS);
        myWorkBuilder.addTag(WORKER_TAG);

        PeriodicWorkRequest myWork = myWorkBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork
                (
                        WORKER_TAG,
                        ExistingPeriodicWorkPolicy.KEEP,
                        myWork
                );
    }
}
