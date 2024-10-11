package com.jeyun.rhdms.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jeyun.rhdms.databinding.FragmentChartBinding;
import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.wrapper.PillBox;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeFragment extends Fragment
{
    private View v;
    private List<String> labels;
    private ArrayList<Pill> dataset; // 복약 정보를 저장할 리스트
    private FragmentChartBinding binding;
    public ScatterChart chart; // 복약 정보를 표현할 chart
    ScatterData chartData = new ScatterData(); // chart에 보여줄 데이터

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentChartBinding.inflate(inflater);
        v = binding.getRoot();

        chart = binding.timeChart;
        chartData = new ScatterData();
        loadData(); // 데이터 불러오기.
        setupChart(); // chart 설정.
        return v;
    }

    private void setupChart() // chart에 넣을 데이터를 설정하는 함수
    {
        Map<String, List<Entry>> entryMap = new HashMap<>();
        for (Pill pill : dataset)
        {
            String key = pill.TAKEN_ST; // key : 복약 상태
            if (!entryMap.containsKey(key)) entryMap.put(key, new ArrayList<>());
            List<Entry> entries = entryMap.get(key);

            String time = pill.TAKEN_TM;
            String h = time.substring(0, 2);
            String m = time.substring(2, 4);

            int x = labels.size();
            float y = (Integer.parseInt(h) * 60 + Integer.parseInt(m));

            Entry entry = new Entry(x, y);
            labels.add(pill.ARM_DT);
            entries.add(entry);
        }


        entryMap.entrySet().forEach(pair ->
        {
            ScatterDataSet set = new ScatterDataSet(pair.getValue(), mapLabel(pair.getKey()));
            set.setColor(mapColor(pair.getKey())); // 라인 색깔
            set.setScatterShapeSize(40f);
            set.setDrawHighlightIndicators(true);
            set.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            set.setValueTextSize(0);
            chartData.addDataSet(set);
        });

        initChart();
    }

    private void initChart() // Chart를 초기화 하는 함수
    {
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels)
        {
            @Override
            public String getFormattedValue(float value) {
                return labels.get((int) value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setValueFormatter(new ValueFormatter()
        {
            public String getFormattedValue(float value)
            {
                int hour = (int) value / 60;
                int minute = (int) value % 60;
                return String.format("%02d:%02d", hour, minute);
            }
        });

        chart.setScaleMinima(1f, 1f);
        chart.getViewPortHandler().setMinimumScaleX(1f);

        chart.setData(chartData);
        chart.invalidate();
    }

    private void loadData()
    {
        Bundle bundle = getArguments();
        PillBox temp = (PillBox) bundle.getSerializable(Header.WRAPPER_DATASET);
        labels = new ArrayList<>();
        dataset = temp.values;
    }

    private int mapColor(String TAKEN_ST) // 복약 상태에 따라 점 색상을 다르게 지정하는 함수
    {
        switch (TAKEN_ST)
        {
            case "TAKEN": return Color.GREEN;
            case "DELAYTAKEN": return Color.BLUE;
            case "OVERTAKEN": return Color.rgb(139, 0, 0);
            case "UNTAKEN": return Color.GRAY;
            case "ERRTAKEN": return Color.RED;
            case "OUTTAKEN": return Color.YELLOW;
        }

        return Color.BLACK;
    }

    private String mapLabel(String TAKEN_ST) // 복약 상태를 한글로 표현하는 함수
    {
        switch (TAKEN_ST)
        {
            case "TAKEN": return "복용";
            case "DELAYTAKEN": return "지연";
            case "OVERTAKEN": return "과복용";
            case "UNTAKEN": return "미복용";
            case "ERRTAKEN": return "오복용";
            case "OUTTAKEN": return "외출복용";
        }

        return "N/A";
    }
}
