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
import com.jeyun.rhdms.databinding.ActivityBloodInfoBinding;
import com.jeyun.rhdms.fragment.GraphFragment;
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.MyCalendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BloodPressureInfoActivity extends AppCompatActivity {

    private String type = "21"; //
    private ActivityBloodInfoBinding binding;
    private CustomCalendar calendar = new MyCalendar();
    protected Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBloodInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateUI();
        initEvents();
        initData();
    }

    private void updateUI()
    {
        executor.execute(() -> {
            List<Blood> monthlyPressureData = new ArrayList<>(); // 최근 혈압 데이터
            BloodHandler bloodHandler = new BloodHandler(type);
            LocalDate today = LocalDate.now();
            int month = 0;

            while (month <= 6) // 최근 6개월 간 데이터 조회
            {
                LocalDate targetDate = today.minusMonths(month);
                // 혈압 데이터 조회
                monthlyPressureData = bloodHandler.getDataInMonth(targetDate);
                if (!monthlyPressureData.isEmpty()) break; // 데이터가 있으면 반복문 종료
                month = month + 1;
            }

            Log.d("BloodPressureActivity", "최근 혈압 데이터:" + monthlyPressureData);

            Blood recentBloodData = monthlyPressureData.get(monthlyPressureData.size() - 1); // 최신 혈압 데이터

            runOnUiThread(() -> {
                updateLastBloodDate(recentBloodData);
                updateLastBloodValue(recentBloodData);
            });
        });
    }

    private void updateLastBloodDate(Blood recentBloodData)
    {
        if (recentBloodData != null)
        {
            String measureDate = recentBloodData.mesure_de; // 측정 날짜
            DateTimeFormatter input = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter output = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate date = LocalDate.parse(measureDate, input);
            String formattedDate = date.format(output);

            String lastDateText = getString(R.string.text_bp_last_recode_date, formattedDate);
            binding.lastBloodDate.setText(lastDateText);
        }
        else
        {
            binding.lastBloodDate.setText("최근 혈압 데이터 없음");
        }
    }

    private void updateLastBloodValue(Blood recentBloodData)
    {
        if (recentBloodData != null)
        {
            String measureVal = recentBloodData.mesure_val;
            String[] parts = measureVal.split("/");
            String systolic = parts[0]; // 수축기 혈압
            String diastolic = parts[1]; // 이완기 혈압

            String lastHighText = getString(R.string.text_bp_last_recode_high, systolic);
            String lastLowText = getString(R.string.text_bp_last_recode_low, diastolic);

            binding.lastBloodHighValue.setText(lastHighText);
            binding.lastBloodLowValue.setText(lastLowText);
        }
        else
        {
            binding.lastBloodHighValue.setText("최근 혈압 데이터 없음");
            binding.lastBloodLowValue.setText("최근 혈압 데이터 없음");
        }
    }

    private void initEvents() // 이벤트 초기화
    {
        // 주간 혹은 월간 데이터 전송
        binding.toggleBloodInfo.setOnClickListener(v ->
        {
            ToggleButton tb = binding.toggleBloodInfo;
            boolean isWeek = tb.isChecked();
            transferData(isWeek);
        });

        // 이전 기간
        binding.buttonDecrease.setOnClickListener(v -> {
            ToggleButton tb = binding.toggleBloodInfo;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.decrease(type);
            transferData(tb.isChecked());
        });

        // 다음 기간
        binding.buttonIncrease.setOnClickListener(v -> {
            ToggleButton tb = binding.toggleBloodInfo;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.increase(type);
            transferData(tb.isChecked());
        });
    }

    private BloodPack loadBloodData(Boolean isWeek) // isWeek가 true이면 주간 혈압 데이터를, false이면 월간 혈압 데이터를 불러옴.
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

    private void transferData(Boolean isWeek) // 혈압 정보를 Fragment(그래프)한테 전달
    {
        executor.execute(() ->
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Header.WRAPPER_DATASET, loadBloodData(isWeek));

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment fragment = new GraphFragment(isWeek, calendar.timeNow);
            fragment.setArguments(bundle);

            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 이전 fragment들이 back stack에 남지 않도록 비움.
            fragmentTransaction.replace(binding.bloodInfoData.getId(), fragment); // 기존 fragment를 새로운 fragment로 교체.

            fragmentTransaction.commit();
        });
    }

    private void initData()
    {
        transferData(true); // 기본값으로는 주별 정보를 불러온다.
    }
}
