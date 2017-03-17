package com.gongzetao.loop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.CacheUtils;
import com.gongzetao.loop.utils.SPUtils;
import com.gongzetao.loop.utils.StatusbarsUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.sortlistview.ClearEditText;
import com.gongzetao.loop.view.sortlistview.EmailAutoCompleteTextView;

/**
 * Created by baixinping on 2016/8/4.
 */
public class LogonActivity extends BaseActivity {

    MyApplication app;
    Context context = this;
    EmailAutoCompleteTextView et_mail;
    EditText et_password;
    ImageView iv_eyes;

    String password;
    public static final String isLoading = "isLoading";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        app = (MyApplication) getApplication();
        setContentView(R.layout.activity_logon);
        StatusbarsUtils.setStatusbars(R.color.statusbars, this);//设置状态栏颜色
        initUI();
    }



    private void initUI() {
        et_mail = (EmailAutoCompleteTextView) findViewById(R.id.logon_et_account_number);
        iv_eyes = (ImageView) findViewById(R.id.password_eyes);
        et_password = (EditText) findViewById(R.id.logon_et_password);
        String localMail = SPUtils.getString(User.str_mail, this);
        String localPassword = SPUtils.getString(User.str_password, this);
        et_mail.setText(localMail);
        et_password.setText(localPassword);
        iv_eyes.setImageResource(R.drawable.password_eyes_normal);
        iv_eyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_eyes.setImageResource(R.drawable.password_eyes_click);
                } else {
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_eyes.setImageResource(R.drawable.password_eyes_normal);
                }
            }
        });

        et_mail.setOnClearSucceed(new ClearEditText.OnClearSucceed() {
            @Override
            public void clearSucceed() {
                et_password.setText("");
            }
        });
        //去掉X
        et_mail.setClearIconVisible(false);
    }
    public void onclick(View view) {
        Intent intent = null;
//        view.gett
        switch (view.getId()) {
            case R.id.logon_tv_forget_password:
                intent = new Intent(this, ApplicationAttestationActivity.class);
                startActivity(intent);
                break;
            case R.id.logon_bt_logon:
                if (isInputOk())
                    logon();
                break;
            case R.id.logon_tv_register:
                intent = new Intent(this, RegisterActivity.class);
                if (intent != null)
                    startActivity(intent);
                break;
        }

    }

    private boolean isInputOk() {
        app.accountNumber = et_mail.getText().toString().trim();
        password = et_password.getText().toString().trim();
        if ("".equals(app.accountNumber) || app.accountNumber == null) {
            ToastUtils.showToast(context, "请输入账号");
            return false;
        }
        if ("".equals(password) || password == null) {
            ToastUtils.showToast(context, "请输入密码");
            return false;
        }
        return true;
    }


    /**
     * 用户登录
     */
    private void logon() {
//        Log.e("qwer", "  mail " + mail + "pass  " + password);
        AVUser.logInInBackground(app.accountNumber, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e == null) {
                    //本地缓存登录账号密码
                    SPUtils.saveString(User.str_mail , app.accountNumber, LogonActivity.this);
                    SPUtils.saveString(User.str_password , password, LogonActivity.this);
                    Intent intent = new Intent(LogonActivity.this, HomeActivity.class);
                    intent.putExtra(User.str_mail, app.accountNumber);
                    startActivity(intent);
                    finish();
                    //获取好友列表，获取列表好友记录
                    CacheUtils.getInfoFriendMailList((MyApplication) getApplication());
                    //保存登陆
                    SPUtils.saveBoolean(isLoading, true, LogonActivity.this);

                } else {
                    if (e.getCode() == 211)
                        ToastUtils.showToast(LogonActivity.this, "用户不存在");
                    else if (e.getCode() == 202)
                        ToastUtils.showToast(LogonActivity.this, "用户名和密码不匹配");
                    else if(e.getCode() == 0)
                        ToastUtils.showToast(LogonActivity.this, "请检查网络连接");
                    ToastUtils.showToast(LogonActivity.this, "登陆失败");
                }
            }
        });
    }



}
