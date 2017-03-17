package com.gongzetao.loop.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.FriendMainPagerActivity;
import com.gongzetao.loop.activity.HotCommentActivity;
import com.gongzetao.loop.activity.PublishPersionActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/8/25.
 */
public class PublishPersionAdapter extends BaseAdapter {
    Context context;
    List<AVObject> commentPraiseList;
    List<AVObject> commentTimeList;
    List<AVObject> commentList;
    AVObject publishContent;

    public void setCommentPraiseListAndCommentTimeList(List<AVObject> commentPraiseList
            , List<AVObject> commentTimeList) {
        this.commentPraiseList = commentPraiseList;
        this.commentTimeList = commentTimeList;
        if (commentList == null)
            commentList = new ArrayList<AVObject>();
        else
            commentList.clear();
        commentList.addAll(commentPraiseList);
        commentList.addAll(commentTimeList);
    }

    //用于标识点赞数据书否加载完
    boolean praiseAlreadyLoad = false;

    public void setPraiseAlreadyLoad(boolean praiseAlreadyLoad) {
        this.praiseAlreadyLoad = praiseAlreadyLoad;
    }

    public PublishPersionAdapter(Context context, List<AVObject> commentPraiseList,
                                 List<AVObject> commentTimeList, AVObject publishContent) {
        this.context = context;
        this.commentPraiseList = commentPraiseList;
        this.commentTimeList = commentTimeList;
        this.publishContent = publishContent;
        if (commentList == null)
            commentList = new ArrayList<AVObject>();
        if (commentPraiseList.size() > 0)
            commentList.addAll(commentPraiseList);
        if (commentTimeList.size() > 0)
            commentList.addAll(commentTimeList);
    }

