package com.gongzetao.loop.utils;

import com.avos.avoscloud.AVUser;

import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class AttentionUtils {

    //判断是否关注了此用户
    public static boolean isAttention(AVUser user, List<AVUser> followeeList) {
        if (user != null && followeeList != null){
            for(int i = 0; i < followeeList.size(); i++){
                if (user.getObjectId().equals(followeeList.get(i).getObjectId()))
                    return true;
            }
            return false;
        }
        return false;
    }
}
