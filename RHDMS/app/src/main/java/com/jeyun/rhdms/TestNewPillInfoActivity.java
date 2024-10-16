package com.jeyun.rhdms;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityTestNewPillInfoBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        initEvents();
    }

    private void initUI()
    {
        // 복약 날짜는 기본값으로 오늘 날짜로 세팅 (일단 수정 못함)
        String currentData = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String year = currentData.substring(0, 4);
        String month = currentData.substring(4, 6);
        String day = currentData.substring(6, 8);
        binding.pmNewPillDate.setText(String.format("%s년 %s월 %s일", year, month, day));

        // 복약 상태 드롭다운 리스트 설정
        initSelector();
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
        // 취소 버튼 클릭 -> 해당 창이 닫힘
        binding.pmNewPillCancel.setOnClickListener(v ->
        {
            finish();
        });

        // 확인 버튼 클릭 -> 복약 정보를 서버에 전송
        binding.pmNewPillOk.setOnClickListener(v ->
        {
            String takenTime = binding.pmNewPillTakenTime.getText().toString();
            String takenState = binding.pmNewPillSelector.getSelectedItem().toString();
            String takenDate = binding.pmNewPillDate.getText().toString();

            // 복약 시각과 복약 상태 둘 다 입력 해야 새로운 복약 정보를 DB에 추가할 수 있음.
            if (takenTime.isEmpty() || takenState.isEmpty())
            {
                Toast.makeText(this, "복용한 시간과 복약 상태를 전부 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                pill = new Pill();
                pill.TAKEN_TM = takenTime.replace(":", "");
                pill.TAKEN_DT = takenDate.replaceAll("[년월일\\s]", "");
                pill.TAKEN_ST = takenState;

                // 단순 확인용
                Toast.makeText(this, pill.TAKEN_DT + " | " + pill.TAKEN_TM + " | " + pill.TAKEN_ST, Toast.LENGTH_SHORT).show();
            }
        });

        // 복약 시각 뷰 클릭 -> 시간 선택 다이얼로그 띄움
        binding.pmNewPillTakenTime.setOnClickListener(v ->
        {
            TimePickerDialog dialog = new TimePickerDialog
                    (
                            this,
                            com.google.android.material.R.style.Theme_MaterialComponents_Dialog,
                            (view, hourOfDay, minute) ->
                            {
                                binding.pmNewPillTakenTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                            },
                            0, 0, true
                    );
            dialog.show();
        });
    }
}
