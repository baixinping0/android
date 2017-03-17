package com.gongzetao.loop.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gongzetao.loop.utils.StatusBarUtils;

/**
 * Created by baixinping on 2016/8/28.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.initSystemBar(this);
//        StatusBarUtils.setWindowStatusBarColor(this, R.color.myBlueTop);
    }
}
