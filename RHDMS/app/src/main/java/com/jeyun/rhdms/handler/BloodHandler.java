package com.jeyun.rhdms.handler;

import android.annotation.SuppressLint;

import com.jeyun.rhdms.handler.entity.Blood;
import com.jeyun.rhdms.handler.entity.Pill;

import org.sql2o.Connection;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SELECT * FROM itf_lbdy_mesure_colct
 * WHERE mesure_tp = 21
 * AND CONVERT(varchar, ARM_DT, 112)
 * BETWEEN %s AND %s AND SUBJECT_ID = %s;
 */

public class BloodHandler extends DataHandler<Blood, LocalDate>
{
    private String type;
    public BloodHandler(String type)
    {
        this.type = type;
    }

    @Override
    public List<Blood> getDataInWeek(LocalDate today)
    {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = monday.format(format);
        String endDate = sunday.format(format);

        String query_format =
                "SELECT * FROM itf_lbdy_mesure_colct " +
                        "WHERE mesure_tp = %s" +
                        "AND CONVERT(varchar, mesure_de, 112) BETWEEN %s AND %s " +
                        "AND SUBJECTID = %s;";

        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, type, startDate, endDate, "1076");
        System.out.println(query);

        try(Connection con = client.open())
        {
            return con.createQuery(query)
                    .executeAndFetch(Blood.class);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<Blood> getDataInMonth(LocalDate today)
    {
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = first.withDayOfMonth(today.lengthOfMonth());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = first.format(format);
        String endDate = last.format(format);

        String query_format =
                "SELECT * FROM itf_lbdy_mesure_colct " +
                        "WHERE mesure_tp = %s" +
                        "AND CONVERT(varchar, mesure_de, 112) BETWEEN %s AND %s " +
                        "AND SUBJECTID = %s;";

        String query = String.format(query_format, type, startDate, endDate, "1076");
        System.out.println(query);

        try(Connection con = client.open())
        {
            return con.createQuery(query)
                    .executeAndFetch(Blood.class);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    @Deprecated
    public Optional<Blood> getData(String id)
    {
        return Optional.empty();
    }

    @Override
    @Deprecated
    public boolean updateData(Blood data)
    {
        return false;
    }
}
