package com.gongzetao.loop.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseRecommendLabelActivity;
import com.gongzetao.loop.bean.Business;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.Official;
import com.gongzetao.loop.bean.Position;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LocationUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.SerUtils;
import com.gongzetao.loop.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by baixinping on 2016/8/21.
 */
public class SearchShowActivity extends BaseRecommendLabelActivity {

    Context context = this;
    ListView lv_searchList;
    EditText et_LookupFrame;
    TextView tv_cancel;
    LinearLayout ll_selectUser;
    TextView tv_friend;
    TextView tv_business;
    TextView tv_official;

    List<String> nearbyUserIdList;

    int friendDate = 1;
    int businessDate = 2;
    int officialDate = 3;

    int date = friendDate;

    SearchListAdapter adapter;
    MyApplication app;
    //搜索结果列表
    List<AVObject> searchList;
    AVUser me;
    //关注列表
    List<AVUser> followeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication) getApplication();
        setContentView(R.layout.activity_search);
        initMe();
        initUI();
        initData();
        initList();
    }

    @Override
    public void initUI() {
        lv_searchList = (ListView) findViewById(R.id.search_lv_search_list);
        et_LookupFrame = (EditText) findViewById(R.id.search_et_lookup_frame);
        tv_cancel = (TextView) findViewById(R.id.search_tv_cancel);
        gv_list = (GridView) findViewById(R.id.recommend_label_gv);
        lv_searchList.setVisibility(View.GONE);
        ll_selectUser = (LinearLayout) findViewById(R.id.search_tv_select_user);
        tv_friend = (TextView) findViewById(R.id.search_tv_friend);
        tv_business = (TextView) findViewById(R.id.search_tv_business);
        tv_official = (TextView) findViewById(R.id.search_tv_official);

        SetClickListener();
    }

    @Override
    public void initOnClickItem(final int position) {
        if(position<14) {
            LocationUtils utils = new LocationUtils(this);
            utils.setSearchSucceedListener(new LocationUtils.SearchSucceedListener() {
                @Override
                public void searchSucceed(List<String> userMail) {

                    LogUtils.MyLog("搜索附近Id成功");
                    nearbyUserIdList = userMail;

                    AVQuery<AVObject> userQuery = new AVQuery<>("_User");
                    userQuery.whereEqualTo("category", User.labelName[position]);

                    AVQuery<AVObject> nearQuery = new AVQuery<>("_User");
                    nearQuery.whereContainedIn(User.str_mail, nearbyUserIdList);
                    // 执行内嵌操作
                    AVQuery<AVObject> query = AVQuery.and(Arrays.asList(nearQuery, userQuery));

                    query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if(e == null && list.size()>0) {
                                AVQuery<AVObject> avQuery = new AVQuery<>(Liveness.str_liveness);
                                avQuery.whereContainedIn(Liveness.str_user, list);
                                avQuery.include(Liveness.str_user);
                                avQuery.selectKeys(Arrays.asList(Liveness.str_livenessCount, Liveness.str_user));
                                avQuery.orderByDescending(Liveness.str_livenessCount);
                                avQuery.findInBackground(new FindCallback<AVObject>() {
                                    @Override
                                    public void done(List<AVObject> list, AVException e) {
                                        if (e == null) {
                                            List<AVObject> users = new ArrayList<>();
                                            for (AVObject object : list
                                                    ) {
                                                users.add((AVObject) object.get(Liveness.str_user));
                                            }
                                            gv_list.setVisibility(View.GONE);
                                            lv_searchList.setVisibility(View.VISIBLE);
                                            ll_selectUser.setVisibility(View.VISIBLE);
                                            searchList = users;
                                            //更新listView
                                            adapter.notifyDataSetChanged();
                                        } else
                                            LogUtils.MyLog(e.toString());
                                    }
                                });
                            }else
                                LogUtils.MyLog(e.toString());
                        }
                    });
                }

                @Override
                public void searchFail(int state) {
                    LogUtils.MyLog("搜索失败了。。。");
                }
            });
            utils.startLocation();
            Position currentPosition = (Position) SerUtils.readObject(this, Position.currentPosition);
            if (currentPosition != null)
                utils.mySearchUser(currentPosition.getLon(), currentPosition.getLat());
        }else {
            if(position == 15)
                setItimOnClick(Business.business);
            if(position == 14)
                setItimOnClick(Official.official);
        }
    }

    private void setItimOnClick(final String table) {
        if(Business.business.equals(table)) {
            Drawable drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.merchant_click);
            tv_business.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);

            drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_person);
            tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_official);
            tv_official.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            date = businessDate;
        }
        else if(Official.official.equals(table)) {
            Drawable drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.official_click);
            tv_official.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);

            drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_person);
            tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_business);
            tv_business.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
            date = officialDate;
        }

        AVQuery<AVObject> query = new AVQuery<>(table);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {

                gv_list.setVisibility(View.GONE);
                lv_searchList.setVisibility(View.VISIBLE);
                ll_selectUser.setVisibility(View.VISIBLE);
                searchList = list;
                //更新listView
                adapter.notifyDataSetChanged();

            }
        });
    }

    private void initMe() {
        //获取我的个人信息以及我关注的好友的信息
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, MyApplication.me.getEmail());
        query.include("followee");
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                me = list.get(0);
                //获取关注列表
                getFollowee();
            }
        });
    }

    /**
     * 获取关注列表
     */
    private void getFollowee() {
        AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(me.getObjectId(), AVUser.class);
        followeeQuery.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                followeeList = avObjects;
            }
        });
    }

    /**
     * 搜索附近的好友
     */
    private void searchNearby() {
        LocationUtils utils = new LocationUtils(this);
        utils.setSearchSucceedListener(new LocationUtils.SearchSucceedListener() {
            @Override
            public void searchSucceed(List<String> userMail) {

                LogUtils.MyLog("搜索附近Id成功");
                nearbyUserIdList = userMail;
                if (date == friendDate)
                    searchUser();
                else if (date == businessDate)
                    searchBusiness();
                else if (date == officialDate)
                    searchOfficial();
            }

            @Override
            public void searchFail(int state) {
                LogUtils.MyLog("搜索失败了。。。");
            }
        });
        utils.startLocation();
        Position currentPosition = (Position) SerUtils.readObject(this, Position.currentPosition);
        if (currentPosition != null)
            utils.mySearchUser(currentPosition.getLon(), currentPosition.getLat());
    }

    /**
     * 搜索周边的商家
     */
    private void searchBusiness() {
        //获取搜索框中的内容
        String searchContent = et_LookupFrame.getText().toString().trim();
        if (searchContent == null || "".equals(searchContent))
            return;
        final AVQuery<AVObject> query = new AVQuery<AVObject>(Business.business);
        query.whereContains(Business.name, searchContent);
//        query.whereContainedIn(Business.number, nearbyUserIdList);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                LogUtils.MyLog("查询最终结果。。。");
                searchList = list;
                //更新listView
                adapter.notifyDataSetChanged();
            }
        });
    }
    /**
     * 搜索周边官方的服务
     */
    private void searchOfficial() {
        //获取搜索框中的内容
        String searchContent = et_LookupFrame.getText().toString().trim();
        if (searchContent == null || "".equals(searchContent))
            return;
        final AVQuery<AVObject> query = new AVQuery<>(Official.official);
        query.whereContains(Official.name, searchContent);
//        query.whereContainedIn(Official.link, nearbyUserIdList);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                LogUtils.MyLog("查询最终结果。。。");
                searchList = list;
                //更新listView
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void searchUser() {
        //获取搜索框中的内容
        String searchContent = et_LookupFrame.getText().toString().trim();
        if (searchContent == null || "".equals(searchContent))
            return;
        final AVQuery<AVObject> tableQuery = new AVQuery<>("_User");
        tableQuery.whereContains(User.str_table, searchContent);
        final AVQuery<AVObject> nameQuery = new AVQuery<>("_User");
        nameQuery.whereContains(User.str_accountName, searchContent);
        final AVQuery<AVObject> attestationQuery = new AVQuery<>("_User");
        attestationQuery.whereContains(User.str_attestation_true, searchContent);
        final AVQuery<AVObject> categoryQuery = new AVQuery<>("_User");
        categoryQuery.whereEqualTo(User.str_category,searchContent);

        //通过输入框中输入的条件进行查询
        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(nameQuery, tableQuery, attestationQuery,categoryQuery));
        query.whereContainedIn(User.str_mail, nearbyUserIdList);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if(e == null && list!=null && list.size()>0) {
                    AVQuery<AVObject> avQuery = new AVQuery<>(Liveness.str_liveness);
                    avQuery.whereContainedIn(Liveness.str_user, list);
                    avQuery.include(Liveness.str_user);
                    avQuery.selectKeys(Arrays.asList(Liveness.str_livenessCount, Liveness.str_user));
                    avQuery.orderByDescending(Liveness.str_livenessCount);
                    avQuery.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if (e == null) {
                                List<AVObject> users = new ArrayList<>();
                                for (AVObject object : list
                                        ) {
                                    users.add((AVObject) object.get(Liveness.str_user));
                                }
                                searchList = users;
                                //更新listView
                                adapter.notifyDataSetChanged();
                            }else
                                LogUtils.MyLog(e.toString());
                        }
                    });


                }else
                    LogUtils.MyLog(e.toString());
            }
        });
    }


    private void initList() {
        if (searchList == null)
            searchList = new ArrayList<AVObject>();
        adapter = new SearchListAdapter();
        lv_searchList.setAdapter(adapter);
    }


    private void SetClickListener() {
        //给搜索框设置键盘搜索监听
        et_LookupFrame.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                   search();
                    searchNearby();
                    return true;
                }
                return false;
            }
        });
        et_LookupFrame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(s.toString())) {
                    gv_list.setVisibility(View.VISIBLE);
                    lv_searchList.setVisibility(View.GONE);
                    ll_selectUser.setVisibility(View.GONE);
                } else {
                    gv_list.setVisibility(View.GONE);
                    lv_searchList.setVisibility(View.VISIBLE);
                    searchNearby();
                    LogUtils.MyLog("开始搜索");
                    ll_selectUser.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchList = new ArrayList<AVObject>();
                Drawable drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.persion_click);
                tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);

                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_official);
                tv_official.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_business);
                tv_business.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                date = friendDate;
                searchNearby();
            }
        });
        tv_business.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchList = new ArrayList<AVObject>();
                Drawable drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.merchant_click);
                tv_business.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);

                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_person);
                tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_official);
                tv_official.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                date = businessDate;
                searchBusiness();
            }
        });
        tv_official.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchList = new ArrayList<AVObject>();
                Drawable drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.official_click);
                tv_official.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);

                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_person);
                tv_friend.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                drawable = SearchShowActivity.this.getResources().getDrawable(R.drawable.icon_search_business);
                tv_business.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
                date = officialDate;
                searchOfficial();
            }
        });
    }


