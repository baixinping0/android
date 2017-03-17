package com.gongzetao.loop.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.ChatMessage;
import com.gongzetao.loop.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/8/24.
 */
public class DateBaseUtils {
    MyApplication application;

    public DateBaseUtils(MyApplication application) {
        this.application = application;
    }

    /**
     * 获取一个好友的聊天信息
     * @param userInfo 好友信息
     * @param count 获取条数
     * @param count 从第fromIndex条数据开始获取
     * @return
     */

    public List<ChatMessage> getOneChatMessage(String userInfo, int count, int fromIndex) {
        Log.e("qwer", "开始查询数据");
        AVUser user = null;
        try {
            user = (AVUser) AVObject.parseAVObject(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fromAccountNumber = (String) user.get(User.str_mail);
        List<ChatMessage> list = new ArrayList<ChatMessage>();
        String tableName = ("chatMessage" + fromAccountNumber).replace("@","").replace(".","");

        //创建数据表,当数据表不存在的时候创建
//        String SQL = "select top " + count
//                + " messageType,messageTime,messageContent from "+ tableName
//                +" where type ='table'"
//                + " order by messageTime desc ;";
//        ASC|DESC]]；


        //执行操作查询数据
//        Cursor cursor = application.sqLiteDatabase.rawQuery(SQL, null);
        if (!tabIsExist(tableName)){
            return new ArrayList<ChatMessage>();
        }
        Cursor cursor = application.sqLiteDatabase.query(tableName, new String[]{"messageType",
                "messageTime","messageContent"},null,null,null,null,"messageTime desc", count + "");
        if (cursor != null){

            if(cursor.moveToPosition(fromIndex)){
                do{
                    int messageType = cursor.getInt(0);
                    long messageTime = cursor.getLong(1);
                    String messageContent = cursor.getString(2);
                    list.add(0,new ChatMessage(messageType, messageTime, messageContent));
                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return list;
    }

    /**
     * 将数据保存到数据库中,
     * 对于每个朋友都要有一个数据库用于保存和这个用户的聊天记录
     * @param userInfo
     * @param message
     */
    public static void saveChatMessageInDataBase(final AVUser userInfo, ChatMessage message) {

        String fromAccountNumber = (String) userInfo.get(User.str_mail);

        //将消息保存数据库中
        String tableName = ("chatMessage" + fromAccountNumber).replace("@","").replace(".","");

        //创建数据表,当数据表不存在的时候创建
        String SQL = "create table if not exists " + tableName + "( "
                +"id integer primary key autoincrement,"
                + "messageType int,"
                + "messageTime long,"
                + "messageContent )";
        //执行操作创建数据表
        MyApplication.sqLiteDatabase.execSQL(SQL);

        ContentValues content = new ContentValues();
        content.put("messageType", message.getType());
        content.put("messageTime", message.getTime());
        content.put("messageContent", message.getContent());

        MyApplication.sqLiteDatabase.insert(tableName, null, content);
    }

    /**
     * 判断某张表是否存在
     * @param tabName 表名
     * @return
     */
    public boolean tabIsExist(String tabName){
        boolean result = false;
        if(tabName == null){
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tabName.trim()+"' ";
            cursor = application.sqLiteDatabase.rawQuery(sql, null);
            if(cursor.moveToNext()){
                int count = cursor.getInt(0);
                if(count>0){
                    result = true;
                }
            }

        } catch (Exception e) {

        }
        return result;
    }


}
