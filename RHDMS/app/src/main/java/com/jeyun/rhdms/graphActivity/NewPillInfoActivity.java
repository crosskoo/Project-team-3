//package com.jeyun.rhdms.graphActivity;
//
//import android.os.Bundle;
//import android.os.PersistableBundle;
//import androidx.annotation.Nullable;
//
//import com.jeyun.rhdms.fragment.WeekPillChartFragment;
//import com.jeyun.rhdms.handler.PillHandler;
//import com.jeyun.rhdms.handler.entity.Pill;
//import com.jeyun.rhdms.handler.entity.wrapper.PillBox;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class NewPillInfoActivity extends GraphActivity<PillBox>
//{
//    public NewPillInfoActivity()
//    {
//        title = "복약 정보";
//        supplier = WeekPillChartFragment::new;
//        function = isWeek ->
//        {
//            PillHandler ph = new PillHandler();
//            List<Pill> pills = isWeek ? ph.getDataIn7days(calendar.timeNow) : ph.getDataInMonth(calendar.timeNow);
//            return new PillBox((ArrayList<Pill>) pills, calendar, isWeek);
//        }; // 복약 정보를 불러오는 함수
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
//    {
//        super.onCreate(savedInstanceState);
//        create();
//    }
//}