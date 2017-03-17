package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.activity.LogonActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.utils.MotionEventUtils;
import com.gongzetao.loop.utils.SPUtils;
import com.gongzetao.loop.utils.ToastUtils;

/**
 * Created by baixinping on 2016/8/7.
 */
public class SettingDataPager extends HomeBasePager {
    public SettingDataPager(Activity activity) {
        super(activity);
    }

    RelativeLayout rl_resetPassword;
    RelativeLayout rl_unregisterLoad;

    @Override
    public void initData() {
        initTitle();
        initSettingUI();
    }

    private void initSettingUI() {
        iv_IconLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) mActivity).viewPager.setCurrentItem(3, false);
            }
        });
        fl_pager.removeAllViews();
        View settingDataView = View.inflate(mActivity, R.layout.layout_home_pager_setting_data, null);
        fl_pager.addView(settingDataView);
        rl_resetPassword = (RelativeLayout) settingDataView.findViewById(R.id.pager_setting_reset_password);
        rl_unregisterLoad = (RelativeLayout) settingDataView.findViewById(R.id.pager_setting_unregister_load);
        MotionEventUtils.setMotionEvent(rl_resetPassword,mActivity);
        MotionEventUtils.setMotionEvent(rl_unregisterLoad,mActivity);
        rl_resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVUser.requestPasswordResetInBackground(MyApplication.me.getEmail(), new RequestPasswordResetCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            ToastUtils.showToast(mActivity, "邮件发送成功，请到邮箱修改密码");
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        rl_unregisterLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, LogonActivity.class);
                mActivity.startActivity(intent);
                mActivity.finish();
                SPUtils.saveBoolean(LogonActivity.isLoading, false,mActivity);
            }
        });
    }
    private void initTitle() {
        tv_PagerDes.setText("设置");
        tv_PagerDes.setVisibility(View.VISIBLE);
        iv_IconAdd.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.INVISIBLE);
        iv_IconLast.setVisibility(View.VISIBLE);
    }
}
