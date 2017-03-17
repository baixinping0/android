package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.base.impl.FriendPager;
import com.gongzetao.loop.base.impl.InfoPager;
import com.gongzetao.loop.base.impl.MainPager;
import com.gongzetao.loop.base.impl.PersionDataPager;
import com.gongzetao.loop.base.impl.PersionPager;
import com.gongzetao.loop.base.impl.SettingDataPager;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.Position;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.LivenessUtils;
import com.gongzetao.loop.utils.LocalPositionUtils;
import com.gongzetao.loop.utils.LocationUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.SerUtils;
import com.gongzetao.loop.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/4.
 */
public class HomeActivity extends BaseActivity {

    public static String recentlyContactFileName = "recentlyContact";
    public static String unReadChatMessageFileName = "unReadChatMessage";

    public ViewPager viewPager;
    public List<HomeBasePager> pagerList;
    public RadioGroup rg_icon;

    public static final int REQUEST_RESULT_CODE_PUBLISH = 0;
    public static final int REQUEST_RESULT_CODE_CHAT = 1;
    public static final int REQUEST_RESULT_CODE_REMIND = 2;
    public static final int REQUEST_RESULT_CODE_PRAISE = 3;
    public static final int REQUEST_RESULT_CODE_COMMENT = 4;
    public static final int REQUEST_RESULT_CODE_UPDATE_LABEL = 5;


    //用来标记pager
    public static final int PAGER_MAIN = 0;
    public static final int PAGER_FRIEND = 1;
    public static final int PAGER_INFO = 2;
    public static final int PAGER_PERSION = 3;
    public static final int PAGER_PERSION_DATA = 4;
    public static final int PAGER_SETTING_DATA = 5;

    boolean isFirstInstall = true;

