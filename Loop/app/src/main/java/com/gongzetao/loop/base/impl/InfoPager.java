package com.gongzetao.loop.base.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.CommentActivity;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.activity.PraiseActivity;
import com.gongzetao.loop.activity.RemindActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.base.HomeBasePager;
import com.gongzetao.loop.bean.ChatMessage;
import com.gongzetao.loop.bean.ChatMessageType;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.Praise;
import com.gongzetao.loop.bean.Remind;
import com.gongzetao.loop.utils.DateBaseUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.utils.SerUtils;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.RefreshListView;
import com.lidroid.xutils.BitmapUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/5.
 */
public class InfoPager extends HomeBasePager {

    TextView tv_unReadRemindCount;
    TextView tv_unReadCommonCount;
    TextView tv_unReadPraiseCount;

    private InfoMessageHandler infoMessageHandler;

    RelativeLayout rl_reply;
    RefreshListView lv_messageList;
    BitmapUtils bitmapUtils;
    public MyAdapter adapter;

    public MyAdapter getAdapter() {
        return adapter;
    }

    //朋友聊天记录的对象的账号集合
    List<String> userInfos;

    int unPraiseCount = 0;
    int unCommonCount = 0;
    int unRemindCount = 0;

    public InfoPager(Activity activity) {
        super(activity);
    }

    @Override
    public void initData() {
        infoMessageHandler = new InfoMessageHandler();
        //注册消息监听
        AVIMMessageManager.registerMessageHandler(AVIMMessage.class, infoMessageHandler);
        //图片加载器
        bitmapUtils = new BitmapUtils(mActivity);
        //获取所有聊天用户的number集合
        initAll();
    }

    private void initAll() {
        //初始化最近聊天
        initRecentlyFriendsInfoAndChatMessages();
        //初始化标题
        initInfoTitle();
        //获取title中的控件
        initUI();
        initHeader();
        //加载数据
        initChatMessageList();
        //加载@我的等

        initHeaderMessageCount();

    }

    private void initHeaderMessageCount() {
        getUnReadPraiseCount();
        getUnReadCommonCount();
        getUnReadRemindCount();
        //刷新成功
        lv_messageList.completeRefresh();
    }

