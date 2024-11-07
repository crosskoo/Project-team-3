package com.jeyun.rhdms.handler;

import com.jeyun.rhdms.handler.entity.User;

import org.sql2o.Connection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserHandler extends DataHandler<User, String>
{

    public Optional<String> findUserInfo(String id, String encryptedPassword) // 유저가 입력한 id과 동일한 정보가 DB에 있는 지 탐색하는 메소드
    {

        String query_format = "SELECT ORGNZT_ID " +
                "FROM lettnemplyrinfo " +
                "WHERE EMPLYR_ID = :userId " +
                "AND PASSWORD = :password";

        try (Connection con = client.open())
        {

            String orgnztId = con.createQuery(query_format)
                    .addParameter("userId", id)
                    .addParameter("password", encryptedPassword)
                    .executeScalar(String.class);

            return Optional.ofNullable(orgnztId); // id, 비밀번호에 해당하는 유저의 orgnztId 반환
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Optional.empty(); // 없으면 빈 객체 반환
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
