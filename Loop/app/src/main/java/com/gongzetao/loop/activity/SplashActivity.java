package com.gongzetao.loop.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogInCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.UpdateApkData;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.InterLinkAssist;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.NetWorkUtils;
import com.gongzetao.loop.utils.SPUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    RelativeLayout rl_root;
    TextView mTv_version;
    int newVersionCode;
    String newVersionName;
    String newVersionScription;
    String newVersionHttp;
    TextView mTv_showProgress;

    AVObject update;

    public static final int ERROR_INTERNET = 1;
    public static final int SHOW_UPDATE_DIALOG = 2;
    public static final int ENTER_HOME = 3;
    int what = 0;
    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR_INTERNET:
                    enterLogon();
                    break;
                case SHOW_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    enterLogon();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        initUI();
        if (!(NetWorkUtils.isNetworkAvailable(this) || NetWorkUtils.ping())) {
            ToastUtils.showToast(this, "网络不可用");
        }
        checkVersion();
    }

    private void initUI() {
        rl_root = (RelativeLayout) findViewById(R.id.splash_rl_root);

        getBackgroundForServer();
        mTv_version = (TextView) findViewById(R.id.activity_splash_tv_version);
        mTv_showProgress = (TextView) findViewById(R.id.activity_splash_tv_show_downLoad_progress);
        String versionName = getVersionName();
        mTv_version.setText(versionName);
        //加入渐变动画
//        AlphaAnimation anim = new AlphaAnimation(0.2f, 1f);
//        anim.setDuration(3000);
//        rl_root.startAnimation(anim);
    }


    /**
     * 获取背景图并加载
     */
    public void getBackgroundForServer() {
        BitmapUtils utils;
        utils = new BitmapUtils(this);
        utils.configDefaultLoadingImage(R.drawable.loop_jump);

        ImageView im = new ImageView(this);
        utils.display(im, InterLinkAssist.SPLASH_LOGO_IMAGE);
        rl_root.setBackground(im.getDrawable());
    }

    public void enterLogon() {
        Intent intent = null;
        String localMail = SPUtils.getString(User.str_mail, this);
        String localPassword = SPUtils.getString(User.str_password, this);
        //说明缓存中有账号,并且账号登陆过
        if ((localMail != null && !"".equals(localMail))
                && SPUtils.getBoolean(LogonActivity.isLoading, this)) {
            logon(localMail, localPassword);
        } else {
            //否则跳转到登录假面，
            Intent logonIntent = new Intent(this, LogonActivity.class);
            startActivity(logonIntent);
            this.finish();
        }

    }

    /**
     * 用户登录
     */
    private void logon(final String mail, final String password) {
        AVUser.logInInBackground(mail, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e == null) {
                    //本地缓存登录账号密码
                    SPUtils.saveString(User.str_mail, mail, SplashActivity.this);
                    SPUtils.saveString(User.str_password, password, SplashActivity.this);
                    MyApplication.accountNumber = mail;
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    intent.putExtra(User.str_mail, MyApplication.accountNumber);
                    startActivity(intent);
                    finish();
                    SPUtils.saveBoolean(LogonActivity.isLoading, true, SplashActivity.this);
                } else {
                    ToastUtils.showToast(SplashActivity.this, "登录失败");
                    Intent logonIntent = new Intent(SplashActivity.this, LogonActivity.class);
                    startActivity(logonIntent);
                    SplashActivity.this.finish();
                }
            }
        });
    }

    public void checkVersion() {
        final long startTime = System.currentTimeMillis();
        AVQuery<AVObject> updateQuery = new AVQuery<AVObject>("Update");//创建查询对象
        //先从缓存加载，加载失败再从网络加载
        updateQuery.whereEqualTo("type", "versionUpdate");
        updateQuery.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        updateQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        update = list.get(0);
                        parseData(update);
                        long endTime = System.currentTimeMillis();
                        if (endTime - startTime <= 2000)
                            handler.sendEmptyMessageDelayed(what, 2000);
                        else
                            handler.sendEmptyMessage(what);
                    } else {
                        //没有更新
                        what = ENTER_HOME;
                        long endTime = System.currentTimeMillis();
                        if (endTime - startTime <= 2000)
                            handler.sendEmptyMessageDelayed(what, 2000);
                        else
                            handler.sendEmptyMessage(what);
                    }
                } else {
                    what = ERROR_INTERNET;
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime <= 2000)
                        handler.sendEmptyMessageDelayed(what, 2000);
                    else
                        handler.sendEmptyMessage(what);
                }
            }
        });
    }

    //解析数据
    private void parseData(AVObject date) {
        newVersionName = (String) date.get(UpdateApkData.versionName);
        newVersionCode = (Integer) date.get(UpdateApkData.versionCode);
        newVersionScription = (String) date.get(UpdateApkData.versionDescription);
        newVersionHttp = (String) date.get(UpdateApkData.downloadURL);
        if (newVersionCode > getVersionCode()) {
            what = SHOW_UPDATE_DIALOG;
        } else {
            what = ENTER_HOME;
        }
    }

    /**
     * 获取当前版本名和版本号
     */
    //******************************************************************
    public String getVersionName() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            String versionName = pi.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getVersionCode() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            int versionCode = pi.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 弹出对话框
     */
    private void showUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog dialog = builder.create();
        builder.setTitle("版本更新");
        builder.setMessage("最新版本" + newVersionName + ":" + newVersionScription);
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //开始更新
                update();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消后，初始化数据，进入主界面
                enterLogon();
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void update() {
        //判断sd卡是否有下载空间
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String targetDir = Environment.getExternalStorageDirectory() + "/update.apk";
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.download(newVersionHttp, targetDir, new RequestCallBack<File>() {
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    mTv_showProgress.setVisibility(View.VISIBLE);
                    mTv_showProgress.setText(current + "/" + total);
                }

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    ToastUtils.showToast(SplashActivity.this, "下载成功");
                    //下载成功自动跳到系统安装页面
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.fromFile(responseInfo.result),
                            "application/vnd.android.package-archive");
                    //startActivity(intent);
                    startActivityForResult(intent, 0);//用户在安装的时候要是点击取消，会调用onActivityResult()方法
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    ToastUtils.showToast(SplashActivity.this, "下载失败");
                    enterLogon();
                    LogUtils.MyLog("下载链接" + newVersionHttp);
                }
            });
        }
    }
}
