package com.jeyun.rhdms.handler;

import com.jeyun.rhdms.handler.entity.Pill;

import org.sql2o.Sql2o;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public abstract class DataHandler<T, K>
{
    private final String url = "jdbc:jtds:sqlserver://211.229.106.53:11433/사용성평가";
    private final String username = "sa";
    private final String password = "test1q2w3e@@";

    protected Sql2o client;
    public DataHandler()
    {
        this.client = new Sql2o(url, username, password);
    }

    public abstract List<T> getDataInWeek(K id);
    public abstract List<T> getDataInMonth(K id);
    public abstract Optional<T> getData(String id);
    public abstract boolean updateData(T data);
}
