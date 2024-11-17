package com.jeyun.rhdms;

import android.animation.LayoutTransition;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.strictmode.FragmentTagUsageViolation;

import com.jeyun.rhdms.databinding.ActivitySettingBinding;
import com.jeyun.rhdms.fragment.AppSettingsFragment;
import com.jeyun.rhdms.fragment.DeviceSettingsFragement;

public class SettingActivity extends AppCompatActivity
{
    private ActivitySettingBinding binding;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
        initEvents();
    }

    private void initEvents()
    {
        // 뒤로 가기
        binding.back.setOnClickListener(v -> finish());

        binding.toggleSetting.setOnClickListener(v -> {
            ToggleButton tb = binding.toggleSetting;
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();

            ConstraintLayout constraintLayout = binding.getRoot();
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            if (tb.isChecked()) // true : 알림 설정 화면
            {
                constraintSet.setGuidelinePercent(R.id.pm_guideline_alarm_set, 0.23f);
                constraintSet.setVisibility(R.id.pm_guideline_device_setting, View.GONE);

                constraintSet.connect(binding.settingFrame.getId(), ConstraintSet.TOP, binding.pmGuidelineAlarmSet.getId(), ConstraintSet.BOTTOM);

                constraintSet.applyTo(constraintLayout);

                Fragment settingFragment = new AppSettingsFragment();
                fragmentTransaction.replace(binding.settingFrame.getId(), settingFragment);
                fragmentTransaction.commit();
            }
            else // false : 복약기 설정 화면
            {
                constraintSet.setGuidelinePercent(R.id.pm_guideline_device_setting, 0.18f);
                constraintSet.setVisibility(R.id.pm_guideline_alarm_set, View.GONE);

                constraintSet.connect(binding.settingFrame.getId(), ConstraintSet.TOP, binding.pmGuidelineDeviceSetting.getId(), ConstraintSet.BOTTOM);

                constraintSet.applyTo(constraintLayout);

                Fragment settingFragment = new DeviceSettingsFragement();
                fragmentTransaction.replace(binding.settingFrame.getId(), settingFragment);
                fragmentTransaction.commit();
            }
        });
    }

    // 기본값으로는 알림 설정 화면이 나오도록 UI 설정
    private void initUI()
    {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(binding.settingFrame.getId(), new AppSettingsFragment());
        fragmentTransaction.commit();
    }
}