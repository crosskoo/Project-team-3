package com.jeyun.rhdms.fragment;

import static com.jeyun.rhdms.adapter.pill.PillInfoAdapter.getIntentSwitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.jeyun.rhdms.PillInfoActivity;
import com.jeyun.rhdms.R;
import com.jeyun.rhdms.adapter.wrapper.PillInfo;
import com.jeyun.rhdms.databinding.FragmentPillCalendarBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.wrapper.PillBox;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

// 복약 기록 캘린더 fragment
public class PillCalendarFragment extends Fragment {
    private View v;
    private FragmentPillCalendarBinding binding;
    private CustomCalendar calendar; // 출력할 복약 기간 시작날짜
    private ArrayList<Pill> dataset; // 복약 정보를 저장할 리스트
    private TextView[] dateViews;   // 날짜 출력 텍스트들
    private ImageView[] pointViews; // 시간을 표시할 점들
    private int parentHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentPillCalendarBinding.inflate(inflater);
        v = binding.getRoot();
        dateViews = new TextView[] {
                binding.textDay0 , binding.textDay1, binding.textDay2, binding.textDay3, binding.textDay4, binding.textDay5, binding.textDay6, binding.textDay7 , binding.textDay8, binding.textDay9, binding.textDay10, binding.textDay11, binding.textDay12, binding.textDay13, binding.textDay14 , binding.textDay15, binding.textDay16, binding.textDay17, binding.textDay18, binding.textDay19, binding.textDay20, binding.textDay21 , binding.textDay22, binding.textDay23, binding.textDay24, binding.textDay25, binding.textDay26, binding.textDay27, binding.textDay28 , binding.textDay29, binding.textDay30, binding.textDay31, binding.textDay32, binding.textDay33, binding.textDay34, binding.textDay35 , binding.textDay36
        };
        pointViews = new ImageView[] {
                binding.calendarPoint0, binding.calendarPoint1, binding.calendarPoint2, binding.calendarPoint3, binding.calendarPoint4, binding.calendarPoint5, binding.calendarPoint6, binding.calendarPoint7, binding.calendarPoint8, binding.calendarPoint9, binding.calendarPoint10, binding.calendarPoint11, binding.calendarPoint12, binding.calendarPoint13, binding.calendarPoint14, binding.calendarPoint15, binding.calendarPoint16, binding.calendarPoint17, binding.calendarPoint18, binding.calendarPoint19, binding.calendarPoint20, binding.calendarPoint21, binding.calendarPoint22, binding.calendarPoint23, binding.calendarPoint24, binding.calendarPoint25, binding.calendarPoint26, binding.calendarPoint27, binding.calendarPoint28, binding.calendarPoint29, binding.calendarPoint30, binding.calendarPoint31, binding.calendarPoint32, binding.calendarPoint33, binding.calendarPoint34, binding.calendarPoint35, binding.calendarPoint36
        };

        initEvent();
        loadData(); // 데이터 불러오기.
        setupChart(); // chart 설정.

