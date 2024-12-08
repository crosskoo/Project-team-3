package com.jeyun.rhdms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jeyun.rhdms.databinding.ActivityAppSettingsBinding;
import com.jeyun.rhdms.databinding.ActivityMenuBinding;
// import com.jeyun.rhdms.graphActivity.NewPillInfoActivity;
// import com.jeyun.rhdms.graphActivity.PressureInfoActivity;
// import com.jeyun.rhdms.graphActivity.SugarInfoActivity;
import com.jeyun.rhdms.handler.SharedPreferenceHandler;
import com.jeyun.rhdms.handler.entity.User;
import com.jeyun.rhdms.util.SettingsManager;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import java.time.LocalDate;

public class AppSettingsActivity extends AppCompatActivity {
    private ActivityAppSettingsBinding binding;
    private SettingsManager settingsManager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        context = getApplicationContext();
        settingsManager = SettingsManager.getInstance(context);

        initUI();
        initEvents();
    }

    private void initUI(){
        binding.buttonIncreaseAlarmCount.setText(Integer.toString(settingsManager.getAlarmCount()));
        binding.buttonIncreaseAlarmDelay.setText(Integer.toString(settingsManager.getAlarmDelay()));
        binding.buttonIncreaseAlarmVibration.setText(Integer.toString(settingsManager.getAlarmVibrationLevel()));
        binding.checkBoxAlarmMute.setChecked(settingsManager.getIsDoNotDisturbEnabled());

        int doNotDisturbStartTime = settingsManager.getDoNotDisturbStartTime();
        int doNotDisturbEndTime = settingsManager.getDoNotDisturbEndTime();
        binding.timePicker1.setHour(doNotDisturbStartTime / 60);
        binding.timePicker1.setMinute(doNotDisturbStartTime % 60);
        binding.timePicker2.setHour(doNotDisturbEndTime / 60);
        binding.timePicker2.setMinute(doNotDisturbEndTime % 60);
    }

    private void initEvents()
    {

        binding.buttonIncreaseAlarmCount.setOnClickListener(v -> {
            int count = SettingsManager.getInstance(getApplicationContext()).increaseAlarmCount(context);
            binding.buttonIncreaseAlarmCount.setText(String.valueOf(count));
        });
        binding.buttonIncreaseAlarmDelay.setOnClickListener(v -> {
            int delay = SettingsManager.getInstance(getApplicationContext()).increaseAlarmDelay(context);
            binding.buttonIncreaseAlarmDelay.setText(String.valueOf(delay));
        });
        binding.buttonIncreaseAlarmVibration.setOnClickListener(v -> {
            int vibrationLevel = SettingsManager.getInstance(getApplicationContext()).increaseAlarmVibrationLevel(context);
            binding.buttonIncreaseAlarmVibration.setText(String.valueOf(vibrationLevel));
        });
        binding.checkBoxAlarmMute.setOnCheckedChangeListener((v, isChecked) -> {
            SettingsManager.getInstance(getApplicationContext()).toggleIsDoNotDisturbEnabled(isChecked);
        });

        binding.timePicker1.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            settingsManager.setDoNotDisturbStartTime(hourOfDay, minute);
        });

        binding.timePicker2.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            settingsManager.setDoNotDisturbEndTime(hourOfDay, minute);
        });
        binding.back.setOnClickListener(v -> finish());
        binding.selection.setOnClickListener(v -> {
            Intent intent = new Intent(AppSettingsActivity.this, DeviceSettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        binding.buttonLogout.setOnClickListener(v -> {
            Inspector.cancelScheduledNotification(getApplicationContext(), LocalDate.now());
            MyWorkManager.cancelPeriodicWork(getApplicationContext());
            Logout();
        }); // 로그아웃
    }

    private void Logout()
    {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말로 로그아웃 하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // 저장된 orgnztId 데이터 삭제
                        SharedPreferenceHandler handler = new SharedPreferenceHandler(getApplicationContext());
                        handler.clearAll();
                        User.getInstance().setOrgnztId(null);

                        // 로그아웃이 정상적으로 되었는지 확인
                        Log.d("ksd", "orgnztId : " + handler.getSavedOrgnztId());
                        Log.d("ksd", "User orgnztId : " + User.getInstance().getOrgnztId()); // 테스트 용

                        // 다시 로그인 화면으로 이동
                        Intent intent = new Intent(AppSettingsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("아니요", null)
                .show();
    }
}
