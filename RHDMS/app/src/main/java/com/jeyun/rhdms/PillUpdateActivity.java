package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.jeyun.rhdms.databinding.ActivityPillInfoUpdateBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.List;

public class PillUpdateActivity extends AppCompatActivity {

    private ActivityPillInfoUpdateBinding binding;
    private ArrayAdapter<String> dataAdapter;
    private Pill pill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityPillInfoUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
    }

    private void initUI() {
        Intent receivedIntent = getIntent();
        Bundle extra = receivedIntent.getExtras();
        
        this.pill = (Pill) extra.getSerializable(Header.PILL);
        
        assert pill != null;

        String year = pill.ARM_DT.substring(0, 4);
        String month = pill.ARM_DT.substring(4, 6);
        String day = pill.ARM_DT.substring(6, 8);

        binding.pmUpdateDate.setText(String.format("%s년 %s월 %s일", year, month, day));

        String formatted = pill.ARM_TP.equals("1") ? "오전" : "오후";
        
        binding.pmUpdateTime.setText(String.format("%s %s ~ %s", formatted, pill.ARM_ST_TM, pill.ARM_ED_TM));

        String hour = pill.TAKEN_TM.substring(0, 2);
        String minutes = pill.TAKEN_TM.substring(2, 4);

        binding.pmUpdateTakenTime.setText(String.format("%s:%s", hour, minutes));

        initSelector();
        
        binding.pmUpdateSelector.setSelection(dataAdapter.getPosition(pill.TAKEN_ST));
    }

    private void initSelector() {
        List<String> list = new ArrayList<>();
        list.add("OUTTAKEN");
        list.add("TAKEN");
        list.add("UNTAKEN");
        list.add("OVERTAKEN");
        list.add("DELAYTAKEN");
        list.add("ERRTAKEN");
        list.add("N/A");
        list.add("N/D");

        this.dataAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                list
        );

        binding.pmUpdateSelector.setAdapter(dataAdapter);
    }
}