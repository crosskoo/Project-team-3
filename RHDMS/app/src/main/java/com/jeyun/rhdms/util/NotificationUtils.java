package com.jeyun.rhdms.util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jeyun.rhdms.R;

public class NotificationUtils
{
    public static final String CHANNEL_ID = "my_channel_id";

    public static void createNotificationChannel(Context context) {
        CharSequence name = "복약 알림";
        String description = "복약 알림을 위한 채널";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setVibrationPattern(null);


        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("MissingPermission")
    public static void sendNotification(Context context, String title, String message)
    {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    @SuppressLint("MissingPermission")
    public static void sendNotificationWithAction(Context context, String title, String message, Class<? extends BroadcastReceiver> receiverClass)
    {
        createNotificationChannel(context);

        // 버튼 클릭 시 실행할 Intent 생성
        Intent intent = new Intent(context, receiverClass);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_noti)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_launcher_foreground, "오늘 알람 받지 않기", pendingIntent)
                .setVibrate(new long[]{0});

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
        vibrateWithIntensity(context, SettingsManager.getInstance(context).getAlarmVibrationLevel(), 500);
    }

    // 진동 세기를 0~3으로 설정하는 메서드
    public static void vibrateWithIntensity(Context context, int intensityLevel, int duration) {
        // Vibrator 객체 가져오기
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // 진동 패턴과 강도를 설정
        long[] pattern;
        int[] amplitudes;

        switch (intensityLevel) {
            case 0: // 진동 없음
                return; // 아무 것도 하지 않음
            case 1: // 약한 진동
                pattern = new long[]{0, duration}; // 100ms 진동
                amplitudes = new int[]{0, 1}; // 약한 강도
                break;
            case 2: // 보통 진동
                pattern = new long[]{0, duration};
                amplitudes = new int[]{0, 64}; // 보통 강도
                break;
            case 3: // 강한 진동
                pattern = new long[]{0, duration};
                amplitudes = new int[]{0, 255}; // 최대 강도
                break;
            default:
                return;
        }

        if (vibrator != null) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, -1);
            vibrator.vibrate(effect);
        }
    }
}
