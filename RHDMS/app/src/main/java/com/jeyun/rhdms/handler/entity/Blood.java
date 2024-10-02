package com.jeyun.rhdms.handler.entity;

import androidx.annotation.NonNull;

public class Blood
{
    public Integer itf_no;
    public Integer SUBJECTID;
    public String pillbox_id;
    public String device_tp;   // 2:혈압, 3:혈당
    public String mesure_tp;   // 21:혈압, 31:혈당
    public String mesure_de;   // 측정일
    public String mesure_tm;   // 측정시간
    public String mesure_val;  // 측정값
    public String itf_tp;      // 인터페이스 처리 구분
    public String reg_dt;      // 등록일
    public String udt_dt;      // 수정일
    public String log_id;
    public String itf_yn;


    @Override
    public String toString() {
        return "Blood{" +
                "itf_no=" + itf_no +
                ", SUBJECTID=" + SUBJECTID +
                ", pillbox_id='" + pillbox_id + '\'' +
                ", device_tp='" + device_tp + '\'' +
                ", mesure_tp='" + mesure_tp + '\'' +
                ", mesure_de='" + mesure_de + '\'' +
                ", mesure_tm='" + mesure_tm + '\'' +
                ", mesure_val='" + mesure_val + '\'' +
                ", itf_tp='" + itf_tp + '\'' +
                ", reg_dt='" + reg_dt + '\'' +
                ", udt_dt='" + udt_dt + '\'' +
                ", log_id='" + log_id + '\'' +
                ", itf_yn='" + itf_yn + '\'' +
                '}';
    }
}
