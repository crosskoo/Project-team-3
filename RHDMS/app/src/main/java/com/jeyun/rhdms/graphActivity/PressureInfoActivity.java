package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;

public class PressureInfoActivity extends GraphActivity<BloodPack>
{
    public PressureInfoActivity()
    {
        final String type = "21";

        title = "혈압 정보";
        supplier = () -> new BarFragment(type);
        function = isWeek ->
        {
            BloodHandler dh = new BloodHandler(type);
            List<Blood> dataset =
                    isWeek ?
                            dh.getDataInWeek(calendar.timeNow) :
                            dh.getDataInMonth(calendar.timeNow);

            System.out.println("TEST : " + dataset);
            if(dataset.isEmpty()) return new BloodPack(new ArrayList<>());
            return new BloodPack((ArrayList<Blood>) dataset);
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState);
        create();
    }
}
