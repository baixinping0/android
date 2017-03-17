package com.gongzetao.loop.application;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.ChatActivity;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.bean.ChatMessage;
import com.gongzetao.loop.bean.ChatMessageType;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.SerUtils;
import com.gongzetao.loop.utils.DataBaseHelper;
import com.gongzetao.loop.utils.DateBaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by baixinping on 2016/8/11.
 */
public class MyApplication extends Application {
    //所有消息的缓存，String：朋友的信息，List：消息集合。
    public static Map<String, List<ChatMessage>> chatMessages;

    //保存未读消息，String：朋友的账号，List：消息集合。
    public static Map<String, Integer> unReadChatMessageCount;

    ReceiverMessageSucceedListener receiverMessageSucceedListener;

    //登录的用户的账号（自己的账号）
    public static  String accountNumber;

    HomeMessageHandler homeMessageHandler;

    public static AVUser me;
    //用于对数据库进行创建和更新操作的对象
    public static DataBaseHelper dataBaseHelper;
    public static SQLiteDatabase sqLiteDatabase;



    @Override
    public void onCreate() {
        this.startService(new Intent(this, PushService.class));
        //初始化对数据库进行操作的对象
        dataBaseHelper = new DataBaseHelper(this, "chatMessage", null, 1);
        //创建或打开数据库,得到对数据库进行增删改查操作的对象
        sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        //初始化消息集合
        if (unReadChatMessageCount == null) {
            unReadChatMessageCount = new TreeMap<String, Integer>();
        }
        if (chatMessages == null) {
            chatMessages = new TreeMap<String, List<ChatMessage>>();
        }
        if (homeMessageHandler == null)
            homeMessageHandler = new HomeMessageHandler();
        .
        //接受文字消息监听
        AVIMMessageManager.registerMessageHandler(AVIMMessage.class, homeMessageHandler);
        //注册极光
//        AVOSCloud.initialize(this, "GGe0R36aUNmhnNVOzIHOKbSO-9Nh9j0Va", "AC6ALSaMNdJ0RpdQJKyGXqar");
        AVOSCloud.initialize(this, "wypNhoxNX6JEIxpfUYaMlJry-9Nh9j0Va", "9K5s529g6bLo6YgwllJjNvH1");
    }

    /**
     * 通过好友账号获取未读消息
     *
     * @param userInfo
     * @return
     */
    public Integer getUnReadChatMessages(AVUser userInfo) {
        if (userInfo != null)
            return unReadChatMessageCount.get(userInfo.getEmail());
        else
            return 0;
    }

    /**
     * 添加未读消息
     *
     * @param userInfo
     * @param
     */
    public void addUnReadChatMessage(AVUser userInfo) {
        //从文件中读出未读的信息
        unReadChatMessageCount = (Map<String, Integer>) SerUtils.readObject(MyApplication.this, HomeActivity.unReadChatMessageFileName);
        if (unReadChatMessageCount == null) {
            unReadChatMessageCount = new TreeMap<String, Integer>();
            unReadChatMessageCount.put(userInfo.getEmail(), 1);
            SerUtils.writeObject(MyApplication.this, HomeActivity.unReadChatMessageFileName, unReadChatMessageCount);
            return;
        }
        Integer count = unReadChatMessageCount.get(userInfo.getEmail());
        if (count == null)
            unReadChatMessageCount.put(userInfo.getEmail(), 1);
        else {
            unReadChatMessageCount.remove(userInfo.getEmail());
            unReadChatMessageCount.put(userInfo.getEmail(), ++count);
        }
        SerUtils.writeObject(MyApplication.this, HomeActivity.unReadChatMessageFileName, unReadChatMessageCount);
    }

    /**
     * 删除已读消息
     *
     * @param userInfo
     * @param
     */
    public void removeUnReadChatMessage(AVUser userInfo) {
        //从文件中读出未读的信息
//        unReadChatMessageCount = (Map<String, Integer>) SerUtils.readObject(MyApplication.this, unReadChatMessageFileName);
        if (unReadChatMessageCount == null)
            return;
        unReadChatMessageCount.remove(userInfo.getEmail());
        SerUtils.writeObject(MyApplication.this, HomeActivity.unReadChatMessageFileName, unReadChatMessageCount);
    }


    /**
     * 通过好友信息获取聊天信息
     *
     * @param userInfo
     * @return
     */
    public List<ChatMessage> getChatMessages(AVUser userInfo) {
        if (chatMessages != null)
            return chatMessages.get(userInfo.getEmail());
        return null;
    }

    /**
     * 添加聊天消息
     *
     * @param userInfo：好友账号
     * @param message：聊天记录
     */
    public static void addChatMessage(AVUser userInfo,
                                      ChatMessage message) {
        List<ChatMessage> list = chatMessages.get(userInfo.getEmail());
        if (list == null) {
            //不存在这样的数据，添加
            List<ChatMessage> newList = new ArrayList();
            newList.add(message);
            chatMessages.put(userInfo.getEmail(), newList);
            return;
        }
        list.add(message);
        chatMessages.remove(userInfo.getEmail());
        chatMessages.put(userInfo.getEmail(), list);
    }

