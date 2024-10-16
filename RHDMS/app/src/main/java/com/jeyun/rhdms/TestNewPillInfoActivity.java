package com.jeyun.rhdms;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityTestNewPillInfoBinding;

public class TestNewPillInfoActivity extends AppCompatActivity {

    private ActivityTestNewPillInfoBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestNewPillInfoBinding.inflate(getLayoutInflater()); // 뷰 바인딩
        setContentView(binding.getRoot());
    }
}
