package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.activity.PublishActivity;
import com.gongzetao.loop.activity.SearchShowActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.Praise;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LivenessUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.NetWorkUtils;
import com.gongzetao.loop.utils.StreamUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.RefreshListView;
import com.gongzetao.loop.view.photoBrowser.ui.PhotoViewActivity;
import com.gongzetao.loop.view.lGNineGrideView.LGNineGrideView;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/5.
 */

/**
 * 此界面展示的是我关注的好友的动态
 */
public class MainPager extends HomeBasePager {

    public static final String resultPublish = "resultPublish";

    RefreshListView mainListView;
    TextView tv_isNetwork;
    public HomeBaseAdapter adapter;
    private int skip = 0;

    int screenWidth;
    BitmapUtils bitmapUtils;
    public List<AVObject> contentList;

    List<AVUser> myAttestationList;

    public static final int REFRESH = 0;
    public static final int ADD = 1;
//    private static final int ONCE = 2;
    private boolean firstEnter = true;

    public MainPager(Activity activity) {
        super(activity);
    }

    @Override
    public void initData() {
        screenWidth = mActivity.getWindowManager().getDefaultDisplay().getWidth();
        bitmapUtils = new BitmapUtils(mActivity);
        initTitle();
        initListener();
        if (firstEnter) {
            initMainPagerUI();
            initMe();
            firstEnter = false;
        }
        initContent();
    }


