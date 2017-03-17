package com.gongzetao.loop.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.SPUtils;
import com.gongzetao.loop.utils.StatusbarsUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.sortlistview.ClearEditText;
import com.lidroid.xutils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/8/4.
 */
public class RegisterActivity extends BaseActivity {

    public static final int labelRequestCode = 0;
    public static final String label = "label";
//    CircleImageView iv_photo;
    ClearEditText et_mail;
    EditText et_name;
    EditText et_password;
    EditText et_table;

    String mail;
    String name;
    String password;
    String table;
    String photo;
    MyApplication app;
    private int position;


//    private List<PhotoInfo> mPhotoList;
//    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
//        @Override
//        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
//            if (resultList != null) {
//                mPhotoList = new ArrayList<>();
//                mPhotoList.addAll(resultList);
//                BitmapUtils utils = new BitmapUtils(RegisterActivity.this);
//                utils.display(iv_photo, mPhotoList.get(0).getPhotoPath());
//            }
//        }
//
//        @Override
//        public void onHanlderFailure(int requestCode, String errorMsg) {
//            ToastUtils.showToast(RegisterActivity.this, "选择图片资源失败");
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        app = (MyApplication) getApplication();
        StatusbarsUtils.setStatusbars(R.color.statusbars, this);
        initUI();
    }


    private void initUI() {
//        iv_photo = (CircleImageView) findViewById(R.id.register_iv_photo);
        et_mail = (ClearEditText) findViewById(R.id.register_et_mail);
        et_name = (EditText) findViewById(R.id.register_et_name);
        et_password = (EditText) findViewById(R.id.register_et_password);
        et_table = (EditText) findViewById(R.id.register_et_table);
        et_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterRecommendActivity(RegisterActivity.this, labelRequestCode);
            }
        });
    }

    public void onclick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.register_bt_register:
                //注册
                register();
                break;
            case R.id.register_tv_last:
                finish();
                break;
//            case R.id.register_iv_photo:
////                MediumUtils.openPhoto(this, mOnHanlderResultCallback, mPhotoList, 1);
//                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (resultCode == RESULT_OK){
           switch (requestCode){
               case labelRequestCode:
                   if (data != null){
                       String label = data.getStringExtra("label");
                       et_table.setText(label);
                       position = data.getIntExtra("position",0);
                   }
           }
       }

    }

    /**
     * 用户点击注册时的操作
     */

    private void register() {
        mail = et_mail.getText().toString().trim();
        name = et_name.getText().toString().trim();
        password = et_password.getText().toString().trim();
        table = et_table.getText().toString().trim();

        if (mail == null || "".equals(mail)) {
            ToastUtils.showToast(this, "请输入账号");
            return;
        }
        if (!mail.matches("^([a-z_0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")) {
            ToastUtils.showToast(this, "请输入正确的邮箱地址");
            return;
        }
        if (name == null || "".equals(name)) {
            ToastUtils.showToast(this, "请输入姓名");
            return;
        }
        if (password == null || "".equals(password)) {
            ToastUtils.showToast(this, "请输入密码");
            return;
        }
        if (table == null || "".equals(table)) {
            ToastUtils.showToast(this, "请输入标签");
            return;
        }
//        if (isSelectPhoto()) {
//            //选择了照片
//            //上传
//            uploadPhoto();
//        } else
        foundUserAccount();
    }

    /**
     * 是否选择照片
     *
     * @return
     */
//    private boolean isSelectPhoto() {
//        if (mPhotoList != null && mPhotoList.size() > 0)
//            return true;
//        return false;
//    }


