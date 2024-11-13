package com.jeyun.rhdms;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.Pill;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticActivity extends AppCompatActivity {

    private TextView pillAvgTextView; // 복약 평균을 표시하는 TextView
    private TextView sugarAvgTextView; // 혈당 평균을 표시하는 TextView
    private TextView pressureAvgTextView; // 혈압 평균을 표시하는 TextView
    private TextView recentMedicationTextView; // 최근 복약 날짜를 표시하는 새로운 TextView

    private PillHandler pillHandler; // PillHandler 객체
    private BloodHandler bloodSugarHandler; // 혈당 데이터를 처리하는 BloodHandler 객체
    private BloodHandler bloodPressureHandler; // 혈압 데이터를 처리하는 BloodHandler 객체

    private Executor executor = Executors.newSingleThreadExecutor(); // 단일 스레드 실행자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // 뷰 초기화
        pillAvgTextView = findViewById(R.id.pill_avg_textview);
        sugarAvgTextView = findViewById(R.id.sugar_avg_textview);
        pressureAvgTextView = findViewById(R.id.pressure_avg_textview);
        recentMedicationTextView = findViewById(R.id.recent_medication_textview);

        // 데이터 가져오기 위한 핸들러 초기화
        pillHandler = new PillHandler();
        bloodSugarHandler = new BloodHandler("31"); // 31은 혈당(Blood Sugar)을 의미
        bloodPressureHandler = new BloodHandler("21"); // 21은 혈압(Blood Pressure)을 의미

        //LocalDate today = LocalDate.now();
        LocalDate today = LocalDate.of(2024, 8, 23); // 예시 날짜

        // 액티비티 시작 시 기본 주간 데이터를 로드
        executor.execute(() -> loadData(today));

        // 최근 복약 데이터를 로드하고 표시
        executor.execute(this::loadRecentMedicationDate);
    }

    private void loadRecentMedicationDate() {
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);
        List<Pill> allPills = new ArrayList<>();
        LocalDate currentMonth = oneYearAgo;

        // 1년 동안의 데이터를 월별로 가져와서 합침
        while (!currentMonth.isAfter(today)) {
            List<Pill> monthlyPills = pillHandler.getDataInMonth(currentMonth);
            allPills.addAll(monthlyPills);
            currentMonth = currentMonth.plusMonths(1);
        }

        // 가장 최근 복약 날짜 찾기
        LocalDate latestDate = getLatestMedicationDate(allPills);

        // UI 업데이트
        runOnUiThread(() -> {
            if (latestDate != null) {
                String formattedDate = latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                recentMedicationTextView.setText("최근 복약 날짜: " + formattedDate);
            } else {
                recentMedicationTextView.setText("최근 복약 날짜: 없음");
            }
        });
    }

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

    private void loadData(LocalDate today) {
        calculateWeeklyAverages(today); // 주간 평균 계산만 수행
    }

    private void calculateWeeklyAverages(LocalDate today) {
        List<Pill> pills = pillHandler.getDataInWeek(today);
        Collections.reverse(pills);
        double pillAverage = calculatePillScore(pills);

        List<Blood> bloodSugarData = bloodSugarHandler.getDataInWeek(today);
        Collections.reverse(bloodSugarData);
        double bloodSugarAverage = calculateBloodAverage(bloodSugarData);

        List<Blood> bloodPressureData = bloodPressureHandler.getDataInWeek(today);
        Collections.reverse(bloodPressureData);

        // 수정된 부분: 수축기와 이완기의 평균을 따로 계산하여 출력
        double[] bloodPressureAverages = calculateBloodPressureAverages(bloodPressureData);

        displayAverages(pillAverage, bloodSugarAverage, bloodPressureAverages[0], bloodPressureAverages[1]);
    }

    public double calculatePillScore(List<Pill> pills) {
        if (pills == null || pills.isEmpty()) return 0;

        double totalScore = 0;
        for (Pill pill : pills) {
            switch (pill.TAKEN_ST) {
                case "TAKEN":
                    totalScore += 1;
                    break;
                case "DELAYTAKEN":
                    totalScore += 0.5;
                    break;
                case "UNTAKEN":
                    totalScore += 0.1;
                    break;
                default:
                    totalScore += 0;
                    break;
            }
        }

        return totalScore / pills.size();
    }

    private double calculateBloodAverage(List<Blood> data) {
        if (data == null || data.isEmpty()) return 0;

        double sum = 0;
        int count = 0;

        for (Blood record : data) {
            try {
                sum += Double.parseDouble(record.mesure_val);
                count++;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return count > 0 ? sum / count : 0;
    }

    // 수정된 함수: 수축기/이완기 형식의 혈압 데이터를 처리하여 각각의 평균 계산
    private double[] calculateBloodPressureAverages(List<Blood> data) {
        if (data == null || data.isEmpty()) return new double[]{0, 0};

        double systolicSum = 0;   // 수축기 합계
        double diastolicSum = 0;  // 이완기 합계
        int count = 0;

        for (Blood record : data) {
            try {
                String[] parts = record.mesure_val.split("/");
                if (parts.length == 2) {
                    double systolic = Double.parseDouble(parts[0].trim());
                    double diastolic = Double.parseDouble(parts[1].trim());

                    systolicSum += systolic;
                    diastolicSum += diastolic;
                    count++;
                } else {
                    System.out.println("혈압 데이터 형식 오류: " + record.mesure_val);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count > 0) {
            double avgSystolic = systolicSum / count;      // 수축기 평균
            double avgDiastolic = diastolicSum / count;   // 이완기 평균
            return new double[]{avgSystolic, avgDiastolic};   // 수축기와 이완기의 평균 반환
        } else {
            return new double[]{0, 0};
        }
    }

    private void displayAverages(double pillAvg, double sugarAvg, double systolicAvg, double diastolicAvg) {
        runOnUiThread(() -> {
            if (pillAvg == 0) {
                pillAvgTextView.setText("최근 복약 정보가 없습니다.");
            } else {
                pillAvgTextView.setText(String.format("주간 복약률: %.2f", pillAvg));
            }

            if (sugarAvg == 0) {
                sugarAvgTextView.setText("최근 혈당 정보가 없습니다.");
            } else {
                sugarAvgTextView.setText(String.format("주간 혈당: %.2f", sugarAvg));
            }

            if (systolicAvg == 0 && diastolicAvg == 0) {
                pressureAvgTextView.setText("최근 혈압 정보가 없습니다.");
            } else {
                pressureAvgTextView.setText(String.format("주간 혈압\n수축기 %.2f mmHg\n이완기 %.2f mmHg", systolicAvg, diastolicAvg));
            }
        });
    }
}
