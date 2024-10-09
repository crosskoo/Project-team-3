package com.jeyun.rhdms.adapter.wrapper;

import com.jeyun.rhdms.handler.entity.Pill;

public class PillInfo
{
    public String date;
    public String status;
    public String raw_status;
    public String start_time;
    public String end_time;
    public String taken_time;
    public String time_type;
    public Pill pill;
    public PillInfo(String date, String status)
    {
        this.date = date;
        this.status = status;
    }

    public PillInfo(Pill pill)
    {
        this.pill = pill;
        this.date = pill.ARM_DT;
        this.status = pill.TAKEN_ST;
        this.raw_status = pill.TAKEN_ST;
        this.start_time = pill.ARM_ST_TM;
        this.end_time = pill.ARM_ED_TM;
        this.taken_time = pill.TAKEN_TM;
        this.time_type = pill.ARM_TP;

    }
}
