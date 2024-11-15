package com.jeyun.rhdms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.jeyun.rhdms.bluetooth.BluetoothHandler;
import com.jeyun.rhdms.bluetooth.NetworkHandler;
import com.jeyun.rhdms.bluetooth.measurement.BloodPressureMeasurement;
import com.jeyun.rhdms.bluetooth.measurement.GlucoseMeasurement;
import com.jeyun.rhdms.bluetooth.measurement.TemperatureMeasurement;
import com.jeyun.rhdms.bluetooth.measurement.unit.GlucoseMeasurementUnit;
import com.jeyun.rhdms.bluetooth.measurement.unit.TemperatureUnit;
import com.jeyun.rhdms.databinding.ActivityBleBinding;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.factory.AlertFactory;
import com.jeyun.rhdms.util.factory.PopupFactory;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BleActivity extends AppCompatActivity {


    private TextView notifyText;
    private TextView measurementValue;
    private static final String DEVICE_ID   = "90000002";
    private static final String SUBJECT_ID  = "20";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;

    private final DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.ENGLISH);

    SharedPreferences           pref;
    SharedPreferences.Editor    editor;
    private TextView textViewBloodPressure;
    private TextView textViewBloodSugar;
    private TextView textViewTemperature;
    private ActivityBleBinding binding;

    String myBloodPressure, myBloodSugar, myTemperature;

    Button removeData;
    Button exitApp;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat     = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    SimpleDateFormat mFormatDate = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat mFormatTime = new SimpleDateFormat("HHmm");

    /////////////////////////////////
    // Json TEST
    static RequestQueue requestQueue;
    final private static String CONNECT_URL = "http://211.229.106.53:8080/restful/lbdy-mesure-colct.json";
    /////////////////////////////////

    private long backKeyPressedTime = 0;
    private PopupFactory<AlertDialog> factory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        checkPermissions();

        this.binding = ActivityBleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        factory = new AlertFactory(binding.getRoot().getContext());
        measurementValue    = binding.bloodPressureValue;
        notifyText          = binding.notifyMessage;
        removeData          = binding.btnRemoveData;
        exitApp             = binding.btnExit;

        Log.v("fdfsfd", "SDfsdf2244ds");
        initRegister();
        initUI();
        initEvents();

        // RequestQueue 객체 생성
        if (Objects.isNull(requestQueue)) requestQueue = Volley.newRequestQueue(getApplicationContext());

    }

    private void registerService(BroadcastReceiver receiver, String action)
    {
        IntentFilter filter = new IntentFilter(action);
        registerReceiver(receiver,  filter, RECEIVER_NOT_EXPORTED);
    }

    private void initRegister()
    {
        registerService(locationServiceStateReceiver, LocationManager.MODE_CHANGED_ACTION);
        registerService(bloodPressureDataReceiver, Header.MEASUREMENT_BLOODPRESSURE);
        registerService(temperatureDataReceiver, Header.MEASUREMENT_TEMPERATURE);
        registerService(glucoseDataReceiver, Header.MEASUREMENT_GLUCOSE);
        registerService(bluetoothConnectRecv, Header.INTENT_BT_CONNECTED);
        registerService(bluetoothDisconnectRecv, Header.INTENT_BT_DISCONNECTED);
    }

    private void initUI()
    {
        pref    = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor  = pref.edit();

        myBloodPressure         = pref.getString("dataBloodPressure",   "No Data");
        myBloodSugar            = pref.getString("dataBloodSugar",      "No Data");
        myTemperature           = pref.getString("dataTemperature",     "No Data");

        textViewBloodPressure   = binding.recentlyBloodPressure;
        textViewBloodSugar      = binding.recentlyBloodSugar;
        textViewTemperature     = binding.recentlyTemperature;

        textViewBloodPressure.setText(String.valueOf(myBloodPressure));
        textViewBloodSugar.setText(String.valueOf(myBloodSugar));
        textViewTemperature.setText(String.valueOf(myTemperature));
    }

    private void initEvents()
    {
        binding.back2.setOnClickListener(v -> finish());
        removeData.setOnClickListener(view ->
        {
            AlertDialog dialog = factory.createPopupYN("최근 데이터 삭제", "정말로 삭제하시겠습니까?",
                    (dg, i) ->
                    {
                        editor.clear();
                        editor.commit();

                        myBloodPressure         = pref.getString("dataBloodPressure",   "No Data");
                        myBloodSugar            = pref.getString("dataBloodSugar",      "No Data");
                        myTemperature           = pref.getString("dataTemperature",     "No Data");

                        textViewBloodPressure.setText(String.valueOf(myBloodPressure));
                        textViewBloodSugar.setText(String.valueOf(myBloodSugar));
                        textViewTemperature.setText(String.valueOf(myTemperature));
                    }, (dg, i) -> dg.cancel());
            dialog.show();
        });

        exitApp.setOnClickListener(view ->
        {
            AlertDialog dialog = factory.createPopupYN("종료", "정말로 종료하시겠습니까?",
                    (dg, which) -> finish(),
                    (dg, which) -> dg.cancel());
            dialog.show();
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000)
        {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000)
        {
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume()
    {
        super.onResume();
        if (getBluetoothManager().getAdapter() != null)
        {
            if (!isBluetoothEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            else
            {
                checkPermissions();
            }

        }

        else
        {
            System.out.println("This device has no Bluetooth hardware");
        }
    }

    private boolean isBluetoothEnabled()
    {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;
        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        BluetoothHandler.getInstance(getApplicationContext());
    }

    @NotNull
    private BluetoothManager getBluetoothManager()
    {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(locationServiceStateReceiver);
        unregisterReceiver(bloodPressureDataReceiver);
        unregisterReceiver(temperatureDataReceiver);
        unregisterReceiver(glucoseDataReceiver);
        unregisterReceiver(bluetoothConnectRecv);
        unregisterReceiver(bluetoothDisconnectRecv);
    }

    private final BroadcastReceiver locationServiceStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                boolean isEnabled = areLocationServicesEnabled();
                System.out.println("Location service state changed to: %s" + (isEnabled ? "on" : "off"));
                checkPermissions();
            }
        }
    };

    private final BroadcastReceiver bloodPressureDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral          = getPeripheral(intent.getStringExtra(Header.MEASUREMENT_EXTRA_PERIPHERAL));
            BloodPressureMeasurement measurement    = (BloodPressureMeasurement) intent.getSerializableExtra(Header.MEASUREMENT_BLOODPRESSURE_EXTRA);

            if (measurement != null) {
                System.out.println("date : ");
                notifyText.setText(String.format(Locale.KOREA, "혈압 데이터 수신 : %s", peripheral.getName()));
                //measurementValue.setText(String.format(Locale.ENGLISH, "%.0f/%.0f %s, %.0f bpm\n%s\n\nfrom %s", measurement.systolic, measurement.diastolic, measurement.isMMHG ? "mmHg" : "kpa", measurement.pulseRate, dateFormat.format(measurement.timestamp), peripheral.getName()));
                measurementValue.setText(String.format(Locale.ENGLISH, "SYS %.0f %s\nDIA %.0f %s\n%.0f bpm", measurement.systolic,     measurement.isMMHG ? "mmHg" : "kpa",
                        measurement.diastolic,    measurement.isMMHG ? "mmHg" : "kpa",
                        measurement.pulseRate ));

                runOnUiThread(() ->
                {
                    // Alert when It comes with the blood pressure out of normal range
                    if(measurement.isHigh() || measurement.isLow())
                    {
                        String message = measurement.isHigh() ? "혈압이 높습니다, 5분 후 다시 측정해주세요." : "혈압이 낮습니다, 5분 후 다시 측정해주세요.";
                        AlertDialog alert = factory.createPopup("경고", message, (dialog, which) -> dialog.cancel());
                        alert.show();
                    }
                });


                editor.putString("dataBloodPressure", String.format(Locale.ENGLISH, "[%s] %.0f / %.0f / %.0f", dateFormat.format(measurement.timestamp), measurement.systolic, measurement.diastolic, measurement.pulseRate));
                editor.apply();

                myBloodPressure = pref.getString("dataBloodPressure", "No Data");
                textViewBloodPressure.setText(String.valueOf(myBloodPressure));

                System.out.println("timestamp : " +  dateFormat.format(measurement.timestamp));
                System.out.println("date : " + mFormatDate.format(measurement.timestamp));
                System.out.println("time : " + mFormatTime.format(measurement.timestamp));

                String tempValue  = String.format(Locale.ENGLISH, "%.0f/%.0f", measurement.systolic, measurement.diastolic);
                System.out.println("tempValue : " + tempValue);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("pillboxno", DEVICE_ID);
                map.put("subjectid", SUBJECT_ID);
                map.put("mesure_tp", "2");
                map.put("mesure_de", mFormatDate.format(measurement.timestamp));
                map.put("mesure_tm", mFormatTime.format(measurement.timestamp));
                map.put("mesure_val", tempValue);

                new Thread(() -> {
                    String response = NetworkHandler.postRequest(CONNECT_URL, map);
                    System.out.println("response : " + response);
                }).start();
            }
        }
    };

    private final BroadcastReceiver temperatureDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral      = getPeripheral(intent.getStringExtra(Header.MEASUREMENT_EXTRA_PERIPHERAL));
            TemperatureMeasurement measurement  = (TemperatureMeasurement) intent.getSerializableExtra(Header.MEASUREMENT_TEMPERATURE_EXTRA);

            if (measurement != null) {
                notifyText.setText(String.format(Locale.KOREA, "체온 데이터 수신 : %s", peripheral.getName()));
                //measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s (%s)\n%s\n\nfrom %s", measurement.temperatureValue, measurement.unit == TemperatureUnit.Celsius ? "celsius" : "fahrenheit", measurement.type, dateFormat.format(measurement.timestamp), peripheral.getName()));
                //measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s \n\nfrom %s", measurement.temperatureValue, measurement.unit == TemperatureUnit.Celsius ? "℃" : "F", peripheral.getName()));
                measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s ", measurement.temperatureValue, measurement.unit == TemperatureUnit.Celsius ? "℃" : "F"));

                mNow = System.currentTimeMillis();
                mDate = new Date(mNow);


                editor.putString("dataTemperature", String.format(Locale.ENGLISH, "[%s] %.1f %s", mFormat.format(mDate), measurement.temperatureValue, measurement.unit == TemperatureUnit.Celsius ? "℃" : "F"));
                editor.apply();

                myTemperature = pref.getString("dataTemperature", "No Data");
                textViewTemperature.setText(String.valueOf(myTemperature));


                String tempValue  = String.format(Locale.KOREA, "%.1f", measurement.temperatureValue);
                System.out.println("tempValue : " + tempValue);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("pillboxno", DEVICE_ID);
                map.put("subjectid", SUBJECT_ID);
                map.put("mesure_tp", "5");
                map.put("mesure_de", mFormatDate.format(mDate));
                map.put("mesure_tm", mFormatTime.format(mDate));
                map.put("mesure_val", tempValue);

                new Thread(() -> {
                    //NetworkHandler.postRequest(url, map);
                    String response = NetworkHandler.postRequest(CONNECT_URL, map);
                    System.out.println("response : " + response);
                }).start();
            }
        }
    };

    private final BroadcastReceiver glucoseDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(Header.MEASUREMENT_EXTRA_PERIPHERAL));
            GlucoseMeasurement measurement = (GlucoseMeasurement) intent.getSerializableExtra(Header.MEASUREMENT_GLUCOSE_EXTRA);

            if (measurement != null)
            {
                notifyText.setText(String.format(Locale.KOREA, "혈당 데이터 수신 : %s", peripheral.getName()));
                measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s", measurement.value, measurement.unit == GlucoseMeasurementUnit.MmolPerLiter ? "mmol/L" : "mg/dL"));

                editor.putString("dataBloodSugar", String.format(Locale.ENGLISH, "[%s] %.1f %s", dateFormat.format(measurement.timestamp), measurement.value, measurement.unit == GlucoseMeasurementUnit.MmolPerLiter ? "mmol/L" : "mg/dL"));
                editor.apply();

                myBloodSugar = pref.getString("dataBloodSugar", "No Data");
                textViewBloodSugar.setText(String.valueOf(myBloodSugar));

                // Alert when It comes with the blood sugar out of normal range
                runOnUiThread(() ->
                {
                    if(measurement.isHigh() || measurement.isLow())
                    {
                        String message = measurement.isHigh() ? "혈당이 높습니다, 나중에 다시 측정해주세요." : "혈당이 낮습니다, 나중에 다시 측정해주세요.";
                        AlertDialog alert = factory.createPopup("경고", message, (dialog, which) -> dialog.cancel());
                        alert.show();
                    }
                });

                System.out.println("timestamp : " + dateFormat.format(measurement.timestamp));
                System.out.println("date : " + mFormatDate.format(measurement.timestamp));
                System.out.println("time : " + mFormatTime.format(measurement.timestamp));

                String tempValue  = String.format(Locale.ENGLISH, "%.1f", measurement.value);
                System.out.println("tempValue : " + tempValue);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("pillboxno", DEVICE_ID);
                map.put("subjectid", SUBJECT_ID);
                map.put("subjectid", SUBJECT_ID);
                map.put("mesure_tp", "3");
                map.put("mesure_de", mFormatDate.format(measurement.timestamp));
                map.put("mesure_tm", mFormatTime.format(measurement.timestamp));
                map.put("mesure_val", tempValue);

                new Thread(() -> {
                    String response = NetworkHandler.postRequest(CONNECT_URL, map);
                    System.out.println("response : " + response);
                }).start();

            }
        }
    };

    private final BroadcastReceiver bluetoothConnectRecv = new BroadcastReceiver() { //DGLEE
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(Header.MEASUREMENT_EXTRA_PERIPHERAL));


            if(peripheral != null)
            {
                notifyText.setText(String.format(Locale.KOREA, "기기 연결 : %s", peripheral.getName()));
            }
            else
            {
                System.out.println("peripheral is null end");
            }
        }
    };

    private final BroadcastReceiver bluetoothDisconnectRecv = new BroadcastReceiver() { //DGLEE
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(Header.MEASUREMENT_EXTRA_PERIPHERAL));

            System.out.println("bluetoothStatusReceiver 2");
            if(peripheral != null)
            {
                notifyText.setText(String.format(Locale.KOREA, "연결 해제 : %s", peripheral.getName()));
            }
            else
            {
                System.out.println("peripheral is null end");
            }
        }
    };

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getApplicationContext()).cm;
        return central.getPeripheral(peripheralAddress);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            System.out.println("could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices()
    {
        if (!areLocationServicesEnabled())
        {
            AlertDialog alert = factory.createPopupYN("Location services are not enabled", "Scanning for Bluetooth peripherals requires locations services to be enabled.",
                    (dialog, which) ->
                    {
                        dialog.cancel();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }, (dialog, which) -> dialog.cancel());
            alert.show();
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if all permission were granted
        boolean allGranted = true;
        for (int result : grantResults)
        {
            if (result != PackageManager.PERMISSION_GRANTED)
            {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            permissionsGranted();
        } else {
            new AlertDialog.Builder(BleActivity.this)
                    .setTitle("Permission is required for scanning Bluetooth peripherals")
                    .setMessage("Please grant permissions")
                    .setPositiveButton("Retry", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        checkPermissions();
                    })
                    .create()
                    .show();
        }
    }
}
