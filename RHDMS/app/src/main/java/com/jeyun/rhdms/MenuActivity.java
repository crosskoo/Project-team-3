package com.jeyun.rhdms;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.jeyun.rhdms.databinding.ActivityMenuBinding;
import com.jeyun.rhdms.graphActivity.BloodPressureInfoActivity;
import com.jeyun.rhdms.graphActivity.BloodSugarInfoActivity;
import com.jeyun.rhdms.graphActivity.PressureInfoActivity;
import com.jeyun.rhdms.graphActivity.SugarInfoActivity;
import com.jeyun.rhdms.graphActivity.NewPillInfoActivity;
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.SharedPreferenceHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.User;
import com.jeyun.rhdms.util.SettingsManager;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import java.time.LocalDate;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;

import java.time.LocalDateTime;
import java.time.LocalTime;
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


        MyWorkManager.schedulePeriodicWork(Inspector.class, getApplicationContext());
        //test
        SettingsManager.getInstance(getApplicationContext()).setDisabledAlarmDate(LocalDate.of(2000, 10, 1));
        
        // 데이터 로드 메서드 호출
        loadRecentBloodData();

        //복약 상태
        updateAdherenceUI();
    }

    private void initEvents()
    {
        binding.buttonStatistics.setOnClickListener(v -> switchActivity(StatisticActivity.class)); // 통계 페이지
        binding.buttonPillInfo.setOnClickListener(v -> switchActivity(PillInfoActivity.class)); // 복약 정보
        binding.buttonPressureInfo.setOnClickListener(v -> switchActivity(BloodPressureInfoActivity.class)); // 혈압 페이지
        binding.buttonSugarInfo.setOnClickListener(v -> switchActivity(BloodSugarInfoActivity.class)); // 혈당 페이지
        binding.buttonBle.setOnClickListener(v -> switchActivity(BleActivity.class)); // 실시간 측정
        binding.buttonSettings.setOnClickListener(v -> switchActivity(SettingMenuActivity.class));  //설정 관련해서 추가.
        binding.buttonLogout.setOnClickListener(v -> {
            Inspector.cancelScheduledNotification(getApplicationContext(), LocalDate.now());
            MyWorkManager.cancelPeriodicWork(getApplicationContext());
            Logout();
        }); // 로그아웃
    }

    private <T> void switchActivity(T cls)
    {
        Intent intent = new Intent(getApplicationContext(), (Class<?>) cls);
        startActivity(intent);
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
                        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("아니요", null)
                .show();
    }

    // 최근 혈당과 혈압 정보 불러오기
    private void loadRecentBloodData() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            BloodHandler bloodSugarHandler = new BloodHandler("31"); // 혈당 타입
            BloodHandler bloodPressureHandler = new BloodHandler("21"); // 혈압 타입
            LocalDate today = LocalDate.now();
            List<Blood> recentBloodSugar = new ArrayList<>();
            List<Blood> recentBloodPressure = new ArrayList<>();

            // 최근 6개월간 데이터 조회
            for (int i = 0; i < 6; i++) {
                LocalDate targetDate = today.minusMonths(i);

                // 혈당 데이터 조회
                List<Blood> monthlySugarData = bloodSugarHandler.getDataInMonth(targetDate);
                if (monthlySugarData != null && !monthlySugarData.isEmpty()) {
                    recentBloodSugar.addAll(monthlySugarData);
                } else {
                    Log.d("MenuActivity", "해당 월에 혈당 데이터 없음: " + targetDate);
                }

                // 혈압 데이터 조회
                List<Blood> monthlyPressureData = bloodPressureHandler.getDataInMonth(targetDate);
                if (monthlyPressureData != null && !monthlyPressureData.isEmpty()) {
                    recentBloodPressure.addAll(monthlyPressureData);
                } else {
                    Log.d("MenuActivity", "해당 월에 혈압 데이터 없음: " + targetDate);
                }
            }

            runOnUiThread(() -> {
                // 최근 혈당 UI 업데이트
                updateBloodSugarUI(recentBloodSugar);

                // 최근 혈압 UI 업데이트
                updateBloodPressureUI(recentBloodPressure);
            });
        });
    }

    // 최근 혈당 UI 업데이트 함수
    private void updateBloodSugarUI(List<Blood> recentBloodSugar) {
        if (!recentBloodSugar.isEmpty()) {
            Blood latestSugar = recentBloodSugar.get(recentBloodSugar.size() - 1);
            String mesureVal = latestSugar.mesure_val;

            try {
                // 혈당 값은 단일 숫자여야 함
                binding.buttonSugarInfo.setText("최근 혈당: " + mesureVal + " mg/dL");
            } catch (NumberFormatException e) {
                e.printStackTrace();
                binding.buttonSugarInfo.setText("혈당 데이터 오류");
            }
        } else {
            binding.buttonSugarInfo.setText("최근 혈당 데이터 없음");
        }
    }

    // 최근 혈압 UI 업데이트 함수
    private void updateBloodPressureUI(List<Blood> recentBloodPressure) {
        if (!recentBloodPressure.isEmpty()) {
            Blood latestPressure = recentBloodPressure.get(recentBloodPressure.size() - 1);
            String mesureVal = latestPressure.mesure_val;

            try {
                // 혈압 값은 "수축기/이완기" 형식이어야 함
                String[] parts = mesureVal.split("/");
                if (parts.length == 2) {
                    String systolic = parts[0];
                    String diastolic = parts[1];
                    binding.buttonPressureInfo.setText("최근 혈압: " + systolic + "/" + diastolic + " mmHg");
                } else {
                    binding.buttonPressureInfo.setText("혈압 데이터 형식 오류");
                }
            } catch (Exception e) {
                e.printStackTrace();
                binding.buttonPressureInfo.setText("혈압 데이터 오류");
            }
        } else {
            binding.buttonPressureInfo.setText("최근 혈압 데이터 없음");
        }
    }

    // 최근 복약시간 불러오기 ( 알람 시작시간을 불러옴. )
    private LocalDateTime getLatestMedicationDateTime(List<Pill> pills) {
        LocalDateTime latestDateTime = null;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm"); // 시간 형식이 'HHmm'이라고 가정

        for (Pill pill : pills) {
            LocalDate pillDate = LocalDate.parse(pill.ARM_DT, dateFormatter);
            LocalTime pillTime = LocalTime.parse(pill.ARM_ST_TM, timeFormatter); // 복약 시간 파싱
            LocalDateTime pillDateTime = LocalDateTime.of(pillDate, pillTime);

            if (latestDateTime == null || pillDateTime.isAfter(latestDateTime)) {
                latestDateTime = pillDateTime;
            }
        }
        return latestDateTime;
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


    private void updateAdherenceUI() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            PillHandler pillHandler = new PillHandler();
            List<Pill> pills = new ArrayList<>();

            // Collect data for the past 6 months
            for (int i = 0; i < 6; i++) {
                LocalDate date = LocalDate.now().minusMonths(i);
                List<Pill> monthlyPills = pillHandler.getDataIn30days(date);
                pills.addAll(monthlyPills);
            }

            LocalDate latestDate = getLatestMedicationDate(pills);

            // 마지막 날짜로부터 30일 정보 받아오기.
            List<Pill> filteredPills = pillHandler.getDataIn30days(latestDate);

            runOnUiThread(() -> {
                if (latestDate != null) {


                    // 계산
                    double totalScore = 0;

                    for (Pill pill : filteredPills) {
                        switch (pill.TAKEN_ST) {
                            case "TAKEN":
                                totalScore += 1;
                                break;
                            case "OUTTAKEN":
                                totalScore += 1;
                                break;
                            case "DELAYTAKEN":
                                totalScore += 0.5;
                                break;
                            case "OVERTAKEN":
                                totalScore += 0.5;
                                break;
                            case "ERRTAKEN":
                                totalScore += 0.1;
                                break;
                            case "UNTAKEN":
                                totalScore += 0.1;
                                break;
                            default:
                                totalScore += 0;
                                break;
                        }
                    }

                    // UI 업데이트
                    String adherenceMessage;
                    int imageResId;
                    if (totalScore >= 80) {
                        adherenceMessage = "양호";
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.green));
                        imageResId = R.drawable.good;
                    } else if (totalScore >= 50) {
                        adherenceMessage = "보통";
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.yellow));
                        imageResId = R.drawable.common;
                    } else {
                        adherenceMessage = "부족";
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.red));
                        imageResId = R.drawable.bad;
                    }

                    binding.adherenceTextView.setText(adherenceMessage);
                    binding.adherenceImageView.setVisibility(View.VISIBLE);

                    String lastTakenMessage = "최근 복용 : " + latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    binding.lastTakenTextView.setText(lastTakenMessage);
                } else {
                    binding.adherenceTextView.setText("데이터 없음");
                    binding.lastTakenTextView.setText("최근 복용 : 없음.");
                    binding.adherenceImageView.setVisibility(View.GONE);
                }
            });
            // Get the latest medication date and time
            LocalDateTime latestDateTime = getLatestMedicationDateTime(pills);

            runOnUiThread(() -> {
                if (latestDateTime != null) {
                    String lastTakenMessage = "오늘의 복약시간: " + latestDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    binding.buttonPillInfo.setText(lastTakenMessage);
                } else {
                    binding.buttonPressureInfo.setText("오늘의 복약시간: 없음.");
                }
            });
        });
    }
}