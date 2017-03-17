package com.gongzetao.loop.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by baixinping on 2016/3/16.
 */
public class ToastUtils {
    public static void showToast(Context context, String msg){
        if("main".equals(Thread.currentThread().getName())){
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }else{
            Looper.prepare();
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
}
