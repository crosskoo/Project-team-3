package com.jeyun.rhdms.bluetooth;

import static com.jeyun.rhdms.util.Header.BLOOD_PRESSURE_SERVICE_UUID;
import static com.jeyun.rhdms.util.Header.GLUCOSE_SERVICE_UUID;
import static com.jeyun.rhdms.util.Header.HEALTH_THERMOMETER_SERVICE_UUID;

import android.content.Context;
import android.os.Handler;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheralCallback;

import java.util.Objects;
import java.util.UUID;

public class BluetoothHandler
{
    public Handler handler;
    public BluetoothCentralManager cm;
    private static BluetoothHandler instance;

    private BluetoothHandler(Context context)
    {
        this.handler = new Handler();

        BluetoothPeripheralCallback pp_callback = CustomPeripheralCallback.getInstance(context, handler);
        BluetoothCentralManagerCallback cm_callback = CustomCentralManagerCallback.getInstance(context, pp_callback, handler);
        cm = new BluetoothCentralManager(context, cm_callback, handler);
        CustomCentralManagerCallback.getInstance().setCentral(this.cm);

        cm.startPairingPopupHack();
        scan();
    }

    public static BluetoothHandler getInstance(Context context)
    {
        if(Objects.isNull(instance)) return (instance = new BluetoothHandler(context));
        return instance;
    }

    public static BluetoothHandler getInstance()
    {
        return instance;
    }

    public void scan()
    {
        handler.postDelayed(() ->
        {
            UUID[] uuids = new UUID[] { BLOOD_PRESSURE_SERVICE_UUID, HEALTH_THERMOMETER_SERVICE_UUID, GLUCOSE_SERVICE_UUID };
            cm.scanForPeripheralsWithServices(uuids);
        }, 1000L);
    }
}
