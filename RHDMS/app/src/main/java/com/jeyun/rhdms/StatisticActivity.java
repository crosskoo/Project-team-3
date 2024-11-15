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

        pillHandler = new PillHandler();
        bloodSugarHandler = new BloodHandler("31"); // 31은 혈당(Blood Sugar)을 의미
        bloodPressureHandler = new BloodHandler("21"); // 21은 혈압(Blood Pressure)을 의미

        LocalDate today = LocalDate.of(2024, 8, 23);// 예시 날짜

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

        // 액티비티 시작 시 기본 주간 데이터를 로드
        executor.execute(() -> loadData(today));

        // 4주간 데이터를 로드하고 처리
        executor.execute(() -> loadMonthlyData(today));

        // 최근 복약 데이터를 로드하고 표시
        executor.execute(this::loadRecentMedicationDate);
    }

    /*
    4주간 데이터를 통계.
    */

    // 4주간의 데이터를 불러오고 처리하는 함수
    private void loadMonthlyData(LocalDate today) {
        List<List<Pill>> weeklyPillsData = new ArrayList<>();
        List<List<Blood>> weeklyBloodSugarData = new ArrayList<>();
        List<List<Blood>> weeklyBloodPressureData = new ArrayList<>();

        // 최근 4주 동안의 데이터를 가져옴
        for (int i = 0; i < 4; i++) {
            LocalDate weekStartDate = today.minusWeeks(i);

            // 복약 데이터
            List<Pill> weeklyPills = pillHandler.getDataIn7days(weekStartDate);
            weeklyPillsData.add(weeklyPills);

            // 혈당 데이터
            List<Blood> weeklyBloodSugar = bloodSugarHandler.getDataIn7days(weekStartDate);
            weeklyBloodSugarData.add(weeklyBloodSugar);

            // 혈압 데이터
            List<Blood> weeklyBloodPressure = bloodPressureHandler.getDataIn7days(weekStartDate);
            weeklyBloodPressureData.add(weeklyBloodPressure);
        }

        // 각 주의 평균 계산
        List<Double> weeklyPillAverages = calculateWeeklyAverages(weeklyPillsData);
        List<Double> weeklySugarAverages = calculatemonthlyBloodAverages(weeklyBloodSugarData);
        List<double[]> weeklyPressureAverages = calculatemonthlyBloodPressureAverages(weeklyBloodPressureData);

        // 최근 한 주와 이전 3주의 평균 차이 계산 (복약, 혈당, 혈압)
        double pillDifference = calculateTotalDifference(weeklyPillAverages);
        double sugarDifference = calculateTotalDifference(weeklySugarAverages);
        double pressureDifference = calculateTotalPressureDifference(weeklyPressureAverages);

        // 상태 평가 및 UI 업데이트
        String pillStatus = evaluatePillDifference(pillDifference);
        String sugarStatus = evaluateSugarDifference(sugarDifference);
        String pressureStatus = evaluatePressureDifference(pressureDifference);

        runOnUiThread(() -> {
            pillstatus.setText(pillStatus);
            sugarstatus.setText(sugarStatus);
            pressurestatus.setText(pressureStatus);
        });
    }

    // 각 주의 혈당 평균 계산
    private List<Double> calculatemonthlyBloodAverages(List<List<Blood>> weeklyData) {
        List<Double> weeklyAverages = new ArrayList<>();

        for (List<Blood> weekData : weeklyData) {
            double totalScore = 0;
            int count = 0;

            for (Blood record : weekData) {
                try {
                    totalScore += Double.parseDouble(record.mesure_val);
                    count++;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            double average = count > 0 ? totalScore / count : 0;
            weeklyAverages.add(average);
        }

        return weeklyAverages;
    }

    // 각 주의 혈압 평균 계산
    private List<double[]> calculatemonthlyBloodPressureAverages(List<List<Blood>> weeklyData) {
        List<double[]> weeklyAverages = new ArrayList<>();

        for (List<Blood> weekData : weeklyData) {
            double systolicSum = 0;
            double diastolicSum = 0;
            int count = 0;

            for (Blood record : weekData) {
                try {
                    String[] parts = record.mesure_val.split("/");
                    if (parts.length == 2) {
                        systolicSum += Double.parseDouble(parts[0].trim());
                        diastolicSum += Double.parseDouble(parts[1].trim());
                        count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            double avgSystolic = count > 0 ? systolicSum / count : 0;
            double avgDiastolic = count > 0 ? diastolicSum / count : 0;

            weeklyAverages.add(new double[]{avgSystolic, avgDiastolic});
        }

        return weeklyAverages;
    }

    // 최근 한 주와 이전 3주의 평균 차이를 계산하는 함수
    private double calculateTotalDifference(List<Double> weeklyAverages) {
        if (weeklyAverages.size() < 4)
            return 0;

        double diff1 = weeklyAverages.get(0) - weeklyAverages.get(1);
        double diff2 = weeklyAverages.get(0) - weeklyAverages.get(2);
        double diff3 = weeklyAverages.get(0) - weeklyAverages.get(3);

        return diff1 + diff2 + diff3;
    }
    // 최근 한 주와 이전 3주의 혈압 차이를 계산하는 함수
    private double calculateTotalPressureDifference(List<double[]> weeklyAverages) {
        if (weeklyAverages.size() < 4) return 0;

        double systolicDiff1 = weeklyAverages.get(1)[0] - weeklyAverages.get(0)[0];
        double systolicDiff2 = weeklyAverages.get(2)[0] - weeklyAverages.get(0)[0];
        double systolicDiff3 = weeklyAverages.get(3)[0] - weeklyAverages.get(0)[0];

        double diastolicDiff1 = weeklyAverages.get(1)[1] - weeklyAverages.get(0)[1];
        double diastolicDiff2 = weeklyAverages.get(2)[1] - weeklyAverages.get(0)[1];
        double diastolicDiff3 = weeklyAverages.get(3)[1] - weeklyAverages.get(0)[1];

        // 수축기와 이완기의 차이를 합산하여 반환
        return (systolicDiff1 + systolicDiff2 + systolicDiff3) + (diastolicDiff1 + diastolicDiff2 + diastolicDiff3);
    }

    // 혈압 차이에 따른 상태 평가 함수
    private String evaluatePressureDifference(double difference) {
        if (difference > 10) {
            return "혈압 상태: 혈압이 상승했습니다. ";
        } else if (difference < -10) {
            return "혈압 상태: 혈압이 감소했습니다. ";
        } else {
            return "혈압 상태: 동일합니다. ";
        }
    }

    // 혈당 차이에 따라 상태를 평가하는 함수
    private String evaluateSugarDifference(double difference) {
        if (difference > 10) {
            return "혈당 상태: 혈당이 상승했습니다. ";
        } else if (difference < -10) {
            return "혈당 상태: 혈당이 감소했습니다. ";
        } else {
            return "혈당 상태: 동일합니다. ";
        }
    }

    // 복약 차이에 따라 상태를 평가하는 함수
    private String evaluatePillDifference(double difference) {
        if (difference > 1) {
            return "복약 상태: 복약률이 증가했습니다. ";
        } else if (difference < -1) {
            return "복약 상태: 복약률이 떨어졌습니다. ";
        } else {
            return "복약 상태: 복약률이 동일합니다. ";
        }
    }

    // 각 주의 복약 점수를 계산하는 함수
    private List<Double> calculateWeeklyAverages(List<List<Pill>> weeklyData) {
        List<Double> weeklyAverages = new ArrayList<>();
        int count=0;

        for (List<Pill> weekData : weeklyData) {
            double totalScore = 0;
            for (Pill pill : weekData) {
                count++;
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
            double average = weekData.isEmpty() ? 0 : totalScore;
            weeklyAverages.add(average);
        }

        return weeklyAverages;
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
                    maxBloodSugar.setText(String.format("최고 혈당: %s mg/dL (%s)", maxBloodSugarRecord.mesure_val, formattedSugarMaxDate));
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
                    maxBloodPressure.setText(String.format("최고 혈압: %s mmHg (%s)", maxBloodPressureRecord.mesure_val, formattedPressureMaxDate));
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