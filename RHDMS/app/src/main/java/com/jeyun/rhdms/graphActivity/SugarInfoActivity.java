package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;

import com.jeyun.rhdms.handler.entity.wrapper.BloodPack;

public class SugarInfoActivity extends GraphActivity<BloodPack>
{
    public SugarInfoActivity()
    {
        final String type = "31";

        title = "혈당 정보";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState);
        create();
    }
}
