package com.jeyun.rhdms.handler.entity;

public class User // 아이디, 비밀번호에 해당하는 orgnztId를 저장하는 싱글톤 클래스
{
    private static User instance;
    private String ORGNZT_ID;

    private User() {}

    public static synchronized User getInstance()
    {
        if (instance == null)
        {
            instance = new User();
        }
        return instance;
    }

    public String getOrgnztId()
    {
        return ORGNZT_ID;
    }

    public void setOrgnztId(String orgnztId)
    {
        this.ORGNZT_ID = orgnztId;
    }
}
