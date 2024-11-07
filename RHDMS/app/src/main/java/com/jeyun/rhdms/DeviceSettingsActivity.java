package com.jeyun.rhdms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.jeyun.rhdms.handler.entity.User;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class DeviceSettingsActivity extends AppCompatActivity {

    private EditText pillboxId;

    // EditTexts for alarm time inputs
    private EditText alarmStartHour1st, alarmStartMinute1st;
    private EditText alarmStartHour2nd, alarmStartMinute2nd;
    private EditText alarmStartHour3rd, alarmStartMinute3rd;

    private EditText alarmDuration1st;  // 첫 번째 알람의 지속 시간
    private EditText alarmDuration2nd;  // 두 번째 알람의 지속 시간
    private EditText alarmDuration3rd; // 세 번째 알람의 지속 시간

    private EditText settingnum;

    private Button SettingResetButton;
    private Button FactoryResetButton;

    private RadioGroup volumeGroup;

    String alarmEndHour1st= String.valueOf(0);
    String alarmEndMinute1st= String.valueOf(0);

    String alarmEndHour2nd= String.valueOf(0);
    String alarmEndMinute2nd= String.valueOf(0);

    String alarmEndHour3rd= String.valueOf(0);
    String alarmEndMinute3rd= String.valueOf(0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        // Initialize EditTexts for pillboxId and alarms
        pillboxId = findViewById(R.id.pillbox_id);

        // Initialize EditTexts for time inputs
        alarmStartHour1st = findViewById(R.id.alarm_start_hour_1st);
        alarmStartMinute1st = findViewById(R.id.alarm_start_minute_1st);

        alarmStartHour2nd = findViewById(R.id.alarm_start_hour_2nd);
        alarmStartMinute2nd = findViewById(R.id.alarm_start_minute_2nd);

        alarmStartHour3rd = findViewById(R.id.alarm_start_hour_3rd);
        alarmStartMinute3rd = findViewById(R.id.alarm_start_minute_3rd);

        alarmDuration1st = findViewById(R.id.alarm1_running_time);
        alarmDuration2nd = findViewById(R.id.alarm2_running_time);
        alarmDuration3rd = findViewById(R.id.alarm3_running_time);

        settingnum = findViewById(R.id.settingnumber);

        SettingResetButton = findViewById(R.id.SettingReset_button);
        FactoryResetButton = findViewById(R.id.DeviceRest_button);

        volumeGroup = findViewById(R.id.volume_group);

        String user_id= User.getInstance().getOrgnztId();
        pillboxId.setText(user_id);

        // 뒤로가기 버튼
        Button backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

        //설정 초기화
        SettingResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 경고 대화상자 생성
                new AlertDialog.Builder(DeviceSettingsActivity.this)
                        .setTitle("경고")
                        .setMessage("설정을 초기화 합니다!!!")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Yes 클릭 시 설정 초기화 실행
                            try {
                                // PillboxID는 그대로 유지
                                String pillboxIdValue = pillboxId.getText().toString();

                                // 알람 크기 및 개수를 0으로 설정
                                alarmDuration1st.setText("0");
                                alarmDuration2nd.setText("0");
                                alarmDuration3rd.setText("0");
                                settingnum.setText("0"); // 알람 개수 0

                                // 나머지 값들을 빈 문자열로 설정
                                alarmStartHour1st.setText("");
                                alarmStartMinute1st.setText("");
                                alarmStartHour2nd.setText("");
                                alarmStartMinute2nd.setText("");
                                alarmStartHour3rd.setText("");
                                alarmStartMinute3rd.setText("");

                                // 볼륨 선택 해제 (RadioGroup)
                                volumeGroup.clearCheck();

                                // JSON 객체 생성 및 전송
                                JSONObject settings = new JSONObject();
                                settings.put("pillboxno", pillboxIdValue);  // 약상자 ID는 유지
                                settings.put("alarmStart_1st", "");
                                settings.put("alarmEnd_1st", "");
                                settings.put("alarmStart_2nd", "");
                                settings.put("alarmEnd_2nd", "");
                                settings.put("alarmStart_3rd", "");
                                settings.put("alarmEnd_3rd", "");
                                settings.put("volume", "");  // 볼륨 값도 빈 문자열로

                                showSendSettingsPopup(settings);  // 팝업으로 확인 후 전송
                                sendSettings(settings);  // 서버로 전송

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton("No", null) // No 클릭 시 아무 작업도 하지 않음
                        .show();
            }
        });

        // 공장 초기화 버튼
        FactoryResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 경고 대화상자 생성
                new AlertDialog.Builder(DeviceSettingsActivity.this)
                        .setTitle("경고")
                        .setMessage("기기를 공장 초기화합니다!!!")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Yes 클릭 시 공장 초기화 실행
                            try {
                                //공장 초기화

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton("No", null) // No 클릭 시 아무 작업도 하지 않음
                        .show();
            }
        });
        //저장 버튼
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 약상자 ID와 볼륨이 입력되었는지 확인
                    if (pillboxId.getText().toString().trim().isEmpty()) {
                        showAlert("오류", "약상자 ID를 입력하세요.");
                        return;
                    }

                    int selectedVolumeId = volumeGroup.getCheckedRadioButtonId();
                    if (selectedVolumeId == -1) { // 아무 볼륨도 선택되지 않은 경우
                        showAlert("오류", "볼륨을 선택하세요.");
                        return;
                    }

                    // settingnum 값에 따라 알람 개수를 확인
                    int numOfAlarms = Integer.parseInt(settingnum.getText().toString());

                    // settingnum이 3을 넘으면 오류 메시지 표시
                    if (numOfAlarms > 3) {
                        showAlert("오류", "알람 설정은 최대 3개까지만 가능합니다.");
                        return;
                    }

                    // 첫 번째 알람의 시작 시간이 올바른지 검사
                    if (numOfAlarms >= 1) {
                        if (!isValidTime(alarmStartHour1st.getText().toString(), alarmStartMinute1st.getText().toString())) {
                            showAlert("오류", "첫 번째 알람의 시작 시간이 올바르지 않습니다.");
                            return;
                        }
                    }

                    // 두 번째 알람의 시작 시간이 올바른지 검사 (필요 시)
                    if (numOfAlarms >= 2) {
                        if (!isValidTime(alarmStartHour2nd.getText().toString(), alarmStartMinute2nd.getText().toString())) {
                            showAlert("오류", "두 번째 알람의 시작 시간이 올바르지 않습니다.");
                            return;
                        }
                    }

                    // 세 번째 알람의 시작 시간이 올바른지 검사 (필요 시)
                    if (numOfAlarms == 3) {
                        if (!isValidTime(alarmStartHour3rd.getText().toString(), alarmStartMinute3rd.getText().toString())) {
                            showAlert("오류", "세 번째 알람의 시작 시간이 올바르지 않습니다.");
                            return;
                        }
                    }

                    // JSON 객체 생성 및 설정 저장
                    JSONObject settings = createSettingsJson();
                    showSendSettingsPopup(settings);
                    sendSettings(settings);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 종료 시간을 계산하는 함수
    private String calculateEndTime(String startHourStr, String startMinuteStr, String durationStr) {
        try {
            int startHour = Integer.parseInt(startHourStr);
            int startMinute = Integer.parseInt(startMinuteStr);
            int duration = Integer.parseInt(durationStr);

            // 분 단위로 총 시간을 계산
            int totalMinutes = startHour * 60 + startMinute + duration;

            // 종료 시간의 시와 분을 다시 구함
            int endHour = (totalMinutes / 60) % 24; // 24시간 형식으로 변환
            int endMinute = totalMinutes % 60;

            return String.format(Locale.getDefault(), "%02d%02d", endHour, endMinute);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ""; // 오류 발생 시 기본값 반환
        }
    }

    // 종료 시간을 계산하는 함수
    private void calculateAndSetEndTime() {
        // 첫 번째 알람 종료 시간 계산 및 설정
        if (isValidTime(alarmStartHour1st.getText().toString(), alarmStartMinute1st.getText().toString())) {
            String endTime1st = calculateEndTime(
                    alarmStartHour1st.getText().toString(),
                    alarmStartMinute1st.getText().toString(),
                    alarmDuration1st.getText().toString());
            alarmEndHour1st = endTime1st.substring(0, 2);  // 종료 시각의 시
            alarmEndMinute1st = endTime1st.substring(2, 4);  // 종료 시각의 분
        }

        // 두 번째 알람 종료 시간 계산 및 설정
        if (isValidTime(alarmStartHour2nd.getText().toString(), alarmStartMinute2nd.getText().toString())) {
            String endTime2nd = calculateEndTime(
                    alarmStartHour2nd.getText().toString(),
                    alarmStartMinute2nd.getText().toString(),
                    alarmDuration2nd.getText().toString());
            alarmEndHour2nd = endTime2nd.substring(0, 2);
            alarmEndMinute2nd = endTime2nd.substring(2, 4);
        }

        // 세 번째 알람 종료 시간 계산 및 설정
        if (isValidTime(alarmStartHour3rd.getText().toString(), alarmStartMinute3rd.getText().toString())) {
            String endTime3rd = calculateEndTime(
                    alarmStartHour3rd.getText().toString(),
                    alarmStartMinute3rd.getText().toString(),
                    alarmDuration3rd.getText().toString());
            alarmEndHour3rd = endTime3rd.substring(0, 2);
            alarmEndMinute3rd = endTime3rd.substring(2, 4);
        }
    }

    // Check if the hour and minute inputs are valid numbers within the correct range
    private boolean isValidTime(String hourStr, String minuteStr) {
        try {
            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);
            return (hour >= 0 && hour < 24) && (minute >= 0 && minute < 60);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private JSONObject createSettingsJson() throws Exception {
        JSONObject settings = new JSONObject();

        // 약상자 ID 설정
        settings.put("pillboxno", pillboxId.getText().toString());

        // settingnum 값에 따라 알람 설정
        int numOfAlarms = Integer.parseInt(settingnum.getText().toString());

        // 종료 시간 계산
        calculateAndSetEndTime();

        if (numOfAlarms >= 1) {
            // 첫 번째 알람 설정
            settings.put("alarmStart_1st", getFormattedTime(
                    alarmStartHour1st.getText().toString(),
                    alarmStartMinute1st.getText().toString()));

            // 첫 번째 알람 종료 시간 설정
            settings.put("alarmEnd_1st", getFormattedTime(alarmEndHour1st, alarmEndMinute1st));
        } else {
            settings.put("alarmStart_1st", "");
            settings.put("alarmEnd_1st", "");
        }

        if (numOfAlarms >= 2) {
            // 두 번째 알람 설정
            settings.put("alarmStart_2nd", getFormattedTime(
                    alarmStartHour2nd.getText().toString(),
                    alarmStartMinute2nd.getText().toString()));

            // 두 번째 알람 종료 시간 설정
            settings.put("alarmEnd_2nd", getFormattedTime(alarmEndHour2nd, alarmEndMinute2nd));
        } else {
            settings.put("alarmStart_2nd", "");
            settings.put("alarmEnd_2nd", "");
        }

        if (numOfAlarms == 3) {
            // 세 번째 알람 설정
            settings.put("alarmStart_3rd", getFormattedTime(
                    alarmStartHour3rd.getText().toString(),
                    alarmStartMinute3rd.getText().toString()));

            // 세 번째 알람 종료 시간 설정
            settings.put("alarmEnd_3rd", getFormattedTime(alarmEndHour3rd, alarmEndMinute3rd));
        } else {
            settings.put("alarmStart_3rd", "");
            settings.put("alarmEnd_3rd", "");
        }

        // 볼륨 설정
        int selectedVolumeId = volumeGroup.getCheckedRadioButtonId();
        RadioButton selectedVolume = findViewById(selectedVolumeId);
        if (selectedVolume != null) {
            settings.put("volume", selectedVolume.getText().toString());
        }

        return settings;
    }
    // Show a popup with the JSON data before sending it
    private void showSendSettingsPopup(JSONObject settings) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Send Settings JSON")
                    .setMessage(settings.toString(4)) // Pretty print with indentation
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send the JSON data to the server
    private void sendSettings(JSONObject settings) {
        try {
            String urlString = "http://211.229.106.53:8080/restful/미정.json";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = settings.toString().getBytes("utf-8");
                os.write(input, 0, input.length);

                int responseCode = conn.getResponseCode();

                new AlertDialog.Builder(this)
                        .setTitle("Response")
                        .setMessage("Response Code: " + responseCode)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 시간을 HHMM 형식으로 포맷하는 메서드 (예: 9 13 -> 0913)
    private String getFormattedTime(String hourStr, String minuteStr) {
        return String.format(Locale.getDefault(), "%02d%02d",
                Integer.parseInt(hourStr), Integer.parseInt(minuteStr));
    }

    // Show an alert dialog for errors or invalid input
    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
