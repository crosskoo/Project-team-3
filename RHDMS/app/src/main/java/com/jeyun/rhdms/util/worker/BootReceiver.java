package com.jeyun.rhdms.util.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 주기적인 WorkManager 작업을 다시 등록
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(Inspector.class, 15, TimeUnit.MINUTES)
                    .setInitialDelay(15, TimeUnit.MINUTES) // 원하는 지연 시간 설정
                    .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "my_periodic_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
            );
        }
    }

}
