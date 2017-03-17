package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.PublishPersionAdapter;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.Praise;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.RefreshListView;
import com.gongzetao.loop.view.photoBrowser.ui.PhotoViewActivity;
import com.gongzetao.loop.view.lGNineGrideView.LGNineGrideView;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/25.
 */
public class PublishPersionActivity extends BaseActivity {

    public static final int requestCode_comment = 0;
    List<AVObject> commentTimeList;
    List<AVObject> commentPraiseCountList;
    RefreshListView lv_commentList;
    RelativeLayout rl_transmit;
    RelativeLayout rl_comment;
    RelativeLayout rl_praise;
    TextView tv_transmit;
    TextView tv_comment;
    TextView tv_praise;
    ImageView iv_last;
    View viewHeader;

    private boolean firstEnter = true;


    PublishPersionAdapter adapter;
    //得到该说说的评论
    AVQuery<AVObject> query = null;
    public static String publishContentSer = "publishContentSer";
    AVObject publishContent;

//    public int commentCount;

    int skip = 0;  //用于进行刷新分页操作时用于进行位置的标志
    public static final int REFRESH = 0;
    public static final int ADD = 1;
    public static final int ONCE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_persion);
        initUI();
        initData();
    }

    //初始化内容
    private void initContent() {
        if (commentPraiseCountList == null)
            commentPraiseCountList = new ArrayList<AVObject>();
        if (commentTimeList == null)
            commentTimeList = new ArrayList<AVObject>();
        adapter = new PublishPersionAdapter(this, commentPraiseCountList, commentTimeList, publishContent);
        lv_commentList.setAdapter(adapter);
        if (firstEnter) {
            initHeader();
            lv_commentList.addHeaderView(viewHeader);
            firstEnter = false;
        }
    }

    private void initHeader() {
        viewHeader = View.inflate(this, R.layout.activity_publish_persion_comment_header, null);
        TextView tv_transmitText = (TextView) viewHeader.findViewById(R.id.publish_comment_header_tv_transmit_text);
        CircleImageView iv_photo = (CircleImageView) viewHeader.findViewById(R.id.publish_comment_header_iv_photo);
        TextView tv_name = (TextView) viewHeader.findViewById(R.id.publish_comment_header_tv_name);
        TextView tv_time = (TextView) viewHeader.findViewById(R.id.publish_comment_header_tv_time);
        TextView tv_text = (TextView) viewHeader.findViewById(R.id.publish_comment_header_tv_text);
        LinearLayout ll_transmitContent = (LinearLayout) viewHeader.findViewById(R.id.publish_comment_header_ll_transmit_content);
        LGNineGrideView gv_picture = (LGNineGrideView) viewHeader.findViewById(R.id.publish_comment_header_gv_picture);
        final AVObject user = publishContent.getAVObject(PublishContent.str_user);

        Boolean isTransmit = (Boolean) publishContent.get(PublishContent.str_is_transmit);
        if (isTransmit != null)
            if (isTransmit) {
                String transmitText = (String) publishContent.get(PublishContent.str_transmit_text);
                tv_transmitText.setVisibility(View.VISIBLE);
                ll_transmitContent.setBackgroundResource(R.color.lineColor);
                tv_transmitText.setText(SpanableStringUtils.getInstace().getExpressionString(PublishPersionActivity.this, transmitText + ""));
                AVUser transmitedUser = (AVUser) publishContent.get(PublishContent.str_transmited_user);
                if (transmitedUser != null) {
                    String text = "@" + transmitedUser.get(User.str_accountName) +
                            " :" + (publishContent.get(PublishContent.str_text));
                    tv_text.setText(SpanableStringUtils.getInstace().
                            getExpressionString(PublishPersionActivity.this,
                                    text));
                }
            } else{
                tv_transmitText.setVisibility(View.GONE);
                ll_transmitContent.setBackgroundResource(R.color.white);
                tv_text.setText(SpanableStringUtils.getInstace().
                        getExpressionString(PublishPersionActivity.this,
                                (String) (publishContent.get(PublishContent.str_text))));
            }
        Date time = publishContent.getCreatedAt();
        if (time == null)
            time = new Date();
        tv_time.setText(DataUtils.getDataTimeText(time));
        if (user != null) {
            Glide.with(this).load((String) user.get(User.str_photo)).into(iv_photo);
            tv_name.setText((String) user.get(User.str_accountName));
        }
        //获取每个人的多张图片
        JSONArray jsonArray = (JSONArray) (publishContent.get(PublishContent.str_picture));
        final List<String> pictureUrls = new ArrayList<String>();
        if (jsonArray != null && jsonArray.size() > 0)
            for (int i = 0; i < jsonArray.size(); i++) {
                pictureUrls.add((String) jsonArray.get(i));
            }
        if (pictureUrls.size() == 0) {
            gv_picture.setVisibility(View.GONE);
            return;
        }
        gv_picture.setUrls(pictureUrls);
        gv_picture.setOnItemClickListener(new LGNineGrideView.OnItemClickListener() {
            @Override
            public void onClickItem(int position, View view) {
                Intent intent = new Intent(PublishPersionActivity.this, PhotoViewActivity.class);
                intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, (ArrayList<String>) pictureUrls);
                PublishPersionActivity.this.startActivity(intent);
            }
        });
    }

    /**
     * 获取评论的数量
     */
