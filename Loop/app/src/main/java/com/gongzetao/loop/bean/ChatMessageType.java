package com.gongzetao.loop.bean;

/**
 * Created by baixinping on 2016/8/23.
 */
public class ChatMessageType {
    public  static final int MESSAGE_TYPE = 4;

    public static final int TEXT = 1;
    public static final int IMAGE = 2;

    //用于区别接受到消息的时候是属于图片还是属于文本
    //type：用于传递消息是标明消息的属性所用的key
    public static final String STR_TYPE = "type";
    public static final String STR_TYPE_TEXT = "text";
    public static final String STR_TYPE_IMAGE = "image";


    public static final String STR_USER_INFO = "userInto";




    //消息的类型：在消息展示的时候用于区别消息的类型
    public  static final int MESSAGE_TYPE_TO_TEXT = 1001;
    public  static final int MESSAGE_TYPE_FROM_TEXT = 1002;
    public  static final int MESSAGE_TYPE_TO_IMAGE = 2001;
    public  static final int MESSAGE_TYPE_FROM_IMAGE = 2002;


}
