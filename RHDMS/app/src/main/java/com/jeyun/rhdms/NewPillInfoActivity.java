package com.jeyun.rhdms;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.jeyun.rhdms.bluetooth.NetworkHandler;
import com.jeyun.rhdms.databinding.ActivityNewPillInfoBinding;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.User;
import com.jeyun.rhdms.util.MyCalendar;
import com.jeyun.rhdms.util.factory.TimePickerDialogFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewPillInfoActivity extends AppCompatActivity
{
    private HashMap<String, Object> newPillInfoMap;
    private ArrayAdapter<String> dataAdapter; // 복약 상태 리스트
    private ActivityNewPillInfoBinding binding;
    private NetworkHandler networkHandler;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private HashMap<String, Object> newPillDataString;
    // private Optional<Pill> lastPillData;
    private PillHandler pillHandler = new PillHandler();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityNewPillInfoBinding.inflate(getLayoutInflater()); // 뷰 바인딩
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), PillInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });

        initUI();
    }


    private void initUI()
    {
        // 복약 상태 드롭다운 리스트 설정
        initSelector();

        // 현재 시각
        LocalDateTime curDate = LocalDateTime.now();

        // 현재 날짜를 newPillDate EditText에 설정
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        String currentDate = curDate.format(dateFormat);
        // Log.d("ui","date : " + currentDate); // 테스트용
        binding.pmNewPillDate.setText(currentDate);

        // 현재 시각을 newPillStartTime EditText에 설정
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        String currentTime = curDate.format(timeFormat);
        // Log.d("ui","time : " + currentTime); // 테스트용
        binding.pmNewPillTakenTime.setText(currentTime);

        // Log.d("arm", User.getInstance().getARM_ST_TM().toString());
        // Log.d("arm", User.getInstance().getARM_ED_TM().toString());
        // Log.d("arm", curDate.toString());

        String state = "";
        // 복약 스케줄과 현재 시각에 맞춰서 설정
        // 현재 시각이 복약 스케줄 안에 있으면 복약 상태를 복용으로 기본 설정
        // 현재 시각이 복약 스케줄 밖에 있으면 지연 복용으로 기본 설정
        if (curDate.isAfter(User.getInstance().getARM_ST_TM()) && curDate.isBefore(User.getInstance().getARM_ED_TM()))
        {
            // Log.d("arm", "현재 시각이 복용 스케줄 안에 있습니다");
            state = "복용";
        }
        else
        {
            // Log.d("arm","현재 시각이 복용 스케줄 바깥에 있습니다");
            state = "지연 복용";
        }
        binding.pmNewPillSelector.setSelection(dataAdapter.getPosition(state));

        // 이벤트 설정
        initEvents();
    }

    private void initSelector()
    {
        List<String> list = new ArrayList<>();
        list.add("(복약 상태를 선택하세요)");
        list.add("복용");
        list.add("외출 복용");
        list.add("지연 복용");

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
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis()); // 현재 날짜 이후의 데이터 선택 불가
        });

        // 취소 버튼 클릭 -> 해당 창이 닫힘
        binding.pmNewPillCancel.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        });

        // 확인 버튼 클릭 -> 복약 정보를 서버에 전송
        binding.pmNewPillOk.setOnClickListener(v -> {
            transferNewPillInfo();
            // setNewData(lastPillData);
            // finish();
        });

        // 복약 시각 뷰 클릭 -> 시간 선택 다이얼로그 띄움
        binding.pmNewPillTakenTime.setOnClickListener(v ->
        {
            TimePickerDialogFactory.showTimePickerDialog(this, time -> {
                binding.pmNewPillTakenTime.setText(time);
            });
        });
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
            case "지연 복용":
                takenState = "DELAYTAKEN";
                break;
            default:

                break;
        }
        return takenState;
    }

    private void transferNewPillInfo() // 복약 정보를 서버에 전송
    {
        newPillInfoMap = new HashMap<String, Object>();
        newPillDataString = new HashMap<String, Object>();

        String takenDate = binding.pmNewPillDate.getText().toString(); // 복약 날짜 설정

        LocalDateTime arm_st_tm = User.getInstance().getARM_ST_TM(); // 알람 시작 시각
        LocalDateTime arm_ed_tm = User.getInstance().getARM_ED_TM(); // 알람 종료 시각
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String scheduledStartTime = arm_st_tm.format(formatter);
        String scheduledEndTime = arm_ed_tm.format(formatter);

        String takenTime = binding.pmNewPillTakenTime.getText().toString();

        String selectedState = binding.pmNewPillSelector.getSelectedItem().toString();
        String takenState = setTakenState(selectedState);

        if (takenDate.isEmpty() || scheduledStartTime.isEmpty() || scheduledEndTime.isEmpty() ||takenTime.isEmpty() || takenState.isEmpty())
        {
            Toast.makeText(this, "정보를 전부 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String year, month, day;
            String[] dateParts = takenDate.replaceAll("[^0-9]", " ").trim().split("\\s+");

            year = dateParts[0];

            if (dateParts[1].length() == 1)
                month = "0" + dateParts[1];
            else
                month = dateParts[1];

            if (dateParts[2].length() == 1)
                day = "0" + dateParts[2];
            else
                day = dateParts[2];

            takenDate = year + month + day;
            Log.d("check date", "year : " + year + ", month : " + month + ", day : " + day);

            Boolean isExist = existDuplicatedPillData(takenDate);

            if (isExist) // 이미 존재하는 복약 데이터면 덮어쓸건지 물어 본다.
            {
                new AlertDialog.Builder(this)
                        .setTitle("복약 데이터 덮어쓰기")
                        .setMessage("이미 존재하는 복약 데이터가 있습니다. 덮어쓰시겠습니까?")
                        .setNegativeButton("아니요", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton("예", (dialog, which) -> {
                            // 데이터 덮어쓰기
                            executor.execute(() -> {
                                if (pillHandler.updateData(takenState, takenTime.replace(":", ""), year + month + day))
                                {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "성공적으로 입력되었습니다.", Toast.LENGTH_LONG).show();
                                        Intent returnIntent = new Intent();
                                        setResult(RESULT_OK, returnIntent);
                                        finish();
                                    });
                                }
                            });
                        })
                        .setCancelable(false)
                        .show();
            }
            else // 없는 복약 데이터면
            {
                newPillInfoMap.put("alarmDate", takenDate);
                newPillInfoMap.put("closeDate",takenDate);
                newPillInfoMap.put("state", takenState);
                newPillInfoMap.put("alarmStartTime", scheduledStartTime.replace(":", ""));
                newPillInfoMap.put("alarmEndTime", scheduledEndTime.replace(":", ""));

                /*
                newPillDataString.put("NewAlarmDate", takenDate);
                newPillDataString.put("NewCloseDate",takenDate);
                newPillDataString.put("NewState", takenState);
                newPillDataString.put("NewAlarmStartTime", scheduledStartTime.replace(":", ""));
                newPillDataString.put("NewAlarmEndTime", scheduledEndTime.replace(":", ""));
                newPillDataString.put("NewTakenTime", takenTime.replace(":", ""));
                 */

                HttpPostRequest();
                Toast.makeText(this, "성공적으로 입력되었습니다.",Toast.LENGTH_LONG).show();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    private void HttpPostRequest() { // 새롭게 생성된 복약 데이터를 json 형태로 전송
        setJsonData();

        executor.execute(() ->
        {
            try
            {
                URL url = new URL("http://211.229.106.53:8080/restful/pillbox-colct.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String json = NetworkHandler.getJsonStringFromMap(newPillInfoMap);
                System.out.println("json:" + json);

                try (OutputStream os = conn.getOutputStream())
                {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                System.out.println("ResponseCode:" + responseCode); // 응답 코드 확인

                conn.disconnect();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

    }

    private void setJsonData()
    {
        String defaultDeviceId = "10071002"; // 임의의 값
        String defaultOpenTime = "1145"; // 임의의 값
        String defaultCloseTime = "1150"; // 임의의 값
        String defaultBeforeWeight = "151"; // 임의의 값
        String defaultNowWeight = "80"; // 임의의 값
        String defaultPaper = "7"; // 임의의 값
        String defaultSubjectId = User.getInstance().getOrgnztId();

        newPillInfoMap.put("deviceId", defaultDeviceId);
        newPillInfoMap.put("openTime", defaultOpenTime);
        newPillInfoMap.put("closeTime", defaultCloseTime);
        newPillInfoMap.put("beforeWeight", defaultBeforeWeight);
        newPillInfoMap.put("nowWeight", defaultNowWeight);
        newPillInfoMap.put("subject_id", defaultSubjectId);
        newPillInfoMap.put("paper", defaultPaper);
    }

    private Boolean existDuplicatedPillData(String takenDate) // 중복된 복약 데이터가 있는지 확인하는 함수
    {
        Callable<Optional<Pill>> task = () -> {
            return pillHandler.getData(takenDate);
        };

        try
        {
            return executor.submit(task).get().isPresent();
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    /*
    private void setNewData(Optional<Pill> lastPillData)
    {
        if (!lastPillData.isPresent())
            return;

        System.out.println(lastPillData);
        Integer NewDragWeight = Integer.parseInt(lastPillData.get().DRG_WEIGHT);
        Integer NewDragBefore = Integer.parseInt(lastPillData.get().DRG_AFTER);
        Integer NewDragAfter = NewDragBefore - NewDragWeight;

        newPillDataString.put("NewSubjectId", User.getInstance().getOrgnztId());
        newPillDataString.put("NewARM_TP", lastPillData.get().ARM_TP);
        newPillDataString.put("NewAPPLC_YN", lastPillData.get().APPLC_YN);
        newPillDataString.put("NewDRG_WEIGHT", NewDragWeight.toString());
        newPillDataString.put("NewDRG_BEFORE", NewDragBefore.toString());
        newPillDataString.put("NewDRG_AFTER", NewDragAfter.toString());
        newPillDataString.put("NewPAPER", lastPillData.get().PAPER);
        newPillDataString.put("NewLogId", lastPillData.get().LOG_ID + 3);
        newPillDataString.put("NewReg_DT", LocalDateTime.now().toString());
        newPillDataString.put("NewUSE_YN", lastPillData.get().USE_YN);

        Iterator<Map.Entry<String, Object>> iterator = newPillDataString.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
    */
}
