package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gongzetao.loop.R;
import com.gongzetao.loop.base.BaseActivity;

/**
 * Created by baixinping on 2016/8/4.
 */
public class IdentifyActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);
    }
    public void onclick(View view){
        Intent intent = null;
        switch (view.getId()){
            case R.id.identity_bt_register_finish:
                break;
            case R.id.identity_tv_last:
                break;
        }
        if (intent != null)
             startActivity(intent);
        finish();
    }
}
