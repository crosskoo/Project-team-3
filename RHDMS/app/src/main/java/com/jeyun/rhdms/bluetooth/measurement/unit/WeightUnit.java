package com.jeyun.rhdms.bluetooth.measurement.unit;

import androidx.annotation.NonNull;

public enum WeightUnit {
    Unknown {
        @NonNull
        @Override
        public String toString() {
            return "unknown";
        }
    },
    Kilograms {
        @NonNull
        @Override
        public String toString() {
            return "Kg";
        }
    },
    Pounds {
        @NonNull
        @Override
        public String toString() {
            return "lbs";
        }
    },
    Stones {
        @NonNull
        @Override
        public String toString() {
            return "st";
        }
    };
}
