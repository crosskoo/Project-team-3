package com.jeyun.rhdms.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jeyun.rhdms.databinding.FragmentAppSettingsBinding;
import com.jeyun.rhdms.util.SettingsManager;

public class AppSettingsFragment extends Fragment
{
    private View view;
    private FragmentAppSettingsBinding binding;
    private SettingsManager settingsManager;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 뷰 바인딩
        this.binding = FragmentAppSettingsBinding.inflate(inflater);
        this.view = binding.getRoot();

        context = requireContext();
        settingsManager = SettingsManager.getInstance(context);

        initUI();
        initEvents();

        return view;
    }

    private void initUI(){
        binding.buttonIncreaseAlarmCount.setText(Integer.toString(settingsManager.getAlarmCount()));
        binding.buttonIncreaseAlarmDelay.setText(Integer.toString(settingsManager.getAlarmDelay()));
        binding.buttonIncreaseAlarmVibration.setText(Integer.toString(settingsManager.getAlarmVibrationLevel()));

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
            int count = SettingsManager.getInstance(context).increaseAlarmCount(context);
            binding.buttonIncreaseAlarmCount.setText(String.valueOf(count));
        });
        binding.buttonIncreaseAlarmDelay.setOnClickListener(v -> {
            int delay = SettingsManager.getInstance(context).increaseAlarmDelay(context);
            binding.buttonIncreaseAlarmDelay.setText(String.valueOf(delay));
        });
        binding.buttonIncreaseAlarmVibration.setOnClickListener(v -> {
            int vibrationLevel = SettingsManager.getInstance(context).increaseAlarmVibrationLevel(context);
            binding.buttonIncreaseAlarmVibration.setText(String.valueOf(vibrationLevel));
        });

        binding.timePicker1.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            settingsManager.setDoNotDisturbStartTime(hourOfDay, minute);
        });

        binding.timePicker2.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            settingsManager.setDoNotDisturbEndTime(hourOfDay, minute);
        });

        // binding.back.setOnClickListener(v -> finish());
    }
}
