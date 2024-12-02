package com.jeyun.rhdms.util.worker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 디바이스가 재부팅되었을 때 알림 설정 작업 재등록
            MyWorkManager.OneTimeWork(Inspector.class, context);
            MyWorkManager.schedulePeriodicWork(Inspector.class, context);
        }
    }
}