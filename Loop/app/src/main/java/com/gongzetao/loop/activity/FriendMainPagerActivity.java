package com.gongzetao.loop.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.FriendMainPagerAdapter;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.StatusbarsUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.RefreshListView;
import com.lidroid.xutils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/9/2.
 */
public class FriendMainPagerActivity extends AppCompatActivity {

    ImageView iv_background;
    CircleImageView iv_photo;
    TextView tv_name;

    ImageView iv_last;
    TextView tv_title;
    RelativeLayout rl_title;
//    View title;

    LinearLayout ll_friend;

    RelativeLayout rl_attention;
    TextView tv_attention;
    LinearLayout tv_attentionShe;
    TextView tv_sendMessage;
    TextView tv_phone;
    LinearLayout tv_sheAttention;
    TextView tv_table;
    TextView tv_attestation;
    TextView tv_sex;
    TextView tv_bornTime;
    TextView tv_sheAttentionCount;
    TextView tv_attentionSheCount;

    RefreshListView lv_friendPublish;
    List<AVObject> friendPublishs;
    List<AVUser> followeeList;
    AVUser user;
    String str_user;
    public static final String ser_user_info = "serUserInfo";

    FriendMainPagerAdapter adapter;

    public static final int REFRESH = 0;
    public static final int ADD = 1;

    private boolean isFirstEnter = true;
    int skip = 0;

    private List<PhotoInfo> mPhotoList;


    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                if (mPhotoList == null)
                    mPhotoList = new ArrayList<PhotoInfo>();
                mPhotoList.addAll(resultList);
                BitmapUtils utils = new BitmapUtils(FriendMainPagerActivity.this);
                utils.display(iv_background, mPhotoList.get(0).getPhotoPath());
                //上传保存图像
                savePhotoInServer();
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            ToastUtils.showToast(FriendMainPagerActivity.this, "选择图片资源失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_main_pager);

        initUI();
        initData();
        initHeader();
        getContentData(REFRESH);
        StatusbarsUtils.setStatusbars(R.color.myBlueTop, this);
    }


