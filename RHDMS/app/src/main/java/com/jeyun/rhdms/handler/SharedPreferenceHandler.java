package com.jeyun.rhdms.handler;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHandler
{
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_ORGNZT_ID = "orgnztId";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPreferenceHandler(Context context)
    {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveOrgnztId(String orgnztId) // orgnztId 저장
    {
        editor.putString(KEY_ORGNZT_ID, orgnztId);
        editor.commit();
    }

    public void clearAll() // 모든 데이터 삭제
    {
        editor.clear();
        editor.commit();
    }

    public String getSavedOrgnztId() // 저장된 orgnztId 불러오기 (없으면 null 리턴)
    {
        return sharedPreferences.getString(KEY_ORGNZT_ID, null);
    }
}