    long startTime = 0;

//    HMessageHandler homeMessageHandler;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLocation();
        MyApplication.accountNumber = getIntent().getStringExtra(User.str_mail);
        recentlyContactFileName += MyApplication.accountNumber;
        unReadChatMessageFileName += MyApplication.accountNumber;
        setContentView(R.layout.activity_home);
        initUI();
        initData();
        initMe();

    }
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            startTime = currentTime;
        } else {
            finish();
        }
    }

    /**
     * 获取位置信息并连续上传
     */
    private void startLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final LocationUtils utils = new LocationUtils(HomeActivity.this);
                utils.setLocationSucceedListener(new LocationUtils.LocationSucceedListener() {
                    @Override
                    public void localSucceed(AMapLocation aMapLocation) {
                        //获取位置信息
                        double lon = aMapLocation.getLongitude();//获取经度
                        double lat = aMapLocation.getLatitude();//获取纬度
                        utils.myUpPosition(MyApplication.me, (double) lat, (double) lon);
                        //获取当前位置
                        Position position = LocalPositionUtils.
                                getCurrentPosition(lon, lat);
                        if (position != null)
                            SerUtils.writeObject(HomeActivity.this, Position.currentPosition, position);
                        else {
                            SerUtils.writeObject(HomeActivity.this, Position.currentPosition,
                                    new Position(lon, lat, aMapLocation.getPoiName()));
                        }
                    }

                    @Override
                    public void localFail() {

                    }
                });
                utils.startLocation();
            }
        }).start();
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
                    if (isFirstInstall && MyApplication.me.getEmail() != null)
                        sendMessage();
                }
            }
        });
    }

    private void initEmotion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpanableStringUtils.getInstace().getFileText(HomeActivity.this.getApplication());
            }
        }).start();
    }

    private void initData() {
        initEmotion();
        pagerList = new ArrayList();
        pagerList.add(new MainPager(this));
        pagerList.add(new FriendPager(this));
        pagerList.add(new InfoPager(this));
        pagerList.add(new PersionPager(this));
        pagerList.add(new PersionDataPager(this));
        pagerList.add(new SettingDataPager(this));

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return pagerList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(pagerList.get(position).view);
                return pagerList.get(position).view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
        //第一次进入首先加载首页
        pagerList.get(PAGER_MAIN).initData();
        //设置radoiButton的监听事件
        rg_icon.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.home_rb_botton_icon_home:
                        viewPager.setCurrentItem(PAGER_MAIN, false);
                        break;
                    case R.id.home_rb_botton_icon_friend:
                        viewPager.setCurrentItem(PAGER_FRIEND, false);
                        break;
                    case R.id.home_rb_botton_icon_info:
                        viewPager.setCurrentItem(PAGER_INFO, false);
                        break;
                    case R.id.home_rb_botton_icon_persion:
                        viewPager.setCurrentItem(PAGER_PERSION, false);
                        break;
                }
            }
        });

        //重复点击时，跳到当前页面
        rg_icon.getChildAt(PAGER_PERSION).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() != PAGER_PERSION)
                    viewPager.setCurrentItem(PAGER_PERSION, false);
            }
        });
        //重复点击时，跳到当前页面
        rg_icon.getChildAt(PAGER_MAIN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() != PAGER_MAIN)
                    viewPager.setCurrentItem(PAGER_MAIN, false);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagerList.get(position).initData();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initUI() {
        viewPager = (ViewPager) findViewById(R.id.home_vp_pager);
        rg_icon = (RadioGroup) findViewById(R.id.home_rg_botton_icon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESULT_CODE_PUBLISH:
                if (resultCode == RESULT_OK) {
                    AVObject resultPublish = new AVObject();
                    String text = data.getStringExtra(PublishContent.str_text);
                    ArrayList<String> pictureUrls = data.getStringArrayListExtra(PublishContent.str_picture);
                    Boolean isTransmit = data.getBooleanExtra(PublishContent.str_is_transmit, false);
                    String transmitText = data.getStringExtra(PublishContent.str_transmit_text);

                    String remindUser = data.getStringExtra(PublishContent.str_transmited_user);
                    AVUser user = null;
                    try {
                        user = (AVUser) AVObject.parseAVObject(remindUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    resultPublish.put(PublishContent.str_transmited_user, user);
                    resultPublish.put(PublishContent.str_transmit_text, transmitText);
                    resultPublish.put(PublishContent.str_text, text);
                    resultPublish.put(PublishContent.str_picture, pictureUrls);
                    resultPublish.put(PublishContent.str_user, MyApplication.me);
                    resultPublish.put(PublishContent.str_is_transmit, isTransmit);
                    resultPublish.put(PublishContent.createdAt, new Date());

                    if (((MainPager) (pagerList.get(PAGER_MAIN))).contentList == null)
                        ((MainPager) (pagerList.get(PAGER_MAIN))).contentList = new ArrayList<>();
                    ((MainPager) (pagerList.get(PAGER_MAIN))).contentList.add(0, resultPublish);
                    (((MainPager) (pagerList.get(PAGER_MAIN))).adapter).notifyDataSetChanged();
                }
                break;
            case REQUEST_RESULT_CODE_CHAT:
                if (resultCode == RESULT_OK) {
//                    MyApplication.unReadChatMessageCount = (Map<String, Integer>) SerUtils.readObject(HomeActivity.this, MyApplication.unReadChatMessageFileName);
                    LogUtils.MyLog(MyApplication.unReadChatMessageCount + "");
                    if (viewPager.getCurrentItem() == HomeActivity.PAGER_INFO) {
                        ((InfoPager) (pagerList.get(PAGER_INFO))).initData();
                    }
                }
                break;
            case REQUEST_RESULT_CODE_REMIND:
                if (resultCode == RESULT_OK) {
                    ((InfoPager) (pagerList.get(PAGER_INFO))).getUnReadRemindCount();
                }
                break;
            case REQUEST_RESULT_CODE_COMMENT:
                if (resultCode == RESULT_OK) {
                    ((InfoPager) (pagerList.get(PAGER_INFO))).getUnReadCommonCount();
                }
                break;
            case REQUEST_RESULT_CODE_PRAISE:
                if (resultCode == RESULT_OK) {
                    ((InfoPager) (pagerList.get(PAGER_INFO))).getUnReadPraiseCount();
                }
                break;
            case REQUEST_RESULT_CODE_UPDATE_LABEL:
                if (resultCode == RESULT_OK) {
                    if (data != null){
                        saveLabel(data);
                    }

                }
                break;

        }
    }

    private void saveLabel(Intent data) {
        TextView textView = ((PersionPager) (pagerList.get(PAGER_PERSION))).tv_lable;
        final String lableContent = data.getStringExtra(RegisterActivity.label);
        textView.setText(lableContent);
        if ("".equals(lableContent) || lableContent == null)
            return;


        final int position = data.getIntExtra("position", 0);

        AVObject user = AVUser.createWithoutData("_User", MyApplication.me.getObjectId());
        user.put(User.str_table, lableContent);
        user.put(User.str_category,User.labelName[position]);
        user.saveInBackground(new SaveCallback() {
               @Override
            public void done(AVException e) {
               if (e == null) {
                    initMe();
                    String table = (String)MyApplication.me.get(User.str_table);
                    if((lableContent != null)&&!(lableContent.equals(table)))
                       publishLable(lableContent);
               } else {
                    ToastUtils.showToast(HomeActivity.this, "请检查网络连接");
               }

            }
                    });
                }

    private void publishLable(String text) {

        //获取当前用户成功,将当前用户设置为说说的一个属性。
//        final AVUser avUser = list.get(0);

        final AVObject object = new AVObject("Publish");
        object.put(PublishContent.str_user, MyApplication.me);
        object.put(PublishContent.str_praise_count, 0);
        object.put(PublishContent.str_comment_count, 0);
        object.put(PublishContent.str_transmit_count, 0);
        object.put(PublishContent.str_is_transmit, false);
        //将评论转发点赞次数设置为0
        object.put(PublishContent.str_text, text);
        object.put(PublishContent.str_transmit_text, "");
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    LivenessUtils.updataLiveness(MyApplication.me, Liveness.str_publish);
                }
            }
        });
    }

    /**
     * 第一次安装软件的时候进行发送操作，使得服务器能够找到此账号
     */
    public void sendMessage() {
        // Tom 用自己的信息作为clientId，获取AVIMClient对象实例
        final String myInfo = MyApplication.me.toString();
        AVIMClient tom = AVIMClient.getInstance(MyApplication.me.getEmail());
        // 与服务器连接
        tom.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    // 创建与朋友之间的对话
                    client.createConversation(Arrays.asList("test"), "me & you", null,
                            new AVIMConversationCreatedCallback() {
                                @Override
                                public void done(AVIMConversation conversation, AVIMException e) {
                                    if (e == null) {
                                        AVIMTextMessage msg = new AVIMTextMessage();
                                        msg.setText(" ");
                                        // 发送消息
                                        conversation.sendMessage(msg, new AVIMConversationCallback() {
                                            @Override
                                            public void done(AVIMException e) {
                                                if (e == null) {
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }
}
