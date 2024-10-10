package com.jeyun.rhdms.bluetooth.measurement;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BloodPressureMeasurement implements Serializable {

    public Integer userID;
    public Float systolic;
    public Float diastolic;
    public Float meanArterialPressure;
    public Date timestamp;
    public boolean isMMHG;
    public Float pulseRate;

    public BloodPressureMeasurement(byte[] value) {
        BluetoothBytesParser parser = new BluetoothBytesParser(value);

        // Parse the flags
        int flags = parser.getIntValue(FORMAT_UINT8);
        isMMHG = !((flags & 0x01) > 0);
        boolean timestampPresent = (flags & 0x02) > 0;
        boolean pulseRatePresent = (flags & 0x04) > 0;
        boolean userIdPresent = (flags & 0x08) > 0;
        boolean measurementStatusPresent = (flags & 0x10) > 0;

        // Get systolic, diastolic and mean arterial pressure
        systolic = parser.getFloatValue(FORMAT_SFLOAT);
        diastolic = parser.getFloatValue(FORMAT_SFLOAT);
        meanArterialPressure = parser.getFloatValue(FORMAT_SFLOAT);

        // Read timestamp
        if (timestampPresent) {
            timestamp = parser.getDateTime();
        } else {
            timestamp = Calendar.getInstance().getTime();
        }

        // Read pulse rate
        if (pulseRatePresent) {
            pulseRate = parser.getFloatValue(FORMAT_SFLOAT);
        }

        // Read userId
        if (userIdPresent) {
            userID = parser.getIntValue(FORMAT_UINT8);
        }
    }

    public boolean isHigh()
    {
        return systolic >= 130 && diastolic >= 80;
    }

    public boolean isLow()
    {
        return systolic <= 90 && diastolic <= 60;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%.0f/%.0f %s, MAP %.0f, %.0f bpm, user %d at (%s)", systolic, diastolic, isMMHG ? "mmHg" : "kPa", meanArterialPressure, pulseRate, userID, timestamp);
    }
}
