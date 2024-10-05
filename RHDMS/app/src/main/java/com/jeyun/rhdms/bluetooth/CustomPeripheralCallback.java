package com.jeyun.rhdms.bluetooth;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.WriteType;

import static com.jeyun.rhdms.util.Header.*;
import static com.welie.blessed.BluetoothBytesParser.bytes2String;

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