    @Override
    public int getCount() {
        if (commentPraiseList.size() > 0)
            return commentList.size() + 1;
        else
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        //将点赞数据加载完成后返回分隔控件
        View view = null;
        if (position == commentPraiseList.size() && commentPraiseList.size() != 0) {
            view = View.inflate(context, R.layout.activity_publish_persion_comment_break, null);
            praiseAlreadyLoad = true;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 5;
            params.bottomMargin = 5;
            view.setLayoutParams(params);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, HotCommentActivity.class);
                    intent.putExtra(PublishPersionActivity.publishContentSer, publishContent.toString());
                    context.startActivity(intent);
                }
            });

            return view;
        }

        ViewHolder holder = null;
        if (convertView == null || !(convertView instanceof LinearLayout)) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.activity_publish_persion_comment_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.publish_comment_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.publish_comment_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.publish_comment_tv_time);
            holder.tv_content = (TextView) convertView.findViewById(R.id.publish_comment_tv_text);
            holder.iv_picture = (ImageView) convertView.findViewById(R.id.publish_comment_iv_picture);
            holder.tv_praise = (TextView) convertView.findViewById(R.id.publish_comment_tv_praise);
            holder.view_line = convertView.findViewById(R.id.publish_comment_break_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (commentPraiseList.size() > 0 && position == commentPraiseList.size() - 1) {
            holder.view_line.setVisibility(View.INVISIBLE);
        }
        AVObject publishComment = null;
        if (position > commentPraiseList.size() && praiseAlreadyLoad) {
            publishComment = commentList.get(position - 1);
        } else {
            publishComment = commentList.get(position);
        }
        final AVObject finalPublishComment = publishComment;

        //得到发表评论的人
        final AVUser user = (AVUser) publishComment.get(Comment.str_comment_user);
        if (user != null) {
            holder.tv_name.setText((String) user.get(User.str_accountName));
            Glide.with(context).load(user.get(User.str_photo)).fitCenter()
                    .error(null).placeholder(R.drawable.picture_load).into(holder.iv_photo);
        }
        Date time = publishComment.getCreatedAt();
        if (time == null)
            time = new Date(System.currentTimeMillis());
        if(DataUtils.isRecent(time)==DataUtils.TODAY)
            holder.tv_time.setText("今天"+DataUtils.getTimeText(time));
        else if(DataUtils.isRecent(time)==DataUtils.YESTERDAY)
            holder.tv_time.setText("昨天"+DataUtils.getTimeText(time));
        else if(DataUtils.isRecent(time)==DataUtils.TDBY)
            holder.tv_time.setText("前天"+DataUtils.getTimeText(time));
        else if(DataUtils.isRecent(time)==DataUtils.THIS_YEAR)
            holder.tv_time.setText(DataUtils.getDataTimeTextNoYS(time));
        else
            holder.tv_time.setText(DataUtils.getDataTimeTextNoS(time));
//        holder.tv_time.setText(DataUtils.getDataTimeText(time));

        String commentText = (String) publishComment.get(Comment.str_comment_text);
        List<AVUser> remindUsers = (List<AVUser>) publishComment.get(Comment.commentRemindUser);
//        String remindText = "";
//        if (remindUsers != null){
//            for (int i = 0; i < remindUsers.size(); i++){
//                remindText += "@" + remindUsers.get(i).get(User.str_accountName) + " ";
//            }
//        }

        holder.tv_content.setText(SpanableStringUtils.getInstace().getExpressionString(context, commentText));

        Integer praiseCount = (Integer) publishComment.get(Comment.str_praise_count);
        if (praiseCount == null)
            holder.tv_praise.setText("0");
        else
            holder.tv_praise.setText(publishComment.get(Comment.str_praise_count) + "");
        String pictureUrl = (String) publishComment.get(Comment.str_comment_picture);
        if ("".equals(pictureUrl) || pictureUrl == null)
            holder.iv_picture.setVisibility(View.GONE);
        else
            Glide.with(context).load(pictureUrl).into(holder.iv_picture);
        final ViewHolder finalHolder = holder;
        holder.tv_praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judgeIsCommentAlreadyPraise(commentList.get(position), finalHolder);
            }
        });
        holder.iv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendMainPagerActivity.class);
                intent.putExtra(FriendMainPagerActivity.ser_user_info, user.toString());
                context.startActivity(intent);
            }
        });
        holder.tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishToCommentReply(context, publishContent, user);
            }
        });
        //avUser 发表说说的人
        AVUser avUser = publishContent.getAVObject(PublishContent.str_user);
        if(user.getObjectId().equals(MyApplication.me.getObjectId())
            ||MyApplication.me.getObjectId().equals(avUser.getObjectId())) {
            if(commentPraiseList!=null&&commentPraiseList.size()>0){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("删除确认");
                    builder.setMessage("确认删除这条评论吗");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            commentPraiseList.get(position).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
//                                        ((FriendMainPagerActivity) context).getContentData(FriendMainPagerActivity.REFRESH);
//                                        //删除说说对应的remind
//                                        deleteRemind(content);
//                                        //删除说说对应的comment
//                                        deleteComment(content);
//                                        //删除说说对应的remind
//                                        deletePraise(content);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }}
        return convertView;
    }

    /**
     * 判断评论是否被点赞
     */
    public void judgeIsCommentAlreadyPraise(final AVObject comment, final ViewHolder holder) {
        //从服务器获取评论对象
        AVQuery<AVObject> query = new AVQuery<>(Comment.str_comment);
        query.whereEqualTo(Comment.str_objectId, comment.getObjectId());
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                ArrayList<AVUser> users = (ArrayList<AVUser>) list.get(0).get(Comment.praiseUser);
                if (users == null || users.size() == 0){
                    saveCommentPraise(list.get(0), holder);
                    return;
                }
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getObjectId().equals(MyApplication.me.getObjectId())) {
                        ToastUtils.showToast(context, "已赞过");
                        return;
                    }
                }
                saveCommentPraise(list.get(0), holder);
            }
        });
    }

    private void saveCommentPraise(final AVObject comment,
                                   final ViewHolder holder) {
        ArrayList<AVUser> users = (ArrayList<AVUser>)(comment.get(Comment.praiseUser));
        if (users == null)
            users = new ArrayList<>();
        users.add(MyApplication.me);
        comment.put(Comment.praiseUser, users);
        comment.increment(Comment.str_praise_count);
        comment.setFetchWhenSave(true);
        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //更新点赞数量
                    String str_currentPraiseCount = (String) holder.tv_praise.getText();
                    int currentCount = 0;
                    if (str_currentPraiseCount != null && !"".equals(str_currentPraiseCount)) {
                        currentCount = Integer.parseInt(str_currentPraiseCount.toString().trim());
                    }
                    //更新服务器端点赞的次数
                    holder.tv_praise.setText((currentCount + 1) + "");
                    ToastUtils.showToast(context, "点赞成功");
                } else {
                    ToastUtils.showToast(context, "点赞失败" + e.getCode());
                }
            }
        });
    }

    public static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_content;
        public ImageView iv_picture;
        public TextView tv_praise;
        public View view_line;
    }

}
