package com.jeyun.rhdms.util.worker;

import android.content.Context;

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

        OneTimeWorkRequest initialWorkRequest = new OneTimeWorkRequest.Builder(cls).build();
        WorkManager.getInstance(context).enqueue(initialWorkRequest);
        PeriodicWorkRequest myWork = myWorkBuilder.build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork
                (
                        WORKER_TAG,
                        ExistingPeriodicWorkPolicy.KEEP,
                        myWork
                );
    }

    public static <T extends  Worker> void OneTimeWork(Class<T> cls, Context context){
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(cls).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
