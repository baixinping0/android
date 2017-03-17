package com.gongzetao.loop.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by baixinping on 2016/7/14.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    ImageView iv_arrow;
    ProgressBar pb_ring;
    ProgressBar pb_loading;
    TextView tv_des, tv_load;

    int headerViewHeight = 0;
    View headerView;
    float downY = 0;
    float moveY = 0;

    int headerCount = 1;

    View footerView;
    int footerViewHeight;
    boolean isLoading = true;

    final int PULL_STATE = 1;
    final int FREE_STATE = 2;
    final int REFRESH_STATE = 3;
    int currentState = PULL_STATE;

    final int REFRESH_SUCCEED = 1;
    final int LOAD_SUCCEED = 2;
    final int REFRESH_FAIL = 3;
    final int LOAD_FAIL = 4;
    final int ALL_LOADING = 5;

    OnRefreshListener listener;
    private boolean permitLoading = true;
    private boolean permitRefresh = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PULL_STATE://下拉状态
                    iv_arrowRotateDown();
                    pb_ring.setVisibility(View.INVISIBLE);
                    tv_des.setText("下拉刷新");
                    break;
                case FREE_STATE://释放刷新状态
                    iv_arrowRotateUP();
                    //描述改为释放刷新
                    tv_des.setText("释放刷新");
                    break;
                case REFRESH_STATE://刷新状态
                    if (currentState == PULL_STATE) {
                        headerView.setPadding(0, -headerViewHeight, 0, 0);
                    } else {
                        if (permitRefresh){
                            headerView.setPadding(0, 0, 0, 0);
                            iv_arrow.clearAnimation();//因为向上的旋转动画有可能没有执行完
                            iv_arrow.setVisibility(View.INVISIBLE);
                            pb_ring.setVisibility(View.VISIBLE);
                            tv_des.setText("正在刷新");
                            if (listener != null)
                                listener.pullRefresh();
                            currentState = REFRESH_STATE;
                        }
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public void setPermitLoading(boolean permitLoading) {
        this.permitLoading = permitLoading;
    }

    public void setPermitRefresh(boolean permitRefresh) {
        this.permitRefresh = permitRefresh;
    }

    public void iv_arrowRotateUP() {
        //箭头旋转180
        RotateAnimation animation = new RotateAnimation(0, 180, Animation.
                RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(500);
        animation.setFillAfter(true);
        iv_arrow.startAnimation(animation);
    }

    public void iv_arrowRotateDown() {
        //箭头旋转180
        RotateAnimation animation = new RotateAnimation(180, 360, Animation.
                RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(500);
        animation.setFillAfter(true);
        iv_arrow.startAnimation(animation);
    }

    public RefreshListView(Context context) {
        super(context);
        initUI();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnScrollListener(this);
        initUI();
    }

    private void initUI() {
        initHeader();
        initFooter();
    }

    private void initFooter() {
        footerView = View.inflate(getContext(), R.layout.refresh_listview_footer_view, null);
        tv_load = (TextView) footerView.findViewById(R.id.footer_view_load);
        pb_loading = (ProgressBar) footerView.findViewById(R.id.loading_pb);
        addFooterView(footerView);
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);
    }

    private void initHeader() {
        headerView = View.inflate(getContext(), R.layout.refresh_listview_header_view, null);

        iv_arrow = (ImageView) headerView.findViewById(R.id.header_view_arrow);
        pb_ring = (ProgressBar) headerView.findViewById(R.id.header_view_pb);
        tv_des = (TextView) headerView.findViewById(R.id.header_view_tv);

        headerView.measure(0, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        addHeaderView(headerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Message message = new Message();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == REFRESH_STATE)
                    break;
                if (downY == 0) {//第一次点击事件被其他控件获取，导致首次downY为0.
                    downY = (int) ev.getRawY();
                }
                moveY = (int) ev.getRawY();
                int offset = (int) (moveY - downY);

                if (offset > 0 && getFirstVisiblePosition() == 0) {
                    int paddingTop = -headerViewHeight + offset;
                    headerView.setPadding(0, paddingTop, 0, 0);

                    if (paddingTop <= 0 && (currentState == FREE_STATE)) {
                        //下拉状态
                        message.what = PULL_STATE;
                        currentState = PULL_STATE;
                        ;
                        handler.sendMessage(message);
                    } else if (paddingTop > 0 && currentState == PULL_STATE) {
                        //释放状态
                        message.what = FREE_STATE;
                        currentState = FREE_STATE;
                        handler.sendMessage(message);
                    }
                    return true;//拦截listView的滑动事件
                }
                break;
            case MotionEvent.ACTION_UP:
                message.what = REFRESH_STATE;
                handler.sendMessage(message);
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 刷新成功失败调用的方法
     */
    Handler refreshLoadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SUCCEED:
                    LogUtils.MyLog("执行到此。。。刷新最后");
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                    currentState = PULL_STATE;
                    iv_arrow.setVisibility(View.VISIBLE);
                    pb_ring.setVisibility(View.INVISIBLE);
                    tv_des.setText("下拉刷新");
                    break;
                case LOAD_SUCCEED:
                    footerView.setPadding(0, -footerViewHeight, 0, 0);
                    tv_load.setText("加载更多");
                    isLoading = true;
                    break;
                case REFRESH_FAIL:
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                    currentState = PULL_STATE;
                    iv_arrow.setVisibility(View.VISIBLE);
                    pb_ring.setVisibility(View.INVISIBLE);
                    tv_des.setText("下拉刷新");
                    break;
                case LOAD_FAIL:
                    footerView.setPadding(0, -footerViewHeight, 0, 0);
                    tv_load.setText("加载更多");
                    isLoading = true;
                    break;

                case ALL_LOADING:
                    footerView.setPadding(0, -footerViewHeight, 0, 0);
                    tv_load.setText("加载更多");
                    pb_loading.setVisibility(VISIBLE);
                    isLoading = true;
                    break;
            }
        }
    };

    public void completeRefresh() {
        headerCount = getHeaderViewsCount();
        LogUtils.MyLog("执行到此。。。刷新数量 " + headerCount);
        tv_des.setText("刷新成功");
        Message message = new Message();
        message.what = REFRESH_SUCCEED;
        refreshLoadHandler.sendMessageDelayed(message, 1000);
    }

    public void completeLoad() {
        tv_load.setText("加载成功");
        Message message = new Message();
        message.what = LOAD_SUCCEED;
        refreshLoadHandler.sendMessageDelayed(message, 1000);
    }

    public void refreshFail() {
        tv_des.setText("刷新失败");
        Message message = new Message();
        message.what = REFRESH_FAIL;
        refreshLoadHandler.sendMessageDelayed(message, 1000);
    }

    public void loadFail() {
        tv_load.setText("加载失败");
        Message message = new Message();
        message.what = LOAD_FAIL;
        refreshLoadHandler.sendMessageDelayed(message, 1000);
    }

    public void allLoading() {
        pb_loading.setVisibility(GONE);
        tv_load.setText("已全部加载");
        Message message = new Message();
        message.what = LOAD_SUCCEED;
        refreshLoadHandler.sendMessageDelayed(message, 1000);
    }

    public String getRefreshTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }


    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * public static int SCROLL_STATE_IDLE = 0;  闲置状态：手指松开
     * public static int SCROLL_STATE_TOUCH_SCROLL = 1;：  手指触摸滑动
     * public static int SCROLL_STATE_FLING = 2;   惯性滑动
     *
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && getLastVisiblePosition() == (getAdapter().getCount() - 1)
                && isLoading) {
            if (permitLoading){
                isLoading = false;
                footerView.setPadding(0, 0, 0, 0);
                setSelection(getCount()); //将对应的item刚到屏幕顶端
                if (listener != null) {
                    listener.loading();
                }
            }
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public interface OnRefreshListener {
        void pullRefresh();

        void loading();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

}
