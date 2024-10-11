package com.jeyun.rhdms.graphActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jeyun.rhdms.databinding.ActivityPillInfoBinding;
import com.jeyun.rhdms.util.CustomCalendar;
import com.jeyun.rhdms.util.MyCalendar;

public abstract class GraphActivity<T> extends AppCompatActivity
{
    protected ActivityPillInfoBinding binding;
    protected String title;

    protected void create()
    {
        binding = ActivityPillInfoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        create();
    }

    protected void initUI()
    {
        TextView tv = binding.pmTextView;
        tv.setText(title);
    }
}