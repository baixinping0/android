package com.gongzetao.loop.base;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.activity.PublishActivity;
import com.gongzetao.loop.application.MyApplication;

/**
 * Created by baixinping on 2016/8/5.
 */
public class HomeBasePager {

    public Activity mActivity;
    public View view;
    public TextView tv_PagerDes;
    public ImageView iv_IconAdd;
    public ImageView iv_IconLast;
    public FrameLayout fl_pager;
    public EditText et_LookupFrame;
    public MyApplication app;

    public HomeBasePager(Activity activity){
        this.mActivity = activity;
        app = (MyApplication) mActivity.getApplication();
        initUI();
        setOnClickListener();
    }

    private void setOnClickListener() {
        iv_IconAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent publishIntent = new Intent(mActivity, PublishActivity.class);
                publishIntent.putExtra(PublishActivity.str_type, PublishActivity.publish);
                mActivity.startActivityForResult(publishIntent, HomeActivity.REQUEST_RESULT_CODE_PUBLISH);
            }
        });
    }


    private void initUI() {
        view = View.inflate(mActivity, R.layout.layout_homebasepager,null);
        tv_PagerDes = (TextView) view.findViewById(R.id.home_tv_pager_des);
        iv_IconAdd = (ImageView) view.findViewById(R.id.home_iv_icon_add);
        iv_IconLast = (ImageView) view.findViewById(R.id.home_iv_icon_last);
        fl_pager = (FrameLayout) view.findViewById(R.id.home_fl_pager);
        et_LookupFrame = (EditText) view.findViewById(R.id.home_et_lookup_frame);
    }

    public void initData(){

    }


}