        return v;
    }

    private void initEvent(){
        // 기간 이동 이벤트
        binding.buttonDecrease.setOnClickListener(v -> {
            ((PillInfoActivity)getContext()).goToPreviousPeriod();
        });
        binding.buttonIncrease.setOnClickListener(v -> {
            ((PillInfoActivity)getContext()).goToNextPeriod();
        });
    }

    private void loadData(){
        Bundle bundle = getArguments();
        PillBox temp = (PillBox) bundle.getSerializable(Header.WRAPPER_DATASET);
        dataset = temp.values;
        calendar = temp.calendar;
    }

    private void setupChart(){
        // 헤더 설정
        binding.calendarHeader.setText(calendar.timeNow.getYear() + "년 " + calendar.timeNow.getMonthValue() + "월");

        // 캘린더 출력 형태 설정
        int currentMonthDays = YearMonth.from(calendar.timeNow).lengthOfMonth();
        int startDay = calendar.timeNow.withDayOfMonth(1).getDayOfWeek().getValue() % 7;
        int rows = (startDay + YearMonth.from(calendar.timeNow).lengthOfMonth() - 1) / 7 + 1;
        if(rows == 6){
            binding.calendarImage.setBackgroundResource(R.drawable.bg_pill_monthdata_6rows);
        }else{
            rows = 5;
            binding.calendarImage.setBackgroundResource(R.drawable.bg_pill_monthdata);
        }


        // 데이터 적용
        Pill pills[] = new Pill[37];
        for(int i = 0; i < dataset.size(); i++){
            String dateString = dataset.get(i).ARM_DT;
            int date = Integer.parseInt(dateString.substring(dateString.length() - 2));
            pills[date + startDay - 1] = dataset.get(i);
        }

        // 오늘 이후의 데이터 표시안함
        long displayCount = ChronoUnit.DAYS.between(calendar.timeNow.withDayOfMonth(1), LocalDate.now()) + 1 + startDay;
        if(displayCount > 37) displayCount = 37;

        // 오늘까지의 복용 데이터 출력
        for(int i = 0; i < 37; i++){
            dateViews[i].setVisibility(View.GONE);
            pointViews[i].setVisibility(View.GONE);

            if(i < startDay || startDay + currentMonthDays <= i) continue;

            dateViews[i].setText(""+ (i-startDay+1));
            setViewMargin(dateViews[i], ((i / 7) / (float) rows), rows, 0);

            if(pills[i] == null || displayCount <= i) continue;

            setViewMargin(pointViews[i], ((i / 7) / (float) rows), rows, 1);

            String status = pills[i].TAKEN_ST;
            if(status.equals("TAKEN")) pointViews[i].setBackgroundResource(R.drawable.ic_medication_light_green);
            else if(status.equals("DELAYTAKEN")) pointViews[i].setBackgroundResource(R.drawable.ic_medication_light_sky_blue);
            else if(status.equals("OUTTAKEN")) pointViews[i].setBackgroundResource(R.drawable.ic_medication_mustard_yellow);
            else if(status.equals("ERRTAKEN") || status.equals("OVERTAKEN")) pointViews[i].setBackgroundResource(R.drawable.ic_medication_coral_pink);
            else if(status.equals("UNTAKEN")) pointViews[i].setBackgroundResource(R.drawable.ic_medication_gray);
            else {
                pointViews[i].setVisibility(View.GONE);
                continue;
            }

            PillInfo pillInfo = new PillInfo(pills[i]);

            //복약 정보 수정
            pointViews[i].setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent_switch = getIntentSwitch(context, pillInfo);
                startActivityForResult(intent_switch, PillInfoActivity.RESET_ADHERENCE);
            });
        }
    }

    //view의 위치 설정
    private void setViewMargin(View view, float ratio, int rows, int mode) {
        Context context = getContext();
        // 부모 레이아웃의 높이 가져오기
        ConstraintLayout parentLayout = (ConstraintLayout) view.getParent(); // 부모 레이아웃 가져오기

        //높이가 측정되지 않은 경우 처리 (View가 아직 레이아웃에 배치되지 않았을 때)
        if (parentHeight == 0) {
            parentLayout.post(() -> {
                parentHeight = parentLayout.getHeight();
                setViewMargin(view, ratio, rows, mode);
            }); // 레이아웃 배치 후 다시 호출
            return;
        }

        float density = context.getResources().getDisplayMetrics().density;
        int margin;

        // 비율 기반 y값 계산
        if(mode == 0){
            if(rows < 6) margin = (int) (parentHeight * ratio) + (int)(parentHeight / rows / 8 - 5 * density);
            else margin = (int) (parentHeight * ratio) + (int)(parentHeight / rows / 6 - 5 * density);
        }else{
            if(rows < 6) margin = (int) (parentHeight * ratio) + (int)(parentHeight / rows / 4 + (parentHeight / rows / 8 * 3 - 10 * density));
            else margin = (int) (parentHeight * ratio) + (int)(parentHeight / rows / 3 + (parentHeight / rows / 3 - 10 * density));
        }


        // LayoutParams를 가져온 후 마진 값 설정
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, margin, params.rightMargin, params.bottomMargin); // 위 마진 설정

        view.setVisibility(View.VISIBLE);
        view.setLayoutParams(params); // 변경된 LayoutParams 적용
    }
}
