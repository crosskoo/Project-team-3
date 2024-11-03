package com.jeyun.rhdms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class DeviceSettingsActivity extends AppCompatActivity {

    private EditText pillboxId;

    // EditTexts for alarm time inputs
    private EditText alarmStartHour1st, alarmStartMinute1st, alarmEndHour1st, alarmEndMinute1st;
    private EditText alarmStartHour2nd, alarmStartMinute2nd, alarmEndHour2nd, alarmEndMinute2nd;
    private EditText alarmStartHour3rd, alarmStartMinute3rd, alarmEndHour3rd, alarmEndMinute3rd;

    private RadioGroup volumeGroup;

    // Switches for enabling/disabling alarms (2nd and 3rd)
    private Switch alarmEnable2nd, alarmEnable3rd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);

        // Initialize EditTexts for pillboxId and alarms
        pillboxId = findViewById(R.id.pillbox_id);

        // Initialize EditTexts for time inputs
        alarmStartHour1st = findViewById(R.id.alarm_start_hour_1st);
        alarmStartMinute1st = findViewById(R.id.alarm_start_minute_1st);
        alarmEndHour1st = findViewById(R.id.alarm_end_hour_1st);
        alarmEndMinute1st = findViewById(R.id.alarm_end_minute_1st);

        alarmStartHour2nd = findViewById(R.id.alarm_start_hour_2nd);
        alarmStartMinute2nd = findViewById(R.id.alarm_start_minute_2nd);
        alarmEndHour2nd = findViewById(R.id.alarm_end_hour_2nd);
        alarmEndMinute2nd = findViewById(R.id.alarm_end_minute_2nd);

        alarmStartHour3rd = findViewById(R.id.alarm_start_hour_3rd);
        alarmStartMinute3rd = findViewById(R.id.alarm_start_minute_3rd);
        alarmEndHour3rd = findViewById(R.id.alarm_end_hour_3rd);
        alarmEndMinute3rd = findViewById(R.id.alarm_end_minute_3rd);

        volumeGroup = findViewById(R.id.volume_group);

        // Initialize switches for 2nd and 3rd alarms
        alarmEnable2nd = findViewById(R.id.alarm_enable_2nd);
        alarmEnable3rd = findViewById(R.id.alarm_enable_3rd);

        // Save button logic
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

                    if (validateTimeInputs()) {
                        JSONObject settings = createSettingsJson();
                        showSendSettingsPopup(settings);
                        sendSettings(settings);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Validate time inputs to ensure they are within valid ranges and that end time is not before start time
    private boolean validateTimeInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // 첫 번째 알람 시간 검증 및 종료 시간이 시작 시간보다 늦은지 확인
        if (!isValidTime(alarmStartHour1st.getText().toString(), alarmStartMinute1st.getText().toString())) {
            errorMessage.append("기본 시간의 시작 시간이 잘못되었습니다.\n");
        }
        if (!isValidTime(alarmEndHour1st.getText().toString(), alarmEndMinute1st.getText().toString())) {
            errorMessage.append("기본 시간의 종료 시간이 잘못되었습니다.\n");
        } else if (!isEndTimeAfterStartTime(alarmStartHour1st.getText().toString(),
                alarmStartMinute1st.getText().toString(),
                alarmEndHour1st.getText().toString(),
                alarmEndMinute1st.getText().toString())) {
            errorMessage.append("기본 시간의 종료 시간이 시작 시간보다 빠를 수 없습니다.\n");
        }

        // 두 번째 알람이 활성화된 경우 시간 검증 및 종료 시간이 시작 시간보다 늦은지 확인
        if (alarmEnable2nd.isChecked()) {
            if (!isValidTime(alarmStartHour2nd.getText().toString(), alarmStartMinute2nd.getText().toString())) {
                errorMessage.append("추가시간1의 시작 시간이 잘못되었습니다.\n");
            }
            if (!isValidTime(alarmEndHour2nd.getText().toString(), alarmEndMinute2nd.getText().toString())) {
                errorMessage.append("추가시간1의 종료 시간이 잘못되었습니다.\n");
            } else if (!isEndTimeAfterStartTime(alarmStartHour2nd.getText().toString(),
                    alarmStartMinute2nd.getText().toString(),
                    alarmEndHour2nd.getText().toString(),
                    alarmEndMinute2nd.getText().toString())) {
                errorMessage.append("추가시간1의 종료 시간이 시작 시간보다 빠를 수 없습니다.\n");
            }
        }

        // 세 번째 알람이 활성화된 경우 시간 검증 및 종료 시간이 시작 시간보다 늦은지 확인
        if (alarmEnable3rd.isChecked()) {
            if (!isValidTime(alarmStartHour3rd.getText().toString(), alarmStartMinute3rd.getText().toString())) {
                errorMessage.append("추가시간2의 시작 시간이 잘못되었습니다.\n");
            }
            if (!isValidTime(alarmEndHour3rd.getText().toString(), alarmEndMinute3rd.getText().toString())) {
                errorMessage.append("추가시간2의 종료 시간이 잘못되었습니다.\n");
            } else if (!isEndTimeAfterStartTime(alarmStartHour3rd.getText().toString(),
                    alarmStartMinute3rd.getText().toString(),
                    alarmEndHour3rd.getText().toString(),
                    alarmEndMinute3rd.getText().toString())) {
                errorMessage.append("추가시간2의 종료 시간이 시작 시간보다 빠를 수 없습니다.\n");
            }
        }

        // 오류 메시지가 있다면 경고 대화 상자를 띄움
        if (errorMessage.length() > 0) {
            showAlert("잘못된 입력", errorMessage.toString());
            return false;
        }

        return true;
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

    // Check if the end time is after the start time
    private boolean isEndTimeAfterStartTime(String startHourStr, String startMinuteStr, String endHourStr, String endMinuteStr) {
        try {
            int startHour = Integer.parseInt(startHourStr);
            int startMinute = Integer.parseInt(startMinuteStr);
            int endHour = Integer.parseInt(endHourStr);
            int endMinute = Integer.parseInt(endMinuteStr);

            if (endHour > startHour) {
                return true; // End hour is later than start hour
            } else if (endHour == startHour && endMinute > startMinute) {
                return true; // End minute is later than start minute when hours are the same
            } else {
                return false; // End time is earlier or the same as start time
            }

        } catch (NumberFormatException e) {
            return false; // Invalid number format in input strings
        }
    }

    // Create JSON object with the settings data
    private JSONObject createSettingsJson() throws Exception {

        JSONObject settings = new JSONObject();

        settings.put("pillboxno", pillboxId.getText().toString());

        // 첫 번째 알람은 항상 설정됨
        settings.put("alarmStart_1st", getFormattedTime(
                alarmStartHour1st.getText().toString(),
                alarmStartMinute1st.getText().toString()));

        settings.put("alarmEnd_1st", getFormattedTime(
                alarmEndHour1st.getText().toString(),
                alarmEndMinute1st.getText().toString()));

        // 두 번째 및 세 번째 알람은 스위치가 켜진 경우에만 설정됨
        settings.put("alarmStart_2nd",
                alarmEnable2nd.isChecked() ? getFormattedTime(
                        alarmStartHour2nd.getText().toString(),
                        alarmStartMinute2nd.getText().toString()) : "");

        settings.put("alarmEnd_2nd",
                alarmEnable2nd.isChecked() ? getFormattedTime(
                        alarmEndHour2nd.getText().toString(),
                        alarmEndMinute2nd.getText().toString()) : "");

        settings.put("alarmStart_3rd",
                alarmEnable3rd.isChecked() ? getFormattedTime(
                        alarmStartHour3rd.getText().toString(),
                        alarmStartMinute3rd.getText().toString()) : "");

        settings.put("alarmEnd_3rd",
                alarmEnable3rd.isChecked() ? getFormattedTime(
                        alarmEndHour3rd.getText().toString(),
                        alarmEndMinute3rd.getText().toString()) : "");


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