package com.jeyun.rhdms.util;

import java.util.UUID;

public interface Header
{
    // Intent key for wrapper
    String PILL = "pill";
    String WRAPPER_DATASET = "dataset";

    // Bluetooth ID
    String INTENT_KEY_ADDRESS = "jeyun.bt_address";
    String INTENT_BT_CONNECTED = "jeyun.bt_connected";
    String INTENT_BT_DISCONNECTED = "jeyun.bt_disconnected";

    String MEASUREMENT_BLOODPRESSURE                = "jeyun.measurement.bloodpressure";
    String MEASUREMENT_BLOODPRESSURE_EXTRA          = "jeyun.measurement.bloodpressure.extra";

    String MEASUREMENT_TEMPERATURE                  = "jeyun.measurement.temperature";
    String MEASUREMENT_TEMPERATURE_EXTRA            = "jeyun.measurement.temperature.extra";

    String MEASUREMENT_GLUCOSE                      = "jeyun.measurement.glucose";
    String MEASUREMENT_GLUCOSE_EXTRA                = "jeyun.measurement.glucose.extra";
    String MEASUREMENT_EXTRA_PERIPHERAL             = "jeyun.measurement.peripheral";


    UUID BLOOD_PRESSURE_SERVICE_UUID                       = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
    UUID BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID    = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Health Thermometer service (HTS)
    UUID HEALTH_THERMOMETER_SERVICE_UUID                   = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
    UUID TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID       = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
    UUID PNP_ID_CHARACTERISTIC_UUID                        = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb"); //DGLEE TEST

    // UUIDs for the Device Information service (DIS)
    UUID DEVICE_INFORMATION_SERVICE_UUID                   = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    UUID MANUFACTURER_NAME_CHARACTERISTIC_UUID             = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    UUID MODEL_NUMBER_CHARACTERISTIC_UUID                  = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Current Time service (CTS)
    UUID CURRENT_TIME_SERVICE_UUID                         = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    UUID CURRENT_TIME_CHARACTERISTIC_UUID                  = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Battery Service (BAS)
    UUID BATTERY_LEVEL_SERVICE_UUID                        = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    UUID BATTERY_LEVEL_CHARACTERISTIC_UUID                 = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
    UUID GLUCOSE_SERVICE_UUID                               = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
    UUID GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID            = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
    UUID GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID    = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");
    UUID GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID    = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
}
