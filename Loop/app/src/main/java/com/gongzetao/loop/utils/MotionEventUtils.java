package com.gongzetao.loop.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.gongzetao.loop.R;

/**
 * Created by yulinbin on 2016/9/17.
 */
public class MotionEventUtils {
    public static void setMotionEvent(View v,final Context context){
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        v.setBackgroundColor(context.getResources().getColor(R.color.lineColor));
                        return false;
                    case (MotionEvent.ACTION_MOVE) :
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        v.setBackgroundColor(Color.WHITE);
                        return false;
                    case (MotionEvent.ACTION_CANCEL) :
                        v.setBackgroundColor(Color.WHITE);
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :

                        return true;
                    default :
                        break;
                }
                return false;
            }
        });
    }
}
