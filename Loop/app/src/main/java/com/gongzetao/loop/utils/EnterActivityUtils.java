package com.gongzetao.loop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.gongzetao.loop.activity.ChatActivity;
import com.gongzetao.loop.activity.FriendMainPagerActivity;
import com.gongzetao.loop.activity.HomeActivity;
import com.gongzetao.loop.activity.PublishActivity;
import com.gongzetao.loop.activity.PublishPersionActivity;
import com.gongzetao.loop.activity.RecommendLableActivity;

/**
 * Created by Administrator on 2016/9/15.
 */
public class EnterActivityUtils {
    public static void enterPublishToCommentReply(Context context, AVObject publishContent, AVUser remindUser){
        Intent intent = new Intent(context, PublishActivity.class);
        intent.putExtra(PublishActivity.str_type, PublishActivity.comment_reply);
        intent.putExtra(PublishActivity.str_publish_content_ser, publishContent.toString());
        intent.putExtra(PublishActivity.str_publish_remind_user_ser, remindUser.toString());
        context.startActivity(intent);
    }

    public static void enterFriendMainPager(Context context, AVUser user){
        Intent intent = new Intent(context, FriendMainPagerActivity.class);
        intent.putExtra(FriendMainPagerActivity.ser_user_info, user.toString());
        context.startActivity(intent);
    }
     public static void enterPublishPersionActivity(Context context, AVObject content){
         Intent intent = new Intent(context, PublishPersionActivity.class);
         intent.putExtra(PublishPersionActivity.publishContentSer, content.toString());
         context.startActivity(intent);
    }

    public static void enterChatActivity(Activity context, AVUser user){
        Intent intent = new Intent( context, ChatActivity.class);
        intent.putExtra(ChatActivity.FRIEND_INFO, user.toString());
        context.startActivityForResult(intent, HomeActivity.REQUEST_RESULT_CODE_CHAT);
    }
    public static void enterRecommendActivity(Activity context, int requestCode){
        Intent intent = new Intent(context, RecommendLableActivity.class);
        context.startActivityForResult(intent, requestCode);
    }



}
