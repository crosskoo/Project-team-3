package com.jeyun.rhdms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jeyun.rhdms.databinding.ActivityMainBinding;
import com.jeyun.rhdms.util.factory.AlertFactory;
import com.jeyun.rhdms.util.factory.PopupFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // 바인딩할 뷰 레이아웃
    private PopupFactory<AlertDialog> factory; // 알림 메시지 factory
    private AlarmManager alarmManager;

    private boolean isRequested = false; // 권한 요청 여부

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        factory = new AlertFactory(view.getContext());
        checkPermission(); // 권한 승인 여부 확인

        // (추후 수정) 버튼 누를 시 메인 화면으로 이동
        binding.buttonLogin.setOnClickListener(v ->
        {
            Intent intent_switch = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(intent_switch);
            finish();
        });
    }

    @Override // 권한 요청 결과 처리
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermission();
    }

    private void checkPermission() // 권환 승인 확인 함수(블루투스 권한, 알림 권한 등)
    {
        // 안드로이드 12 (API 31) 이상에서 블루투스 권한 승인 여부
        boolean isBluetoothPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED;

        // 안드로이드 13 (API 33) 이상에서 알람 권한 승인 여부
        boolean isNotificationPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

        // 진동 권한 승인 여부
        boolean isVibratePermissionDenied = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED;

        // 안드로이드 12 (API 31) 이상에서 정확한 알람 권한 승인 여부
        // alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // boolean isScheduleExactAlarmDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms();

        // 여러 권한 중 하나라도 승인이 안 되어 있다면
        // if (isBluetoothPermissionDenied || isNotificationPermissionDenied || isVibratePermissionDenied || isScheduleExactAlarmDenied)
        if (isBluetoothPermissionDenied || isNotificationPermissionDenied || isVibratePermissionDenied)
        {
            AlertDialog alert;
            if (isRequested) // 권한 요청은 전에 했지만 권한 승인이 되지 않은 경우
            {
                alert = factory.createPopup("권한 요청", "수동으로 권한을 활성화해주세요!", (dialog, which) -> gotoAppSettings());
            }
            else // 처음 권한 요청을 하는 경우
            {
                alert = factory.createPopupYN("권한 요청", "해당 권한이 필수적으로 요구됩니다.",
                        (dialog, which) -> requestPermission(),
                        (dialog, which) -> gotoAppSettings());
            }

            alert.show();
        }
    }

    private void gotoAppSettings() // App 설정으로 이동
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void requestPermission()
    {
        List<String> permissionsList = new ArrayList<>(); // 요청할 권한을 저장할 리스트

        // 안드로이드 12 (API 31) 이상에서 블루투스 권한 승인이 안 되어 있다면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        }

        // 안드로이드 13 (API 33) 이상에서 알람 권한 승인이 안 되어 있다면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // 진동 권한 승인이 안되어 있다면
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED)
        {
            permissionsList.add(Manifest.permission.VIBRATE);
        }

        // 안드로이드 12 (API 31) 이상에서 정확한 알람 권한 승인이 안 되어 있다면
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms())
        {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
        */

        if (!permissionsList.isEmpty()) // 요청할 권한이 있다면
        {
            isRequested = true;
            String[] permissions = permissionsList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissions, permissions.length);
        }
    }
}