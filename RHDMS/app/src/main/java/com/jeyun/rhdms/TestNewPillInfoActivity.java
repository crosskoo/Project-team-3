package com.jeyun.rhdms;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityTestNewPillInfoBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.factory.TimePickerDialogFactory;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestNewPillInfoActivity extends AppCompatActivity
{
    private Pill pill;
    private ArrayAdapter<String> dataAdapter; // 복약 상태 리스트
    private ActivityTestNewPillInfoBinding binding;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityTestNewPillInfoBinding.inflate(getLayoutInflater()); // 뷰 바인딩
        setContentView(binding.getRoot());

        initUI();
    }

    private void initUI()
    {
        // 복약 상태 드롭다운 리스트 설정
        initSelector();
        // 이벤트 설정
        initEvents();
    }

    private void initSelector()
    {
        List<String> list = new ArrayList<>();
        list.add("");
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
        binding.pmNewPillSelector.setAdapter(dataAdapter);
    }

    private void initEvents() // 이벤트 설정 함수
    {
        // 날짜 뷰 클릭 -> 날짜 선택 창이 열림
        binding.pmNewPillDate.setOnClickListener(v ->
        {
            // 현재 날짜
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // 날짜 선택 창 화면
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) ->
                    {
                        binding.pmNewPillDate.setText(String.format("%s년 %s월 %s일", selectedYear, selectedMonth + 1, selectedDay));
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // 복약 시작 시간 뷰 클릭 -> 시작 시간 선택
        binding.pmNewPillStartTime.setOnClickListener(v -> {
            TimePickerDialogFactory.showTimePickerDialog(this, time -> {
                binding.pmNewPillStartTime.setText(time);
            });
        });
        // 복약 종료 시간 뷰 클릭 -> 종료 시간 선택
        binding.pmNewPillEndTime.setOnClickListener(v -> {
            TimePickerDialogFactory.showTimePickerDialog(this, time -> {
                binding.pmNewPillEndTime.setText(time);
            });
        });

        // 취소 버튼 클릭 -> 해당 창이 닫힘
        binding.pmNewPillCancel.setOnClickListener(v -> {
            finish();
        });

        // 확인 버튼 클릭 -> 복약 정보를 서버에 전송
        binding.pmNewPillOk.setOnClickListener(v -> transferNewPillInfo());

        // 복약 시각 뷰 클릭 -> 시간 선택 다이얼로그 띄움
        binding.pmNewPillTakenTime.setOnClickListener(v ->
        {
            TimePickerDialogFactory.showTimePickerDialog(this, time -> {
                binding.pmNewPillTakenTime.setText(time);
            });
        });
    }

    private void transferNewPillInfo() // 복약 정보를 서버에 전송
    {
        String takenDate = binding.pmNewPillDate.getText().toString();
        String scheduledStartTime = binding.pmNewPillStartTime.getText().toString();
        String scheduledEndTime = binding.pmNewPillEndTime.getText().toString();
        String takenTime = binding.pmNewPillTakenTime.getText().toString();
        String takenState = binding.pmNewPillSelector.getSelectedItem().toString();

        if (takenDate.isEmpty() || scheduledStartTime.isEmpty() || scheduledEndTime.isEmpty() || takenTime.isEmpty() || takenState.isEmpty())
        {
            Toast.makeText(this, "정보를 전부 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            pill = new Pill();
            pill.TAKEN_DT = takenDate.replaceAll("[년월일\\s]", "");
            pill.ARM_ST_TM = scheduledStartTime.replace(":", "");
            pill.ARM_ED_TM = scheduledEndTime.replace(":", "");
            pill.TAKEN_TM = takenTime.replace(":", "");
            pill.TAKEN_ST = takenState;

            // 확인용
            Toast.makeText(this,
                    pill.TAKEN_DT + " | " + pill.ARM_ST_TM + " | " + pill.ARM_ED_TM + " | " + pill.TAKEN_TM + " | " + pill.TAKEN_ST,
                    Toast.LENGTH_LONG).show();
        }
    }
}
