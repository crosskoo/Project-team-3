package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.jeyun.rhdms.databinding.ActivityMenuBinding;
import com.jeyun.rhdms.graphActivity.PressureInfoActivity;
import com.jeyun.rhdms.graphActivity.SugarInfoActivity;
import com.jeyun.rhdms.graphActivity.NewPillInfoActivity;
import com.jeyun.rhdms.util.SettingsManager;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import java.time.LocalDate;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
        initEvents();
        Log.v("MenuActivity", "onCreate");


        MyWorkManager.schedulePeriodicWork(Inspector.class, getApplicationContext());
        //test
        SettingsManager.getInstance(getApplicationContext()).setDisabledAlarmDate(LocalDate.of(2000, 10, 1));
    }

    private void initEvents()
    {
        binding.buttonPillInfo.setOnClickListener(v -> switchActivity(NewPillInfoActivity.class));
        binding.buttonPillList.setOnClickListener(v -> switchActivity(PillListActivity.class));
        binding.buttonPressureInfo.setOnClickListener(v -> switchActivity(PressureInfoActivity.class));
        binding.buttonSugarInfo.setOnClickListener(v -> switchActivity(SugarInfoActivity.class));
        binding.buttonBle.setOnClickListener(v -> switchActivity(BleActivity.class));
    }

    private <T> void switchActivity(T cls)
    {
        Intent intent = new Intent(getApplicationContext(), (Class<?>) cls);
        startActivity(intent);
    }
}