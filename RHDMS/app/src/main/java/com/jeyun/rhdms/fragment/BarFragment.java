package com.jeyun.rhdms.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.jeyun.rhdms.databinding.FragmentBarBinding;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.List;

public class BarFragment extends Fragment
{
    private View view;
    private FragmentBarBinding binding;

    private CandleStickChart chart; // 차트
    private CandleData chartData; // 차트에 넣을 데이터
    private List<Blood> dataset; // 혈압 정보를 저장할 배열
    private List<String> labels;

    private String type; // 21 : 혈압, 32 : 혈당

    public BarFragment(String type)
    {
        this.type = type;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // 뷰 바인딩
        this.binding = FragmentBarBinding.inflate(inflater);
        this.view = binding.getRoot();

        // 데이터 설정 및 차트 설정
        this.chart = binding.candleChart;
        this.dataset = new ArrayList<>();
        this.chartData = new CandleData();
        loadData();
        loadChart();

        return view;
    }

    private void loadChart() // 차트를 불러오는 함수
    {
        setupChart();
        if(!dataset.isEmpty())
        {
            initChart();
        }
    }

    private void loadData() // 혈압 정보를 가져오는 함수
    {
        Bundle bundle = getArguments();
        BloodPack pack = (BloodPack) bundle.getSerializable(Header.WRAPPER_DATASET);
        dataset.addAll(pack.values);
        labels = new ArrayList<>();
    }

    private void setupChart() // 차트에 넣을 데이터를 설정하는 함수
    {
        List<CandleEntry> entries = new ArrayList<>();
        this.dataset.forEach(blood ->
        {
            float low, open, high, close;
            String[] values = blood.mesure_val.split("/"); // 측정값을 '/'를 기준으로 나눈다.
            low = open = 0f;
            high = close = Float.parseFloat(values[0]);

            if (type.equals("21")) low = open = Float.parseFloat(values[1]); // 21 : 혈압
            CandleEntry entry = new CandleEntry(labels.size(), high, low, open, close);
            labels.add(blood.mesure_de); // labels 리스트에 혈압 측정일을 저장. (mesure_de : 측정일)
            entries.add(entry);
        });

        // System.out.println("TEST : " + entries);

        CandleDataSet candleDataSet = new CandleDataSet(entries, type.equals("21") ? "혈압" : "혈당");
        candleDataSet.setIncreasingColor(Color.RED);
        candleDataSet.setDecreasingColor(Color.RED);
        candleDataSet.setNeutralColor(Color.RED);
        candleDataSet.setShadowColorSameAsCandle(true);
        candleDataSet.setShowCandleBar(true);
        candleDataSet.setBarSpace(0.3f);
        candleDataSet.setShadowWidth(0f);
        candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        chartData.addDataSet(candleDataSet);
    }

    private void initChart() // 차트를 설정하는 함수
    {
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)
        {
            @Override
            public String getFormattedValue(float value)
            {
                String label = labels.get((int) value);
                String month = label.substring(4, 6);
                String days = label.substring(6, 8);
                return String.format("%s/%s", month, days);
            }
        });

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setValueFormatter(new ValueFormatter()
        {
            @Override
            public String getFormattedValue(float value)
            {
                String unit = type.equals("21") ? "mmHg" : "mg/dL";
                return String.format("%.2f (%s)", value, unit);
            }
        });

        chart.setScaleMinima(1f, 1f);
        chart.getViewPortHandler().setMinimumScaleX(1f);

        // System.out.println("TEST : FLAG 2");
        chart.setDrawGridBackground(false);
        chart.setData(chartData);
        // System.out.println("TEST : FLAG 3");
        chart.invalidate();
        // System.out.println("TEST : FLAG 4");
    }
}
