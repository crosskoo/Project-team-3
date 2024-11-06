package com.jeyun.rhdms.handler.entity;

public class User
{
    private String EMPLYR_ID; // 사용자 ID
    private String ORGNZT_ID; // 기관 번호 (대상 환자 번호와 동일)
    private String USER_NM; // 사용자 이름
    private String PASSWORD; // 비밀번호 (암호화된)

    public String getEMPLYR_NO() {
        return EMPLYR_ID;
    }

    public String getORGNZT_ID() {
        return ORGNZT_ID;
    }
}
