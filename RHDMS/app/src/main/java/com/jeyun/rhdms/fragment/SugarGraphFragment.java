package com.jeyun.rhdms.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.jeyun.rhdms.R;
import com.jeyun.rhdms.databinding.FragmentSugarGraphBinding;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.List;

public class SugarGraphFragment extends Fragment {

    private View view;
    private FragmentSugarGraphBinding binding;
    private String type = "31";
    private Boolean isWeek; // 주별, 월별에 따라 차트 세팅이 달라짐. (추후 수정)

    private LineChart chart; // 라인 차트
    private LineData chartData; // 차트에 넣을 데이터
    private List<Blood> dataset; // 혈당 정보 저장
    private List<String> labels; // 혈당 측정일 저장

    public SugarGraphFragment(Boolean isWeek)
    {
        this.isWeek = isWeek;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 뷰 바인딩
        this.binding = FragmentSugarGraphBinding.inflate(inflater);
        this.view = binding.getRoot();

        // 데이터 설정
        this.chart = binding.sugarChart;
        this.dataset = new ArrayList<>();
        this.chartData = new LineData();
        loadData();
        loadChart();

        return view;
    }

    private void loadChart()
    {
        setupChart(isWeek);
        if(!dataset.isEmpty())
        {
            initChart();
        }
    }

    private void loadData() // 혈당 정보를 가져오는 함수
    {
        Bundle bundle = getArguments();
        BloodPack pack = (BloodPack) bundle.getSerializable(Header.WRAPPER_DATASET);
        dataset.addAll(pack.values);
        labels = new ArrayList<>();
    }

    private void setupChart(Boolean isWeek) // 차트에 넣을 데이터를 설정하는 함수
    {
        List<Entry> sugarentries = new ArrayList<>(); // 혈당
        this.dataset.forEach(blood ->
        {
            float value = Float.parseFloat(blood.mesure_val);
            Entry sugarentry = new Entry(labels.size(), value);

            labels.add(blood.mesure_de); // labels 리스트에 혈당 측정일을 저장. (mesure_de : 측정일)
            sugarentries.add(sugarentry);
        });

        LineDataSet sugarDataSet = new LineDataSet(sugarentries, "Sugar"); // 혈당 데이터

        int sugarColor = ContextCompat.getColor(requireContext(), R.color.bs);
        sugarDataSet.setCircleColor(sugarColor); // 색상
        sugarDataSet.setDrawCircles(true); // 원형 표시
        sugarDataSet.setDrawValues(false); // y 값은 표시하지 않음.
        sugarDataSet.setDrawFilled(false); // 라인 밑 면 채우기 비활성화

        sugarDataSet.setDrawCircleHole(false);
        sugarDataSet.setCircleHoleColor(Color.TRANSPARENT);

        if (isWeek) // 주간 데이터 설정
        {
            sugarDataSet.setCircleRadius(6f); // 주간 데이터 -> 크게
            sugarDataSet.setColor(Color.TRANSPARENT); // 라인 색상 투명 (안 보이게 설정)
        }
        else // 월간 데이터 설정
        {
            sugarDataSet.setCircleRadius(2f); // 월간 데이터 -> 작게
            sugarDataSet.setColor(sugarColor); // 라인 색상
        }

        chartData.addDataSet(sugarDataSet);
    }

    private void initChart()
    {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP); // x축 위치 설정
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels) // x축 표시 형식 설정
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

        YAxis yAxisLeft = chart.getAxisLeft(); // 왼쪽 y축 비활성화
        yAxisLeft.setEnabled(false);
        YAxis yAxisRight = chart.getAxisRight(); // 오른쪽 y축 활성화
        yAxisRight.setEnabled(true);

        applyCustomFont(xAxis, yAxisRight); // x축 y축 폰트 지정
        chart.setBackgroundColor(Color.LTGRAY); // 차트 배경 색
        chart.setExtraOffsets(20f, 20f, 20f, 20f);
        chart.getLegend().setEnabled(false); // 범례 비활성화
        chart.setDescription(null); // 설명 비활성화
        chart.setDrawGridBackground(false); // 그리드 비활성화
        chart.setData(chartData); // 차트에 데이터 설정
        chart.invalidate(); // 데이터가 바뀔 시 업데이트
    }

    private void applyCustomFont(XAxis xAxis, YAxis yAxis)
    {
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.nanumsquarer);
        xAxis.setTypeface(typeface);
        yAxis.setTypeface(typeface);
    }
}
