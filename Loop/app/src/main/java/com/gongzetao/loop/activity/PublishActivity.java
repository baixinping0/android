package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.Remind;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.LivenessUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.lGNineGrideView.LGNineGrideView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/8/6.
 */
public class PublishActivity extends BaseActivity {

    //发布内容
    public static final int publish = 1;
    //发表评论
    public static final int comment = 2;
    //评论回复
    public static final int comment_reply = 3;
    //转发发布内容
    public static final int transmit = 4;

    public boolean isClickRemind = false;
    //@好友时的识别码
    public static final int remindFriendCode = 0;

    public static String str_type = "type";
    public static String str_publish_content_ser = "publish_content_ser";
    public static String str_publish_remind_user_ser = "publish_remind_user_ser";
    public static int type;

    int pictureCount;
    //发表内容的id
    String publishContentSer;
    AVObject publishContent;
    //要@的人
    String publishContentRemindUserSer;
    AVObject publishContentRemindUser;

    EditText et_content;
    LGNineGrideView gv_picture;
    TextView tv_title;
    FrameLayout fl_transmitContent;
    TextView tv_publish;

    private List<PhotoInfo> mPhotoList;
    List<String> pictureLocalUrlList = null;
    List<String> pictureServerUrlList = null;
    View faceView;

    String text;
    MyApplication app;

    List<AVObject> remindUserList;
    List<String> remindUserText;
    String remindText;

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                mPhotoList = new ArrayList<PhotoInfo>();
                mPhotoList.addAll(resultList);
                pictureLocalUrlList = new ArrayList<String>();
                for (int i = 0; i < mPhotoList.size(); i++) {
                    pictureLocalUrlList.add(mPhotoList.get(i).getPhotoPath());
                }
                gv_picture.setUrls(pictureLocalUrlList);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            ToastUtils.showToast(PublishActivity.this, "选择图片资源失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (MyApplication) getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_comment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initEmotion();
        initUI();
        initData();
    }

