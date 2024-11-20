package com.jeyun.rhdms.fragment;

import static com.jeyun.rhdms.adapter.pill.PillInfoAdapter.getIntentSwitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.jeyun.rhdms.PillInfoActivity;
import com.jeyun.rhdms.R;
import com.jeyun.rhdms.adapter.wrapper.PillInfo;
import com.jeyun.rhdms.databinding.FragmentWeekPillChartBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.wrapper.PillBox;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 7일 복약 기록 차트
public class WeekPillChartFragment extends Fragment {
    private View v;
    private ArrayList<Pill> dataset; // 복약 정보를 저장할 리스트
    Pill[] pills = new Pill[7]; // 출력할 데이터
    private CustomCalendar calendar; // 출력할 복약 기간 시작날짜
    private FragmentWeekPillChartBinding binding;
    private ConstraintLayout constraintLayout;
    private View line;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentWeekPillChartBinding.inflate(inflater);
        v = binding.getRoot();
        constraintLayout = v.findViewById(R.id.innerLayout);
        line = v.findViewById(R.id.line_view1);

        loadData(); // 데이터 불러오기.
        setupChart(); // chart 설정.
        return v;
    }


    // 차트에 넣을 데이터 설정
    private void setupChart()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 오늘 이후의 데이터 표시안함
        long displayCount = ChronoUnit.DAYS.between(calendar.timeNow, LocalDate.now()) + 7;
        if(displayCount > 7) displayCount = 7;

        // 출력 데이터 세팅
        for(int i = 0; i < displayCount; i++){
            for (int j = 0; j < dataset.size(); j++) {
                long daysBetween = ChronoUnit.DAYS.between(calendar.timeNow, LocalDate.parse(dataset.get(j).ARM_DT, formatter));
                if (daysBetween == i-6){
                    pills[i] = dataset.get(j);
                    break;
                }
            };
        }

        initChart();
    }

    // 차트 구성
    private void initChart()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        // 날짜 텍스트 뷰
        TextView dateView[] = {binding.date1, binding.date2, binding.date3, binding.date4, binding.date5, binding.date6, binding.date7 };
        for(int i = 0; i < 7; i++){
            dateView[i].setText(calendar.timeNow.minusDays(6-i).format(formatter));
        }
        // 시간 텍스트 뷰
        TextView timeView[] = {binding.textTime1, binding.textTime2, binding.textTime3, binding.textTime4, binding.textTime5};
        for(int i = 0; i < 7; i++){
            dateView[i].setText(calendar.timeNow.minusDays(6-i).format(formatter));
        }
        // point 이미지 뷰
        ImageView points[] = {binding.point1, binding.point2, binding.point3, binding.point4, binding.point5, binding.point6, binding.point7};


        int dataTime[] = new int[7];
        int maxTime = 0, minTime = 1440;
        for(int i = 0; i < 7; i++){
            if(pills[i] == null){ // 내용 없을시 숨기기
                points[i].setVisibility(View.GONE);
            }else{  // 내용 존재시 색깔, 시간에 따른 위치 비율 조정
                points[i].setVisibility(View.VISIBLE);
                if(pills[i].TAKEN_ST.equals("UNTAKEN")) points[i].setBackgroundResource(R.drawable.circle_red);
                else if(pills[i].TAKEN_ST.equals("TAKEN")) points[i].setBackgroundResource(R.drawable.circle_green);
                else if(pills[i].TAKEN_ST.equals("OVERTAKEN")) points[i].setBackgroundResource(R.drawable.circle_blue);
                else points[i].setBackgroundResource(R.drawable.circle_gray);

                String time = pills[i].TAKEN_TM;
                String h = time.substring(0, 2);
                String m = time.substring(2, 4);
                dataTime[i] = Integer.parseInt(h) * 60 + Integer.parseInt(m);
                if(maxTime < dataTime[i]) maxTime = dataTime[i];
                if(dataTime[i] < minTime) minTime = dataTime[i];
                PillInfo pillInfo = new PillInfo(pills[i]);

                //복약 정보 수정
                points[i].setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent_switch = getIntentSwitch(context, pillInfo);
                    startActivityForResult(intent_switch, PillInfoActivity.RESET_ADHERENCE);
                });
            }
        }

        // point 위치 조정
        for(int i = 0; i < 7; i++){
            if(pills[i] == null) continue;
            // y위치 비율 조정
            float bias = (dataTime[i]-minTime)*1.0f/(maxTime-minTime)*0.96f+0.02f;
            setVerticalBias(points[i], bias);
        }

        // 시간대 표시
        int time[] = new int[5];
        if(maxTime < minTime){
            maxTime = 1440;
            minTime = 0;
        }

        for (int i = 0; i < 5; i++){
            time[i] = minTime + (maxTime-minTime)*i/4;
            time[i] = (time[i] / 10) * 10;
            if(i > 0 && time[i] == time[i-1]) {
                timeView[i].setVisibility(View.GONE);
                continue;
            }
            timeView[i].setVisibility(View.VISIBLE);
            timeView[i].setText(String.format("%02d:%02d", time[i]/60, time[i] % 60));
            setVerticalBias(timeView[i], ((time[i]-minTime)/(float)(maxTime-minTime))*0.96f + 0.02f);
        }

    }

    // 데이터 로드
    private void loadData()
    {
        Bundle bundle = getArguments();
        PillBox temp = (PillBox) bundle.getSerializable(Header.WRAPPER_DATASET);
        dataset = temp.values;
        calendar = temp.calendar;
    }

    // point좌표 y값 설정(bias는 비율)
    public void setVerticalBias(View view, float bias) {
        // 뷰를 포함하는 ConstraintLayout 가져오기
        ConstraintLayout parentLayout = (ConstraintLayout) view.getParent();

        // ConstraintSet 생성하여 현재 제약을 가져옴
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);

        // vertical bias 설정 후 적용
        constraintSet.setVerticalBias(view.getId(), bias);
        constraintSet.applyTo(parentLayout);
    }
}
