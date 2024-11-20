package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.app.AlertDialog;
import java.time.format.TextStyle;
import android.content.DialogInterface;

public class StatisticActivity extends AppCompatActivity {

    private TextView pillAvg; // 주간 복약률을 표시하는 TextView
    private TextView sugarAvg; // 혈당 평균을 표시하는 TextView
    private TextView pressureAvg; // 혈압 평균을 표시하는 TextView
    private TextView recentMedication; // 최근 복약 날짜를 표시하는 TextView
    private TextView maxBloodSugar; // 최고 혈당을 표시하는 TextView
    private TextView maxBloodPressure; // 최고 혈압을 표시하는 TextView
    private TextView pillstatus;
    private TextView sugarstatus;
    private TextView pressurestatus;

    private TextView date;
    private PillHandler pillHandler; // PillHandler 객체
    private BloodHandler bloodSugarHandler; // 혈당 데이터를 처리하는 BloodHandler 객체
    private BloodHandler bloodPressureHandler; // 혈압 데이터를 처리하는 BloodHandler 객체

    private Executor executor = Executors.newSingleThreadExecutor(); // 단일 스레드 실행자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // 뷰 초기화
        pillAvg = findViewById(R.id.pill_avg);
        sugarAvg = findViewById(R.id.sugar_avg);
        pressureAvg = findViewById(R.id.pressure_avg);
        recentMedication = findViewById(R.id.recent_medication);
        maxBloodSugar = findViewById(R.id.max_blood_sugar);
        maxBloodPressure = findViewById(R.id.max_blood_pressure);
        pillstatus = findViewById(R.id.pill_status);
        sugarstatus = findViewById(R.id.blood_sugar_status);
        pressurestatus =findViewById(R.id.blood_pressure_status);

        date = findViewById(R.id.week_date_range);

        // 기준 관련.
        Button statistic_criteria = findViewById(R.id.statistic_criteria);

        // back 버튼 참조
        Button backButton = findViewById(R.id.back);

        // back 버튼 클릭 시 메뉴 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });

        pillHandler = new PillHandler();
        bloodSugarHandler = new BloodHandler("31"); // 31은 혈당(Blood Sugar)을 의미
        bloodPressureHandler = new BloodHandler("21"); // 21은 혈압(Blood Pressure)을 의미

        //LocalDate today = LocalDate.now();
        //TEST용
        LocalDate today = LocalDate.of(2024, 8, 23);

        // 이전 주간의 시작일과 종료일 계산 (오늘 포함한 일주일)
        LocalDate startOfPreviousWeek = today.minusDays(6); // 오늘을 포함하여 6일 전이 시작일
        LocalDate endOfPreviousWeek = today; // 오늘이 종료일

        // 포맷터 설정 (yyyy-MM-dd 형식)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 시작일과 종료일을 포맷팅하여 문자열로 변환
        String formattedStartOfPreviousWeek = startOfPreviousWeek.format(formatter);
        String formattedEndOfPreviousWeek = endOfPreviousWeek.format(formatter);

        // TextView에 이전 주간 날짜 범위 출력
        date.setText(String.format("통계 주: %s ~ %s", formattedStartOfPreviousWeek, formattedEndOfPreviousWeek));

        // 통계 기준
        statistic_criteria.setOnClickListener(v -> showPillScoreCriteria());

        // 액티비티 시작 시 기본 주간 데이터를 로드
        executor.execute(() -> loadData(today));

        // 이번달과 저번달을 비교.
        executor.execute(() -> loadMonthlyComparison(today));

