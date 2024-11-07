package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jeyun.rhdms.R;
import com.jeyun.rhdms.databinding.ActivityBloodSugarInfoBinding;
import com.jeyun.rhdms.fragment.SugarGraphFragment;
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.MyCalendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BloodSugarInfoActivity extends AppCompatActivity {

    private String type = "31"; // 혈당
    private ActivityBloodSugarInfoBinding binding;
    private CustomCalendar calendar = new MyCalendar(); // 현재 날짜
    private Boolean isWeek = true;
    protected Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBloodSugarInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateUI();
        updateDateRange(isWeek);
        initEvents();
        initData();
    }

    private void updateUI() // 주간 / 월간에 따라 최대 최소값 찾기
    {
        executor.execute(() -> {
            BloodHandler dh = new BloodHandler(type);
            List<Blood> dataset =
                    isWeek ?
                            dh.getDataInWeek(calendar.timeNow) :
                            dh.getDataInMonth(calendar.timeNow);

            final float[] MaxMinValues = {Float.MIN_VALUE, Float.MAX_VALUE};

            if (!dataset.isEmpty())
            {
                for (Blood blood : dataset)
                {
                    float value = Float.parseFloat(blood.mesure_val);
                    if (value > MaxMinValues[0]) MaxMinValues[0] = value;
                    if (value < MaxMinValues[1]) MaxMinValues[1] = value;
                }
            }
            else
            {
                MaxMinValues[0] = MaxMinValues[1] = 0f;
            }

            Log.d("BloodSugarInfo", "MaxMinValues : " + MaxMinValues[0] + ", " + MaxMinValues[1]);

            runOnUiThread(() -> {
                updateSugarValue(MaxMinValues[0], MaxMinValues[1]);
            });
        });
    }

    private void updateSugarValue(float maxValue, float minValue) // 최대, 최소를 UI에 반영
    {
        String maxValueStr = Float.toString(maxValue);
        String minValueStr = Float.toString(minValue);

        Log.d("BloodSugarInfo", "maxValueStr : " + maxValueStr + ", minValueStr : " + minValueStr);
        String maxText = getString(R.string.text_bs_max_recode_value, maxValueStr);
        String minText = getString(R.string.text_bs_min_recode_value, minValueStr);

        binding.sugarMaxValue.setText(maxText);
        binding.sugarMinValue.setText(minText);
    }


    private void updateDateRange(Boolean isWeek) // 구간 텍스트 업데이트
    {
        String startDate = null;
        String endDate = null;
        DateTimeFormatter output = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (isWeek) // 주간이면 해당 주 월 ~ 일까지
        {
            LocalDate monday = calendar.timeNow.with(DayOfWeek.MONDAY);
            LocalDate sunday = calendar.timeNow.with(DayOfWeek.SUNDAY);

            startDate = monday.format(output);
            endDate = sunday.format(output);
        }
        else // 월간이면 해당 월 첫 날 ~ 마지막 날까지
        {
            LocalDate firstDay = calendar.timeNow.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDay = calendar.timeNow.with(TemporalAdjusters.lastDayOfMonth());

            startDate = firstDay.format(output);
            endDate = lastDay.format(output);
        }

        String curDateRangeText = getString(R.string.text_date_range, startDate, endDate);
        binding.lastSugarDateRange.setText(curDateRangeText);
    }

    private void initEvents() // 버튼 별 이벤트 초기화
    {
        // 주간 혹은 월간 데이터 전송
        binding.toggleBloodInfo.setOnClickListener(v ->
        {
            ToggleButton tb = binding.toggleBloodInfo;
            isWeek = tb.isChecked();

            updateUI();
            updateDateRange(isWeek);
            transferData(isWeek);
        });

        // 이전 기간
        binding.buttonDecrease.setOnClickListener(v -> {
            ToggleButton tb = binding.toggleBloodInfo;
            isWeek = tb.isChecked();
            int type = isWeek ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.decrease(type);

            updateUI();
            updateDateRange(isWeek);
            transferData(isWeek);
        });

        // 다음 기간
        binding.buttonIncrease.setOnClickListener(v -> {
            ToggleButton tb = binding.toggleBloodInfo;
            isWeek = tb.isChecked();
            int type = isWeek ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.increase(type);

            updateUI();
            updateDateRange(isWeek);
            transferData(isWeek);
        });

        // 뒤로가기
        binding.back.setOnClickListener(v -> finish());
    }

    private BloodPack loadSugarData(Boolean isWeek) // isWeek가 true이면 주간 혈당 데이터를, false이면 월간 혈당 데이터를 불러옴.
    {
        BloodHandler dh = new BloodHandler(type);
        List<Blood> dataset =
                isWeek ?
                        dh.getDataInWeek(calendar.timeNow) :
                        dh.getDataInMonth(calendar.timeNow);

        System.out.println("TEST : " + dataset);
        if(dataset.isEmpty()) return new BloodPack(new ArrayList<>());
        return new BloodPack((ArrayList<Blood>) dataset);
    }

    private void transferData(Boolean isWeek) // 혈당 정보를 Fragment(그래프)한테 전달
    {
        executor.execute(() ->
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Header.WRAPPER_DATASET, loadSugarData(isWeek));

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment fragment = new SugarGraphFragment(isWeek, calendar.timeNow);
            fragment.setArguments(bundle);

            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 이전 fragment들이 back stack에 남지 않도록 비움.
            fragmentTransaction.replace(binding.sugarInfoData.getId(), fragment); // 기존 fragment를 새로운 fragment로 교체.

            fragmentTransaction.commit();
        });
    }

    private void initData()
    {
        transferData(true); // 기본값으로는 주별 정보를 불러온다.
    }
}
