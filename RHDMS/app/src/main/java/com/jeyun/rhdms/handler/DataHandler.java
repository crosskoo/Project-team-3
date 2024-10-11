package com.jeyun.rhdms.handler;


import org.sql2o.Sql2o;

import java.util.List;
import java.util.Optional;

/**
 * 데이터베이스와 연결하여 주/월 단위로 데이터를 읽어온다.
 * @param <T> Entity (읽을 테이블)
 */

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
