package com.gongzetao.loop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class CommentAdapter extends BaseAdapter {

    List<AVObject> replys;
    Context context;

    public void setPraises(List<AVObject> replys) {
        this.replys = replys;
    }

    public CommentAdapter(Context context, List<AVObject> replys) {
        this.context = context;
        this.replys = replys;
    }

    @Override
    public int getCount() {
        return replys.size();
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
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.activity_comment_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.activity_reply_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.activity_reply_item_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.activity_reply_item_tv_time);
            holder.tv_reply = (TextView) convertView.findViewById(R.id.activity_reply_item_tv_reply);
            holder.tv_commentText = (TextView) convertView.findViewById(R.id.activity_reply_item_tv_praise_text);
            holder.iv_publishPicture = (ImageView) convertView.findViewById(R.id.activity_reply_item_iv_reply_publish_picture);
            holder.tv_publishText = (TextView) convertView.findViewById(R.id.activity_reply_item_tv_reply_publish_text);
            holder.ll_publish = (LinearLayout) convertView.findViewById(R.id.activity_reply_item_ll_publish);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //获取评论对象
        AVObject reply = replys.get(position);
        //获取被评论的用户
        AVUser replyeddUser = (AVUser) reply.get(Comment.str_commented_User);
        //获取评论的用户
        final AVUser replyUser = (AVUser) reply.get(Comment.str_comment_user);
        //获评论的动态
        final AVObject publish = (AVObject) reply.get(Comment.str_publish);

        //评论人的头像
        Glide.with(context).load(replyUser.get(User.str_photo)).into(holder.iv_photo);
        //评论人的姓名
        holder.tv_name.setText((CharSequence) replyUser.get(User.str_accountName));
        //评论时间
        holder.tv_time.setText(DataUtils.getDataTimeText(reply.getCreatedAt()));
        //评论内容
        holder.tv_commentText.setText((CharSequence) reply.get(Comment.str_comment_text));
        List<String> pictureUrls = null;
        if (publish != null)
            pictureUrls = (List<String>) publish.get(PublishContent.str_picture);
        if (pictureUrls != null && pictureUrls.size() > 0) {
            Glide.with(context).load(pictureUrls.get(0)).into(holder.iv_publishPicture);
        } else {
            Glide.with(context).load(R.drawable.default_picture).into(holder.iv_publishPicture);
        }
        if (publish != null){
            holder.tv_publishText.setText((String) publish.get(PublishContent.str_text));

        }

        holder.ll_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishPersionActivity(context, publish);
            }
        });
        holder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterFriendMainPager(context, replyUser);
            }
        });
        holder.tv_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishToCommentReply(context, publish,replyUser);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_reply;
        public TextView tv_commentText;
        public ImageView iv_publishPicture;
        public TextView tv_publishText;
        public LinearLayout ll_publish;
    }
}
