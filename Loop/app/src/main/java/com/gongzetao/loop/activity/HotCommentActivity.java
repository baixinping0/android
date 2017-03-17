package com.gongzetao.loop.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.HotCommentAdapter;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.view.RefreshListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/8/25.
 */
public class HotCommentActivity extends BaseActivity {
    List<AVObject> commentList;
    HotCommentAdapter adapter;
    RefreshListView lv_comment;
    ImageView iv_lase;

    int skip = 0;
    public static final int REFRESH = 1000;
    public static final int ADD = 1001;
    public static final int ONCE = 1002;

    AVObject publishContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_comment);
        initUI();
        try {
            publishContent = AVObject.parseAVObject(getIntent().getStringExtra(PublishPersionActivity.publishContentSer));
        } catch (Exception e) {
            e.printStackTrace();
        }
        initData(ONCE);

    }

    private void initContent() {
        if (commentList == null)
            commentList = new ArrayList<AVObject>();
        adapter = new HotCommentAdapter(this, commentList, publishContent);
        lv_comment.setAdapter(adapter);
    }

    private void initData(final int state) {
        AVQuery query = new AVQuery<AVObject>(Comment.str_comment);//创建查询对象
        query.limit(3);// 最多返回结果数
        query.include(Comment.str_comment_user);//得到评论说说的人的信息
        query.whereEqualTo(Comment.str_publish, publishContent);
        //先从缓存加载，加载失败再从网络加载
        if (state == ONCE)
            query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        else
            query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.skip(skip);//跳过多少skip进行加载
        query.orderByDescending(Comment.str_praise_count);//按照评论的数量进行排序
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                //先从缓存加载，再从网络加载，若网络加载失败则不更新界面0
                if (list == null)//若获取到的数据为空则不更新界面
                    return;
                if (list.size() == 0){
                    lv_comment.allLoading();//说明已经加载全部
                    return;
                }
                //获取数据源
                if (state == REFRESH || state == ONCE)
                    commentList = list;// 若是刷新操作或者第一次加载，直接赋值
                if (state == ADD)
                    commentList.addAll(list);//若是加载更多，则将数据直接加入

                //刷新UI
                if (state == ONCE){
                    initContent();//若是第一次加载，初始化adapter
                    return;
                }
                //为刷新和加载更多初始化adapter数据
                adapter.setCommentList(commentList);
                if (state == REFRESH) {
                    adapter.notifyDataSetChanged();//若是刷新操作，直接更新UI
                    lv_comment.completeRefresh();
                    return;
                }
                if (state == ADD) {
                    adapter.notifyDataSetChanged();//若是刷新操作，直接更新UI
                    lv_comment.completeLoad();
                }
            }
        });
    }

    private void initUI() {
        lv_comment = (RefreshListView) findViewById(R.id.hot_comment_lv_comment);
        iv_lase = (ImageView) findViewById(R.id.hot_comment_iv_icon_last);
        iv_lase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lv_comment.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                skip = 0;
                initData(REFRESH);
            }

            @Override
            public void loading() {
                skip += 5;
                initData(ADD);
            }
        });
    }
}
