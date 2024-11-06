package com.jeyun.rhdms.handler.entity;

import com.jeyun.rhdms.handler.DataHandler;

import org.sql2o.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserHandler extends DataHandler<User, String>
{
    private String id;
    public UserHandler(String id)
    {
        this.id = id;
    }

    public Boolean findUserInfo(String id) // 유저가 입력한 id과 동일한 정보가 DB에 있는 지 탐색하는 메소드
    {
        String query_format = "SELECT COUNT(*) " +
                "FROM lettnemplyrinfo " +
                "WHERE EMPLYR_ID = :userId";

        try (Connection con = client.open())
        {
            int count =  con.createQuery(query_format)
                    .addParameter("userId", id)
                    .executeScalar(Integer.class);
            return count > 0; // 있으면 true, 없으면 false 반환
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false; // 없으면 false 반환
        }
    }

    // 테스트 용
    public void showTableInfo() {
        String query_format = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH\n" +
                "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                "WHERE TABLE_NAME = 'lettnemplyrinfo';";

        List<Map<String, Object>> columnInfo = new ArrayList<>();

        try (Connection con = client.open()) {
            columnInfo = con.createQuery(query_format)
                    .executeAndFetchTable()
                    .asList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 컬럼 정보 출력 (컬럼명, 데이터 타입, 최대 길이)
        for (Map<String, Object> row : columnInfo) {
            System.out.println("Column Name: " + row.get("COLUMN_NAME") +
                    ", Data Type: " + row.get("DATA_TYPE") +
                    ", Max Length: " + row.get("CHARACTER_MAXIMUM_LENGTH"));
        }
    }


    // 나머지 메소드들은 사용하지 않는다.
    @Override
    @Deprecated
    public List<User> getDataInWeek(String id) {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<User> getDataInMonth(String id) {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public Optional<User> getData(String id) {
        return Optional.empty();
    }

    @Override
    @Deprecated
    public boolean updateData(User data) {
        return false;
    }
}
