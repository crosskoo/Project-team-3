package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jeyun.rhdms.databinding.ActivityBloodInfoBinding;
import com.jeyun.rhdms.fragment.GraphFragment;
import com.jeyun.rhdms.handler.BloodHandler;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.MyCalendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BloodPressureInfoActivity extends AppCompatActivity {

    private String type = "21"; //
    private ActivityBloodInfoBinding binding;
    private CustomCalendar calendar = new MyCalendar();
    protected Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBloodInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initEvents();
        initData();
    }

    private void initEvents() // 이벤트 초기화
    {
        binding.toggleBloodInfo.setOnClickListener(v ->
        {
            ToggleButton tb = binding.toggleBloodInfo;
            boolean isWeek = tb.isChecked();
            transferData(isWeek);
        });
    }

    private BloodPack loadBloodData(Boolean isWeek) // isWeek가 true이면 주간 혈압 데이터를, false이면 월간 혈압 데이터를 불러옴.
    {
        BloodHandler dh = new BloodHandler(type);
        List<Blood> dataset =
                isWeek ?
                        dh.getDataInWeek(calendar.timeNow) :
                        dh.getDataInMonth(calendar.timeNow);

        System.out.println("TEST : " + dataset);
        if(dataset.isEmpty()) return new BloodPack(new ArrayList<>());
        return new BloodPack((ArrayList<Blood>) dataset);
    }

    private void transferData(Boolean isWeek) // 혈압 정보를 Fragment(그래프)한테 전달
    {
        executor.execute(() ->
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Header.WRAPPER_DATASET, (Serializable) loadBloodData(isWeek));

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment fragment = new GraphFragment(isWeek);
            fragment.setArguments(bundle);

            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE); // 이전 fragment들이 back stack에 남지 않도록 비움.
            fragmentTransaction.replace(binding.bloodInfoData.getId(), fragment); // 기존 fragment를 새로운 fragment로 교체.

            fragmentTransaction.commit();
        });
    }

    private void initData()
    {
        transferData(true); // 기본값으로는 주별 정보를 불러온다.
    }
}
