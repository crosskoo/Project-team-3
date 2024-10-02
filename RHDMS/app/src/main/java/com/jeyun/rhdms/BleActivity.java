package com.jeyun.rhdms;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jeyun.rhdms.databinding.ActivityBleBinding;

/* This Activity is imported Code, given by Jeyun */
public class BleActivity extends AppCompatActivity {

    private ActivityBleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.binding = ActivityBleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