//    private void search() {
//        //获取搜索框中的内容
//        String searchContent = et_LookupFrame.getText().toString().trim();
//        if (searchContent == null || "".equals(searchContent))
//            return;
//        final AVQuery<AVUser> tableQuery = new AVQuery<AVUser>("_User");
//        tableQuery.whereContains(User.str_table, searchContent);
//        final AVQuery<AVUser> nameQuery = new AVQuery<AVUser>("_User");
//        nameQuery.whereContains(User.str_accountName, searchContent);
//        final AVQuery<AVUser> attestationQuery = new AVQuery<AVUser>("_User");
//        attestationQuery.whereContains(User.str_attestation, searchContent);
//
////        Arrays arrays = Arrays.
//
//        AVQuery<AVUser> query = AVQuery.or(Arrays.asList(nameQuery, tableQuery, attestationQuery));
//        query.findInBackground(new FindCallback<AVUser>() {
//            @Override
//            public void done(List<AVUser> list, AVException e) {
//                searchList = list;
//                //更新listView
//                adapter.notifyDataSetChanged();
//            }
//        });
//
//    }

    class SearchListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (date == friendDate){
                return friendUiItem(convertView, position);
            }
            if (date == businessDate){
                return businessUiItem(convertView, position);
            }else if(date==officialDate){
                return officialUiItem(convertView, position);
            }
            return null;
        }
    }

    private View officialUiItem(View convertView, int position) {
        OfficialViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new OfficialViewHolder();
            convertView = View.inflate(this, R.layout.activity_search_official_list_item, null);
            viewHolder.iv_photo = (ImageView) convertView.findViewById(R.id.search_item_official_iv_photo);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.search_item_official_tv_name);
            viewHolder.tv_explain = (TextView) convertView.findViewById(R.id.search_item_official_tv_explain);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (OfficialViewHolder) convertView.getTag();
        }

        final AVObject business = searchList.get(position);
        String photo = (String) business.get(Official.photo);
        if (!TextUtils.isEmpty(photo))
            Glide.with(this).load(photo).into(viewHolder.iv_photo);
        viewHolder.tv_name.setText((CharSequence) business.get(Official.name));
        viewHolder.tv_explain.setText((CharSequence) business.get(Official.explain));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchShowActivity.this, OfficialShowActivity.class);
                intent.putExtra(OfficialShowActivity.str_title, (String)business.get(Official.name));
                intent.putExtra(OfficialShowActivity.str_url, (String)business.get(Official.link));
                startActivity(intent);
            }
        });
        return convertView;
    }
    static class OfficialViewHolder {
        public ImageView iv_photo;
        public TextView tv_name;
        public TextView tv_explain;
    }
    private View businessUiItem(View convertView, int position) {
        BusinessViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new BusinessViewHolder();
            convertView = View.inflate(this, R.layout.activity_search_business_list_item, null);
            viewHolder.iv_photo = (ImageView) convertView.findViewById(R.id.search_item_business_iv_photo);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.search_item_business_tv_name);
            viewHolder.tv_explain = (TextView) convertView.findViewById(R.id.search_item_business_tv_explain);
            viewHolder.tv_position = (TextView) convertView.findViewById(R.id.search_item_business_tv_explain);
            viewHolder.iv_price = (TextView) convertView.findViewById(R.id.search_item_business_tv_price);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (BusinessViewHolder) convertView.getTag();
        }

        final AVObject business = searchList.get(position);
        String photo = (String) business.get(Business.photo);
        if (photo != null)
            Glide.with(this).load(photo).into(viewHolder.iv_photo);
        viewHolder.tv_name.setText((CharSequence) business.get(Business.name));
        viewHolder.tv_explain.setText((CharSequence) business.get(Business.explain));
        viewHolder.tv_position.setText((CharSequence) business.get(Business.position));
        viewHolder.iv_price.setText((CharSequence) business.get(Business.price));
        return convertView;
    }
    static class BusinessViewHolder {
        public ImageView iv_photo;
        public TextView tv_name;
        public TextView tv_explain;
        public TextView tv_position;
        public TextView iv_price;
    }
    FriendViewHolder holder = null;
    private View friendUiItem(View convertView, int position) {

        if (convertView == null) {
            holder = new FriendViewHolder();
            convertView = View.inflate(context, R.layout.activity_search_friend_list_item, null);
            holder.iv_photo = (ImageView) convertView.findViewById(R.id.search_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.search_item_tv_name);
            holder.tv_table = (TextView) convertView.findViewById(R.id.search_item_tv_table);
            holder.tv_attestation = (TextView) convertView.findViewById(R.id.search_item_tv_attestation);
            holder.iv_attention = (ImageView) convertView.findViewById(R.id.search_item_tv_attention);
            convertView.setTag(holder);
        } else {
            holder = (FriendViewHolder) convertView.getTag();
        }
        final AVObject user = searchList.get(position);
        holder.tv_name.setText((CharSequence) user.get(User.str_accountName));
        holder.tv_table.setText((CharSequence) user.get(User.str_table));
        holder.tv_attestation.setText((CharSequence) user.get(User.str_attestation_true));
        String userPhoto = (String)user.get(User.str_photo);
        if(!TextUtils.isEmpty(userPhoto))
            Glide.with(SearchShowActivity.this).load(userPhoto).into(holder.iv_photo);
        final FriendViewHolder finalHolder = holder;
        if (isAttention((AVUser)user)) {
            holder.iv_attention.setEnabled(false);
            holder.iv_attention.setImageResource(R.drawable.alreaday_attention);
        } else {
            holder.iv_attention.setImageResource(R.drawable.no_attention);
            attention(holder.iv_attention, (AVUser)user);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterFriendMainPager(SearchShowActivity.this, (AVUser)user);
            }
        });
        return convertView;
    }
    static class FriendViewHolder {
        public ImageView iv_photo;
        public TextView tv_name;
        public TextView tv_table;
        public TextView tv_attestation;
        public ImageView iv_attention;
    }
    /**
     * 关注好友
     *
     * @param iv_attention
     * @param user
     */
    private void attention(final ImageView iv_attention, final AVUser user) {
        iv_attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //关注好友
                AVUser.getCurrentUser().followInBackground(user.getObjectId(), new FollowCallback() {
                    @Override
                    public void done(AVObject object, AVException e) {
                        if (e == null) {
                            ToastUtils.showToast(context, "关注成功");
                            holder.iv_attention.setImageResource(R.drawable.no_attention);
                            //更新关注列表
                            getFollowee();
                            iv_attention.setImageResource(R.drawable.alreaday_attention);
                        } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                            ToastUtils.showToast(context, "关注失败");
                            holder.iv_attention.setImageResource(R.drawable.alreaday_attention);
                        }else
                            holder.iv_attention.setImageResource(R.drawable.alreaday_attention);
                    }
                });
            }
        });
    }

    /**
     * 取消关注
     *
     * @param view
     * @param user
     */
    private void cancelAttention(final ImageView view, final AVUser user) {

        AVUser.getCurrentUser().unfollowInBackground(user.getObjectId(), new FollowCallback() {
            @Override
            public void done(AVObject object, AVException e) {
                if (e == null) {
                    ToastUtils.showToast(context, "取消关注成功");
                    view.setImageResource(R.drawable.no_attention);
                    view.setEnabled(true);
                } else {
                    ToastUtils.showToast(context, "取消关注失败");
                }
            }
        });
    }


    //判断是否关注了此用户
    private boolean isAttention(AVUser user) {
        if (user != null && followeeList != null) {
            for (int i = 0; i < followeeList.size(); i++) {
                if (user.getObjectId().equals(followeeList.get(i).getObjectId())
                        ||user.getObjectId().equals(MyApplication.me.getObjectId()))
                    return true;
            }
            return false;
        }
        return false;
    }




}
