package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.RefreshListView;
import com.gongzetao.loop.view.sortlistview.CharacterParser;
import com.gongzetao.loop.view.sortlistview.ClearEditText;
import com.gongzetao.loop.view.sortlistview.PinyinComparator;
import com.gongzetao.loop.view.sortlistview.SideBar;
import com.gongzetao.loop.view.sortlistview.SortAdapter;
import com.gongzetao.loop.view.sortlistview.SortModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by baixinping on 2016/8/5.
 */
public class FriendPager extends HomeBasePager {
    public static final int ATTENTION_ME = 1000;
    public static final int MY_ATTENTION = 1001;

    RefreshListView lv_friendList;
    List<AVUser> userList;
    RadioButton rb_attentionMe;
    RadioButton rb_myAttention;
    View viewHeader;
    ClearEditText et_lookUp;
    RadioGroup radioGroup;
    ListView friendList;
    TextView noContentHint;

    SortAdapter adapter;
    List<SortModel> mSortList;
    List<SortModel> searchDateList;
    private Comparator<? super SortModel> pinyinComparator;
    private CharacterParser characterParser;
    private List<AVUser> followerList;

    SideBar sideBar;

    private boolean firstEnter = true;

    public FriendPager(Activity activity) {
        super(activity);
    }

    @Override
    public void initData() {
        LogUtils.MyLog(MyApplication.unReadChatMessageCount + "   friend");
        initTitle();
        pinyinComparator = new PinyinComparator();
        characterParser = CharacterParser.getInstance();
        initHomePagerUI();
//        initHeader();
        initDefaultList();
        if (firstEnter){
            getFriends();
            firstEnter = false;
        }
    }

    private void initDefaultList() {
        initHeader();
        if (userList == null)
            userList = new ArrayList<>();
        filledData(userList);
        Collections.sort(mSortList, pinyinComparator);
        if (adapter == null)
            adapter = new SortAdapter(mActivity, mSortList);
        lv_friendList.addHeaderView(viewHeader);
        lv_friendList.setAdapter(adapter);
    }

