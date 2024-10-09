package com.jeyun.rhdms.bluetooth;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.jeyun.rhdms.bluetooth.measurement.BloodPressureMeasurement;
import com.jeyun.rhdms.bluetooth.measurement.GlucoseMeasurement;
import com.jeyun.rhdms.bluetooth.measurement.TemperatureMeasurement;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.WriteType;

import static com.jeyun.rhdms.util.Header.*;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;
import static com.welie.blessed.BluetoothBytesParser.bytes2String;

import static java.lang.Math.abs;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class CustomPeripheralCallback extends BluetoothPeripheralCallback
{
    private Context context;
    private Handler handler;
    private int currentTimeCounter;

    private static CustomPeripheralCallback instance;

    private CustomPeripheralCallback(Context context, Handler handler)
    {
        this.context = context;
        this.handler = handler;
        this.currentTimeCounter = 0;
    }

    public static synchronized CustomPeripheralCallback getInstance()
    {
        return instance;
    }

    public static synchronized CustomPeripheralCallback getInstance(Context context, Handler handler)
    {
        if(Objects.isNull(instance)) return (instance = new CustomPeripheralCallback(context, handler));
        else return instance;
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral)
    {
        peripheral.requestMtu(185);
        peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
        peripheral.readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
        peripheral.readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);
        peripheral.readPhy();

        BluetoothGattCharacteristic currentTimeCharacteristic = peripheral.getCharacteristic(CURRENT_TIME_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID);
        if(Objects.nonNull(currentTimeCharacteristic))
        {
            peripheral.setNotify(currentTimeCharacteristic, true);
            if ((currentTimeCharacteristic.getProperties() & PROPERTY_WRITE) > 0)
            {
                if (!isOmronBPM(peripheral.getName()))
                {
                    BluetoothBytesParser parser = new BluetoothBytesParser();
                    parser.setCurrentTime(Calendar.getInstance());
                    peripheral.writeCharacteristic(currentTimeCharacteristic, parser.getValue(), WriteType.WITH_RESPONSE);
                }
            }
        }

        // Try to turn on notifications for other characteristics
        peripheral.readCharacteristic(BATTERY_LEVEL_SERVICE_UUID,   BATTERY_LEVEL_CHARACTERISTIC_UUID);
        peripheral.setNotify(BLOOD_PRESSURE_SERVICE_UUID,           BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
        peripheral.setNotify(HEALTH_THERMOMETER_SERVICE_UUID,       TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
        peripheral.setNotify(GLUCOSE_SERVICE_UUID,                  GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID, true);
        peripheral.setNotify(GLUCOSE_SERVICE_UUID,                  GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, true);
        peripheral.setNotify(GLUCOSE_SERVICE_UUID,                  GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, true);
    }

    @Override
    public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status)
    {
        UUID uuid = characteristic.getUuid();
        final boolean isNotifying = peripheral.isNotifying(characteristic);
        System.out.println("Status : " + status);
        System.out.println("isNotifying : " + isNotifying);
        System.out.println("UUID : " + uuid);
        if(uuid.equals(GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID)) writeGetAllGlucoseMeasurements(peripheral);
    }

    @Override
    public void onCharacteristicWrite(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status)
    {
        UUID uuid = characteristic.getUuid();
        System.out.println("UUID : " + uuid);
        System.out.println("Status : " + status);
        System.out.println("From " + bytes2String(value) + " To " + uuid);
    }

    @Override
    public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull GattStatus status)
    {
        if(status != GattStatus.SUCCESS) return;

        UUID uuid = characteristic.getUuid();
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        if (uuid.equals(BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID))
        {
            BloodPressureMeasurement measurement = new BloodPressureMeasurement(value);
            Intent intent = new Intent(MEASUREMENT_BLOODPRESSURE);
            intent.putExtra(MEASUREMENT_BLOODPRESSURE_EXTRA, measurement);
            sendMeasurement(intent, peripheral);
        }

        else if (uuid.equals(TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID))
        {
            TemperatureMeasurement measurement = new TemperatureMeasurement(value);
            Intent intent = new Intent(MEASUREMENT_TEMPERATURE);
            intent.putExtra(MEASUREMENT_TEMPERATURE_EXTRA, measurement);
            sendMeasurement(intent, peripheral);
        }

        else if (uuid.equals((GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID)))
        {
            GlucoseMeasurement measurement = new GlucoseMeasurement(value);
            Intent intent = new Intent(MEASUREMENT_GLUCOSE);
            intent.putExtra(MEASUREMENT_GLUCOSE_EXTRA, measurement);
            sendMeasurement(intent, peripheral);
            System.out.println("%s" + measurement);
        }

        else if (uuid.equals(CURRENT_TIME_CHARACTERISTIC_UUID))
        {
            System.out.println("DGLEE time uuid check");
            Date currentTime = parser.getDateTime();
            // ### added some line to show the received Current Time from server


            System.out.println("Received device time: %s" + currentTime);

            // Deal with Omron devices where we can only write currentTime under specific conditions
            if (isOmronBPM(peripheral.getName()))
            {
                BluetoothGattCharacteristic bloodpressureMeasurement = peripheral.getCharacteristic(BLOOD_PRESSURE_SERVICE_UUID, BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID);
                if (bloodpressureMeasurement == null) return;

                boolean isNotifying = peripheral.isNotifying(bloodpressureMeasurement);
                if (isNotifying) currentTimeCounter++;

                // We can set device time for Omron devices only if it is the first notification and currentTime is more than 10 min from now
                long interval = abs(Calendar.getInstance().getTimeInMillis() - currentTime.getTime());
                if (currentTimeCounter == 1 && interval > 10 * 60 * 1000)
                {
                    parser.setCurrentTime(Calendar.getInstance());
                    peripheral.writeCharacteristic(characteristic, parser.getValue(), WriteType.WITH_RESPONSE);
                }
            }
        }

        else if (uuid.equals(BATTERY_LEVEL_CHARACTERISTIC_UUID))
        {
            int batteryLevel = parser.getIntValue(FORMAT_UINT8);
            System.out.println("Received battery level %d%%" + batteryLevel);
        }

        else if (uuid.equals(MANUFACTURER_NAME_CHARACTERISTIC_UUID))
        {
            String manufacturer = parser.getStringValue(0);
            System.out.println("Received manufacturer: %s" + manufacturer);
        }

        else if (uuid.equals(MODEL_NUMBER_CHARACTERISTIC_UUID))
        {
            String modelNumber = parser.getStringValue(0);
            System.out.println("Received modelnumber: %s" + modelNumber);
        }

        else if (uuid.equals(PNP_ID_CHARACTERISTIC_UUID))
        {
            String modelNumber = parser.getStringValue(0);
            System.out.println("Received pnp: %s" + modelNumber);
        }
    }

    private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral )
    {
        intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
        context.sendBroadcast(intent);
    }

    private void writeGetAllGlucoseMeasurements(@NotNull BluetoothPeripheral peripheral)
    {
        byte OP_CODE_REPORT_STORED_RECORDS = 1;
        byte OPERATOR_ALL_RECORDS = 1;
        final byte[] command = new byte[] {OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS};
        peripheral.writeCharacteristic(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, command, WriteType.WITH_RESPONSE);
    }

    private boolean isOmronBPM(final String name)
    {
        return name.contains("BLESmart_") || name.contains("BLEsmart_");
    }
}
