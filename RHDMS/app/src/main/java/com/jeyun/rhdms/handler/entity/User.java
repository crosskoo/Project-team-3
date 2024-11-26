package com.jeyun.rhdms.handler.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User // 아이디, 비밀번호에 해당하는 orgnztId를 저장하는 싱글톤 클래스
{
    private static User instance;
    private String ORGNZT_ID;
    private LocalDateTime ARM_ST_TM; // 알람 시작 시각
    private LocalDateTime ARM_ED_TM; // 알람 종료 시각
    private LocalDateTime today = LocalDateTime.now();
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
        // this.ORGNZT_ID = orgnztId;
        this.ORGNZT_ID = "002"; // (테스트)
    }

    public LocalDateTime getARM_ST_TM()
    {
        return ARM_ST_TM;
    }

    public void setARM_ST_TM(LocalDateTime ARM_ST_TM)
    {
        LocalDateTime newARM_ST_TM = LocalDateTime.of(
                today.getYear(),
                today.getMonth(),
                today.getDayOfMonth(),
                ARM_ST_TM.getHour(),
                ARM_ST_TM.getMinute(),
                0
        );
        this.ARM_ST_TM = newARM_ST_TM;
    }

    public LocalDateTime getARM_ED_TM()
    {
        return ARM_ED_TM;
    }

    public void setARM_ED_TM(LocalDateTime ARM_ED_TM)
    {
        LocalDateTime newARM_ED_TM = LocalDateTime.of(
                today.getYear(),
                today.getMonth(),
                today.getDayOfMonth(),
                ARM_ED_TM.getHour(),
                ARM_ED_TM.getMinute(),
                0
        );
        this.ARM_ED_TM = newARM_ED_TM;
    }

}
