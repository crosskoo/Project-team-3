package com.jeyun.rhdms.util;

import java.time.format.DateTimeFormatter;

public class MyCalendar extends CustomCalendar
{
    @Override
    // 설정된 단위(7일 or 한달)만큼 현재 날짜에서 그 만큼 더하는 함수
    public void increase(int type)
    {
        int unit = getUnit(type, timeNow);
        timeNow = timeNow.plusDays(unit);
    }

    @Override
    // 설정된 단위(7일 or 한달)만큼 현재 날짜에서 그 만큼 빼는 함수
    public void decrease(int type)
    {
        int unit = getUnit(type, timeNow);
        timeNow = timeNow.minusDays(unit);
    }

    @Override
    // 날짜를 'yyyyMMdd' 형태로 표현하는 함수
    public String getFormattedTime()
    {
        return timeNow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
