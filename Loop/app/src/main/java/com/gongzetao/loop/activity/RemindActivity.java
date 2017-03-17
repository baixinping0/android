package com.gongzetao.loop.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.RemindAdapter;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.Remind;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.StatusbarsUtils;
import com.gongzetao.loop.view.RefreshListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class RemindActivity extends AppCompatActivity {
    RefreshListView lv_remind;
    ImageView iv_last;
    List<AVObject> reminds;
    List<AVObject> unRead;
    TextView tv_title;

    public static final int REFRESH = 0;
    public static final int ADD = 1;
    int skip = 0;
    RemindAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind);
        initUI();
        initData(REFRESH);
        getUnRead();
        StatusbarsUtils.setStatusbars(R.color.myBlueTop, this);
    }

    private void initContent() {
        //创建listview适配器
        if (reminds == null)
            reminds = new ArrayList<AVObject>();
        if (adapter == null) {
            adapter = new RemindAdapter(this, reminds);
            lv_remind.setAdapter(adapter);
        } else {
            adapter.setReminds(reminds);
            adapter.notifyDataSetChanged();
        }
    }

    private void getUnRead() {
        AVQuery<AVObject> query = new AVQuery<AVObject>(Remind.remind);//创建查询对象
        query.whereEqualTo(Remind.remindedUser, MyApplication.me);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);//刷新和加载更多直接从网络加载
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null)
                    unRead = list;
                initUnReadToAlready();
            }
        });
    }

    private void initUnReadToAlready() {
        if (unRead != null)
            for (int i = 0; i < unRead.size(); i++) {
                AVObject object = unRead.get(i);
                object.put(Remind.isLook, true);
                object.saveInBackground();
            }
    }

    private void initData(final int state) {
        AVQuery<AVObject> query = new AVQuery<AVObject>(Remind.remind);//创建查询对象
        query.limit(5);     //最多返回10条结果
        query.skip(skip);   //从第skip条开始获取
        query.whereEqualTo(Remind.remindedUser, MyApplication.me);
        query.include(Remind.remindedUser);
        query.include(Remind.remindUser);
        query.include(Remind.remindPublish);
        query.include(Remind.remindPublish + "." + PublishContent.str_user);
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
                        lv_remind.loadFail();
                    if (state == REFRESH)
                        lv_remind.refreshFail();//初始化数据完成之后
                    return;
                }

                //获取数据源
                if (state == REFRESH) {
                    reminds = list;// 若是刷新操作或者第一次加载，直接赋值
                    initContent();
                    LogUtils.MyLog("加载数据。。。");
                    lv_remind.completeRefresh();
                    return;
                }
                if (state == ADD) {
                    reminds.addAll(list);//若是加载更多，则将数据直接加入
                    initContent();
                    if (list.size() > 0)
                        lv_remind.completeLoad();
                    else
                        lv_remind.allLoading();
                }
            }
        });
    }

    private void initUI() {
        lv_remind = (RefreshListView) findViewById(R.id.activity_remind_list_remind);
        iv_last = (ImageView) findViewById(R.id.title_icon_last);
        tv_title = (TextView) findViewById(R.id.title_tv_title);
        tv_title.setText("@我的");
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
        lv_remind.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
