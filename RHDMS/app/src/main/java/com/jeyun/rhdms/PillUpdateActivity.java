package com.jeyun.rhdms;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityPillInfoUpdateBinding;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PillUpdateActivity extends AppCompatActivity
{
    private ActivityPillInfoUpdateBinding binding;
    private ArrayAdapter<String> dataAdapter;
    private Pill pill;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPillInfoUpdateBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initUI();
        initEvents();
    }

    private void initEvents()
    {
        binding.pmUpdateCancel.setOnClickListener(v ->
        {
            finish();
        });

        binding.pmUpdateTakenTime.setOnClickListener(v ->
        {
            String raw = pill.TAKEN_TM;
            int h = Integer.parseInt(raw.substring(0, 2));
            int m = Integer.parseInt(raw.substring(2, 4));

            TimePickerDialog dialog = new TimePickerDialog
                    (
                            this,
                            com.google.android.material.R.style.Theme_MaterialComponents_Dialog,
                            (view, hourOfDay, minute) ->
                            {
                                binding.pmUpdateTakenTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                            },
                            h, m, true
                    );
            dialog.show();
        });

        binding.pmUpdateOk.setOnClickListener(v ->
        {
            executor.execute(() ->
            {
                String updatedTM = binding.pmUpdateTakenTime.getText().toString();
                String updatedST = binding.pmUpdateSelector.getSelectedItem().toString();

                pill.TAKEN_TM = updatedTM.replace(":", "");
                pill.TAKEN_ST = updatedST;

                PillHandler ph = new PillHandler();
                if(ph.updateData(pill))
                {
                    runOnUiThread(() ->
                    {
                        Toast.makeText(this, pill.TAKEN_TM + " | " + pill.TAKEN_ST, Toast.LENGTH_SHORT)
                                .show();
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    });
                }

            });
        });
    }

    private void initUI()
    {
        Intent receivedIntent = getIntent();
        Bundle extra = receivedIntent.getExtras();
        this.pill = (Pill) extra.getSerializable(Header.PILL);

        assert pill != null;
        String year = pill.ARM_DT.substring(0, 4);
        String month = pill.ARM_DT.substring(4, 6);
        String day = pill.ARM_DT.substring(6, 8);
        binding.pmUpdateDate.setText(String.format("%s년 %s월 %s일", year, month, day));

        String formatted = pill.ARM_TP.equals("1") ? "오전" : "오후";
        binding.pmUpdateTime.setText(String.format("%s %s ~ %s",  formatted, pill.ARM_ST_TM, pill.ARM_ED_TM));

        String hour = pill.TAKEN_TM.substring(0, 2);
        String minutes = pill.TAKEN_TM.substring(2, 4);
        binding.pmUpdateTakenTime.setText(String.format("%s:%s", hour, minutes));

        initSelector();
        binding.pmUpdateSelector.setSelection(dataAdapter.getPosition(pill.TAKEN_ST));
    }

    private void initSelector()
    {
        List<String> list = new ArrayList<>();
        list.add("OUTTAKEN");
        list.add("TAKEN");
        list.add("UNTAKEN");
        list.add("OVERTAKEN");
        list.add("DELAYTAKEN");
        list.add("ERRTAKEN");
        list.add("N/A");
        list.add("N/D");

        this.dataAdapter = new ArrayAdapter<>
                (
                        getApplicationContext(),
                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                        list
                );
        binding.pmUpdateSelector.setAdapter(dataAdapter);
    }


}