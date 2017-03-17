package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.view.sortlistview.CharacterParser;
import com.gongzetao.loop.view.sortlistview.ClearEditText;
import com.gongzetao.loop.view.sortlistview.PinyinComparator;
import com.gongzetao.loop.view.sortlistview.SideBar;
import com.gongzetao.loop.view.sortlistview.SortAdapter;
import com.gongzetao.loop.view.sortlistview.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by baixinping on 2016/8/22.
 */
public class AttentionListActivity extends BaseActivity {

    ListView lv_friendsList;
    ImageView iv_cancel;
    MyApplication app;
    AVUser me;
    List<SortModel> mSortList;
    List<SortModel> searchDateList;
    TextView tv_title;

    public static final int ATTENTION_ME = 1;
    public static final int MY_ATTENTION = 2;
    public static final String type = "type";
    private int dateType;

    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText mClearEditText;
    List<AVUser> friendsList;

    private CharacterParser characterParser;
    private PinyinComparator pinyinComparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication) getApplication();
        setContentView(R.layout.activity_attention_list);
        initUI();
        initType();
        initData();
    }

    private void initType() {
        dateType = getIntent().getIntExtra(type, 0);
    }
    /**
     * 获取好友数据
     */
    private void initData() {
        initMe();
    }
    private void initMe() {
        //获取我的个人信息以及我关注的好友的信息
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, MyApplication.me.getEmail());
        //先从网络加载，再从缓存加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (list != null && list.size() > 0) {
                    me = list.get(0);
                    //获取关注列表
                    getFolloweer();
                }

            }
        });
    }

    /**
     * 获取关注列表
     */
    private void getFolloweer(){
        AVQuery<AVUser> query = null;
        if (dateType == ATTENTION_ME){
            query = AVUser.followerQuery(me.getObjectId(), AVUser.class);
            query.include("follower");
        }
        else if (dateType == MY_ATTENTION){
            query = AVUser.followeeQuery(me.getObjectId(), AVUser.class);
            query.include("followee");
        }
        //先从本地加载，加载失败再从网络加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                if (avObjects != null && avObjects.size() > 0) {
                    friendsList = avObjects;
                    //初始化内容
                    initContent();
                }
            }
        });
    }

    private void initContent() {

        filledData(friendsList);
        Collections.sort(mSortList, pinyinComparator);
        if (friendsList == null){
            friendsList = new ArrayList<AVUser>();
        }
        adapter = new SortAdapter(this, mSortList);
        lv_friendsList.setAdapter(adapter);
    }

    private void initUI() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        iv_cancel = (ImageView) findViewById(R.id.title_icon_last);
        sideBar = (SideBar) findViewById(R.id.attention_list_sb_char);
        dialog = (TextView) findViewById(R.id.attention_list_tv_dialog);
        lv_friendsList = (ListView) findViewById(R.id.attention_list_lv_friends_list);
        sideBar.setTextView(dialog);
        mClearEditText = (ClearEditText) findViewById(R.id.attention_list_et_search);
        tv_title  = (TextView) findViewById(R.id.title_tv_title);
        if (dateType == ATTENTION_ME)
            tv_title.setText("关注我的");
        else
            tv_title.setText("我关注的");
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    lv_friendsList.setSelection(position);
                }
            }
        });


        lv_friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(AttentionListActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_INFO, mSortList.get(position).getUser().toString());
                startActivity(intent);
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    /**
     * @param users
     * @return
     */
    private List<SortModel> filledData(List<AVUser> users){
        mSortList = new ArrayList<SortModel>();

        for(int i=0; i<users.size(); i++){
            SortModel sortModel = new SortModel();
            sortModel.setUser(users.get(i));
            String pinyin = characterParser.getSelling((String)users.get(i).get(User.str_accountName));
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if(sortString.matches("[A-Z]")){
                sortModel.setSortLetters(sortString.toUpperCase());
            }else{
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * @param filterStr
     */
    private void filterData(String filterStr){
        searchDateList = new ArrayList<SortModel>();

        if(TextUtils.isEmpty(filterStr)){
            searchDateList = mSortList;
        }else{
            searchDateList.clear();
            for(SortModel sortModel : mSortList){
                String name = (String)sortModel.getUser().get(User.str_accountName);
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    searchDateList.add(sortModel);
                }
            }
        }

        Collections.sort(searchDateList, pinyinComparator);
        adapter.updateListView(searchDateList);
        lv_friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(AttentionListActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_INFO, searchDateList.get(position).getUser().toString());
                startActivity(intent);
            }
        });
    }
}
