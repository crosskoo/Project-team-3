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
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.SharedPreferenceHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.User;
import com.jeyun.rhdms.util.SettingsManager;
import com.jeyun.rhdms.util.worker.Inspector;
import com.jeyun.rhdms.util.worker.MyWorkManager;

import java.time.Duration;
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
        // binding.buttonSettings.setOnClickListener(v -> switchActivity(SettingMenuActivity.class));  //설정 관련해서 추가.
        binding.buttonSettings.setOnClickListener(v -> switchActivity(AppSettingsActivity.class)); // 설정 페이지
        // 주간/월간 복약 점수 토글 이벤트 추가
        binding.adherenceTextView.setOnClickListener(v -> toggleAdherenceScore());
    }

    private boolean isWeekly = true; // 현재 점수 표시 상태 (true: 주간, false: 월간)

    private void toggleAdherenceScore() {
        if (isWeekly) {
            // 현재 주간 점수 표시 중 -> 월간 점수로 전환
            calculateMonthlyAdherence();
        } else {
            // 현재 월간 점수 표시 중 -> 주간 점수로 전환
            updateAdherenceUI();
        }
        // 상태 토글
        isWeekly = !isWeekly;
    }

    private <T> void switchActivity(T cls)
    {
        Intent intent = new Intent(getApplicationContext(), (Class<?>) cls);
        startActivity(intent);
    }

    private void calculateMonthlyAdherence() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            PillHandler pillHandler = new PillHandler();
            List<Pill> pills = new ArrayList<>();

            // 지난 한 달간의 데이터 수집
            LocalDate today = LocalDate.now();
            List<Pill> monthlyPills = pillHandler.getDataIn30days(today);
            pills.addAll(monthlyPills);

            // 월간 복약 점수 및 순응률 계산
            double totalScore = 0;
            int maxScore = pills.size();

            for (Pill pill : pills) {
                switch (pill.TAKEN_ST) {
                    case "TAKEN":
                    case "OUTTAKEN":
                        totalScore += 1;
                        break;
                    case "DELAYTAKEN":
                        totalScore += 0.5;
                        break;
                    case "OVERTAKEN":
                    case "ERRTAKEN":
                    case "UNTAKEN":
                        totalScore += 0.1;
                        break;
                    default:
                        totalScore += 0;
                        break;
                }
            }

            double adherencePercentage = (totalScore / maxScore) * 100;

            // UI 업데이트
            double finalTotalScore = totalScore;
            runOnUiThread(() -> updateMonthlyAdherenceUI(finalTotalScore, adherencePercentage));
        });
    }

    private void updateMonthlyAdherenceUI(double totalScore, double adherencePercentage) {
        String adherenceMessage;
        int imageResId;

        if (adherencePercentage >= 80) {
            adherenceMessage = String.format("월간 복약 점수: %.1f (탁월한 관리)", totalScore);
            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.good));
            imageResId = R.drawable.good;
        } else if (adherencePercentage >= 50) {
            adherenceMessage = String.format("월간 복약 점수: %.1f (준수한 관리)", totalScore);
            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.common));
            imageResId = R.drawable.common;
        } else if (adherencePercentage > 0) {
            adherenceMessage = String.format("월간 복약 점수: %.1f (미흡한 관리)", totalScore);
            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.bad));
            imageResId = R.drawable.bad;
        }
        else{
            adherenceMessage = String.format(" No Data !!!");
            binding.adherenceTextView.setTextColor(getResources().getColor(R.color.bad));
            imageResId = R.drawable.bad;
        }

        // UI 업데이트
        binding.adherenceTextView.setText(adherenceMessage);
        binding.adherenceImageView.setImageResource(imageResId);
        binding.adherencePercentTextView.setText(
                String.format("복약 순응률 : %.1f%%", adherencePercentage)
        );
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
            for (int i = 5; i >= 0; i--) {
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
            LocalDate measureDate = LocalDate.parse(latestSugar.mesure_de, DateTimeFormatter.ofPattern("yyyyMMdd"));
            try {
                // Display blood sugar value with date
                binding.buttonSugarInfo.setText("최근 혈당: " + mesureVal + " mg/dL\n(" + measureDate.toString() + ")");
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
            LocalDate measureDate = LocalDate.parse(latestPressure.mesure_de , DateTimeFormatter.ofPattern("yyyyMMdd"));

            try {
                // Blood pressure value should be in "systolic/diastolic" format
                String[] parts = mesureVal.split("/");
                if (parts.length == 2) {
                    String systolic = parts[0];
                    String diastolic = parts[1];
                    // Display blood pressure value with date
                    binding.buttonPressureInfo.setText("최근 혈압: " + systolic + "/" + diastolic + " mmHg\n(" + measureDate.toString() + ")");
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

    // 최근 복약시간 불러오기 ( 알림 종료시간을 불러옴. )
    private LocalDateTime getLatestMedicationEndTime(List<Pill> pills) {
        LocalDateTime latestEndTime = null;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm"); // 시간 형식이 'HHmm'이라고 가정

        for (Pill pill : pills) {
            LocalDate pillDate = LocalDate.parse(pill.ARM_DT, dateFormatter);
            LocalTime pillTime = LocalTime.parse(pill.ARM_ED_TM, timeFormatter); // 복약 시간 파싱
            LocalDateTime pillDateTime = LocalDateTime.of(pillDate, pillTime);

            if (latestEndTime == null || pillDateTime.isAfter(latestEndTime)) {
                latestEndTime = pillDateTime;
            }
        }
        return latestEndTime;
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

            for (int i = 0; i < 6; i++) {
                LocalDate date = LocalDate.now().minusMonths(i);
                List<Pill> monthlyPills = pillHandler.getDataIn30days(date);
                pills.addAll(monthlyPills);
            }

            LocalDate today = LocalDate.now();
            LocalDate latestDate = getLatestMedicationDate(pills);

            // 마지막 날짜로부터 30일 정보 받아오기.
            List<Pill> filteredPills = pillHandler.getDataIn30days(today);

            runOnUiThread(() -> {
                if (latestDate != null) {

                    // 계산
                    double totalScore = 0;
                    int maxScore = filteredPills.size();

                    for (Pill pill : filteredPills) {
                        switch (pill.TAKEN_ST) {
                            case "TAKEN":
                            case "OUTTAKEN":
                                totalScore += 1;
                                break;
                            case "DELAYTAKEN":
                                totalScore += 0.5;
                                break;
                            case "OVERTAKEN":
                            case "ERRTAKEN":
                            case "UNTAKEN":
                                totalScore += 0.1;
                                break;
                            default:
                                totalScore += 0;
                                break;
                        }
                    }

                    double adherencePercentage = (totalScore / maxScore) * 100;
                    Log.d("test", "percent : " + adherencePercentage);

                    // UI 업데이트
                    String adherenceMessage;
                    int imageResId;
                    if (adherencePercentage >= 80) {
                        adherenceMessage = String.format("주간 복약 점수: %.1f (탁월한 관리)", totalScore);
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.good));
                        imageResId = R.drawable.good;
                    } else if (adherencePercentage >= 50) {
                        adherenceMessage = String.format("주간 복약 점수: %.1f (준수한 관리)", totalScore);
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.common));
                        imageResId = R.drawable.common;
                    } else if (adherencePercentage >0)  {
                        adherenceMessage = String.format("주간 복약 점수: %.1f (미흡한 관리)", totalScore);
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.bad));
                        imageResId = R.drawable.bad;
                    }
                    else{
                        adherenceMessage = String.format(" No Data !!!");
                        binding.adherenceTextView.setTextColor(getResources().getColor(R.color.bad));
                        imageResId = R.drawable.bad;
                    }

                    binding.adherenceTextView.setText(adherenceMessage);
                    binding.adherenceImageView.setImageResource(imageResId);
                    binding.adherencePercentTextView.setText(
                            String.format("복약 순응률 : %.1f%%", adherencePercentage)
                    );
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
            LocalDateTime latestEndTime = getLatestMedicationEndTime(pills);

            // 복약 스케줄 설정
            User.getInstance().setARM_ST_TM(latestDateTime);
            User.getInstance().setARM_ED_TM(latestEndTime);
            Log.d("schedule",
                    "latestDateTime : " +
                    User.getInstance().getARM_ST_TM() +
                    "latestEndTime : " +
                    User.getInstance().getARM_ED_TM());

            runOnUiThread(() -> {
                List<Pill> todayPills = pillHandler.getTodayMedicationData(today);

                if (latestDateTime != null) {
                    LocalDateTime now = LocalDateTime.now(); // 현재 시간 가져오기
                    String message;

                    // 현재 시간과 복약 시간의 차이 계산
                    long minutesDifference = Duration.between(now, latestDateTime).toMinutes();

                    if (minutesDifference >= 0 && minutesDifference <= 30) {
                        // 복약 시간이 현재 시간의 30분 이내인 경우
                        message = "현재 복약을 해야 합니다!!";
                    } else if (now.isAfter(latestDateTime)) {
                        // 현재 시간이 복약 시간 이후인 경우
                        if (todayPills.isEmpty()) {
                            // 오늘 복약 데이터가 없는 경우
                            message = "오늘 복약을 하지 않았습니다!";
                        } else {
                            Pill todayPill = todayPills.get(0); // 오늘의 복약 정보.
                            if ("TAKEN".equals(todayPill.TAKEN_ST)) {
                                message = "오늘 복약을 완료했습니다.\n( 복약 시간: " + todayPill.TAKEN_TM + " )";
                            } else {
                                message = "오늘 복약을 하지 않았습니다!";
                            }
                        }
                    } else {
                        // 복약 시간이 아직 멀리 있는 경우
                        message = "오늘의 복약 시간: " + latestDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    }

                    binding.buttonPillInfo.setText(message); // 버튼 텍스트 업데이트
                } else {
                    // 복약 시간이 없을 경우
                    binding.buttonPillInfo.setText("오늘의 복약시간: 없음.");
                }
            });
        });
    }
}