package com.jeyun.rhdms;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

                String selectedString = binding.pmUpdateSelector.getSelectedItem().toString();
                String updatedST = setTakenState(selectedString);

                pill.TAKEN_TM = updatedTM.replace(":", "");
                pill.TAKEN_ST = updatedST;

                PillHandler ph = new PillHandler();
                if(ph.updateData(pill))
                {
                    runOnUiThread(() ->
                    {
                        Log.d("update",pill.TAKEN_TM + " | " + pill.TAKEN_ST); // 테스트용
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

        String hour = pill.TAKEN_TM.substring(0, 2);
        String minutes = pill.TAKEN_TM.substring(2, 4);
        binding.pmUpdateTakenTime.setText(String.format("%s:%s", hour, minutes));

        initSelector();
        String setText = changeTakenStateIntoString(pill.TAKEN_ST);
        // Log.d("test", "setText: " + setText); // 테스트용"
        binding.pmUpdateSelector.setSelection(dataAdapter.getPosition(setText));
    }

    private String setTakenState(String selectedString)
    {
        String takenState = "";

        switch (selectedString)
        {
            case "복용":
                takenState = "TAKEN";
                break;
            case "외출 복용":
                takenState = "OUTTAKEN";
                break;
            case "미복용":
                takenState = "UNTAKEN";
                break;
            case "지연 복용":
                takenState = "DELAYTAKEN";
                break;
            default:
                break;
        }
        return takenState;
    }

    private String changeTakenStateIntoString(String takenState)
    {
        String setText = "";

        switch (takenState)
        {
            case "TAKEN":
                setText = "복용";
                break;
            case "OUTTAKEN":
                setText = "외출 복용";
                break;
            case "UNTAKEN":
                setText = "미복용";
                break;
            case "DELAYTAKEN":
                setText = "지연 복용";
                break;
            case "ERRTAKEN":
                setText = "오복용";
                break;
            case "OVERTAKEN":
                setText = "오복용"; // 과복용도 오복용에 포함
                break;
            default:
                setText = "복약 상태 Error"; // 예외 처리
        }
        return setText;
    }

    private void initSelector()
    {
        List<String> list = new ArrayList<>();
        list.add("복용");
        list.add("외출 복용");
        list.add("미복용");
        list.add("지연 복용");
        list.add("오복용");

        this.dataAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                        list
        ) {
            @Override
            public boolean isEnabled(int position) {

                return !getItem(position).equals("오복용"); // "오복용" 항목은 사용자가 선택 불가
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                if (getItem(position).equals("오복용")) // "오복용" 항목 색상을 회색으로 설정
                {
                    textView.setTextColor(Color.LTGRAY);
                }
                return view;
            }
        };

        binding.pmUpdateSelector.setAdapter(dataAdapter);
    }


}