    /**
     * 接收消息：
     * 用户在此界面时会受到来自所有人的消息，先进性判断，再分类保存。
     */
    public class HomeMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            if (message instanceof AVIMTextMessage) {

                Map map = ((AVIMTextMessage) message).getAttrs();
                if(map==null)
                    return;
                String type = (String) map.get(ChatMessageType.STR_TYPE);
                String userInfo = (String) map.get(ChatMessageType.STR_USER_INFO);
                ChatMessage receiverMessage = null;

                if (ChatMessageType.STR_TYPE_TEXT.equals(type)) {
                    //次消息为文本消息，创建message
                    receiverMessage = new ChatMessage(ChatMessageType.MESSAGE_TYPE_FROM_TEXT,
                            System.currentTimeMillis(), ((AVIMTextMessage) message).getText());
                } else {
                    //次消息为文件消息，创建message
                    receiverMessage = new ChatMessage(ChatMessageType.MESSAGE_TYPE_FROM_IMAGE,
                            System.currentTimeMillis(), ((AVIMTextMessage) message).getText());
                }
                //获取消息number，并通过此number区别消息，并分类存储
                String fromUser = message.getFrom();
                AVUser user = null;

                try {
                    user = (AVUser) AVObject.parseAVObject(userInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (type.equals(ChatMessageType.STR_TYPE_TEXT))
                    showNotification(((AVIMTextMessage) message).getText(), user);
                else if (type.equals(ChatMessageType.STR_TYPE_IMAGE))
                    showNotification("[图片]", user);


                //更新未读消息数量
                addUnReadChatMessage(user);
                //添加到聊天消息缓存
                addChatMessage(user, receiverMessage);
                if (receiverMessageSucceedListener != null)
                    receiverMessageSucceedListener.receiverMessageSucceed(user);

                //保存最近聊天人的信息
                saveRecentlyContact(user);
                //将消息保存到数据库中
                DateBaseUtils.saveChatMessageInDataBase(user, receiverMessage);
            }
        }

        public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        }
    }

    /**
     * 消息状态提示
     *
     * @param message
     * @param fromUser
     */
    private void showNotification(String message, AVUser fromUser) {
        String title = (String) fromUser.get(User.str_accountName);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent clickIntent = new Intent(this, ChatActivity.class);
        clickIntent.putExtra(ChatActivity.FRIEND_INFO, fromUser.toString());
        PendingIntent pi = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //2,设置状态栏上的小图标
        builder.setSmallIcon(R.mipmap.ic_logo)//图标
                .setContentTitle(title)//标题
                .setContentText(message);//通知
        builder.setAutoCancel(false);
        builder.setTicker(title + ":" + message);
        builder.setContentIntent(pi);

        //设置通知默认的状态，包括：声音，震动，呼吸灯
        //使用DEFAULT_VIBRATE需要设置权限
        builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);//可选值NotificationCompat.DEFAULT_xxx

        //  builder.setNumber(5);

        //3,创建通知
        Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        //4,NotificationManager 来进行通知的发送
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999, n);
    }

    /**
     * 将最近参与聊天的好友的信息保存到文件中
     *
     * @param fromUserInfo
     */
    public void saveRecentlyContact(final AVUser fromUserInfo) {
        //从文件中读出好友的信息
        ArrayList<String> list = (ArrayList<String>) SerUtils.readObject(MyApplication.this, HomeActivity.recentlyContactFileName);
        if (list == null)
            list = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            AVUser user = null;
            try {
                user = (AVUser) AVObject.parseAVObject(list.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (user.getEmail().equals(fromUserInfo.getEmail())) {
                list.remove(i);//文件中已经存在此好友,则将好友信息删除
            }
        }
        list.add(0, fromUserInfo.toString());
        SerUtils.writeObject(MyApplication.this, HomeActivity.recentlyContactFileName, list);
    }

    /**
     * 将最近参与聊天的好友的信息删除
     *
     * @param fromUserInfo
     */
    public void removeRecentlyContact(final AVUser fromUserInfo) {
        //从文件中读出好友的信息
        ArrayList<String> list = (ArrayList<String>) SerUtils.readObject(MyApplication.this, HomeActivity.recentlyContactFileName);
        if (list == null)
            return;
        for (int i = 0; i < list.size(); i++) {
            AVUser user = null;
            try {
                user = (AVUser) AVObject.parseAVObject(list.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (user.getEmail().equals(fromUserInfo.getEmail())) {
                list.remove(i);//文件中已经存在此好友,则将好友信息删除
            }
        }
        SerUtils.writeObject(MyApplication.this, HomeActivity.recentlyContactFileName, list);
    }


    public void setReceiverMessageSucceedListener(ReceiverMessageSucceedListener receiverMessageSucceedListener) {
        this.receiverMessageSucceedListener = receiverMessageSucceedListener;
    }

    public interface ReceiverMessageSucceedListener {
        void receiverMessageSucceed(AVUser user);
    }
}

