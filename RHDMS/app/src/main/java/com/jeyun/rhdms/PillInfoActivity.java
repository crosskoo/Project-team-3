package com.jeyun.rhdms;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jeyun.rhdms.databinding.ActivityPillInfoBinding;
import com.jeyun.rhdms.fragment.PillCalendarFragment;
import com.jeyun.rhdms.fragment.WeekPillChartFragment;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.wrapper.PillBox;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.MyCalendar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class PillInfoActivity extends AppCompatActivity {
    protected ActivityPillInfoBinding binding;
    protected CustomCalendar calendar = new MyCalendar();
    protected Executor executor = Executors.newSingleThreadExecutor();

    protected Supplier<Fragment> supplier;
    protected String title = "복약 정보";

    public static final int RESET_ADHERENCE = 56384645;

    protected void create()
    {
        binding = ActivityPillInfoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initUI(); // UI 설정
        initEvent(); // 이벤트 리스너 설정
        initData(); // 초기 데이터 설정
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ToggleButton tb = binding.togglePillInfo;
        loadData(tb.isChecked());
    }

    protected void initUI()
    {
        TextView tv = binding.pmTextView;
        supplier = WeekPillChartFragment::new;
        tv.setText(title);
    }

    // '주' 혹은 '월'에 맞는 데이터를 불러오고 그 데이터에 맞게 fragment를 교체하는 함수.
    // 데이터를 불러오는 데 오래 걸릴 수 있으므로 비동기적으로 수행.
    protected void loadData(boolean isWeek)
    {
        executor.execute(() ->
        {
            PillHandler ph = new PillHandler();
            List<Pill> pills = isWeek ? ph.getDataIn7days(calendar.timeNow) : ph.getDataInMonth(calendar.timeNow);
            PillBox pillBox = new PillBox((ArrayList<Pill>) pills, calendar, isWeek);

            // 기간 설정 & 복약 순응률 계산
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isWeek){
                        binding.textDuration.setText(calendar.timeNow.minusDays(6).format(formatter) + " ~ " + calendar.timeNow.format(formatter));
                    }else{
                        binding.textDuration.setText(calendar.timeNow.withDayOfMonth(1).format(formatter) + " ~ " + calendar.timeNow.withDayOfMonth(calendar.timeNow.lengthOfMonth()).format(formatter));
                    }

                    // 복약 순응률 계산
                    setAdherenceRate(pills);
                }
            });

            Bundle bundle = new Bundle();
            // (function으로부터 불러온 데이터들을 bundle에 넣는다. 데이터는 자식 클래스마다 다름.)
            bundle.putSerializable(Header.WRAPPER_DATASET, (Serializable)pillBox);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // fragment에 아까 전에 불러온 데이터를 전달.
            // (자식클래스마다 supplier의 fragment가 다름.)
            Fragment fragment = supplier.get();
            fragment.setArguments(bundle);

            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 이전 fragment들이 back stack에 남지 않도록 비움.
            fragmentTransaction.replace(binding.pmFrame.getId(), fragment); // 기존 fragment를 새로운 fragment로 교체.

            fragmentTransaction.commit();
        });
    }

    // 각 버튼에 대한 이벤트 핸들러를 설정하는 함수
    // (버튼을 눌렀을 때 어떤 이벤트가 실행되는가?)
    protected void initEvent()
    {
        // 토글 버튼을 누를 때마다 '주' / '월' 이 바뀌고 그것에 따라 가져오는 데이터가 다름.
        binding.togglePillInfo.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillInfo;
            boolean isWeek = tb.isChecked();
            supplier = isWeek ? WeekPillChartFragment::new : PillCalendarFragment::new;
            loadData(isWeek);
        });
        // 복약 수동 입력 창 열기
        binding.pillupdateEnter.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NewPillInfoActivity.class);
            startActivity(intent);
            finish();
        });
        
        binding.back.setOnClickListener(v -> finish());
    }

    // 기본값으로는 '주'에 해당하는 데이터를 불러옴.
    protected void initData()
    {
        loadData(true);
    }

    //복약 순응률 설정
    private void setAdherenceRate(List<Pill> pills){
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");
        int adherenceCount = 0;
        int totalCount = 0;
        for(int i = 0; i < pills.size(); i++){
            if(0 < ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(pills.get(i).ARM_DT, formatter2))) continue;
            totalCount++;
            if(pills.get(i).TAKEN_ST.equals("TAKEN")) adherenceCount++;
        }
        if(totalCount == 0) binding.textAdherencePercentage.setText("-");
        else binding.textAdherencePercentage.setText(String.format("%.1f", adherenceCount / (float)totalCount * 100) + "%");
    }
    
    // 이전 기간 데이터 로드 함수
    public void goToPreviousPeriod(){
        ToggleButton tb = binding.togglePillInfo;
        int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
        calendar.decrease(type);
        loadData(tb.isChecked());
    }
    // 이후 기간 데이터 로드 함수
    public void goToNextPeriod(){
        ToggleButton tb = binding.togglePillInfo;
        int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
        calendar.increase(type);
        loadData(tb.isChecked());
    }
}
