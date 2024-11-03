package com.jeyun.rhdms.handler.entity.wrapper;

import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.util.CustomCalendar;

import java.io.Serializable;
import java.util.ArrayList;

public class PillBox implements Serializable
{
    public ArrayList<Pill> values;
    public CustomCalendar calendar;
    public boolean isWeek;

    public PillBox(ArrayList<Pill> values, CustomCalendar calendar, boolean isWeek)
    {
        this.values = values;
        this.calendar = calendar;
        this.isWeek = isWeek;
    }
}
