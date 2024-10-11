package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.jeyun.rhdms.databinding.ActivityPillInfoBinding;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.Header;
import com.jeyun.rhdms.util.MyCalendar;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GraphActivity<T> extends AppCompatActivity
{
    protected ActivityPillInfoBinding binding;
    protected CustomCalendar calendar = new MyCalendar();
    protected Executor executor = Executors.newSingleThreadExecutor();

    protected Function<Boolean, T> function; // 자식 클래스마다 불러오는 데이터 및 그 함수가 다름.
    protected Supplier<Fragment> supplier; // 자식 클래스마다 fragment가 다름.
    protected String title; // 자식 클래스마다 title 값이 다름.

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

    protected void initUI()
    {
        TextView tv = binding.pmTextView;
        tv.setText(title);
    }

    // '주' 혹은 '월'에 맞는 데이터를 불러오고 그 데이터에 맞게 fragment를 교체하는 함수.
    // 데이터를 불러오는 데 오래 걸릴 수 있으므로 비동기적으로 수행.
    protected void loadData(boolean isWeek)
    {
        executor.execute(() ->
        {
            Bundle bundle = new Bundle();
            // (function으로부터 불러온 데이터들을 bundle에 넣는다. 데이터는 자식 클래스마다 다름.)
            bundle.putSerializable(Header.WRAPPER_DATASET, (Serializable) function.apply(isWeek));

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
            loadData(isWeek);
        });

        // 토글 버튼에 설정된 값만큼 날짜가 증가하고 그 날짜에 해당하는 데이터를 불러옴.
        binding.buttonIncrease.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillInfo;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.increase(type);
            loadData(tb.isChecked());
        });

        // 토글 버튼에 설정된 값만큼 날짜가 줄어들고 그 날짜에 해당하는 데이터를 불러옴.
        binding.buttonDecrease.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillInfo;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.decrease(type);
            loadData(tb.isChecked());
        });
    }

    // 기본값으로는 '주'에 해당하는 데이터를 불러옴.
    protected void initData()
    {
        loadData(true);
    }
}