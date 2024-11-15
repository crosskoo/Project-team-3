package com.jeyun.rhdms.handler;

import android.annotation.SuppressLint;
import android.content.Context;

import com.jeyun.rhdms.handler.entity.Pill;
import com.jeyun.rhdms.handler.entity.User;

import org.sql2o.Connection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PillHandler extends DataHandler<Pill, LocalDate>
{
    private String orgnztId;

    public PillHandler()
    {
        super();
    }

    // AlarmReceiver에서 사용하는 PillHandler 생성자
    public PillHandler(Context context)
    {
        super();
        SharedPreferenceHandler handler = new SharedPreferenceHandler(context);
        orgnztId = handler.getSavedOrgnztId();
    }

    public List<Pill> getDataInWeek(LocalDate today)
    {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = monday.format(format);
        String endDate = sunday.format(format);

        String query_format =
                "SELECT * FROM tb_drug " +
                        "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " +
                        "AND SUBJECT_ID = %s;";


        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, User.getInstance().getOrgnztId());

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

    // 날짜로부터 이전으로 7일의 복약 데이터 가져오는 함수
    public List<Pill> getDataIn7days(LocalDate today)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = today.minusDays(6).format(format);
        String endDate = today.format(format);

        String query_format =
                "SELECT * FROM tb_drug " +
                        "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " +
                        "AND SUBJECT_ID = %s;";


        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, User.getInstance().getOrgnztId());

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

    // 날짜로부터 이전으로 30일의 복약 데이터 가져오는 함수
    public List<Pill> getDataIn30days(LocalDate today)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = today.minusDays(29).format(format);
        String endDate = today.format(format);

        String query_format =
                "SELECT * FROM tb_drug " +
                        "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " +
                        "AND SUBJECT_ID = %s;";


        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, User.getInstance().getOrgnztId());

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

    public List<Pill> getDataInMonth(LocalDate today)
    {
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = first.withDayOfMonth(today.lengthOfMonth());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startDate = first.format(format);
        String endDate = last.format(format);

        String query_format =
                "SELECT * FROM tb_drug " +
                        "WHERE CONVERT(varchar, ARM_DT, 112) BETWEEN %s AND %s " +
                        "AND SUBJECT_ID = %s;";

        @SuppressLint("DefaultLocale")
        String query = String.format(query_format, startDate, endDate, User.getInstance().getOrgnztId());

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

    @Override
    public Optional<Pill> getData(String id)
    {
        String query_format =
                "SELECT * FROM tb_drug " +
                        "WHERE ARM_DT = '%s' " +
                        "AND SUBJECT_ID = '%s';";

        String query = String.format(query_format, id, User.getInstance().getOrgnztId());

        try(Connection con = client.open())
        {
            return Optional.ofNullable(con.createQuery(query).executeAndFetchFirst(Pill.class));
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean updateData(Pill data)
    {
        String query_format =
                "UPDATE tb_drug " +
                        "SET TAKEN_ST = '%s', " +
                        "TAKEN_TM = '%s' " +
                        "WHERE SUBJECT_ID = '%s' " +
                        "AND ARM_DT = '%s'";

        String query = String.format(query_format, data.TAKEN_ST, data.TAKEN_TM, User.getInstance().getOrgnztId(), data.ARM_DT);
        System.out.println(query);

        try(Connection con = client.open())
        {
            con.createQuery(query).executeUpdate();
            return true;
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static String dateToString(LocalDate date)
    {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        return String.format("%d%02d%02d", year, month, day);
    }
}
