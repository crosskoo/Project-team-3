package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.jeyun.rhdms.databinding.ActivityMenuBinding;
import com.jeyun.rhdms.graphActivity.PressureInfoActivity;
import com.jeyun.rhdms.graphActivity.SugarInfoActivity;
import com.jeyun.rhdms.graphActivity.NewPillInfoActivity;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

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

        MyWorkManager.schedulePeriodicWork(Inspector.class, getApplicationContext(), 15);
    }


    private void initEvents()
    {
        binding.buttonPillInfo.setOnClickListener(v -> switchActivity(NewPillInfoActivity.class));
        binding.buttonPillList.setOnClickListener(v -> switchActivity(PillListActivity.class));
        binding.buttonPressureInfo.setOnClickListener(v -> switchActivity(PressureInfoActivity.class));
        binding.buttonSugarInfo.setOnClickListener(v -> switchActivity(SugarInfoActivity.class));
        binding.buttonBle.setOnClickListener(v -> switchActivity(BleActivity.class));
<<<<<<< HEAD
        binding.buttonSettings.setOnClickListener(v -> switchActivity(SettingMenuActivity.class));  //설정 관련해서 추가.
=======
>>>>>>> main
        binding.buttonTestNewPillInfo.setOnClickListener(v -> switchActivity(TestNewPillInfoActivity.class)); // 테스트 용
    }

    private <T> void switchActivity(T cls)
    {
        Intent intent = new Intent(getApplicationContext(), (Class<?>) cls);
        startActivity(intent);
    }
}
