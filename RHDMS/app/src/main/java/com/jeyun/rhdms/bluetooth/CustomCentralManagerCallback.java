package com.jeyun.rhdms.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.HciStatus;
import com.welie.blessed.ScanFailure;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import static com.jeyun.rhdms.util.Header.*;


public class CustomCentralManagerCallback extends BluetoothCentralManagerCallback
{
    private Context context;
    private Handler handler;
    private BluetoothCentralManager central;
    private BluetoothPeripheralCallback callback;
    private static CustomCentralManagerCallback instance;

    private CustomCentralManagerCallback(Context context, BluetoothPeripheralCallback callback, Handler handler)
    {
        this.context = context;
        this.handler = handler;
        this.callback = callback;
    }

    public void setCentral(BluetoothCentralManager cm)
    {
        this.central = cm;
    }

    public static synchronized CustomCentralManagerCallback getInstance()
    {
        return instance;
    }

    public static synchronized CustomCentralManagerCallback getInstance(Context context, BluetoothPeripheralCallback callback, Handler handler)
    {
        if(Objects.isNull(instance)) return (instance = new CustomCentralManagerCallback(context, callback, handler));
        else return instance;
    }

    @Override
    public void onConnectedPeripheral(@NonNull BluetoothPeripheral peripheral)
    {
        String equipName = peripheral.getName();
        String equipAddr = peripheral.getAddress();

        System.out.println("Connected to " + equipName);
        System.out.println("Connected Address " + equipAddr);

        Intent intent = new Intent(INTENT_BT_CONNECTED);
        intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, equipAddr);
        context.sendBroadcast(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull HciStatus status)
    {
        String name = peripheral.getName();
        System.out.println("Connection Failed : " + name + " / " + status);
    }

    @Override
    public void onDisconnectedPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull HciStatus status)
    {
        String equipName = peripheral.getName();
        String equipAddr = peripheral.getAddress();

        System.out.println("Disconnected to " + equipName);
        System.out.println("Disconnected Address " + equipAddr);
        System.out.println("Equipment Status : " + status);

        Intent intent = new Intent(INTENT_BT_DISCONNECTED);
        intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, equipAddr);
        context.sendBroadcast(intent);

        BluetoothPeripheral connectedPeripheral = central.getPeripheral(peripheral.getAddress());
        central.cancelConnection(connectedPeripheral);
        try { central.close(); } catch (IllegalArgumentException e) { e.printStackTrace(); scan(); }

        // Reconnect to this device when it becomes available again
        BluetoothPeripheralCallback peripheralCallback = CustomPeripheralCallback.getInstance(context, handler);
        handler.postDelayed(() -> central.autoConnectPeripheral(peripheral, peripheralCallback), 5000);
    }

    @Override
    public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult)
    {
        String name = peripheral.getName();
        System.out.println("Found peripheral '" + name + "'");
        central.stopScan();

        BondState state = peripheral.getBondState();

        if (name.contains("Contour") && state == BondState.NONE) central.createBond(peripheral, callback);
        else central.connectPeripheral(peripheral, callback);

    }

    @Override
    public void onBluetoothAdapterStateChanged(int state)
    {
        System.out.println("bluetooth adapter changed state to " + state);
        if (state == BluetoothAdapter.STATE_ON)
        {
            central.startPairingPopupHack();
            scan();
        }

        // DGLEE TEST
        if (state == BluetoothAdapter.STATE_ON) System.out.println("onBluetoothAdapterStateChanged ----- STATE_ON");
        else if (state == BluetoothAdapter.STATE_OFF) System.out.println("onBluetoothAdapterStateChanged ----- STATE_OFF");
        else if (state == BluetoothAdapter.STATE_CONNECTED) System.out.println("onBluetoothAdapterStateChanged ----- STATE_CONNECTED");
        else if (state == BluetoothAdapter.STATE_CONNECTING) System.out.println("onBluetoothAdapterStateChanged ----- STATE_CONNECTING");
        else if (state == BluetoothAdapter.STATE_DISCONNECTED) System.out.println("onBluetoothAdapterStateChanged ----- STATE_DISCONNECTED");
        else System.out.println("onBluetoothAdapterStateChanged ----- %d" + state);

    }

    @Override
    public void onScanFailed(@NotNull ScanFailure scanFailure)
    {
        System.out.println("scanning failed with error " + scanFailure);
    }

    private void scan()
    {
        handler.postDelayed(() ->
        {
            UUID[] uuids = new UUID[] { BLOOD_PRESSURE_SERVICE_UUID, HEALTH_THERMOMETER_SERVICE_UUID, GLUCOSE_SERVICE_UUID };
            central.scanForPeripheralsWithServices(uuids);
        }, 1000L);
    }
}
