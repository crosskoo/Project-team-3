package com.jeyun.rhdms;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityPillListBinding;


public class PillListActivity extends AppCompatActivity
{
    private ActivityPillListBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPillListBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);
    }
}