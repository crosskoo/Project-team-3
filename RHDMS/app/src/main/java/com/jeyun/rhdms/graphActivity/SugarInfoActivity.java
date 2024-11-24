//package com.jeyun.rhdms.graphActivity;
//
//import android.os.Bundle;
//import android.os.PersistableBundle;
//
//import androidx.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.jeyun.rhdms.fragment.BarFragment;
//import com.jeyun.rhdms.handler.BloodHandler;
//import com.jeyun.rhdms.handler.entity.Blood;
//import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;
//
//public class SugarInfoActivity extends GraphActivity<BloodPack>
//{
//    public SugarInfoActivity()
//    {
//        final String type = "31";
//
//        title = "혈당 정보";
//        supplier = () -> new BarFragment(type);
//        function = isWeek ->
//        {
//            BloodHandler dh = new BloodHandler(type);
//            List<Blood> dataset =
//                    isWeek ?
//                            dh.getDataInWeek(calendar.timeNow) :
//                            dh.getDataInMonth(calendar.timeNow);
//
//            System.out.println("TEST : " + dataset);
//            if(dataset.isEmpty()) return new BloodPack(new ArrayList<>());
//            return new BloodPack((ArrayList<Blood>) dataset);
//        };
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
//    {
//        super.onCreate(savedInstanceState);
//        create();
//    }
//}