    private void initData() {

        //获取将要执行的操作类型
        type = getIntent().getIntExtra(str_type, 0);
        if (type == comment || type == comment_reply || type == transmit) {
            pictureCount = 1;
            //通过类型更新title
            if (type == transmit) {
                tv_title.setText("转发");
                et_content.setHint("转发心得...");
            } else {
                tv_title.setText("发表评论");
                findViewById(R.id.publish_icon_picture).setVisibility(View.GONE);
            }
            //获取传入的将要@的用户和转发或评论的发布内容
            publishContentSer = getIntent().getStringExtra(str_publish_content_ser);
            publishContentRemindUserSer = getIntent().getStringExtra(str_publish_remind_user_ser);
            try {
                publishContent = AVObject.parseAVObject(publishContentSer);
                publishContentRemindUser = AVObject.parseAVObject(publishContentRemindUserSer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //如果是转发，则初始化转发内容
            if (type == transmit) {
                initTransmit();
            }
        }

        if (type == publish) {
            pictureCount = 9;
            tv_title.setText("更新内容");
        }
        //对好友的评论进行回复
        if (type == comment_reply) {
            et_content.setHint("评论内容...");
            //添加将要@的人
            if (remindUserList == null)
                remindUserList = new ArrayList<AVObject>();
            remindUserList.add((AVUser) publishContentRemindUser);
            et_content.append(SpanableStringUtils.getInstace().getExpressionString(this, "@" + publishContentRemindUser.get(User.str_accountName) + " "));
        }
    }

    /**
     * //初始化转发内容
     */
    private void initTransmit() {
        //获取图片
        List<String> pictureUrls = (List<String>) publishContent.get(PublishContent.str_picture);
        //获取内容
        String text = (String) publishContent.get(PublishContent.str_text);
        //获取用户（要转发的publish的用户）
        AVUser user = (AVUser) publishContent.get(PublishContent.str_user);
        //初始化布局
        View view = View.inflate(this, R.layout.activity_publish_comment_transmit_item, null);
        ImageView iv_picture = (ImageView) view.findViewById(R.id.activity_publish_comment_transmit_item_iv_picture);
        TextView tv_text = (TextView) view.findViewById(R.id.activity_publish_comment_transmit_item_tv_text);
        //获取图片
        if (pictureUrls != null && pictureUrls.size() > 0)
            Glide.with(this).load(pictureUrls.get(0)).into(iv_picture);
        else {
            Glide.with(this).load((String) user.get(User.str_photo)).into(iv_picture);
        }
        tv_text.setText(text);
        //加载布局
        fl_transmitContent.setVisibility(View.VISIBLE);
        fl_transmitContent.addView(view);

        //添加将要@的人
        if (remindUserList == null)
            remindUserList = new ArrayList<AVObject>();
        remindUserList.add(user);
    }

    private void initUI() {
        gv_picture = (LGNineGrideView) findViewById(R.id.publish_gv_picture);
        et_content = (EditText) findViewById(R.id.et_message_frame);
        tv_title = (TextView) findViewById(R.id.publish_tv_title);
        fl_transmitContent = (FrameLayout) findViewById(R.id.publish_fl_transmit_content);
        faceView = findViewById(R.id.ll_facechoose);
        et_content.setFocusable(true);
        et_content.requestFocus();
        tv_publish = (TextView) findViewById(R.id.publish_tv_publish);
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if ("@".equals(s.subSequence(start, start + count).toString())) {
//                    Intent intent = new Intent(PublishActivity.this, RemindFriendsActivity.class);
//                    startActivityForResult(intent, remindFriendCode);
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.publish_tv_cancel:
                finish();
                break;
            case R.id.publish_tv_publish://点击发布
                //获取输入框中的内容
                text = et_content.getText().toString().trim();
                //若操作类型为发布动态，则直接发布动态
                if (type == publish || type == transmit) {
                    if (text == null || "".equals(text)) {
                        text = "";
                    }
                    publish();
                }
                //若操作类型为评论，则直接评论
                if (type == comment || type == comment_reply) {
                    if (text == null || "".equals(text)) {
                        ToastUtils.showToast(this, "内容为空");
                        return;
                    }
                    comment();
                }
                break;
            case R.id.publish_icon_picture:
                MediumUtils.openPhoto(this, mOnHanlderResultCallback,
                        mPhotoList, pictureCount);
                break;
            case R.id.publish_icon_remind_red:
                Intent intent = new Intent(this, RemindFriendsActivity.class);
                startActivityForResult(intent, remindFriendCode);
                break;
            case R.id.iv_icon_emoticon:
                // 隐藏表情选择框
                if (faceView.getVisibility() == View.VISIBLE) {
                    faceView.setVisibility(View.GONE);
                } else {
                    faceView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * 初始化
     */
    private void initEmotion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpanableStringUtils.getInstace().getFileText(PublishActivity.this);
            }
        }).start();
    }

    private void comment() {
        saveCommentInServer();
        Intent intent = new Intent();
        intent.putExtra(Comment.str_comment_text, text);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void saveCommentInServer() {
        //创建评论对象
        final AVObject object = new AVObject(Comment.str_comment);
        //所属说说对象
        object.put(Comment.str_publish, publishContent);
        //评论者
        object.put(Comment.str_comment_user, MyApplication.me);
        //被评论者
        object.put(Comment.str_commented_User, publishContent.get(PublishContent.str_user));
        //评论时间
        object.put(Comment.str_comment_time, System.currentTimeMillis());
        //评论内容
//        if (remindUserText != null){
//            for (int i = 0; i < remindUserText.size(); i++)
//                text.replaceAll("", remindUserText.get(i));
//        }
        object.put(Comment.str_comment_text, text);
        //是否被阅读
        object.put(Comment.isLook, false);
        //评论中提醒的对象
        object.put(Comment.commentRemindUser, remindUserList);
        //保存评论
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    ToastUtils.showToast(PublishActivity.this, "评论成功");

                    LivenessUtils.updataLiveness((AVUser) publishContent.get(PublishContent.str_user), Liveness.str_commented);
                    LivenessUtils.updataLiveness(MyApplication.me, Liveness.str_comment);

                    //保存提醒
                    saveRemind(publishContent, text);
                    publishContent.increment(PublishContent.str_comment_count);
                    publishContent.setFetchWhenSave(true);
                    //更新说说中的评论的次数
                    publishContent.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null)
                                LogUtils.MyLog(e.getCode() + "");
                        }
                    });
                    finish();
                } else {
                    ToastUtils.showToast(PublishActivity.this, "评论失败" + e.getCode());
                }
            }
        });
    }

    private void publish() {
        if (pictureLocalUrlList != null && pictureLocalUrlList.size() > 0) {
            //有图片则上传图片，上传完成后执行保存
            pictureServerUrlList = new ArrayList<String>();
            uploadPicture();
        } else {
            //没有图片则判断内容是否为空，不为空上传
            if ("".equals(text) && type == publish) {
                ToastUtils.showToast(this, "内容为空");
                return;
            }
            savePublishInServer();
        }
        completePublishClickReturn();
    }

    private void completePublishClickReturn() {
        Intent intent = new Intent();
        text = et_content.getText().toString().trim();
        et_content.setText("");
        if (type == publish) {
            intent.putExtra(PublishContent.str_text, text);
            intent.putStringArrayListExtra(PublishContent.str_picture, (ArrayList<String>) pictureLocalUrlList);
            intent.putExtra(PublishContent.str_is_transmit, false);
        }
        if (type == transmit) {
//            intent.putExtra(PublishContent.str_transmit_text, text);
            intent.putExtra(PublishContent.str_text, (String) publishContent.get(PublishContent.str_text));
            JSONArray jsonArray = (JSONArray) publishContent.get(PublishContent.str_picture);

            ArrayList<String> pictureUrls = new ArrayList<>();
            if (jsonArray != null)
                for (int i = 0; i < jsonArray.size(); i++) {
                    pictureUrls.add((String) jsonArray.get(i));
                }
            intent.putStringArrayListExtra(PublishContent.str_picture, pictureUrls);
            intent.putExtra(PublishContent.str_is_transmit, true);

            String currentUserName = (String) publishContentRemindUser.get(User.str_accountName);
            String currentTransmitText = (String) publishContent.get(PublishContent.str_transmit_text);
            intent.putExtra(PublishContent.str_transmited_user, publishContentRemindUser.toString());
            if (currentTransmitText == null || "".equals(currentTransmitText))
                intent.putExtra(PublishContent.str_transmit_text, text);
            else    //说说已经被转发过
                intent.putExtra(PublishContent.str_transmit_text, text + "  // @" + currentUserName + " :" + currentTransmitText);

        }
        setResult(RESULT_OK, intent);
        finish();
    }


    /**
     * //创建图片资源并上传,不断回调自己，用i作为结束条件
     * 图片上传
     * <p/>
     * pictureLocalUrlList：保存已经选择好的本地图片路径
     * pictureServerUrlList:保存图片返回的URL
     */
    private void uploadPicture() {
        for (int i = 0; i < pictureLocalUrlList.size(); i++) {
            AVFile file = null;
            try {
                File localFile = new File(pictureLocalUrlList.get(i));
                file = AVFile.withFile(localFile.getName(), localFile);
            } catch (FileNotFoundException e) {
                ToastUtils.showToast(this, "图片资源错误，请重新选择");
                return;
            }
            if (file != null) {
                final AVFile finalFile = file;
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        pictureServerUrlList.add(finalFile.getUrl());//将上传图片后返回的URL地址进行保存
                        if (pictureServerUrlList.size() == pictureLocalUrlList.size()) {
                            if (type == publish)
                                savePublishInServer();
                        }
                    }
                });
            }
        }

    }

    /**
     * 将发表内容上传服务器
     */
    private void savePublishInServer() {
        //查找当前用户
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");
        query.whereEqualTo(User.str_mail, app.accountNumber);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (e == null) {
                    final AVObject object = new AVObject("Publish");
                    //获取当前用户成功,将当前用户设置为说说的一个属性。
                    object.put(PublishContent.str_user, list.get(0));
                    object.put(PublishContent.str_praise_count, 0);
                    object.put(PublishContent.str_comment_count, 0);
                    object.put(PublishContent.str_transmit_count, 0);
                    if (type == transmit) {
                        object.put(PublishContent.str_is_transmit, true);
                        //将评论转发点赞次数设置为0
                        object.put(PublishContent.str_text, (String) publishContent.get(PublishContent.str_text));
                        List<String> pictureUrls = (List<String>) publishContent.get(PublishContent.str_picture);
                        if (pictureUrls != null && pictureUrls.size() > 0) {
                            object.put(PublishContent.str_picture, pictureUrls);
                        }
                        String currentUserName = (String) publishContentRemindUser.get(User.str_accountName);
                        String currentTransmitText = (String) publishContent.get(PublishContent.str_transmit_text);
                        object.put(PublishContent.str_transmited_user, publishContentRemindUser);
                        if (currentTransmitText == null || "".equals(currentTransmitText))
                            object.put(PublishContent.str_transmit_text, text);
                        else    //说说已经被转发过
                            object.put(PublishContent.str_transmit_text, text + "  // @" + currentUserName + " :" + currentTransmitText);
                    }
                    if (type == publish) {
                        object.put(PublishContent.str_is_transmit, false);
                        //将评论转发点赞次数设置为0
                        object.put(PublishContent.str_text, text);
                        object.put(PublishContent.str_transmit_text, "");
                        if (pictureServerUrlList != null && pictureServerUrlList.size() > 0) {
                            object.put(PublishContent.str_picture, pictureServerUrlList);
                        }
                    }
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                ToastUtils.showToast(PublishActivity.this, "发表成功");
                                LivenessUtils.updataLiveness(MyApplication.me, Liveness.str_publish);
                                //保存提醒
                                saveRemind(object, text);
                                if (type == transmit) {
                                    //更新转发次数
                                    publishContent.increment(PublishContent.str_transmit_count);
                                    publishContent.setFetchWhenSave(true);
                                    publishContent.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(AVException e) {

                                        }
                                    });
                                    LivenessUtils.updataLiveness((AVUser) publishContentRemindUser, Liveness.str_transmited);
                                    LivenessUtils.updataLiveness(MyApplication.me, Liveness.str_transmit);
                                }
                            } else {
                                ToastUtils.showToast(PublishActivity.this, "发表失败");
                            }
                        }
                    });
                }
            }
        });
    }

    private void saveRemind(final AVObject publishContent, final String et_content) {
        if (remindUserList != null)
            for (int i = 0; i < remindUserList.size(); i++) {
                //创建@对象
                AVObject object = new AVObject(Remind.remind);
                //被@的用户
                object.put(Remind.remindedUser, remindUserList.get(i));
                //主动@的用户3
                object.put(Remind.remindUser, MyApplication.me);
                //@的动态
                object.put(Remind.remindPublish, publishContent);
                //是否被阅读
                object.put(Remind.isLook, false);
                //保存@的类型
                object.put(Remind.remindType, type);
                //@的内容
                object.put(Remind.remindText, text);
                //保存@
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            ToastUtils.showToast(PublishActivity.this, "@成功");
                        } else {
                            ToastUtils.showToast(PublishActivity.this, "@失败" + e.getCode());
                        }
                    }
                });
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case remindFriendCode:
                if (resultCode == RESULT_OK) {
                    String userStr = data.getStringExtra(User.user);
                    AVUser user = null;
                    try {
                        user = (AVUser) AVObject.parseAVObject(userStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (user != null) {
                        if (remindUserList == null)
                            remindUserList = new ArrayList<AVObject>();
                        remindUserList.add(user);
                        String userName = (String) user.get(User.str_accountName);
//                        if (isClickRemind)
//                            et_content.getText().delete(et_content.getSelectionStart() - 1, et_content.getSelectionStart());
                        et_content.append(SpanableStringUtils.getInstace().getExpressionString(this, "@" + userName + " "));
                        if (remindUserText == null) {
                            remindUserText = new ArrayList<>();
                        }
                    }

                }
        }
    }
}