    public void getUnReadPraiseCount() {
        AVQuery<AVObject> query = new AVQuery<AVObject>(Praise.publishPraise);
        query.whereEqualTo(Praise.isLook, false);
        query.whereEqualTo(Praise.praisedUser, MyApplication.me);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    unPraiseCount = i;
                    if (unPraiseCount > 0) {
                        tv_unReadPraiseCount.setVisibility(View.VISIBLE);
                        tv_unReadPraiseCount.setText(unPraiseCount + "");
                    } else {
                        tv_unReadPraiseCount.setVisibility(View.INVISIBLE);
                    }
                } else {
                    ToastUtils.showToast(mActivity, "请检查网络连接");
                    lv_messageList.refreshFail();
                }
            }
        });
    }

    public void getUnReadCommonCount() {
        AVQuery<AVObject> query = new AVQuery<AVObject>(Comment.str_comment);
        query.whereEqualTo(Comment.isLook, false);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.whereEqualTo(Comment.str_commented_User, MyApplication.me);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    unCommonCount = i;
                    if (unCommonCount > 0) {
                        tv_unReadCommonCount.setVisibility(View.VISIBLE);
                        tv_unReadCommonCount.setText(unCommonCount + "");
                    } else {
                        tv_unReadCommonCount.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // 查询失败'
                    ToastUtils.showToast(mActivity, "请检查网络连接");
                    lv_messageList.refreshFail();
                }
            }
        });
    }

    public void getUnReadRemindCount() {
        AVQuery<AVObject> query = new AVQuery<AVObject>(Remind.remind);
        query.whereEqualTo(Remind.isLook, false);
        query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
        query.whereEqualTo(Remind.remindedUser, MyApplication.me);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    unRemindCount = i;
                    if (unRemindCount > 0) {
                        tv_unReadRemindCount.setVisibility(View.VISIBLE);
                        tv_unReadRemindCount.setText(unRemindCount + "");
                    } else {
                        tv_unReadRemindCount.setVisibility(View.INVISIBLE);
                    }
                } else {
                    // 查询失败
                    ToastUtils.showToast(mActivity, "请检查网络连接");
                    lv_messageList.refreshFail();
                }
            }
        });
    }


    /**
     * 初始化最近聊天的朋友和聊天信息
     */
    private void initRecentlyFriendsInfoAndChatMessages() {
        userInfos = (List<String>) SerUtils.readObject(mActivity, HomeActivity.recentlyContactFileName);
        if (userInfos != null)
            for (int i = 0; i < userInfos.size(); i++) {
                AVUser user = null;
                try {
                    user = (AVUser) AVObject.parseAVObject(userInfos.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (user != null)
                    if (MyApplication.chatMessages.get(user.getEmail()) == null) {
                        //从数据库中获取好友的聊天信息。
                        List<ChatMessage> messages = new DateBaseUtils(app).getOneChatMessage(user.toString(), 10, 0);
                        MyApplication.chatMessages.put(user.getEmail(), messages);
                    }
            }
    }

    private void initInfoTitle() {
        tv_PagerDes.setText("消息");
        tv_PagerDes.setVisibility(View.VISIBLE);
        iv_IconAdd.setVisibility(View.INVISIBLE);
        et_LookupFrame.setVisibility(View.INVISIBLE);
        iv_IconLast.setVisibility(View.INVISIBLE);
    }

    private void initUI() {
        // 将之前添加view清除
        fl_pager.removeAllViews();
        View infoPager = View.inflate(mActivity, R.layout.layout_home_pager_info, null);
        fl_pager.addView(infoPager);
        lv_messageList = (RefreshListView) infoPager.findViewById(R.id.info_lv_message_list);
        lv_messageList.setPermitLoading(false);
        lv_messageList.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void pullRefresh() {
                LogUtils.MyLog("开始刷新。。。。");
                //下拉刷新执行操作
                initHeaderMessageCount();
            }

            @Override
            public void loading() {
                //上划加载更多执行操作

            }
        });
    }


    private void initChatMessageList() {
        if (userInfos == null)
            userInfos = new ArrayList<String>();
        if (adapter == null) {
            adapter = new MyAdapter();
            lv_messageList.setAdapter(adapter);
        } else
            adapter.notifyDataSetChanged();
    }

    private void initHeader() {
        View listViewHeader = View.inflate(mActivity, R.layout.layout_home_pager_info_header, null);
        RadioGroup radioGroup = (RadioGroup) listViewHeader.findViewById(R.id.info_pager_rg_botton_icon);
        tv_unReadRemindCount = (TextView) listViewHeader.findViewById(R.id.pager_info_header_tv_remind_remind_count);
        tv_unReadPraiseCount = (TextView) listViewHeader.findViewById(R.id.pager_info_header_tv_praise_remind_count);
        tv_unReadCommonCount = (TextView) listViewHeader.findViewById(R.id.pager_info_header_tv_reply_remind_count);
        radioGroup.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyIntent = new Intent(mActivity, CommentActivity.class);
                mActivity.startActivityForResult(replyIntent, HomeActivity.REQUEST_RESULT_CODE_COMMENT);
            }
        });
        radioGroup.getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent remindIntent = new Intent(mActivity, RemindActivity.class);
                mActivity.startActivityForResult(remindIntent, HomeActivity.REQUEST_RESULT_CODE_REMIND);
            }
        });
        radioGroup.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent praiseIntent = new Intent(mActivity, PraiseActivity.class);
                mActivity.startActivityForResult(praiseIntent, HomeActivity.REQUEST_RESULT_CODE_PRAISE);
            }
        });
        lv_messageList.addHeaderView(listViewHeader);
        if (adapter == null)
            adapter = new MyAdapter();
        if (userInfos == null)
            userInfos = new ArrayList<>();
        lv_messageList.setAdapter(adapter);

    }


    public class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if(userInfos!=null)
                return userInfos.size();
            else return 0;
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

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(mActivity, R.layout.layout_home_pager_info_item, null);
                viewHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.info_item_iv_photo);
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.info_item_tv_name);
                viewHolder.tv_time = (TextView) convertView.findViewById(R.id.info_item_tv_time);
                viewHolder.tv_message = (TextView) convertView.findViewById(R.id.info_item_tv_message);
                viewHolder.tv_remindMessage = (TextView) convertView.findViewById(R.id.info_item_remind_message);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //当有用户的时候再进行对布局初始化
            AVUser userInfo = null;
            if (userInfos.size() > 0) {
                //获取用户
                try {
                    userInfo = (AVUser) AVObject.parseAVObject(userInfos.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String userPhoto = (String) userInfo.get(User.str_photo);
                if(!TextUtils.isEmpty(userPhoto))
                    bitmapUtils.display(viewHolder.iv_photo, userPhoto);
                viewHolder.tv_name.setText((String) userInfo.get(User.str_accountName));

                List<ChatMessage> messages = app.getChatMessages(userInfo);
                MyApplication.chatMessages.put(userInfo.getEmail(), messages);
                if (messages != null) {
                    //获取最后一条消息
                    ChatMessage message = messages.get(messages.size() - 1);
                    if (message.getType() == ChatMessageType.MESSAGE_TYPE_TO_TEXT ||
                            message.getType() == ChatMessageType.MESSAGE_TYPE_FROM_TEXT)
                        viewHolder.tv_message.setText(message.getContent());
                    else if (message.getType() == ChatMessageType.MESSAGE_TYPE_TO_IMAGE ||
                            message.getType() == ChatMessageType.MESSAGE_TYPE_FROM_IMAGE)
                        viewHolder.tv_message.setText("[图片]");
                    Date date = new Date(message.getTime());
                    if(DataUtils.isRecent(date)==DataUtils.TODAY)
                        viewHolder.tv_time.setText(DataUtils.getTimeText(date));
                    else if(DataUtils.isRecent(date)==DataUtils.YESTERDAY)
                        viewHolder.tv_time.setText("昨天");
                    else if(DataUtils.isRecent(date)==DataUtils.TDBY)
                        viewHolder.tv_time.setText("前天");
                    else if(DataUtils.isWeeks(date))
                        viewHolder.tv_time.setText(DataUtils.getWeekText(date));
                    else if(DataUtils.isRecent(date)==DataUtils.THIS_YEAR)
                        viewHolder.tv_time.setText(DataUtils.getDataTextNoY(date));
                    else
                        viewHolder.tv_time.setText(DataUtils.getDataText(date));
                }
                Integer unReadCount = 0;
                if (userInfo != null)
                    unReadCount = app.getUnReadChatMessages(userInfo);
                if (unReadCount != null) {
                    if (unReadCount > 99)
                        unReadCount = 99;
                    viewHolder.tv_remindMessage.setVisibility(View.VISIBLE);
                    viewHolder.tv_remindMessage.setText(unReadCount + "");
                }

            }
            final AVUser finalUserInfo = userInfo;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom));

                    builder.setPositiveButton(R.string.deletetThisChat, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            userInfos.remove(finalUserInfo);
                            app.removeRecentlyContact(finalUserInfo);
                            initRecentlyFriendsInfoAndChatMessages();
                            notifyDataSetChanged();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mActivity, ChatActivity.class);
