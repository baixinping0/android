package com.gongzetao.loop.bean;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by baixinping on 2016/8/13.
 */
public class DataBaseUtils {
    public  void saveMessage(Context context,String friendNumber, ChatMessage message){
        //通过friendNumber找到对应的表，将信息存入表中。
        //信息：时间，内容，类型
        SQLiteDatabase db =context.openOrCreateDatabase("test_db.db", context.MODE_PRIVATE, null);
//        db.
        MyDatabaseUtil myDatabaseUtil = new MyDatabaseUtil(context, "db1.db", null, 1);
//
//        db1 = this.openOrCreateDatabase("db1.db", Context.MODE_PRIVATE, null);
    }

    //判断数据库中表是否存在
    public class MyDatabaseUtil extends SQLiteOpenHelper {
        public MyDatabaseUtil(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
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
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = this.getReadableDatabase();//此this是继承SQLiteOpenHelper类得到的
                String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"+tabName.trim()+"' ";
                cursor = db.rawQuery(sql, null);
                if(cursor.moveToNext()){
                    int count = cursor.getInt(0);
                    if(count>0){
                        result = true;
                    }
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
            return result;
        }

    }
}
