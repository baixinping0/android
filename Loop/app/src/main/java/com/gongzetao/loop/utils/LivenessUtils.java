package com.gongzetao.loop.utils;

import android.support.annotation.NonNull;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.gongzetao.loop.bean.Liveness;
import com.gongzetao.loop.bean.UpdateApkData;

import java.util.List;

/**
 * Created by yulinbin on 2016/10/6.
 */
public class LivenessUtils {


    /**
     * 更新用户活跃度的函数
     * @param avUser 要增加活跃度的用户
     */
    public static void updataLiveness(final AVUser avUser, final String key){
        if(avUser != null) {
            AVQuery<AVObject> query = new AVQuery<>(Liveness.str_liveness);
            query.whereEqualTo(Liveness.str_user, avUser);
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    AVObject liveness = null;
                    if (e == null) {
                        if (list.size() == 0) {
                            nullUpdata(avUser, key);
                        } else {
                            liveness = list.get(0);
                            notNullUpdata(key, liveness);
                        }
                    } else {
                        if (e.getCode() == 101) {
                            nullUpdata(avUser, key);
                        }
                    }
                }
            });
        }else{
            LogUtils.MyLog("要增加活跃度的用户为空！！！");
        }
    }

    private static void notNullUpdata(final String key, final AVObject liveness) {
        liveness.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                liveness.increment(key);
                liveness.setFetchWhenSave(true);
                liveness.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        livenessCountUpdate(key,liveness);
                    }
                });
            }
        });
    }

    private static void livenessCountUpdate(final String key,final AVObject liveness) {
        int scorer = 0;
        if(Liveness.str_praiseed.equals(key))
            scorer = 1;
        if(Liveness.str_commented.equals(key))
            scorer = 3;
        if(Liveness.str_publish.equals(key))
            scorer = 3;
        if(Liveness.str_transmit.equals(key))
            scorer = 2;
        if(Liveness.str_transmited.equals(key))
            scorer = 4;

        final int finalScorer = scorer;
        liveness.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                liveness.increment(Liveness.str_livenessCount, finalScorer);
                liveness.setFetchWhenSave(true);
                liveness.saveInBackground();
            }
        });
    }

    @NonNull
    private static void nullUpdata(AVUser avUser,final String key) {
        final AVObject liveness = new AVObject(Liveness.str_liveness);
        liveness.put(Liveness.str_user,avUser);
        liveness.put(Liveness.str_comment,0);
        liveness.put(Liveness.str_follower,0);
        liveness.put(Liveness.str_praise,0);
        liveness.put(Liveness.str_praiseed,0);
        liveness.put(Liveness.str_commented,0);
        liveness.put(Liveness.str_livenessCount,0);
        liveness.put(Liveness.str_transmit,0);
        liveness.put(Liveness.str_publish,0);
        liveness.put(Liveness.str_transmited, 0);
        LogUtils.MyLog("创建liveness对象");
        liveness.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    notNullUpdata(key, liveness);
                    LogUtils.MyLog("Liveness创建成功");

                }
                else {
                    LogUtils.MyLog("baocun liveness " + e.toString());
                }
            }
        });
    }


}
