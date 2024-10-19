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
import com.jeyun.rhdms.databinding.ActivityDeviceSettingsBinding;

public class DeviceSettingsActivity extends AppCompatActivity {

    private EditText pillboxId;
    private Button alarmStartHour1st, alarmStartMinute1st, alarmEndHour1st, alarmEndMinute1st;
    private Button alarmStartHour2nd, alarmStartMinute2nd, alarmEndHour2nd, alarmEndMinute2nd;
    private Button alarmStartHour3rd, alarmStartMinute3rd, alarmEndHour3rd, alarmEndMinute3rd;
    private RadioGroup volumeGroup;

    private Switch alarmEnable1st, alarmEnable2nd, alarmEnable3rd;
    private ActivityDeviceSettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceSettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize EditText for pillboxId
        pillboxId = findViewById(R.id.pillbox_id);
        pillboxId.setText(""); // Set initial query to an empty string

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
                try {
                    JSONObject settings = createSettingsJson();
                    showSendSettingsPopup(settings);
                    sendSettings(settings);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private JSONObject createSettingsJson() throws Exception {
        JSONObject settings = new JSONObject();
        settings.put("pillboxno", pillboxId.getText().toString());

        settings.put("alarmStart_1st", alarmEnable1st.isChecked() ? getFormattedTime(alarmStartHour1st.getText().toString(), alarmStartMinute1st.getText().toString()) : "");
        settings.put("alarmEnd_1st", alarmEnable1st.isChecked() ? getFormattedTime(alarmEndHour1st.getText().toString(), alarmEndMinute1st.getText().toString()) : "");

        settings.put("alarmStart_2nd", alarmEnable2nd.isChecked() ? getFormattedTime(alarmStartHour2nd.getText().toString(), alarmStartMinute2nd.getText().toString()) : "");
        settings.put("alarmEnd_2nd", alarmEnable2nd.isChecked() ? getFormattedTime(alarmEndHour2nd.getText().toString(), alarmEndMinute2nd.getText().toString()) : "");

        settings.put("alarmStart_3rd", alarmEnable3rd.isChecked() ? getFormattedTime(alarmStartHour3rd.getText().toString(), alarmStartMinute3rd.getText().toString()) : "");
        settings.put("alarmEnd_3rd", alarmEnable3rd.isChecked() ? getFormattedTime(alarmEndHour3rd.getText().toString(), alarmEndMinute3rd.getText().toString()) : "");

        int selectedVolumeId = volumeGroup.getCheckedRadioButtonId();
        RadioButton selectedVolume = findViewById(selectedVolumeId);
        if (selectedVolume != null) {
            settings.put("volume", selectedVolume.getText().toString());
        }

        return settings;
    }

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

                // Display response code in an alert dialog
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

    private String getFormattedTime(String hourStr, String minuteStr) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                Integer.parseInt(hourStr), Integer.parseInt(minuteStr));
    }
}