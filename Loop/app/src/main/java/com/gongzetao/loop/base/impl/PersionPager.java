package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.ApplicationAttestationActivity;
import com.gongzetao.loop.activity.FriendMainPagerActivity;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.bean.Position;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DialogUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LocalPositionUtils;
import com.gongzetao.loop.utils.LocationUtils;
import com.gongzetao.loop.utils.MotionEventUtils;
import com.gongzetao.loop.utils.SerUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.List;

/**
 * Created by baixinping on 2016/8/5.
 */
public class PersionPager extends HomeBasePager {
    public PersionPager(Activity activity) {
        super(activity);
    }


    LinearLayout ll_persion_title;
    LinearLayout ll_setting;
    LinearLayout ll_share;
    LinearLayout ll_lable;
    public TextView tv_lable;
    TextView tv_persion_attestation;

    ImageView iv_photo;
    TextView tv_local;


    AVUser userInfo;

    List<Position> positions;

    @Override
    public void initData() {
        initPositions();
        iniTitle();
        initPersionUI();
        initLocal();
        initPersionInfo();
    }

    /**
     * 初始化位置信息
     */
    private void initPositions() {
        positions = LocalPositionUtils.getPositions();
    }

    private void initPersionInfo() {
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");
        query.whereEqualTo(User.str_mail, MyApplication.me.getEmail());
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (e == null) {
                    userInfo = list.get(0);
                    initPersionData();
                }
            }
        });
    }

    private void initPersionData() {

        if (userInfo != null) {
            String photo = (String) userInfo.get(User.str_photo);
            if (photo != null && !"".equals(photo))
                Glide.with(mActivity).load(photo).into(iv_photo);
        }

    }

    /**
     * 初始化当前位置
     */
    private void initLocal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LocationUtils utils = new LocationUtils(mActivity);
                utils.setLocationSucceedListener(new LocationUtils.LocationSucceedListener() {
                    @Override
                    public void localSucceed(AMapLocation aMapLocation) {
                        //获取当前位置的经纬度
                        double lat = aMapLocation.getLatitude();
                        double lon = aMapLocation.getLongitude();
//                        tv_local.setText(aMapLocation.getPoiName() + " lat " + lat + " lon " + lon);
                        //获取当前位置
                        Position currentPosition = LocalPositionUtils.getCurrentPosition(lon, lat);
                        if (currentPosition != null) {
                            tv_local.setText(currentPosition.getName());
                            //将当前位置进行缓存
                            SerUtils.writeObject(mActivity, Position.currentPosition, currentPosition);
                        } else {
                            tv_local.setText(aMapLocation.getPoiName());
                            SerUtils.writeObject(mActivity, Position.currentPosition,
                                    new Position(lon, lat, aMapLocation.getPoiName()));
                        }

                    }

                    @Override
                    public void localFail() {
                        //获取数据失败则将缓存中的数据进行加载
                        Position position = (Position) SerUtils.readObject(mActivity, Position.currentPosition);
                        if (position != null) {
                            tv_local.setText(position.getName());
                        }
                    }

                });

                utils.startLocation();
            }
        }).start();
    }

    private void initPersionUI() {
        //将之前添加view清除
        fl_pager.removeAllViews();
        View persionPager = View.inflate(mActivity, R.layout.layout_home_pager_persion, null);
        fl_pager.addView(persionPager);

        ll_share = (LinearLayout) persionPager.findViewById(R.id.persion_ll_persion_share);
        ll_lable = (LinearLayout) persionPager.findViewById(R.id.persion_ll_persion_lable);
        ll_persion_title = (LinearLayout) persionPager.findViewById(R.id.persion_ll_persion_title);
        ll_setting = (LinearLayout) persionPager.findViewById(R.id.persion_ll_setting);
        tv_lable = (TextView) persionPager.findViewById(R.id.persion_tv_persion_lable);
        tv_lable.setText((String) MyApplication.me.get(User.str_table));
        iv_photo = (ImageView) persionPager.findViewById(R.id.persion_iv_persion_photo);
        tv_local = (TextView) persionPager.findViewById(R.id.persion_tv_persion_local);
        tv_persion_attestation = (TextView) persionPager.findViewById(R.id.persion_tv_persion_attestation);
        MotionEventUtils.setMotionEvent(ll_setting, mActivity);
        MotionEventUtils.setMotionEvent(ll_lable, mActivity);
        MotionEventUtils.setMotionEvent(ll_share, mActivity);
        MotionEventUtils.setMotionEvent(ll_persion_title, mActivity);

        //用户头像点击事件
        ll_persion_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) mActivity).viewPager.setCurrentItem(((HomeActivity) mActivity).PAGER_PERSION_DATA, false);
            }
        });
        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, FriendMainPagerActivity.class);
                intent.putExtra(FriendMainPagerActivity.ser_user_info, MyApplication.me.toString());
                mActivity.startActivity(intent);
            }
        });
        MotionEventUtils.setMotionEvent(tv_persion_attestation,mActivity);
        tv_persion_attestation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ApplicationAttestationActivity.class);
                mActivity.startActivity(intent);
            }
        });


        //设置点击事件
        ll_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) mActivity).viewPager.setCurrentItem(((HomeActivity) mActivity).PAGER_SETTING_DATA, false);

            }
        });
        final String beforeLable = tv_lable.getText().toString().trim();
        ll_lable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnterActivityUtils.enterRecommendActivity(mActivity, HomeActivity.REQUEST_RESULT_CODE_UPDATE_LABEL);

//
            }
        });

    }

    private void initMe() {
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, MyApplication.me.getEmail());
        query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<AVUser>() {
            @Override
            public void done(List<AVUser> list, AVException e) {
                if (list != null && list.size() > 0) {
                    MyApplication.me = list.get(0);
                    userInfo = MyApplication.me;
                }
            }
        });
    }

    private void iniTitle() {
        tv_PagerDes.setText("个人");
        tv_PagerDes.setVisibility(View.VISIBLE);
        iv_IconAdd.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.INVISIBLE);
        iv_IconLast.setVisibility(View.INVISIBLE);
    }
}