    private void initMe() {
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, MyApplication.accountNumber);
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (list != null && list.size() > 0) {
                    MyApplication.me = list.get(0);
                    getFolloweer();
                }
            }
        });
    }
    private void initMainPagerUI() {
        //初始化布局
        View mainView = View.inflate(mActivity, R.layout.layout_home_pager_main, null);
        //获取listView
        mainListView = (RefreshListView) mainView.findViewById(R.id.main_lv_content_items);
        tv_isNetwork = (TextView) mainView.findViewById(R.id.main_tv_is_network);
        //将之前布局中的控件清空并添加现有布局
        fl_pager.removeAllViews();
        //将当前布局添加进去
        fl_pager.addView(mainView);
        mainListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                //下拉刷新执行操作
                skip = 0;//
                getContentData(REFRESH);
            }

            @Override
            public void loading() {
                //上划加载更多执行操作
                skip += 5;
                getContentData(ADD);
            }
        });
        if (!NetWorkUtils.isNetworkAvailable(mActivity) && !NetWorkUtils.ping()) {
            tv_isNetwork.setVisibility(View.VISIBLE);
        } else {
            tv_isNetwork.setVisibility(View.GONE);
        }
    }



    private void initListener() {
        //设置搜索框焦点状态改变时间监听
        et_LookupFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SearchShowActivity.class);
                mActivity.startActivity(intent);
            }
        });
    }


    /**
     * /**
     * 获取我关注的好友列表
     */
    private void getFolloweer() {
        AVQuery<AVUser> query = null;
        query = AVUser.followeeQuery(MyApplication.me.getObjectId(), AVUser.class);
        query.include("followee");
        //先从本地加载，加载失败再从网络加载
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> avObjects, AVException avException) {
                //avObjects 就是用户的关注用户列表
                if (avException == null) {
                    myAttestationList = avObjects;
//                    if (firstEnter)
//                        getContentData(ONCE);
//                    else
                        getContentData(REFRESH);
                }
            }
        });
    }


    /**
     * 获取所有数据
     */
    public void getContentData(final int state) {

        final AVQuery<AVObject> tableQuery1 = new AVQuery<>(PublishContent.str_publish);
        tableQuery1.whereEqualTo(PublishContent.str_user, MyApplication.me);
        final AVQuery<AVObject> tableQuery2 = new AVQuery<>(PublishContent.str_publish);
        tableQuery2.whereContainedIn(PublishContent.str_user, myAttestationList);

        AVQuery<AVObject> query = AVQuery.or(Arrays.asList(tableQuery1, tableQuery2));
        query.limit(10);     //最多返回10条结果
        query.skip(skip);   //从第skip条开始获取
        query.include(PublishContent.str_user);
        query.include(PublishContent.str_transmited_user);
        //第一次先从缓存加载，加载失败再从网络加载
        if (state == ADD)
            query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
//        else
//            query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.orderByDescending("createdAt");//按照时间升序排列
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                //获取缓存数据成功，返回的异常e为空,若list == null，则e不为空
                //异常情况，可能是网络问题，可能后台的类被删除
                if (e != null) {//获取数据出现异常，缓存加载成功
                    ToastUtils.showToast(mActivity, "请检查网络是否连接" );
                    //获取数据失败，若之前从缓存获取有数据，则直接更新界面，此时数据仍是从缓存中获取的数据
                    //若没有数据的时候,初始化界面或自动给数据赋值为空
                    initContent();
                    if (state == ADD)
                        mainListView.loadFail();
                    if (state == REFRESH)
                        mainListView.refreshFail();//初始化数据完成之后
                    return;
                }

                //获取数据源
                if (state == REFRESH) {
                    contentList = list;// 若是刷新操作或者第一次加载，直接赋值
                    initContent();
                    mainListView.completeRefresh();
                    return;
                }
                if (state == ADD) {
                    contentList.addAll(list);//若是加载更多，则将数据直接加入
                    initContent();
                    if (list.size() > 0)
                        mainListView.completeLoad();
                    else
                        mainListView.allLoading();
                }
            }
        });
    }

    private void initContent() {

        //创建listview适配器
        if (contentList == null)
            contentList = new ArrayList<AVObject>();
        if (adapter == null) {
            adapter = new HomeBaseAdapter();
            mainListView.setAdapter(adapter);
        } else
            adapter.notifyDataSetChanged();
    }


    //初始化标题栏
    private void initTitle() {
        tv_PagerDes.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.VISIBLE);
        iv_IconLast.setVisibility(View.INVISIBLE);
        iv_IconAdd.setVisibility(View.VISIBLE);
    }

    public class HomeBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contentList.size();
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
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mActivity, R.layout.layout_home_content_item, null);
                holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.main_iv_photo);
                holder.tv_name = (TextView) convertView.findViewById(R.id.main_tv_name);
                holder.tv_time = (TextView) convertView.findViewById(R.id.main_tv_time);
                holder.tv_text = (TextView) convertView.findViewById(R.id.main_tv_text);
                holder.gv_picture = (LGNineGrideView) convertView.findViewById(R.id.main_gv_picture);
                holder.tv_transmit = (TextView) convertView.findViewById(R.id.main_tv_transmit);
                holder.tv_comment = (TextView) convertView.findViewById(R.id.main_tv_comment);
                holder.tv_praise = (TextView) convertView.findViewById(R.id.main_tv_praise);
                holder.tv_transmitText = (TextView) convertView.findViewById(R.id.main_tv_transmit_text);
