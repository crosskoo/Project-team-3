package com.jeyun.rhdms.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.jeyun.rhdms.databinding.FragmentGraphBinding;
import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
import com.jeyun.rhdms.util.Header;

import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment {

    private View view;
    private FragmentGraphBinding binding;
    private String type = "21";
    private Boolean isWeek; // 주별, 월별에 따라 차트 세팅이 달라짐. (추후 수정)

    private LineChart chart; // 라인 차트
    private LineData chartData; // 차트에 넣을 데이터
    private List<Blood> dataset; // 혈압 정보 저장
    private List<String> labels; // 혈압 측정일 저장

    public GraphFragment(Boolean isWeek)
    {
        this.isWeek = isWeek;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 뷰 바인딩
        this.binding = FragmentGraphBinding.inflate(inflater);
        this.view = binding.getRoot();

        // 데이터 설정
        this.chart = binding.lineChart;
        this.dataset = new ArrayList<>();
        this.chartData = new LineData();
        loadData();
        loadChart();

        return view;
    }

    private void loadChart()
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
        List<Entry> highentries = new ArrayList<>(); // 수축기 혈압
        List<Entry> lowentries = new ArrayList<>(); // 이완기 혈압
        this.dataset.forEach(blood ->
        {
            float low, open, high, close;
            String[] values = blood.mesure_val.split("/"); // 측정값을 '/'를 기준으로 나눈다.
            low = open = 0f;
            high = close = Float.parseFloat(values[0]);

            if (type.equals("21")) low = open = Float.parseFloat(values[1]); // 21 : 혈압
            Entry highentry = new Entry(labels.size(), high);
            Entry lowentry = new Entry(labels.size(), low);

            labels.add(blood.mesure_de); // labels 리스트에 혈압 측정일을 저장. (mesure_de : 측정일)
            highentries.add(highentry);
            lowentries.add(lowentry);
        });

        LineDataSet HighlineDataSet = new LineDataSet(highentries, "High Pressure"); // 수축기 혈압 데이터
        HighlineDataSet.setColor(Color.TRANSPARENT); // 라인 색상 투명 (안 보이게 설정)
        HighlineDataSet.setCircleColor(Color.RED); // 색상
        HighlineDataSet.setDrawCircles(true); // 원형 표시
        HighlineDataSet.setCircleRadius(10f); // 원의 반지름
        HighlineDataSet.setDrawValues(false); // y 값은 표시하지 않음.
        HighlineDataSet.setDrawFilled(false); // 라인 채우기 비활성화

        HighlineDataSet.setDrawCircleHole(false);
        HighlineDataSet.setCircleHoleColor(Color.TRANSPARENT);

        LineDataSet LowlineDataSet = new LineDataSet(lowentries, "Low Pressure"); // 이완기 혈압 데이터
        LowlineDataSet.setColor(Color.TRANSPARENT); // 라인 색상 투명 (안 보이게 설정)
        LowlineDataSet.setCircleColor(Color.YELLOW); // 색상
        LowlineDataSet.setDrawCircles(true); // 원형 표시
        LowlineDataSet.setCircleRadius(10f); // 원의 반지름
        LowlineDataSet.setDrawValues(false); // y 값은 표시하지 않음.
        LowlineDataSet.setDrawFilled(false); // 라인 채우기 비활성화

        LowlineDataSet.setDrawCircleHole(false);
        LowlineDataSet.setCircleHoleColor(Color.TRANSPARENT);

        chartData.addDataSet(HighlineDataSet);
        chartData.addDataSet(LowlineDataSet);
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

        chart.getLegend().setEnabled(false); // 범례 비활성화
        chart.setDescription(null); // 설명 비활성화
        chart.setDrawGridBackground(false); // 그리드 비활성화
        chart.setData(chartData); // 차트에 데이터 설정
        chart.invalidate(); // 데이터가 바뀔 시 업데이트
    }
}
