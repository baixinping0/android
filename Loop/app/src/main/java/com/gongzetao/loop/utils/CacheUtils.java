package com.gongzetao.loop.utils;

import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baixinping on 2016/8/14.
 */
public class CacheUtils {
    /**
     * 获取聊天消息好友的账号列表
     */
    static List<String> infoFriendInfoList;

    public static void getInfoFriendMailList(MyApplication myApplication) {
        if (!SerUtils.fileIsExists(HomeActivity.recentlyContactFileName))
            infoFriendInfoList = new ArrayList<String>();
        else
            infoFriendInfoList = (List<String>) SerUtils.readObject(myApplication, HomeActivity.recentlyContactFileName);
    getMessageDataForDataBase(myApplication);
}

    /**
     * 通过账号从数据库中获取好友的聊天信息（每人获取十条）,
     * 并将信息缓存到MyApplication.chatMessages中
     */
    private static void getMessageDataForDataBase(MyApplication myApplication) {
        for (int i = 0; i < infoFriendInfoList.size(); i++) {
            List<ChatMessage> list = new DateBaseUtils(myApplication).getOneChatMessage(infoFriendInfoList.get(i), 10, 0);
            if (list.size() > 0) {
                MyApplication.chatMessages.put(infoFriendInfoList.get(i), list);
            }
        }
    }
}
