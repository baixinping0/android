package com.gongzetao.loop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.PublishActivity;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.LogUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/25.
 */
public class HotCommentAdapter extends BaseAdapter {

    Context context;
    List<AVObject> commentList;
    AVObject publishContent;

    public void setCommentList(List<AVObject> commentList) {
        this.commentList = commentList;
    }

    //用于标识点赞数据书否加载完
    boolean praiseAlreadyLoad = false;

    public void setPraiseAlreadyLoad(boolean praiseAlreadyLoad) {
        this.praiseAlreadyLoad = praiseAlreadyLoad;
    }

    public HotCommentAdapter(Context context, List<AVObject> commentList, AVObject publishContent) {
        this.context = context;
        this.commentList = commentList;
        this.publishContent = publishContent;
    }

    @Override
    public int getCount() {
        return commentList.size();
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
            convertView = android.view.View.inflate(context, R.layout.activity_publish_persion_comment_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.publish_comment_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.publish_comment_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.publish_comment_tv_time);
            holder.tv_content = (TextView) convertView.findViewById(R.id.publish_comment_tv_text);
            holder.iv_picture = (ImageView) convertView.findViewById(R.id.publish_comment_iv_picture);
            holder.tv_praise = (TextView) convertView.findViewById(R.id.publish_comment_tv_praise);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AVObject finalPublishComment = commentList.get(position);
        //得到说说评论对象

        //得到发表评论的人
        final AVUser user = (AVUser) finalPublishComment.get(Comment.str_comment_user);
        LogUtils.MyLog("position" + position + "  错误  " + holder);
        if (user != null)
            holder.tv_name.setText((String) user.get(User.str_accountName));
        holder.tv_time.setText(DataUtils.getDataTimeText(new Date((Long) finalPublishComment.get(Comment.str_comment_time))));
        holder.tv_content.setText((String) finalPublishComment.get(Comment.str_comment_text));
        //初始化评论次数
        Integer praiseCount = (Integer) finalPublishComment.get(Comment.str_praise_count);
        if (praiseCount == null)
            holder.tv_praise.setText("0");
        else
            holder.tv_praise.setText(finalPublishComment.get(Comment.str_praise_count) + "");
        String pictureUrl = (String) finalPublishComment.get(Comment.str_comment_picture);
        if (pictureUrl != null && !"".equals(pictureUrl))
            Glide.with(context).load(pictureUrl).into(holder.iv_picture);
        else
            holder.iv_picture.setVisibility(View.GONE);
        final ViewHolder finalHolder = holder;
        holder.tv_praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalPublishComment.increment(Comment.str_praise_count);
                finalPublishComment.setFetchWhenSave(true);
                finalPublishComment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        finalHolder.tv_praise.setText((Integer.parseInt(finalHolder.tv_praise.getText().toString()) + 1) + "");
                    }
                });
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublishActivity.class);
                intent.putExtra(PublishActivity.str_type, PublishActivity.comment_reply);
                intent.putExtra(PublishActivity.str_publish_content_ser, publishContent.toString());
                intent.putExtra(PublishActivity.str_publish_remind_user_ser, user.toString());
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_content;
        public ImageView iv_picture;
        public TextView tv_praise;
    }
}
