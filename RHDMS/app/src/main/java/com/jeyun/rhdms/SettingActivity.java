package com.jeyun.rhdms;

import android.os.Bundle;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.strictmode.FragmentTagUsageViolation;

import com.jeyun.rhdms.databinding.ActivitySettingBinding;
import com.jeyun.rhdms.fragment.AppSettingsFragment;

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

            if (tb.isChecked()) // true : 알림 설정 화면
            {
                Fragment settingFragment = new AppSettingsFragment();
                fragmentTransaction.replace(binding.settingFrame.getId(), settingFragment);
                fragmentTransaction.commit();
            }
            // false : 복약기 설정 화면
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