    private void getFriends() {
        AVQuery<AVUser> query = null;
        query = AVUser.followerQuery(MyApplication.me.getObjectId(), AVUser.class);
        query.include("follower");
        //先从本地加载，加载失败再从网络加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                if (avException != null) {
                    ToastUtils.showToast(mActivity, "请检查网络连接");
                    lv_friendList.refreshFail();
                    return;
                }
                if (avObjects != null) {
                    followerList = avObjects;
                    getfriendList();
                }
            }
        });
    }

    private void getfriendList() {
        AVQuery<AVUser> query = null;

        query = AVUser.followeeQuery(MyApplication.me.getObjectId(), AVUser.class);
        query.include("followee");
        //先从本地加载，加载失败再从网络加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.whereContainedIn("followee", followerList);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                if (avException != null) {
                    ToastUtils.showToast(mActivity, "请检查网络连接");
                    lv_friendList.refreshFail();
                    return;
                }
                //avObjects 就是用户的关注用户列表
                if (avObjects != null&&avObjects.size()>0) {
                    noContentHint.setVisibility(View.GONE);
//                    friendList.setVisibility(View.VISIBLE);
                    userList = avObjects;
                    //初始化内容
                    initList();
                }else{
                    noContentHint.setText("您还没有好友哦,快去关注别人吧");
                    noContentHint.setVisibility(View.VISIBLE);
//                    friendList.setVisibility(View.GONE);
                    userList = new ArrayList<AVUser>();
                    initList();
                }
            }
        });
    }

    private void initList() {
        filledData(userList);
        Collections.sort(mSortList, pinyinComparator);
        if (adapter == null)
            adapter = new SortAdapter(mActivity, mSortList);
        else {
            adapter.setList(mSortList);
            adapter.notifyDataSetChanged();
        }
        lv_friendList.completeRefresh();
    }

    /**
     * 动态更换RadioButton的显示效果
     * @param icon 要设置给RadioButton的图片id
     * @param radioButton 要给哪个RadioButton设置
     */
    private void setRadioButtonIcon(int icon,RadioButton radioButton){
        Drawable drawable = mActivity.getResources().getDrawable(icon);
        radioButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
    }
    RadioButton myFriend = null;
    RadioButton myAttention = null;
    RadioButton attentionMe = null;
    private void initHeader() {
        viewHeader = View.inflate(mActivity, R.layout.layout_home_pager_friend_header, null);

        radioGroup = (RadioGroup) viewHeader.findViewById(R.id.friend_pager_rg_botton_icon);
        rb_attentionMe = (RadioButton) viewHeader.findViewById(R.id.friend_pager_rb_my_attention);
        rb_myAttention = (RadioButton) viewHeader.findViewById(R.id.friend_pager_rb_attention_me);
        et_lookUp = (ClearEditText) viewHeader.findViewById(R.id.layout_friend_pager_et_lookup_frame);
        friendList = (ListView)viewHeader.findViewById(R.id.friend_list);
        noContentHint = (TextView)viewHeader.findViewById(R.id.no_content_hint);
        myFriend = (RadioButton)viewHeader.findViewById(R.id.friend_pager_rb_my_friend);
        myAttention = (RadioButton)viewHeader.findViewById(R.id.friend_pager_rb_attention_me);
        attentionMe = (RadioButton)viewHeader.findViewById(R.id.friend_pager_rb_my_attention);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.friend_pager_rb_my_friend:
                        getFriends();
                        setRadioButtonIcon(R.drawable.my_friend_click, myFriend);
                        setRadioButtonIcon(R.drawable.icon_attention_me, attentionMe);
                        setRadioButtonIcon(R.drawable.icon_my_attention, myAttention);
                        break;
                    case R.id.friend_pager_rb_attention_me:
                        getFolloweer(MY_ATTENTION);
                        setRadioButtonIcon(R.drawable.my_attention_click, myAttention);
                        setRadioButtonIcon(R.drawable.icon_my_friends, myFriend);
                        setRadioButtonIcon(R.drawable.icon_attention_me, attentionMe);
                        break;
                    case R.id.friend_pager_rb_my_attention:
                        getFolloweer(ATTENTION_ME);
                        setRadioButtonIcon(R.drawable.attention_me_click, attentionMe);
                        setRadioButtonIcon(R.drawable.icon_my_friends, myFriend);
                        setRadioButtonIcon(R.drawable.icon_my_attention, myAttention);
                        break;
                }
            }
        });

        et_lookUp.addTextChangedListener(new TextWatcher() {
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
        et_lookUp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                et_lookUp.setFocusable(true);
                et_lookUp.setFocusableInTouchMode(true);
            }
        });

    }

    private void initHomePagerUI() {
        View friendPager = View.inflate(mActivity, R.layout.layout_home_pager_friend, null);
        //        将之前添加view清除
        fl_pager.removeAllViews();
        fl_pager.addView(friendPager);
        lv_friendList = (RefreshListView) friendPager.findViewById(R.id.friend_lv_friend_list);
        lv_friendList.setPermitLoading(false);
        sideBar = (SideBar) friendPager.findViewById(R.id.friend_sb_char);


        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    lv_friendList.setSelection(position);
                }
            }
        });


        lv_friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                EnterActivityUtils.enterChatActivity(mActivity, mSortList.get(position - 2).getUser());
            }
        });
        lv_friendList.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                if (rb_attentionMe.isChecked())
                    getFolloweer(ATTENTION_ME);
                else if (rb_myAttention.isChecked())
                    getFolloweer(MY_ATTENTION);
                else
                    getFriends();
            }

            @Override
            public void loading() {

            }
        });
    }

    private void initTitle() {
        tv_PagerDes.setText("好友");
        tv_PagerDes.setVisibility(View.VISIBLE);
        iv_IconAdd.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.INVISIBLE);
        iv_IconLast.setVisibility(View.INVISIBLE);
    }

    private List<SortModel> filledData(List<AVUser> users) {
        mSortList = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            SortModel sortModel = new SortModel();
            sortModel.setUser(users.get(i));
            String pinyin = characterParser.getSelling((String) users.get(i).get(User.str_accountName));

            String sortString = null;
            if (pinyin != null)
                sortString = pinyin.substring(0, 1).toUpperCase();
            else pinyin = "";

            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * @param filterStr
     */
    private void filterData(String filterStr) {
        searchDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            searchDateList = mSortList;
        } else {
            searchDateList.clear();
            for (SortModel sortModel : mSortList) {
                String name = (String) sortModel.getUser().get(User.str_accountName);
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    searchDateList.add(sortModel);
                }
            }
        }

        Collections.sort(searchDateList, pinyinComparator);
        adapter.updateListView(searchDateList);

        lv_friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                EnterActivityUtils.enterChatActivity(mActivity, searchDateList.get(position - 2).getUser());
            }
        });
    }


    private void getFolloweer(final int state){
        AVQuery<AVUser> query = null;
        if (state == ATTENTION_ME){
            query = AVUser.followerQuery(MyApplication.me.getObjectId(), AVUser.class);
            query.include("follower");
        }
        else if (state == MY_ATTENTION){
            query = AVUser.followeeQuery(MyApplication.me.getObjectId(), AVUser.class);
            query.include("followee");
        }
        //先从本地加载，加载失败再从网络加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                if (avObjects != null && avObjects.size() > 0) {
                    noContentHint.setVisibility(View.GONE);
//                    friendList.setVisibility(View.VISIBLE);
                    userList = avObjects;
                    //初始化内容
                    initList();
                }else{
                    userList = new ArrayList<AVUser>();
                    initList();
                    if(state == ATTENTION_ME)
                        noContentHint.setText("还没有人关注你哦");
                    if(state==MY_ATTENTION)
                        noContentHint.setText("您还没有关注任何好友哦");
                    noContentHint.setVisibility(View.VISIBLE);
//                    friendList.setVisibility(View.GONE);
                }
            }
        });
    }
}