//    private void uploadPhoto() {
//        //创建图片资源并上传
//        File file = new File(mPhotoList.get(0).getPhotoPath());
//        AVFile avFile = null;
//        try {
//            avFile = AVFile.withAbsoluteLocalPath(file.getName(), file.getAbsolutePath());
//        } catch (FileNotFoundException e) {
//            ToastUtils.showToast(this, "图片资源错误，请重新选择");
//            return;
//        }
//        if (avFile != null) {
//            final AVFile finalFile = avFile;
//            avFile.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(AVException e) {
//                    //上传成功
//                    photo = finalFile.getUrl();
//                    foundUserAccount();
//                }
//            });
//        }
//    }

    /**
     * 创建账号
     */
    private void foundUserAccount() {
        String[] categoryName = new String[]
                {"其他", "出售", "出租", "韩剧", "游戏", "动漫", "运动",
                        "音乐", "电影", "明星", "电视剧", "家乡", "学习", "活动"};
        final AVUser user = new AVUser();// 新建 User 对象实例
        user.setUsername(mail);// 设置用户名
        user.setPassword(password);// 设置密码
        user.setEmail(mail);// 设置邮箱
        user.put(User.str_table, table);//设置标签
        user.put(User.str_accountName, name);//设置账户名
        user.put(User.str_attestation, "");
        user.put(User.str_category,categoryName[position]);
        if (photo != null)
            user.put(User.str_photo, photo);//设置头像
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 注册成功
                    createLiveness(user);//创建活跃度表
                    ToastUtils.showToast(RegisterActivity.this, "创建账号成功，请前往邮箱进行验证");
                    defineRcommend(user);
                    logon();
                } else {
                    if (e.getCode() == 203)
                        ToastUtils.showToast(RegisterActivity.this, "邮箱已存在");
                    else if (e.getCode() == 202)
                        ToastUtils.showToast(RegisterActivity.this, "用户名已存在");
                    else if (e.getCode() == 0)
                        ToastUtils.showToast(RegisterActivity.this, "请检查网络连接");
                    else
                        ToastUtils.showToast(RegisterActivity.this, e.getCode() + "");
                }
            }
        });

    }

    private void createLiveness(AVUser avUser) {
        final AVObject liveness = new AVObject(Liveness.str_liveness);
        liveness.put(Liveness.str_user,avUser);
        liveness.put(Liveness.str_comment,0);
        liveness.put(Liveness.str_follower,0);
        liveness.put(Liveness.str_praise,0);
        liveness.put(Liveness.str_praiseed,0);
        liveness.put(Liveness.str_commented,0);
        liveness.put(Liveness.str_livenessCount,0);
        liveness.put(Liveness.str_transmit,0);
        liveness.put(Liveness.str_publish,0);
        liveness.put(Liveness.str_transmited, 0);
        liveness.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    LogUtils.MyLog("Liveness创建成功");
                }
                else {
                    LogUtils.MyLog("baocun liveness " + e.toString());
                }
            }
        });
    }
//    private void saveLable() {
//        String[] categoryName = new String[]
//                {"其他", "出售", "出租", "韩剧", "游戏", "动漫", "运动",
//                        "音乐", "电影", "明星", "电视剧", "家乡", "学习", "活动"};
//        final AVObject label = new AVObject("Label");
//        label.put("category1",categoryName[position]);
//        label.put("lableContent", table);
//        label.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(AVException e) {
//                if (e == null) {
//                    foundUserAccount();
//                } else {
//                    ToastUtils.showToast(RegisterActivity.this, "保存标签内容失败");
//                }
//            }
//        });
//    }

    private void defineRcommend(final AVUser self) {
        AVQuery<AVObject> query = new AVQuery<>("DefineRcommend");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list != null) {
                        for (AVObject object : list
                                ) {
                            String userId = (String) object.get("userId");
                            if (!"null".equals(userId)) {
                                self.followInBackground(userId, new FollowCallback() {
                                    @Override
                                    public void done(AVObject object, AVException e) {
                                        if (e == null) {
//                                          //关注成功
                                        } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                                            LogUtils.MyLog("关注失败");
                                        } else {
                                            LogUtils.MyLog(e.toString() + "..." + e.getCode());
                                        }
                                    }
                                });
                                LogUtils.MyLog("-->" + userId);
                            }
                        }
                    }
                } else {
                    LogUtils.MyLog("查询异常" + e.toString());
                }
            }
        });
    }

    /**
     * 用户登录
     */
    private void logon() {
        AVUser.logInInBackground(mail, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e == null) {
                    //本地缓存登录账号密码
                    SPUtils.saveString(User.str_mail, mail, RegisterActivity.this);
                    SPUtils.saveString(User.str_password, password, RegisterActivity.this);
                    app.accountNumber = mail;
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    intent.putExtra(User.str_mail, app.accountNumber);
                    startActivity(intent);
                    finish();
                    SPUtils.saveBoolean(LogonActivity.isLoading, true, RegisterActivity.this);
                } else {
                    if (e.getCode() == 211)
                        ToastUtils.showToast(RegisterActivity.this, "用户不存在");
                    else if (e.getCode() == 202)
                        ToastUtils.showToast(RegisterActivity.this, "用户名和密码不匹配");
                }
            }
        });
    }
}
