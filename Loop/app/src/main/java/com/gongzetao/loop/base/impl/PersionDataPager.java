package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.MediumUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.utils.DialogUtils;
import com.lidroid.xutils.BitmapUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by baixinping on 2016/8/7.
 */
public class PersionDataPager extends HomeBasePager {
    public PersionDataPager(Activity activity) {
        super(activity);
    }

    BitmapUtils utils = new BitmapUtils(mActivity);

    RelativeLayout rl_photo;
    ImageView iv_photo;
    RelativeLayout rl_name;
    TextView tv_name;
    RelativeLayout rl_phoneNumber;
    TextView tv_phoneNumber;
    RelativeLayout rl_attestation;
    TextView tv_attestation;
    RelativeLayout rl_sex;
    TextView tv_sex;
    RelativeLayout rl_bornTime;
    TextView tv_bornTime;


    AVObject userInfo;

    private List<PhotoInfo> mPhotoList;
    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                if (mPhotoList == null)
                    mPhotoList = new ArrayList<PhotoInfo>();
                mPhotoList.addAll(resultList);
                utils.display(iv_photo, mPhotoList.get(0).getPhotoPath());
                //上传保存图像
                savePhotoInServer();
                upDateMe();
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            ToastUtils.showToast(mActivity, "选择图片资源失败");
        }
    };

    private void upDateMe() {
        AVQuery<AVUser> query = new AVQuery<AVUser>("_User");//创建查询对象
        query.whereEqualTo(User.str_mail, app.accountNumber);
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

    private void savePhotoInServer() {
        final File file = new File(mPhotoList.get(0).getPhotoPath());
        AVFile avFile = null;
        try {
            avFile = AVFile.withFile(file.getName(), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final AVFile finalFinal = avFile;
        finalFinal.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                    user.put(User.str_photo, finalFinal.getUrl());
                    user.saveInBackground();
                }
            }
        });

    }

    @Override
    public void initData() {
        initTitle();
        initPersionDataUI();
        initPersionInfo();
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
        String photo = (String) userInfo.get(User.str_photo);
        if (photo != null && "".equals(photo))
            utils.display(iv_photo, photo);
        tv_name.setText((CharSequence) userInfo.get(User.str_accountName));
        tv_phoneNumber.setText((CharSequence) userInfo.get(User.str_phone_number));
        Long time = (Long) userInfo.get(User.str_born_time);
        if (time != null)
            tv_bornTime.setText(DataUtils.getDataTimeTextNoH(new Date(time)));
        String attestation = (String) userInfo.get(User.str_attestation);
        if ("".equals(attestation) || attestation == null)
            tv_attestation.setText("未认证");
        else
            tv_attestation.setText(attestation);
        tv_sex.setText((CharSequence) userInfo.get(User.str_sex));
    }

    private void initPersionDataUI() {
        iv_IconLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) mActivity).viewPager.setCurrentItem(((HomeActivity) mActivity).PAGER_PERSION, false);
            }
        });
        fl_pager.removeAllViews();
        View persionDataView = View.inflate(mActivity, R.layout.layout_home_pager_persion_data, null);
        fl_pager.addView(persionDataView);
        rl_photo = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_photo);
        iv_photo = (ImageView) persionDataView.findViewById(R.id.persion_data_iv_photo);
        rl_name = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_name);
        tv_name = (TextView) persionDataView.findViewById(R.id.persion_data_tv_name);
        rl_phoneNumber = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_phone_number);
        tv_phoneNumber = (TextView) persionDataView.findViewById(R.id.persion_data_tv_phone_number);
        rl_attestation = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_attestation);
        tv_attestation = (TextView) persionDataView.findViewById(R.id.persion_data_tv_attestation);
        rl_sex = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_sex);
        tv_sex = (TextView) persionDataView.findViewById(R.id.persion_data_tv_sex);
        rl_bornTime = (RelativeLayout) persionDataView.findViewById(R.id.persion_data_rl_born_time);
        tv_bornTime = (TextView) persionDataView.findViewById(R.id.persion_data_tv_born_time);

        rl_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediumUtils.openPhoto(mActivity, mOnHanlderResultCallback, mPhotoList, 1);
            }
        });

        rl_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogUtils utils = new DialogUtils();
                utils.setOnButtonClickListener(new DialogUtils.OnButtonClickListener() {
                    @Override
                    public void clickCancel() {

                    }

                    @Override
                    public void clickOk() {
                        final String name = utils.ev_value.getText().toString().trim();
                        if ("".equals(name) || name == null)
                            return;
                        AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                        user.put(User.str_accountName, name);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                tv_name.setText(name);
                                upDateMe();
                            }
                        });
                    }
                });
                utils.openUpdateDialog(mActivity);
                utils.ev_value.setText(tv_name.getText().toString());
                utils.setTvChageHintText("");
            }
        });
        rl_phoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DialogUtils utils = new DialogUtils();
                utils.setOnButtonClickListener(new DialogUtils.OnButtonClickListener() {
                    @Override
                    public void clickCancel() {
                    }

                    @Override
                    public void clickOk() {
                        final String phoneNumber = utils.ev_value.getText().toString().trim();
                        if ("".equals(phoneNumber) || phoneNumber == null)
                            return;
                        AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                        user.put(User.str_phone_number, phoneNumber);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                tv_phoneNumber.setText(phoneNumber);
                                upDateMe();
                            }
                        });
                    }
                });
                utils.openUpdateDialog(mActivity);
                utils.ev_value.setText(tv_phoneNumber.getText().toString());
                utils.setTvChageHintText("");
            }
        });
        rl_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogUtils utils = new DialogUtils();
                utils.setOnDateChange(new DialogUtils.OnDateChange() {
                    @Override
                    public void dateChange() {
                        AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                        user.put(User.str_sex, utils.rb_sex.getText());
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                tv_sex.setText(utils.rb_sex.getText());
                                upDateMe();
                            }
                        });
                    }
                });
                utils.openSexSelectDialog(mActivity, 1);
            }
        });
        rl_bornTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogUtils utils = new DialogUtils();
                utils.setOnDateChange(new DialogUtils.OnDateChange() {
                    @Override
                    public void dateChange() {
                        AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
                        user.put(User.str_born_time, utils.longDate);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                tv_bornTime.setText(utils.strDate);
                                upDateMe();
                            }
                        });
                    }
                });
                Long time = (Long) userInfo.get(User.str_born_time);
                int year = 2000;
                int month = 01;
                int day = 01;
                if (time != null) {
                    Date date = new Date(time);
                    year = date.getYear();
                    month = date.getMonth();
                    day = date.getDay();
                }
                utils.openDateSelectDialog(mActivity, year, month, day);
//                utils.ev_value.setText(tv_bornTime.getText().toString());
            }
        });
        String userPhoto = (String)MyApplication.me.get(User.str_photo);
        if(!TextUtils.isEmpty(userPhoto))
            Glide.with(mActivity).load(userPhoto).into(iv_photo);
    }

    private void initTitle() {
        tv_PagerDes.setText("个人资料");
        tv_PagerDes.setVisibility(View.VISIBLE);
        iv_IconAdd.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.INVISIBLE);
        iv_IconLast.setVisibility(View.VISIBLE);
    }
}
