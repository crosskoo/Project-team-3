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
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private EditText pillboxId;
    private Button alarmStartHour1st, alarmStartMinute1st, alarmEndHour1st, alarmEndMinute1st;
    private Button alarmStartHour2nd, alarmStartMinute2nd, alarmEndHour2nd, alarmEndMinute2nd;
    private Button alarmStartHour3rd, alarmStartMinute3rd, alarmEndHour3rd, alarmEndMinute3rd;
    private RadioGroup volumeGroup;

    private Switch alarmEnable1st, alarmEnable2nd, alarmEnable3rd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        pillboxId = findViewById(R.id.pillbox_id);

        // Initialize buttons
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

        // Initialize switches
        alarmEnable1st = findViewById(R.id.alarm_enable_1st);
        alarmEnable2nd = findViewById(R.id.alarm_enable_2nd);
        alarmEnable3rd = findViewById(R.id.alarm_enable_3rd);

        // Set up time buttons
        setupTimeButton(alarmStartHour1st, "hour", 24);
        setupTimeButton(alarmStartMinute1st, "minute", 60);
        setupTimeButton(alarmEndHour1st, "hour", 24);
        setupTimeButton(alarmEndMinute1st, "minute", 60);

        setupTimeButton(alarmStartHour2nd, "hour", 24);
        setupTimeButton(alarmStartMinute2nd, "minute", 60);
        setupTimeButton(alarmEndHour2nd, "hour", 24);
        setupTimeButton(alarmEndMinute2nd, "minute", 60);

        setupTimeButton(alarmStartHour3rd, "hour", 24);
        setupTimeButton(alarmStartMinute3rd, "minute", 60);
        setupTimeButton(alarmEndHour3rd, "hour", 24);
        setupTimeButton(alarmEndMinute3rd, "minute", 60);

        Button saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsPopup();
                // sendSettings(); // Uncomment this line to send settings to server once it's ready
            }
        });
    }

    private void setupTimeButton(Button button, final String type, final int max) {
        button.setOnClickListener(new View.OnClickListener() {
            int value = 0;

            @Override
            public void onClick(View v) {
                if (type.equals("hour")) {
                    value = (value + 1) % max;
                } else if (type.equals("minute")) {
                    value = (value + 10) % max;
                }
                button.setText(String.format(Locale.getDefault(), "%02d", value));
            }
        });
    }

    private void showSettingsPopup() {
        try {
            JSONObject settings = new JSONObject();
            settings.put("pillboxno", pillboxId.getText().toString());

            // Check each alarm's enable switch and set times accordingly
            if (alarmEnable1st.isChecked()) {
                settings.put("alarmStart_1st", getFormattedTime(alarmStartHour1st.getText().toString(),
                        alarmStartMinute1st.getText().toString()));
                settings.put("alarmEnd_1st", getFormattedTime(alarmEndHour1st.getText().toString(),
                        alarmEndMinute1st.getText().toString()));
            } else {
                settings.put("alarmStart_1st", "");
                settings.put("alarmEnd_1st", "");
            }

            if (alarmEnable2nd.isChecked()) {
                settings.put("alarmStart_2nd", getFormattedTime(alarmStartHour2nd.getText().toString(),
                        alarmStartMinute2nd.getText().toString()));
                settings.put("alarmEnd_2nd", getFormattedTime(alarmEndHour2nd.getText().toString(),
                        alarmEndMinute2nd.getText().toString()));
            } else {
                settings.put("alarmStart_2nd", "");
                settings.put("alarmEnd_2nd", "");
            }

            if (alarmEnable3rd.isChecked()) {
                settings.put("alarmStart_3rd", getFormattedTime(alarmStartHour3rd.getText().toString(),
                        alarmStartMinute3rd.getText().toString()));
                settings.put("alarmEnd_3rd", getFormattedTime(alarmEndHour3rd.getText().toString(),
                        alarmEndMinute3rd.getText().toString()));
            } else {
                settings.put("alarmStart_3rd", "");
                settings.put("alarmEnd_3rd", "");
            }

            int selectedVolumeId = volumeGroup.getCheckedRadioButtonId();
            RadioButton selectedVolume = findViewById(selectedVolumeId);
            if (selectedVolume != null) {
                settings.put("volume", selectedVolume.getText().toString());
            }

            // Show the JSON in a popup dialog
            new AlertDialog.Builder(this)
                    .setTitle("Settings JSON")
                    .setMessage(settings.toString(4)) // Pretty print with indentation
                    .setPositiveButton(android.R.string.ok, null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    private void sendSettings() {
        try {
            JSONObject settings = new JSONObject();
            settings.put("pillboxno", pillboxId.getText().toString());

            // Include switch states in JSON
            settings.put("alarmEnable_1st", alarmEnable1st.isChecked());
            settings.put("alarmEnable_2nd", alarmEnable2nd.isChecked());
            settings.put("alarmEnable_3rd", alarmEnable3rd.isChecked());

            settings.put("alarmStart_1st", getFormattedTime(alarmStartHour1st.getText().toString(),
                                                              alarmStartMinute1st.getText().toString()));
            settings.put("alarmEnd_1st", getFormattedTime(alarmEndHour1st.getText().toString(),
                                                            alarmEndMinute1st.getText().toString()));

            settings.put("alarmStart_2nd", getFormattedTime(alarmStartHour2nd.getText().toString(),
                                                              alarmStartMinute2nd.getText().toString()));
            settings.put("alarmEnd_2nd", getFormattedTime(alarmEndHour2nd.getText().toString(),
                                                            alarmEndMinute2nd.getText().toString()));

            settings.put("alarmStart_3rd", getFormattedTime(alarmStartHour3rd.getText().toString(),
                                                              alarmStartMinute3rd.getText().toString()));
            settings.put("alarmEnd_3rd", getFormattedTime(alarmEndHour3rd.getText().toString(),
                                                            alarmEndMinute3rd.getText().toString()));

            int selectedVolumeId = volumeGroup.getCheckedRadioButtonId();
            RadioButton selectedVolume = findViewById(selectedVolumeId);
            if (selectedVolume != null) {
                settings.put("volume", selectedVolume.getText().toString());
            }

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
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    private String getFormattedTime(String hourStr, String minuteStr) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                Integer.parseInt(hourStr), Integer.parseInt(minuteStr));
    }
}