    private void savePhotoInServer() {
        final File file = new File(mPhotoList.get(0).getPhotoPath());
        AVFile avFile = null;
        try {
            avFile = AVFile.withFile(file.getName(), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final AVFile finalFinal = avFile;
        finalFinal.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                    user.put(User.str_main_pager_background, finalFinal.getUrl());
                    user.saveInBackground();
                }
            }
        });

    }


    private void initData() {
        //获取朋友基本信息
        str_user = getIntent().getStringExtra(ser_user_info);
        try {
            user = (AVUser) AVObject.parseAVObject(str_user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getContentData(final int state) {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Publish");//创建查询对象
        query.limit(5);     //最多返回10条结果
        query.skip(skip);   //从第skip条开始获取
        query.include(PublishContent.str_user);
        query.whereEqualTo(PublishContent.str_user, user);
        if (state == ADD)
            query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);//刷新和加载更多直接从网络加载
        else
            query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);//第一次先从缓存加载，加载失败再从网络加载
        query.orderByDescending("createdAt");//按照时间升序排列
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                //获取缓存数据成功，返回的异常e为空,若list == null，则e不为空
                //异常情况，可能是网络问题，可能后台的类被删除
                if (e != null) {//获取数据出现异常，缓存加载成功
                    //获取数据失败，若之前从缓存获取有数据，则直接更新界面，此时数据仍是从缓存中获取的数据
                    //若没有数据的时候,初始化界面或自动给数据赋值为空
                    initContent();
                    if (state == ADD)
                        lv_friendPublish.loadFail();
                    if (state == REFRESH)
                        lv_friendPublish.refreshFail();//初始化数据完成之后
                    return;
                }

                //获取数据源
                if (state == REFRESH) {
                    friendPublishs = list;// 若是刷新操作或者第一次加载，直接赋值
                    initContent();
                    LogUtils.MyLog("加载数据。。。");
                    lv_friendPublish.completeRefresh();
                    return;
                }
                if (state == ADD) {
                    friendPublishs.addAll(list);//若是加载更多，则将数据直接加入
                    initContent();
                    if (list.size() > 0)
                        lv_friendPublish.completeLoad();
                    else
                        lv_friendPublish.allLoading();
                }
            }
        });
    }

    private void initContent() {
        //创建listview适配器
        if (friendPublishs == null)
            friendPublishs = new ArrayList<AVObject>();
        if (adapter == null) {
            adapter = new FriendMainPagerAdapter(this, friendPublishs, user);
            lv_friendPublish.setAdapter(adapter);
        } else {
            adapter.setContentList(friendPublishs);
            adapter.notifyDataSetChanged();
        }
    }

    private void initHeader() {
        View view = View.inflate(this, R.layout.activity_friend_main_pager_header, null);

        rl_title = (RelativeLayout) view.findViewById(R.id.title_root);
        iv_last = (ImageView)view.findViewById(R.id.title_icon_last);
        tv_title = (TextView)view.findViewById(R.id.title_tv_title);

        iv_background = (ImageView) view.findViewById(R.id.friend_main_pager_header_iv_background);
        iv_photo = (CircleImageView) view.findViewById(R.id.friend_main_pager_header_iv_photo);
        tv_name = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_name);
        ll_friend = (LinearLayout) view.findViewById(R.id.friend_main_pager_header_ll_friend);
        rl_attention = (RelativeLayout) view.findViewById(R.id.friend_main_pager_header_rl_attention);
        tv_attention = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_attention);
        tv_attentionShe = (LinearLayout) view.findViewById(R.id.friend_main_pager_header_ll_attention_she);
        tv_sendMessage = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_send_message);
        tv_phone = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_phone);
        tv_sheAttention = (LinearLayout) view.findViewById(R.id.friend_main_pager_header_ll_she_attention);
        tv_table = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_lable);
        tv_attestation = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_attestation);
        tv_sex = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_sex);
        tv_bornTime = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_born_time);

        tv_sheAttentionCount = (TextView) view.findViewById(R.id.friend_main_pager_header_tv_she_attention_count);
        tv_attentionSheCount= (TextView) view.findViewById(R.id.friend_main_pager_header_tv_attention_she_count);

        getFolloweeFriendCount(tv_attentionSheCount);
        getFriendFolloweeCount(tv_sheAttentionCount);
        //初始化姓名
        tv_name.setText((String) user.get(User.str_accountName));
        String photoUrl = (String) user.get(User.str_photo);
        //初始化图像
        if (!TextUtils.isEmpty(photoUrl))
            Glide.with(this).load(photoUrl).into(iv_photo);
        //初始化背景图
        String backgroundUrl = (String) user.get(User.str_main_pager_background);
        if (!TextUtils.isEmpty(backgroundUrl))
            Glide.with(this).load(backgroundUrl).into(iv_background);
        //加载header

        rl_title.setVisibility(View.VISIBLE);


        tv_title.setText("TA的主页");
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300,300);
        params.setMargins(20, 300, 0, 0);
        iv_photo.setLayoutParams(params);

        //若好友是自己则直接返回
        if (user.getObjectId().equals(MyApplication.me.getObjectId())) {
            ll_friend.setVisibility(View.GONE);
            //设置背景图点击事件
            iv_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediumUtils.openPhoto(FriendMainPagerActivity.this, mOnHanlderResultCallback, mPhotoList, 1);
                }
            });
            tv_title.setText("个人主页");
            rl_attention.setVisibility(View.GONE);
        }
        getFollowee();
        tv_table.setText((String) user.get(User.str_table));
        tv_attestation.setText((String) user.get(User.str_attestation));
        tv_sex.setText((String) user.get(User.str_sex));
        Date date = null;
        Long time = (Long) user.get(User.str_born_time);
        if (time != null) {
            date = new Date(time);
            tv_bornTime.setText(DataUtils.getDataText(date));
        }
        //加载header
        lv_friendPublish.addHeaderView(view);

        tv_attentionShe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendMainPagerActivity.this, FriendAttentionActivity.class);
                intent.putExtra(FriendAttentionActivity.ser_user_info, str_user);
                intent.putExtra(FriendAttentionActivity.str_type, FriendAttentionActivity.ATTENTION_FRIEND);
                startActivity(intent);
            }
        });
        tv_sheAttention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendMainPagerActivity.this, FriendAttentionActivity.class);
                intent.putExtra(FriendAttentionActivity.ser_user_info, str_user);
                intent.putExtra(FriendAttentionActivity.str_type, FriendAttentionActivity.FRIEND_ATTENTION);
                startActivity(intent);
            }
        });

        tv_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendMainPagerActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_INFO, (String) user.toString());
                FriendMainPagerActivity.this.startActivity(intent);
            }
        });

        tv_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber =
                        (String) user.get(User.str_phone_number);
                if ("".equals(phoneNumber) || phoneNumber == null) {
                    ToastUtils.showToast(FriendMainPagerActivity.this, "对方没有核实电话号码");
                    return;
                }
                //意图：想干什么事
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                //url:统一资源定位符
                //uri:统一资源标示符（更广）
                intent.setData(Uri.parse("tel:" + phoneNumber));
                //开启系统拨号器
                startActivity(intent);
            }
        });
    }

    private void initUI() {
        lv_friendPublish = (RefreshListView) findViewById(R.id.friend_main_pager_lv_publish);

        lv_friendPublish.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                skip = 0;
                getContentData(REFRESH);
            }

            @Override
            public void loading() {
                skip += 5;
                getContentData(ADD);
            }
        });
    }

    /**
     * 获取关注列表
     */
    private void getFollowee() {
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(MyApplication.me.getObjectId(), AVUser.class);
        followeeQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                if (avException == null) {
                    //avObjects 就是用户的关注用户列表
                    followeeList = avObjects;
                    if (isAttention(user)) {
                        tv_attention.setText("已关注");
                        cancelAttention(rl_attention, user, tv_attention);
                    } else {
                        attention(rl_attention, user, tv_attention);
                    }
                    //获取朋友的动态信息
                }
            }
        });
    }

    //判断是否关注了此用户
    private boolean isAttention(AVUser user) {
        if (user != null && followeeList != null) {
            for (int i = 0; i < followeeList.size(); i++) {
                if (user.getObjectId().equals(followeeList.get(i).getObjectId()))
                    return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 关注好友
     *
     * @param relativeLayout
     * @param user
     */
    private void attention(final RelativeLayout relativeLayout, final AVUser user
            , final TextView tv_attention) {
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关注好友
                AVUser.getCurrentUser().followInBackground(user.getObjectId(), new FollowCallback() {
                    @Override
                    public void done(AVObject object, AVException e) {
                        if (e == null) {
                            ToastUtils.showToast(FriendMainPagerActivity.this, "关注成功");
                            //更新关注列表
                            getFollowee();
                            tv_attention.setText("已关注");
                        } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                            ToastUtils.showToast(FriendMainPagerActivity.this, "关注失败");
                        }
                    }
                });
            }
        });
    }

    /**
     * 取消关注
     *
     * @param relativeLayout
     * @param user
     */
    private void cancelAttention(final RelativeLayout relativeLayout, final AVUser user,
                                 final TextView tv_attention) {

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVUser.getCurrentUser().unfollowInBackground(user.getObjectId(), new FollowCallback() {
                    @Override
                    public void done(AVObject object, AVException e) {
                        if (e == null) {
                            ToastUtils.showToast(FriendMainPagerActivity.this, "取消关注成功");
                            tv_attention.setText("加关注");
                        } else {
                            ToastUtils.showToast(FriendMainPagerActivity.this, "取消关注失败");
                        }
                    }
                });
            }
        });
    }

    /**
     * 获取朋友的关注列表
     */
    private void getFriendFolloweeCount(final View view){
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(user.getObjectId(), AVUser.class);
//        followeeQuery.include("followee");
        followeeQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null){
                    ((TextView)view).setText(i+"");
                }
            }
        });
    }
    /**
     * 获取关注朋友的列表
     */
    private void getFolloweeFriendCount(final View view){
        AVQuery<AVUser> followerQuery = AVUser.followerQuery(user.getObjectId(), AVUser.class);
//        followerQuery.include("follower");
        followerQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null){
                    ((TextView)view).setText(i+"");
                }
            }
        });
    }
}