//    public void getCommentCount() {
//        AVQuery<AVObject> query = new AVQuery<AVObject>(Comment.str_comment);
//        query.whereEqualTo(Comment.str_publish, publishContent);
//        query.countInBackground(new CountCallback() {
//            @Override
//            public void done(int i, AVException e) {
//                commentCount = i;
//                initData();
//            }
//        });
//    }

    /**
     * 获取数据
     */
    private void initData() {
        //此处初始化是由于需要两次不同条件的查询，只初始化一次
        query = new AVQuery<AVObject>(Comment.str_comment);//创建查询对象

        try {
            //取出传过来的说说内容
            publishContent = AVObject.parseAVObject(getIntent().getStringExtra(publishContentSer));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Integer commentCount = (Integer) publishContent.get(PublishContent.str_comment_count);
        if (commentCount == null) {
            tv_comment.setText("0");
            commentCount = 0;
        } else
            tv_comment.setText(commentCount + "");
        if (commentCount > 20)
            getCommentPraiseCountList(ONCE);
        else
            getCommentTimeList(ONCE);

        Integer praiseCount = (Integer) publishContent.get(PublishContent.str_praise_count);
        if (praiseCount == null) {
            tv_praise.setText("0");
            praiseCount = 0;
        } else
            tv_praise.setText(praiseCount + "");

        Integer transmitCount = (Integer) publishContent.get(PublishContent.str_transmit_count);
        if (transmitCount == null) {
            tv_transmit.setText("0");
            transmitCount = 0;
        } else
            tv_transmit.setText(transmitCount + "");
    }

    /**
     * 通过点赞数量获取评论
     * @param state
     */
    private void getCommentPraiseCountList(final int state) {
        query.limit(3);// 最多返回结果数
        query.include(Comment.str_comment_user);//得到评论说说的人的信息
        query.include(Comment.commentRemindUser);
        query.whereEqualTo(Comment.str_publish, publishContent);
        //先从缓存加载，加载失败再从网络加载
        if (state == ONCE)
            query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        else
            query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.skip(0);//跳过多少skip进行加载
        query.orderByDescending(Comment.str_praise_count);//按照评论的数量进行排序
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (list == null || list.size() == 0) {
                    initContent();
                    return;
                } else
                    commentPraiseCountList = list;// 获取数据成功
                getCommentTimeList(state);
            }
        });
    }

    /**
     * 通过时间顺序获取评论
     * @param state
     */
    public void getCommentTimeList(final int state) {
        query.limit(10);// 最多返回结果数
        query.include(Comment.str_comment_user);//得到评论说说的人的信息
        query.include(Comment.commentRemindUser);
        //先从缓存加载，加载失败再从网络加载
        if (state == ONCE)
            query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        else
            query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.skip(skip);//跳过多少skip进行加载
        query.orderByDescending("createdAt");//按照时间升序排列
        query.whereEqualTo(Comment.str_publish, publishContent);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                //先从缓存加载，再从网络加载，若网络加载失败则不更新界面0
                if (list == null) {//若获取到的数据为空则不更新界面
                    initContent();
                    return;
                }
                if (list.size() == 0) {
                    if (state == ONCE)
                        initContent();
                    else
                        lv_commentList.allLoading();//说明已经加载全部
                    return;
                }
                //获取数据源
                if (state == REFRESH || state == ONCE)
                    commentTimeList = list;// 若是刷新操作或者第一次加载，直接赋值
                if (state == ADD)
                    commentTimeList.addAll(list);//若是加载更多，则将数据直接加入

                //刷新UI
                if (state == ONCE) {
                    initContent();//若是第一次加载，初始化adapter
                    return;
                }
                //为刷新和加载更多初始化adapter数据
                adapter.setCommentPraiseListAndCommentTimeList(commentPraiseCountList,
                        commentTimeList);
                if (state == REFRESH) {
                    adapter.notifyDataSetChanged();//若是刷新操作，直接更新UI
                    lv_commentList.completeRefresh();
                    return;
                }
                if (state == ADD) {
                    adapter.notifyDataSetChanged();//若是刷新操作，直接更新UI
                    lv_commentList.completeLoad();
                }
            }
        });
    }

    private void initUI() {
        lv_commentList = (RefreshListView) findViewById(R.id.publish_persion_lv_comment_list);
        rl_transmit = (RelativeLayout) findViewById(R.id.publish_persion_rl_transmit);
        rl_comment = (RelativeLayout) findViewById(R.id.publish_persion_rl_comment);
        rl_praise = (RelativeLayout) findViewById(R.id.publish_persion_rl_praise);
        tv_transmit = (TextView) findViewById(R.id.publish_persion_tv_transmit);
        tv_comment = (TextView) findViewById(R.id.publish_persion_tv_comment);
        tv_praise = (TextView) findViewById(R.id.publish_persion_tv_praise);
        iv_last = (ImageView) findViewById(R.id.publish_persion_iv_icon_last);
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lv_commentList.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                //下拉刷新执行操作
                skip = 0;//
                initData();
                lv_commentList.completeRefresh();
            }

            @Override
            public void loading() {
                //上划加载更多执行操作
                skip += 10;
                getCommentTimeList(ADD);
            }
        });
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.publish_persion_rl_transmit:
                Intent intent = new Intent(PublishPersionActivity.this, PublishActivity.class);
                intent.putExtra(PublishActivity.str_publish_content_ser, publishContent.toString());
                intent.putExtra(PublishActivity.str_publish_remind_user_ser, ((AVUser)publishContent.get(PublishContent.str_user)).toString());
                intent.putExtra(PublishActivity.str_type, PublishActivity.transmit);
                PublishPersionActivity.this.startActivityForResult(intent, HomeActivity.REQUEST_RESULT_CODE_PUBLISH);
                break;
            case R.id.publish_persion_rl_comment:
                Intent commentIntent = new Intent(this, PublishActivity.class);
                commentIntent.putExtra(PublishActivity.str_type, PublishActivity.comment);
                commentIntent.putExtra(PublishActivity.str_publish_content_ser, publishContent.toString());
                this.startActivityForResult(commentIntent, requestCode_comment);
                break;
            case R.id.publish_persion_rl_praise:
                AVUser user = (AVUser) publishContent.get(PublishContent.str_user);
                judgeIsPublishAlreadyPraise(user, publishContent, tv_praise);
                break;
            case R.id.publish_persion_iv_icon_last:
                finish();
                break;
        }
    }

    /**
     * 判断动态是否已经点赞
     *
     * @param praisedUser
     * @param publish
     * @param tv_praise
     */
    public void judgeIsPublishAlreadyPraise(final AVObject praisedUser, final AVObject publish,
                                            final TextView tv_praise) {
        AVQuery<AVObject> query = new AVQuery<>(Praise.publishPraise);
        query.whereEqualTo(Praise.praiseUser, MyApplication.me);
        query.whereEqualTo(Praise.publish, publish);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null){//获取数据成功
                    if (list.size() == 0)//关联对象不存在或者网络错误查询失败
                        savePublishPraise(praisedUser, publish, tv_praise);
                    else {
                        ToastUtils.showToast(PublishPersionActivity.this, "已赞过");
                    }
                }else if (e.getCode() == 101){//若e.getCode = 101说明对象不存在，对象不存在，保存对象
                    savePublishPraise(praisedUser, publish, tv_praise);
                }else {
                    ToastUtils.showToast(PublishPersionActivity.this, "点赞失败");
                }
            }
        });

    }

    private void savePublishPraise(AVObject praisedUser, final AVObject publish,
                                   final TextView tv_praise) {
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
                    String str_currentPraiseCount = (String) tv_praise.getText();
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
                            tv_praise.setText((finalCurrentCount + 1) + "");

                        }
                    });
                    ToastUtils.showToast(PublishPersionActivity.this, "点赞成功");
                } else {
                    ToastUtils.showToast(PublishPersionActivity.this, "点赞失败" + e.getCode());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case requestCode_comment:
                if (resultCode == RESULT_OK) {
                    String text = data.getStringExtra(Comment.str_comment_text);
                    AVObject comment = new AVObject();
                    comment.put(Comment.str_comment_text, text);
                    comment.put(Comment.str_comment_user, MyApplication.me);
                    commentTimeList.add(0, comment);
                    adapter.setCommentPraiseListAndCommentTimeList(commentPraiseCountList, commentTimeList);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
