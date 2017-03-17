package com.gongzetao.loop.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by baixinping on 2016/8/12.
 */
public class SPUtils {
    static SharedPreferences preference;
    public static void initSP(Context context){
        preference = context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }
    public static String getString(String key, Context context){
        initSP(context);
        String value = preference.getString(key, "").trim();
        return value;
    }
    public static void saveString(String key, String value, Context context){
        initSP(context);
        preference.edit().putString(key, value).commit();
    }
    public static boolean getBoolean(String key, Context context){
        initSP(context);
        boolean value = preference.getBoolean(key, false);
        return value;
    }
    public static void saveBoolean(String key, boolean value, Context context){
        initSP(context);
        preference.edit().putBoolean(key, value).commit();
    }


}
