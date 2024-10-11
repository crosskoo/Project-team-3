package com.jeyun.rhdms;

import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jeyun.rhdms.adapter.wrapper.PillInfo;
import com.jeyun.rhdms.adapter.pill.PillInfoAdapter;
import com.jeyun.rhdms.databinding.ActivityPillListBinding;
import com.jeyun.rhdms.handler.PillHandler;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.MyCalendar;
import com.jeyun.rhdms.util.factory.PillWrapperFactory;
import com.jeyun.rhdms.util.factory.WrapperFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PillListActivity extends AppCompatActivity
{
    private ActivityPillListBinding binding;
    private PillInfoAdapter adapter;
    private CustomCalendar calendar = new MyCalendar();
    private Executor executor = Executors.newSingleThreadExecutor();
    private WrapperFactory<Pill, PillInfo> factory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPillListBinding.inflate(getLayoutInflater());
        factory = new PillWrapperFactory();

        View view = binding.getRoot();
        setContentView(view);

        initEvents();
        initTable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        ToggleButton button = binding.togglePillList;
        loadData(button.isChecked());
    }

    private void initEvents()
    {
        binding.togglePillList.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillList;
            boolean isWeek = tb.isChecked();
            loadData(isWeek);
        });

        binding.plButtonIncrease.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillList;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.increase(type);
            loadData(tb.isChecked());
        });

        binding.plButtonDecrease.setOnClickListener(v ->
        {
            ToggleButton tb = binding.togglePillList;
            int type = tb.isChecked() ? CustomCalendar.WEEK : CustomCalendar.MONTH;
            calendar.decrease(type);
            loadData(tb.isChecked());
        });
    }

    private void initTable()
    {
        binding.plList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PillInfoAdapter(Collections.emptyList());
        binding.plList.setAdapter(adapter);
        loadData(true);
    }

    private void loadData(boolean isWeek)
    {
        executor.execute(() ->
        {
            try
            {
                Map<String, String> func = mapper();
                PillHandler ph = new PillHandler();
                List<Pill> data = isWeek ? ph.getDataInWeek(calendar.timeNow) : ph.getDataInMonth(calendar.timeNow);

                List<PillInfo> list = new ArrayList<>();
                for(Pill pill : data)
                {
                    PillInfo pillInfo = factory.createWrapper(pill);
                    pillInfo.status = func.getOrDefault(pillInfo.status, "N/A");
                    list.add(pillInfo);
                }

                Collections.reverse(list);
                runOnUiThread(() -> adapter.setInfoList(list));
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private Map<String, String> mapper()
    {
        Map<String, String> func = new HashMap<>();
        func.put("TAKEN", "정상복용");
        func.put("OUTTAKEN", "외출복용");
        func.put("UNTAKEN", "미복용");
        func.put("OVERTAKEN", "과복용");
        func.put("ERRTAKEN", "오복용");
        func.put("DELAYTAKEN", "지연복용");

        return func;
    }
}