        // 최근 복약 데이터를 로드하고 표시
        executor.execute(this::loadRecentMedicationDate);
    }

    // 통계 기준표를 보여주는 함수
    private void showPillScoreCriteria() {
        // AlertDialog.Builder를 사용하여 팝업 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("통계 기준");

        // 복약 점수 기준 설명
        String message = "복약 점수는 다음과 같이 계산됩니다:\n\n" +
                "1점: 정상복약, 외출복약\n" +
                "0.5점: 지연복약, 과복약\n" +
                "0.1점: 오복약, 미복약\n" +
                "0점: 그 외 상태\n\n" +
                "상태 측정 기준:\n\n" +
                "!!상태는 전 월과 이번 월의 데이터를 비교합니다.!!\n"+
                "복약 동일 기준 : -1 ~ 1\n"+
                "혈압 동일 기준 : -10 ~ 10\n"+
                "혈당 동일 기준 : -10 ~ 10\n";

        builder.setMessage(message);

        // 확인 버튼 추가
        builder.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());

        // 다이얼로그 보여주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
    이번 달고 저번 달 비교.
    */

    private void loadMonthlyComparison(LocalDate today) {
        // 이번 달 데이터 가져오기
        List<Pill> currentMonthPills = pillHandler.getDataInMonth(today);
        List<Blood> currentMonthBloodSugar = bloodSugarHandler.getDataInMonth(today);
        List<Blood> currentMonthBloodPressure = bloodPressureHandler.getDataInMonth(today);

        // 전달 데이터 가져오기
        LocalDate previousMonth = today.minusMonths(1); // 전달 계산
        List<Pill> previousMonthPills = pillHandler.getDataInMonth(previousMonth);
        List<Blood> previousMonthBloodSugar = bloodSugarHandler.getDataInMonth(previousMonth);
        List<Blood> previousMonthBloodPressure = bloodPressureHandler.getDataInMonth(previousMonth);

        // 각 달의 평균 계산
        double currentPillAverage = calculatePillScoreAverages(currentMonthPills);
        double previousPillAverage = calculatePillScoreAverages(previousMonthPills);

        double currentSugarAverage = calculateBloodAverage(currentMonthBloodSugar);
        double previousSugarAverage = calculateBloodAverage(previousMonthBloodSugar);

        double[] currentPressureAverages = calculateBloodPressureAverages(currentMonthBloodPressure);
        double[] previousPressureAverages = calculateBloodPressureAverages(previousMonthBloodPressure);

        // 차이 계산
        double pillDifference = currentPillAverage - previousPillAverage;
        double sugarDifference = currentSugarAverage - previousSugarAverage;
        double pressureDifferenceSystolic = currentPressureAverages[0] - previousPressureAverages[0];
        double pressureDifferenceDiastolic = currentPressureAverages[1] - previousPressureAverages[1];

        // 상태 평가
        String pillStatus = evaluatePillDifference(pillDifference, previousMonth);  // 전달의 월 정보 추가
        String sugarStatus = evaluateSugarDifference(sugarDifference, previousMonth);  // 전달의 월 정보 추가
        String pressureStatus = evaluatePressureDifference(pressureDifferenceSystolic, pressureDifferenceDiastolic, previousMonth);  // 전달의 월 정보 추가

        /*
        // 전달과 이번 달의 데이터를 UI에 출력
        runOnUiThread(() -> {
            pillstatus.setText(String.format("이번달 복약률: %.2f, 전달 복약률: %.2f", currentPillAverage, previousPillAverage));
            sugarstatus.setText(String.format("이번달 혈당 평균: %.2f mg/dL, 전달 혈당 평균: %.2f mg/dL", currentSugarAverage, previousSugarAverage));
            pressurestatus.setText(String.format("이번달 혈압 평균: 수축기 %.2f / 이완기 %.2f mmHg\n전달 혈압 평균: 수축기 %.2f / 이완기 %.2f mmHg",
                    currentPressureAverages[0], currentPressureAverages[1], previousPressureAverages[0], previousPressureAverages[1]));
        });
        */

        runOnUiThread(() -> {
            // 복약률 상태 출력
            if (currentPillAverage == 0 && previousPillAverage == 0) {
                pillstatus.setText("2달간 복약 데이터가 없습니다.");
            } else if (currentPillAverage == 0) {
                pillstatus.setText("이번달 복약 데이터가 없습니다.");
            } else if (previousPillAverage == 0) {
                pillstatus.setText("전달 복약 데이터가 없습니다.");
            } else {
                pillstatus.setText(pillStatus);
            }

            // 혈당 상태 출력
            if (currentSugarAverage == 0 && previousSugarAverage == 0) {
                sugarstatus.setText("2달간 혈당 데이터가 없습니다.");
            } else if (currentSugarAverage == 0) {
                sugarstatus.setText("이번달 혈당 데이터가 없습니다.");
            } else if (previousSugarAverage == 0) {
                sugarstatus.setText("전달 혈당 데이터가 없습니다.");
            } else {
                sugarstatus.setText(sugarStatus);
            }

            // 혈압 상태 출력
            if (currentPressureAverages[0] == 0 && currentPressureAverages[1] == 0 &&
                    previousPressureAverages[0] == 0 && previousPressureAverages[1] == 0) {
                pressurestatus.setText("2달간 혈압 데이터가 없습니다.");
            } else if (currentPressureAverages[0] == 0 && currentPressureAverages[1] == 0) {
                pressurestatus.setText("이번달 혈압 데이터가 없습니다.");
            } else if (previousPressureAverages[0] == 0 && previousPressureAverages[1] == 0) {
                pressurestatus.setText("전달 혈압 데이터가 없습니다.");
            } else {
                pressurestatus.setText(pressureStatus);
            }
        });

    }


    public double calculatePillScoreAverages(List<Pill> pills) {
        if (pills == null || pills.isEmpty()) return 0;

        double totalScore = 0;
        int count=0;

        for (Pill pill : pills) {
            switch (pill.TAKEN_ST) {
                case "TAKEN":
                case "OUTTAKEN":
                    totalScore += 1;
                    break;
                case "DELAYTAKEN":
                case "OVERTAKEN":
                    totalScore += 0.5;
                    break;
                case "ERRTAKEN":
                case "UNTAKEN":
                    totalScore += 0.1;
                    break;
                default:
                    totalScore += 0;
                    break;
            }
            count++;
        }

        return totalScore/count;
    }


    // 혈압 차이에 따른 상태 평가 함수
    private String evaluatePressureDifference(double systolicDiff, double diastolicDiff, LocalDate previousMonth) {
        String previousMonthName = previousMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        if (systolicDiff > 10 || diastolicDiff > 10) {
            return String.format("혈압 상태: %s에 비해 평균 혈압이 상승했습니다.", previousMonthName);
        } else if (systolicDiff < -10 || diastolicDiff < -10) {
            return String.format("혈압 상태: %s에 비해 평균 혈압이 감소했습니다.", previousMonthName);
        } else {
            return String.format("혈압 상태: %s과 평균 혈압이 비슷합니다.", previousMonthName);
        }
    }

    // 혈당 차이에 따라 상태를 평가하는 함수
    private String evaluateSugarDifference(double difference, LocalDate previousMonth) {
        String previousMonthName = previousMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        if (difference > 10) {
            return String.format("혈당 상태: %s에 비해 평균 혈당이 상승했습니다.", previousMonthName);
        } else if (difference < -10) {
            return String.format("혈당 상태: %s에 비해 평균 혈당이 감소했습니다.", previousMonthName);
        } else {
            return String.format("혈당 상태: %s의 평균 혈당과 비슷합니다.", previousMonthName);
        }
    }

    // 복약 차이에 따라 상태를 평가하는 함수
    private String evaluatePillDifference(double difference, LocalDate previousMonth) {
        String previousMonthName = previousMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        if (difference > 0.1) {
            return String.format("복약 상태: %s보다 복약률이 증가했습니다.", previousMonthName);
        } else if (difference < -0.1) {
            return String.format("복약 상태: %s보다 복약률이 떨어졌습니다.", previousMonthName);
        } else {
            return String.format("복약 상태: %s의 복약률과 유사합니다.", previousMonthName);
        }
    }

    /*
    여기서 부터 주간 평균 구하기.
     */

    private void loadData(LocalDate today) {
        calculateWeeklyAverages(today);
    }

    private void calculateWeeklyAverages(LocalDate today) {
        List<Pill> pills = pillHandler.getDataIn7days(today); // 주간 복약 데이터 가져오기
        Collections.reverse(pills);
        double pillAverage = calculatePillScore(pills); // 주간 복약률 계산

        List<Blood> bloodSugarData = bloodSugarHandler.getDataIn7days(today);
        Collections.reverse(bloodSugarData);

        List<Blood> bloodPressureData = bloodPressureHandler.getDataIn7days(today);
        Collections.reverse(bloodPressureData);

        double bloodSugarAverage = calculateBloodAverage(bloodSugarData);
        double[] bloodPressureAverages = calculateBloodPressureAverages(bloodPressureData);

        Blood maxBloodSugarRecord = findMaxValue(bloodSugarData);
        Blood maxBloodPressureRecord = findMaxBloodPressure(bloodPressureData);

        displayAverages(pillAverage, bloodSugarAverage, bloodPressureAverages[0], bloodPressureAverages[1], maxBloodSugarRecord, maxBloodPressureRecord);
    }

    private void loadRecentMedicationDate() {
        LocalDate today = LocalDate.now(); // 현재 날짜
        LocalDate sixMonthsAgo = today.minusMonths(6); // 6개월 전 날짜

        List<Pill> allPills = new ArrayList<>(); // 모든 복약 데이터를 저장할 리스트
        LocalDate currentMonth = today; // 현재 달부터 시작

        // 6개월 동안의 데이터를 월별로 가져와서 합침
        for (int i = 0; i < 6; i++) {
            List<Pill> monthlyPills = pillHandler.getDataInMonth(currentMonth); // 해당 월의 데이터 가져오기
            allPills.addAll(monthlyPills); // 데이터를 리스트에 추가
            currentMonth = currentMonth.minusMonths(1); // 한 달 전으로 이동
        }

        // 가장 최근 복약 날짜 찾기
        LocalDate latestDate = getLatestMedicationDate(allPills);

        // UI 업데이트
        runOnUiThread(() -> {
            if (latestDate != null) {
                String formattedDate = latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                recentMedication.setText("최근 복약 날짜: " + formattedDate);
            } else {
                recentMedication.setText("최근 복약 날짜: 없음");
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

    public double calculatePillScore(List<Pill> pills) {
        if (pills == null || pills.isEmpty()) return 0;

        double totalScore = 0;

        for (Pill pill : pills) {
            switch (pill.TAKEN_ST) {
                case "TAKEN":
                case "OUTTAKEN":
                    totalScore += 1;
                    break;
                case "DELAYTAKEN":
                case "OVERTAKEN":
                    totalScore += 0.5;
                    break;
                case "ERRTAKEN":
                case "UNTAKEN":
                    totalScore += 0.1;
                    break;
                default:
                    totalScore += 0;
                    break;
            }
        }

        return totalScore;
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

    private double[] calculateBloodPressureAverages(List<Blood> data) {
        if (data == null || data.isEmpty()) return new double[]{0, 0};

        double systolicSum = 0;
        double diastolicSum = 0;
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
            double avgSystolic = systolicSum / count;
            double avgDiastolic = diastolicSum / count;
            return new double[]{avgSystolic, avgDiastolic};
        } else {
            return new double[]{0, 0};
        }
    }

    private Blood findMaxValue(List<Blood> data) {
        if (data == null || data.isEmpty()) return null;

        return Collections.max(data, (record1, record2) -> Double.compare(Double.parseDouble(record1.mesure_val), Double.parseDouble(record2.mesure_val)));
    }

    private Blood findMaxBloodPressure(List<Blood> data) {
        if (data == null || data.isEmpty()) return null;

        return Collections.max(data, (record1, record2) -> Double.compare(Double.parseDouble(record1.mesure_val.split("/")[0]), Double.parseDouble(record2.mesure_val.split("/")[0])));
    }

    private void displayAverages(double pillavg, double sugaravg, double systolicavg, double diastolicavg, Blood maxBloodSugarRecord, Blood maxBloodPressureRecord) {

        runOnUiThread(() -> {

            if (pillavg == 0) {
                pillAvg.setText("최근 복약 정보가 없습니다.");
            } else {
                pillAvg.setText(String.format("주간 복약 점수: %.1f / 7 점", pillavg));
            }

            if (sugaravg == 0) {
                sugarAvg.setText("최근 혈당 정보가 없습니다.");
            } else {
                sugarAvg.setText(String.format("주간 혈당: %.2f mg/dL", sugaravg));
            }

            if (systolicavg == 0 && diastolicavg == 0) {
                pressureAvg.setText("최근 혈압 정보가 없습니다.");
            } else {
                pressureAvg.setText(String.format("주간 혈압\n수축기 %.2f mmHg\n이완기 %.2f mmHg", systolicavg, diastolicavg));
            }

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            if (maxBloodSugarRecord != null) {
                try {
                    LocalDate sugarMaxDate = LocalDate.parse(maxBloodSugarRecord.mesure_de, inputFormatter);
                    String formattedSugarMaxDate = sugarMaxDate.format(outputFormatter);
                    maxBloodSugar.setText(String.format("주간 최고 혈당: %s mg/dL (%s)", maxBloodSugarRecord.mesure_val, formattedSugarMaxDate));
                } catch (Exception e) {
                    e.printStackTrace();
                    maxBloodSugar.setText("최고 혈당 기록이 없습니다.");
                }
            } else {
                maxBloodSugar.setText("최고 혈당 기록이 없습니다.");
            }

            if (maxBloodPressureRecord != null) {
                try {
                    LocalDate pressureMaxDate = LocalDate.parse(maxBloodPressureRecord.mesure_de, inputFormatter);
                    String formattedPressureMaxDate = pressureMaxDate.format(outputFormatter);
                    maxBloodPressure.setText(String.format("주간 최고 혈압: %s mmHg (%s)", maxBloodPressureRecord.mesure_val, formattedPressureMaxDate));
                } catch (Exception e) {
                    e.printStackTrace();
                    maxBloodPressure.setText("최고 혈압 기록이 없습니다.");
                }
            } else {
                maxBloodPressure.setText("최고 혈압 기록이 없습니다.");
            }
        });
    }
}