//                holder.ll_title = (RelativeLayout) convertView.findViewById(R.id.persion_ll_persion_title);
                holder.iv_attestation = (ImageView) convertView.findViewById(R.id.main_iv_attestation);
                holder.ll_transmitColor = (LinearLayout) convertView.findViewById(R.id.main_ll_transmit_color);

                holder.rl_transmit = (RelativeLayout) convertView.findViewById(R.id.main_rl_transmit);
                holder.rl_comment = (RelativeLayout) convertView.findViewById(R.id.main_rl_comment);
                holder.rl_praise = (RelativeLayout) convertView.findViewById(R.id.main_rl_praise);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //获取说说对象
            final AVObject content = contentList.get(position);

            Date time = content.getCreatedAt();
            if (time == null)
                time = new Date(System.currentTimeMillis());
            if(DataUtils.isRecent(time)==DataUtils.TODAY)
                holder.tv_time.setText("今天"+DataUtils.getTimeText(time));
            else if(DataUtils.isRecent(time)==DataUtils.YESTERDAY)
                holder.tv_time.setText("昨天"+DataUtils.getTimeText(time));
            else if(DataUtils.isRecent(time)==DataUtils.TDBY)
                holder.tv_time.setText("前天"+DataUtils.getTimeText(time));
            else if(DataUtils.isRecent(time)==DataUtils.THIS_YEAR)
                holder.tv_time.setText(DataUtils.getDataTimeTextNoYS(time));
            else
                holder.tv_time.setText(DataUtils.getDataTimeTextNoS(time));
            //判断动态是否是转发
            Boolean isTransmit = (Boolean) content.get(PublishContent.str_is_transmit);
            if (isTransmit != null)
                if (isTransmit) {
                    String transmitText = (String) content.get(PublishContent.str_transmit_text);
                    holder.tv_transmitText.setVisibility(View.VISIBLE);
                    holder.ll_transmitColor.setBackgroundResource(R.color.lineColor);
                    holder.tv_transmitText.setText(SpanableStringUtils.getInstace().getExpressionString(mActivity, transmitText + ""));
                    AVUser transmitedUser = (AVUser) content.get(PublishContent.str_transmited_user);
                    if (transmitedUser != null) {
                        String name = (String) transmitedUser.get(User.str_accountName);
                        holder.tv_text.setText(SpanableStringUtils.getInstace().
                                getExpressionString(mActivity,
                                        "@" + name + " :" + (content.get(PublishContent.str_text))));
                    }
                } else {
                    holder.tv_transmitText.setVisibility(View.GONE);
                    holder.ll_transmitColor.setBackgroundResource(R.color.content_background);
                    holder.tv_text.setText(SpanableStringUtils.getInstace().
                            getExpressionString(mActivity,
                                    (String) (content.get(PublishContent.str_text))));
                }

            //获取转发次数
            Integer transmitCount = (Integer) content.get(PublishContent.str_transmit_count);
            if (transmitCount == null)
                holder.tv_transmit.setText("0");
            else
                holder.tv_transmit.setText(transmitCount.toString());
            //获取评论次数
            Integer commentCount = (Integer) content.get(PublishContent.str_comment_count);
            if (commentCount == null)
                holder.tv_comment.setText("0");
            else
                holder.tv_comment.setText(commentCount.toString());
            //获取点赞
            Integer praiseCount = (Integer) content.get(PublishContent.str_praise_count);
            if (praiseCount == null)
                holder.tv_praise.setText("0");
            else
                holder.tv_praise.setText(praiseCount.toString());
            //获取发表说说的用户
            final AVUser user = content.getAVObject(PublishContent.str_user);
            ArrayList<String> pictureUrl = null;
            if (user != null) {
                String photo = (String) user.get(User.str_photo);
                if (photo == null || "".equals(photo)) {
                    holder.iv_photo.setImageResource(R.drawable.persion_default_photo);
                } else
                    Glide.with(mActivity).load(photo).into(holder.iv_photo);
                holder.tv_name.setText((String) user.get(User.str_accountName));
                if (!TextUtils.isEmpty((String)user.get(User.str_attestation_true))) {
                    holder.iv_attestation.setVisibility(View.VISIBLE);
                }
            }
            //获取每个人的多张图片
            pictureUrl = (ArrayList<String>) content.get(PublishContent.str_picture);
            if (pictureUrl == null)
                pictureUrl = new ArrayList<>();
            //将图片填充九宫格
            final ArrayList<String> finalPictureUrl = pictureUrl;
            holder.gv_picture.setUrls(finalPictureUrl);
            holder.gv_picture.setOnItemClickListener(new LGNineGrideView.OnItemClickListener() {
                @Override
                public void onClickItem(int position, View view) {
                    Intent intent = new Intent(mActivity, PhotoViewActivity.class);
                    intent.putExtra(PhotoViewActivity.position, position);
                    intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, (ArrayList<String>) finalPictureUrl);
                    mActivity.startActivity(intent);
                }
            });
            //转发点击事件监听
            holder.rl_transmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, PublishActivity.class);
                    intent.putExtra(PublishActivity.str_publish_content_ser, content.toString());
                    intent.putExtra(PublishActivity.str_publish_remind_user_ser, user.toString());
                    intent.putExtra(PublishActivity.str_type, PublishActivity.transmit);
                    mActivity.startActivityForResult(intent, HomeActivity.REQUEST_RESULT_CODE_PUBLISH);
                }
            });
            //评论点击事件监听
            holder.rl_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnterActivityUtils.enterPublishPersionActivity(mActivity, content);
                }
            });
            //点赞点击事件监听
            holder.rl_praise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    judgeIsAlreadyPraise(user, content, holder);
                }
            });

            holder.tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnterActivityUtils.enterFriendMainPager(mActivity, user);
                }
            });

            holder.iv_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnterActivityUtils.enterFriendMainPager(mActivity, user);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EnterActivityUtils.enterPublishPersionActivity(mActivity, content);
                }
            });
            return convertView;
        }
    }

    public void judgeIsAlreadyPraise(final AVObject praisedUser, final AVObject publish,
                                     final ViewHolder holder) {
        AVQuery<AVObject> query = new AVQuery<>(Praise.publishPraise);
        query.whereEqualTo(Praise.praiseUser, MyApplication.me);
        query.whereEqualTo(Praise.publish, publish);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {//获取数据成功
                    if (list.size() == 0)//关联对象不存在或者网络错误查询失败
                        savePraise(praisedUser, publish, holder);
                    else {
                        ToastUtils.showToast(mActivity, "已赞过");
                    }
                } else if (e.getCode() == 101) {//若e.getCode = 101说明对象不存在，对象不存在，保存对象
                    savePraise(praisedUser, publish, holder);
                } else {
                    ToastUtils.showToast(mActivity, "点赞失败");
                }
            }
        });
    }

    private void savePraise(final AVObject praisedUser, final AVObject publish,
                            final ViewHolder holder) {
        //创建点赞对象
        AVObject object = new AVObject(Praise.publishPraise);
        //被点赞的用户
        object.put(Praise.praisedUser, praisedUser);
        //主动点赞的用户
        object.put(Praise.praiseUser, MyApplication.me);
        //点赞的动态
        object.put(Praise.publish, publish);
        //点赞是否被阅读
        object.put(Praise.isLook, false);
        //保存点赞
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //更新点赞数量
                    String str_currentPraiseCount = (String) holder.tv_praise.getText();
                    int currentCount = 0;
                    if (str_currentPraiseCount != null && !"".equals(str_currentPraiseCount)) {
                        currentCount = Integer.parseInt(str_currentPraiseCount.toString().trim());
                    }
                    final int finalCurrentCount = currentCount;
                    //更新服务器端点赞的次数
                    publish.increment(PublishContent.str_praise_count);
                    publish.setFetchWhenSave(true);
                    publish.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            //更新说说中的点赞的次数
                            holder.tv_praise.setText((finalCurrentCount + 1) + "");

                        }
                    });
                    LivenessUtils.updataLiveness(MyApplication.me, Liveness.str_praise);
                    LivenessUtils.updataLiveness((AVUser) praisedUser, Liveness.str_praiseed);
                    ToastUtils.showToast(mActivity, "点赞成功");
                } else {
                    ToastUtils.showToast(mActivity, "点赞失败" + e.getCode());
                }
            }
        });
    }

    static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_text;
        public LGNineGrideView gv_picture;
        public TextView tv_transmit;
        public TextView tv_comment;
        public TextView tv_praise;
        public RelativeLayout rl_transmit;
        public RelativeLayout rl_comment;
        public RelativeLayout rl_praise;
        public TextView tv_transmitText;
        public ImageView iv_attestation;
        public LinearLayout ll_transmitColor;
    }
}
