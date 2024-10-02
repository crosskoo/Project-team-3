package com.jeyun.rhdms.handler.entity;

import java.io.Serializable;

// 복약
public class Pill implements Serializable
{
    public String DRUG_NO;   // 시퀀스
    public String SUBJECT_ID;   // 대상자 ID
    public String ARM_DT;   // 알람일
    public String ARM_TP;   // 알람 구분 [1:오전, 2:오후]
    public String ARM_ST_TM;   // 알람 시작 시간
    public String ARM_ED_TM;   // 알람 종료 시간
    public String TAKEN_ST;   // 복약 상태
    public String TAKEN_TM;   // 복약 시간
    public String APPLC_YN;   // 지문 인식 여부
    public String TAKEN_DT;   // 복약 일자
    public String DRG_WEIGHT;   // 복약 무게
    public String DRG_BEFORE;   // 복약 전 무게
    public String DRG_AFTER;   // 복약 후 무게
    public String PAPER;      // 실제 약 무게
    public int LOG_ID;      // 로그 ID
    public String REG_DT;   // 데이터 등록 날짜
    public String USE_YN;   // 사용 유무 [Y:사용, N:사용 안함]

    @Override
    public String toString() {
        return "Pill{" +
                "DRUG_NO='" + DRUG_NO + '\'' +
                ", SUBJECT_ID='" + SUBJECT_ID + '\'' +
                ", ARM_DT='" + ARM_DT + '\'' +
                ", ARM_TP='" + ARM_TP + '\'' +
                ", ARM_ST_TM='" + ARM_ST_TM + '\'' +
                ", ARM_ED_TM='" + ARM_ED_TM + '\'' +
                ", TAKEN_ST='" + TAKEN_ST + '\'' +
                ", TAKEN_TM='" + TAKEN_TM + '\'' +
                ", APPLC_YN='" + APPLC_YN + '\'' +
                ", TAKEN_DT='" + TAKEN_DT + '\'' +
                ", DRG_WEIGHT='" + DRG_WEIGHT + '\'' +
                ", DRG_BEFORE='" + DRG_BEFORE + '\'' +
                ", DRG_AFTER='" + DRG_AFTER + '\'' +
                ", PAPER='" + PAPER + '\'' +
                ", LOG_ID=" + LOG_ID +
                ", REG_DT='" + REG_DT + '\'' +
                ", USE_YN='" + USE_YN + '\'' +
                '}';
    }
}
