package com.gongzetao.loop.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.FriendAttentionAdapter;
import com.gongzetao.loop.application.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class FriendAttentionActivity extends AppCompatActivity {
    ListView lv_friendList;
    ImageView iv_last;
    TextView tv_title;
    String str_user;
    AVUser user;
    public static final String ser_user_info = "serUserInfo";

    List<AVUser> myFolloweeList;
    List<AVUser> friendList;
    FriendAttentionAdapter adapter;

    public static final String str_type = "type";

    public static int FRIEND_ATTENTION = 1;
    public static int ATTENTION_FRIEND = 2;

    public static int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_attention);
        type = getIntent().getIntExtra(str_type, 0);
        initUI();
        initData();
    }

    private void initList() {
        if (friendList == null)
            friendList = new ArrayList<AVUser>();
        adapter = new FriendAttentionAdapter(this, friendList,myFolloweeList);
        lv_friendList.setAdapter(adapter);
    }

    private void initData() {
        //获取好友的信息
        str_user = getIntent().getStringExtra(ser_user_info);
        try {
            user = (AVUser) AVObject.parseAVObject(str_user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (type == FRIEND_ATTENTION)
            getFriendFollowee();
        else if (type == ATTENTION_FRIEND)
            getFriendFollower();
    }

    private void initUI() {
        iv_last = (ImageView) findViewById(R.id.title_icon_last);
        tv_title = (TextView) findViewById(R.id.title_tv_title);
        if (type == FRIEND_ATTENTION)
            tv_title.setText("他关注的");
        else if (type == ATTENTION_FRIEND)
            tv_title.setText("关注他的");
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        lv_friendList = (ListView) findViewById(R.id.friend_attention_lv_list);
    }

    /**
     * 获取自己的关注列表，用于判断朋友的好友是否被关注
     */
    private void getMyFollowee(){
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(MyApplication.me.getObjectId(), AVUser.class);
        followeeQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                if (avException == null){
                    //avObjects 就是用户的关注用户列表
                    myFolloweeList = avObjects;
                    initList();
                }
            }
        });
    }

    /**
     * 获取朋友的关注列表
     */
    private void getFriendFollowee(){
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(user.getObjectId(), AVUser.class);
        followeeQuery.include("followee");
        followeeQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
               if (avException == null){
                   //avObjects 就是用户的关注用户列表
                   friendList = avObjects;
                   //获取我自己的关注列表
                   getMyFollowee();
               }
            }
        });
    }
    /**
     * 获取关注朋友的列表
     */
    private void getFriendFollower(){
        AVQuery<AVUser> followerQuery = AVUser.followerQuery(user.getObjectId(), AVUser.class);
        followerQuery.include("follower");
        followerQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                if (avException == null){
                    //avObjects 就是用户的关注用户列表
                    friendList = avObjects;
                    //获取我自己的关注列表
                    getMyFollowee();
                }
            }
        });
    }
}
