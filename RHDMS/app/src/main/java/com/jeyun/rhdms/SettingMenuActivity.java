package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_menu);

        // Setting up buttons for App Setting and Device Setting
        Button appSettingButton = findViewById(R.id.buttonAppSetting);
        Button deviceSettingButton = findViewById(R.id.buttonDeviceSetting);

        appSettingButton.setOnClickListener(this::goToAppSetting);
        deviceSettingButton.setOnClickListener(this::goToDeviceSetting);
    }

    private void goToAppSetting(View view) {
        // Intent to navigate to App Setting screen
        Intent intent = new Intent(this, AppSettingsActivity.class);
        startActivity(intent);
    }

    private void goToDeviceSetting(View view) {
        // Intent to navigate to Device Setting screen
        Intent intent = new Intent(this, DeviceSettingsActivity.class);
        startActivity(intent);
    }
}
