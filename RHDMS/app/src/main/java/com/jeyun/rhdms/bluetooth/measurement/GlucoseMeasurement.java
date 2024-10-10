package com.jeyun.rhdms.bluetooth.measurement;

import static com.welie.blessed.BluetoothBytesParser.FORMAT_SFLOAT;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_SINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;

import com.jeyun.rhdms.bluetooth.measurement.unit.GlucoseMeasurementUnit;
import com.welie.blessed.BluetoothBytesParser;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class GlucoseMeasurement implements Serializable {

    public final GlucoseMeasurementUnit unit;
    public Date timestamp;
    public int sequenceNumber;
    public boolean contextWillFollow;
    public float value;

    public GlucoseMeasurement(byte[] byteArray) {
        BluetoothBytesParser parser = new BluetoothBytesParser(byteArray);

        // Parse flags
        final int flags = parser.getIntValue(FORMAT_UINT8);
        final boolean timeOffsetPresent = (flags & 0x01) > 0;
        final boolean typeAndLocationPresent = (flags & 0x02) > 0;
        unit = (flags & 0x04) > 0 ? GlucoseMeasurementUnit.MmolPerLiter : GlucoseMeasurementUnit.MiligramPerDeciliter;
        contextWillFollow = (flags & 0x10) > 0;

        // Sequence number is used to match the reading with an optional glucose context
        sequenceNumber = parser.getIntValue(FORMAT_UINT16);

        // Read timestamp
        timestamp = parser.getDateTime();

        if (timeOffsetPresent) {
            int timeOffset = parser.getIntValue(FORMAT_SINT16);
            timestamp = new Date(timestamp.getTime() + (timeOffset * 60000));
        }

        if (typeAndLocationPresent) {
            final float glucoseConcentration = parser.getFloatValue(FORMAT_SFLOAT);
            final int multiplier = unit == GlucoseMeasurementUnit.MiligramPerDeciliter ? 100000 : 1000;
            value = glucoseConcentration * multiplier;
        }
    }

    public boolean isHigh()
    {
        return value >= 120;
    }

    public boolean isLow()
    {
        return value <= 100;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"%.1f %s, at (%s)", value, unit == GlucoseMeasurementUnit.MmolPerLiter ? "mmol/L" : "mg/dL", timestamp);
    }
}