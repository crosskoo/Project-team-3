package com.jeyun.rhdms.util;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class CustomCalendar implements Serializable
{
    public LocalDate timeNow; // 현재 날짜
    public static final int WEEK = 1;
    public static final int MONTH = 2;

    public CustomCalendar()
    {
        this.timeNow = LocalDate.now();
    }

    protected int getUnit(int type, LocalDate date) // type 값에 따라 일주일 혹은 한 달을 반환
    {
        switch(type)
        {
            case CustomCalendar.WEEK: return 7;
            case CustomCalendar.MONTH: return date.lengthOfMonth();
        }

        return 1;
    }

    public abstract void increase(int type);
    public abstract void decrease(int type);
    public abstract String getFormattedTime();
}
