package com.gongzetao.loop.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by baixinping on 2016/8/23.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    Context context;
    String dataBaseName;
    int version;

    /**
     *
     * @param context
     * @param dataBaseName 数据库名
     * @param factory
     * @param version
     */
    public DataBaseHelper(Context context, String dataBaseName,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dataBaseName, factory, version);
        this.context = context;
        this.dataBaseName = dataBaseName;
        this.version = version;
    }


    /**
     *数据库创建的时候会调用此方法
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