//                    intent.putExtra(ChatActivity.FRIEND_INFO, finalUserInfo.toString());
//                    mActivity.startActivityForResult(intent, HomeActivity.REQUEST_RESULT_CODE_CHAT);
                    EnterActivityUtils.enterChatActivity(mActivity, finalUserInfo);
                }
            });

            return convertView;
        }
    }

    public static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_message;
        public TextView tv_remindMessage;

    }

    /**
     * 接收消息：
     * 用户在此界面时会受到来自所有人的消息，先进性判断，再分类保存。
     */
    public class InfoMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            if (message instanceof AVIMTextMessage) {
                initRecentlyFriendsInfoAndChatMessages();
                adapter.notifyDataSetChanged();
            }
        }

        public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        }
    }
//    convertView.setOnLongClickListener(new View.OnLongClickListener() {
//        @Override
//        public boolean onLongClick(final View v) {
//            AlertDialog dialog = new AlertDialog.Builder(mActivity).create();
//
//            dialog.setButton("删除该聊天", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ToastUtils.showToast(mActivity, "shanchu onclick");
//                    v.setVisibility(View.GONE);
//                }
//            });
//            dialog.show();
//            dialog.getWindow().setLayout(550, 260);
//            dialog.setView(view, 0, 0, 0, 0);
//            return true;
//        }
//    });

}
