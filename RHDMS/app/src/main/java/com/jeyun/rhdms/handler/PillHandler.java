package com.jeyun.rhdms.handler;

import android.annotation.SuppressLint;
import com.jeyun.rhdms.handler.entity.Pill;
import org.sql2o.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class PillHandler extends DataHandler<Pill, LocalDate>
{
    public PillHandler()
    {
        super();
    }

    // 주에 속하는 데이터 조회
    public List<Pill> getDataInWeek(LocalDate today)
    {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = monday.format(format);
        String endDate = sunday.format(format);

        String query_format ="SELECT * FROM tb_drug " + "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " + "AND SUBJECT_ID = %s;";

        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, "1076");
        System.out.println(query);

        try(Connection con = client.open())
        {
            return con.createQuery(query)
                    .executeAndFetch(Pill.class);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // 월에 속하는 데이터 조회
    public List<Pill> getDataInMonth(LocalDate today)
    {
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = first.withDayOfMonth(today.lengthOfMonth());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = first.format(format);
        String endDate = last.format(format);

        String query_format ="SELECT * FROM tb_drug " + "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " + "AND SUBJECT_ID = %s;";

        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, "1076");
        System.out.println(query);

        try(Connection con = client.open())
        {
            return con.createQuery(query)
                    .executeAndFetch(Pill.class);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
