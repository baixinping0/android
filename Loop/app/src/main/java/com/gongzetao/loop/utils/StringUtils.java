package com.gongzetao.loop.utils;

/**
 * Created by baixinping on 2016/8/16.
 */
public class StringUtils {

    public static String encode(String str){
      return  str.replace("@","_").replace(".","-");
    }
    public static String unEncode(String str){
        return str.replace("_","@").replace("-",".");
    }
}
