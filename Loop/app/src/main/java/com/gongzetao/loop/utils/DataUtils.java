package com.gongzetao.loop.utils;

import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by baixinping on 2016/8/13.
 */
public class DataUtils {

    public final static int TODAY = 0;
    public final static int YESTERDAY = -1;
    public final static int TDBY = -2;
    public final static int THIS_YEAR = 1;
    public final static int LTA = 1;

    public static String getNotDataTime(){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }
    public static String getDataTimeText(Date data){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getDataTimeTextNoH(Date data){
        String pattern = "yyyy年MM月dd日";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getTimeText(Date date) {
        String pattern = "HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    public static String getDataTimeTextNoYS(Date data){
        String pattern = "MM月dd日 HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getWeekTimeText(Date data){
        String pattern = "E HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getWeekText(Date data){
        String pattern = "EEE";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getDataTimeTextNoS(Date data){
        String pattern = "yyyy-MM-dd HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getDataTextNoY(Date data){
        String pattern = "MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }
    public static String getDataText(Date data){
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(data);
    }

    public static int getYear(Date data){
        String pattern = "yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return Integer.parseInt(format.format(data));
    }
    public static int getMonth(Date data){
        String pattern = "MM";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return Integer.parseInt(format.format(data));
    }
    public static int getDay(Date data){
        String pattern = "dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return Integer.parseInt(format.format(data));
    }
    public static int isRecent(Date date) {
        Calendar c = Calendar.getInstance();
        int year = getYear(date);
        int month = getMonth(date);
        int day = getDay(date);
        if(year==c.get(Calendar.YEAR)&&month==c.get(Calendar.MONTH)+1&&day==c.get(Calendar.DAY_OF_MONTH))
            return TODAY;
        else if(year==c.get(Calendar.YEAR)&&month==c.get(Calendar.MONTH)+1&&day==c.get(Calendar.DAY_OF_MONTH)-1)
            return YESTERDAY;
        else if(year==c.get(Calendar.YEAR)&&month==c.get(Calendar.MONTH)+1&&day==c.get(Calendar.DAY_OF_MONTH)-2)
            return TDBY;
        else if(year==c.get(Calendar.YEAR))
            return THIS_YEAR;
        return LTA;
    }
    public static boolean isWeeks(Date date) {
        Calendar c = Calendar.getInstance();
        int year = getYear(date);
        int month = getMonth(date);
        int day = getDay(date);
        if(year==c.get(Calendar.YEAR)&&month==c.get(Calendar.MONTH)+1&&c.get(Calendar.DAY_OF_MONTH)-day<7)
            return true;
        return false;

    }
}
