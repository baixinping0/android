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
 * Created by baixinping on 2016/8/26.
 */
public class RemindFriendsActivity extends BaseActivity {
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private ClearEditText mClearEditText;

    private  ImageView iv_last;
    TextView tv_title;

    List<AVUser> friendsList;


    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    private PinyinComparator pinyinComparator;
    MyApplication app;
    AVUser me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind_friends);
        app = (MyApplication) getApplication();
        initViews();
        initData();
    }

    private void initData() {
        //填充数据,获取关注的好友列表

        //获取我的个人信息以及我关注的好友的信息
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, app.accountNumber);
        query.include("followee");
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (list != null && list.size() > 0)
                    me = list.get(0);
                //获取关注列表
                getFollowee();
            }
        });
    }
    //获取关注列表
    private void getFollowee(){
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(me.getObjectId(), AVUser.class);
        followeeQuery.include("followee");
        followeeQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                friendsList = avObjects;
                //初始化内容
                initContent();
            }
        });
    }

    private void initContent() {

        SourceDateList = filledData(friendsList);
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
    }

    private void initViews() {
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        sideBar = (SideBar) findViewById(R.id.remind_friends_sb_char);
        dialog = (TextView) findViewById(R.id.remind_friends_tv_dialog);
        sortListView = (ListView) findViewById(R.id.remind_friends_lv_friends_list);
        sideBar.setTextView(dialog);
        mClearEditText = (ClearEditText) findViewById(R.id.remind_friends_et_search);

        iv_last = (ImageView) findViewById(R.id.title_icon_last);
        tv_title = (TextView) findViewById(R.id.title_tv_title);
        tv_title.setText("@好友");
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
                if(position != -1){
                    sortListView.setSelection(position);
                }
            }
        });


        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(User.user,((SortModel) adapter.getItem(position)).getUser().toString() );
                RemindFriendsActivity.this.setResult(RESULT_OK, intent);
                RemindFriendsActivity.this.finish();
            }
        });
    }
    /**
     * @param users
     * @return
     */
    private List<SortModel> filledData(List<AVUser> users){
        List<SortModel> mSortList = new ArrayList<SortModel>();

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
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(SortModel sortModel : SourceDateList){
                String name = (String)sortModel.getUser().get(User.str_accountName);
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    filterDateList.add(sortModel);
                }
            }
        }

        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

}
