package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import com.jeyun.rhdms.handler.entity.wrapper.PillBox;

public class NewPillInfoActivity extends GraphActivity<PillBox>
{
    public NewPillInfoActivity()
    {
        title = "복약 정보";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState)
    {
        super.onCreate(savedInstanceState);
        create();
    }
}