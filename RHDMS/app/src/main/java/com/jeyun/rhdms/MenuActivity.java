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

import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;
    private LocalDate startDate;
    private LocalDate lastTakenDate;


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

        //복약 상태
        updateAdherenceUI();
    }


    private void initEvents()
    {
        binding.buttonPillInfo.setOnClickListener(v -> switchActivity(NewPillInfoActivity.class));
        binding.buttonPillList.setOnClickListener(v -> switchActivity(PillListActivity.class));
        binding.buttonPressureInfo.setOnClickListener(v -> switchActivity(PressureInfoActivity.class));
        binding.buttonSugarInfo.setOnClickListener(v -> switchActivity(SugarInfoActivity.class));
        binding.buttonBle.setOnClickListener(v -> switchActivity(BleActivity.class));
        binding.buttonSettings.setOnClickListener(v -> switchActivity(SettingMenuActivity.class));  //설정 관련해서 추가.
    }

    private <T> void switchActivity(T cls)
    {
        Intent intent = new Intent(getApplicationContext(), (Class<?>) cls);
        startActivity(intent);
    }

    //최근 복약 날짜 불러오기
    private LocalDate getLatestMedicationDate(List<Pill> pills) {
        LocalDate latestDate = null;

        for (Pill pill : pills) {
            LocalDate pillDate = LocalDate.parse(pill.ARM_DT, DateTimeFormatter.ofPattern("yyyyMMdd"));
            if (latestDate == null || pillDate.isAfter(latestDate)) {
                latestDate = pillDate;
            }
        }

        return latestDate;
    }
    
    //최근 복약일로부터 30일 분석.
    private void updateAdherenceUI() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
                PillHandler pillHandler = new PillHandler();
                List<Pill> pills = new ArrayList<>();

                // Collect data for the past 12 months
                for (int i = 0; i < 12; i++) {
                    LocalDate date = LocalDate.now().minusMonths(i);
                    List<Pill> monthlyPills = pillHandler.getDataInMonth(date);
                    pills.addAll(monthlyPills);

                }

                LocalDate latestDate = getLatestMedicationDate(pills);

                runOnUiThread(() -> {
                    if (latestDate != null) {

                        LocalDate startDate = latestDate.minusDays(30);
                        List<Pill> filteredPills = pills.stream()
                                .filter(pill -> {
                                    LocalDate pillDate = LocalDate.parse(pill.ARM_DT, DateTimeFormatter.ofPattern("yyyyMMdd"));
                                    return !pillDate.isBefore(startDate) && !pillDate.isAfter(latestDate);
                                })
                                .collect(Collectors.toList());



                        int daysAdhered = (int) filteredPills.stream()
                                .filter(pill -> "TAKEN".equals(pill.TAKEN_ST))
                                .count();

                        double adherenceRate = ((double) daysAdhered / 30) * 100;
                        String adherenceMessage;
                        int imageResId;


                        if (adherenceRate >= 80) {
                            adherenceMessage = "양호";
                            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.green));
                            imageResId = R.drawable.good;
                        } else if (adherenceRate >= 50) {
                            adherenceMessage = "보통";
                            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.yellow));
                            imageResId = R.drawable.common;
                        } else {
                            adherenceMessage = "부족";
                            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.red));
                            imageResId = R.drawable.bad;
                        }

                        binding.adherenceTextView.setText(adherenceMessage);
                        binding.adherenceImageView.setImageResource(imageResId);
                        binding.adherenceImageView.setVisibility(View.VISIBLE);

                        String lastTakenMessage = "최근 복용 : " + latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        binding.lastTakenTextView.setText(lastTakenMessage);
                    } else {
                        binding.adherenceTextView.setText("데이터 없음");
                        binding.lastTakenTextView.setText("최근 복용 : 없음.");
                        binding.adherenceImageView.setVisibility(View.GONE);
                    }
                });
        });
    }
}