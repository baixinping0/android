package com.gongzetao.loop.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import com.gongzetao.loop.activity.PublishActivity;
import com.gongzetao.loop.activity.PublishPersionActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.Comment;
import com.gongzetao.loop.bean.Praise;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.Remind;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.ToastUtils;
import com.gongzetao.loop.view.CircleImageView;
import com.gongzetao.loop.view.photoBrowser.ui.PhotoViewActivity;
import com.gongzetao.loop.view.lGNineGrideView.LGNineGrideView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by baixinping on 2016/9/2.
 */
public class FriendMainPagerAdapter extends BaseAdapter {

    List<AVObject> contentList;
    AVUser user;
    Context context;

    public void setContentList(List<AVObject> contentList) {
        this.contentList = contentList;
    }

    public FriendMainPagerAdapter(Context context,
                                  List<AVObject> contentList,
                                  AVUser user) {
        this.contentList = contentList;
        this.user = user;
        this.context = context;
    }

    @Override
    public int getCount() {
        return contentList.size();
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.layout_home_content_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.main_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.main_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.main_tv_time);
            holder.tv_text = (TextView) convertView.findViewById(R.id.main_tv_text);
            holder.gv_picture = (LGNineGrideView) convertView.findViewById(R.id.main_gv_picture);
            holder.tv_transmit = (TextView) convertView.findViewById(R.id.main_tv_transmit);
            holder.tv_comment = (TextView) convertView.findViewById(R.id.main_tv_comment);
            holder.tv_praise = (TextView) convertView.findViewById(R.id.main_tv_praise);
            holder.tv_transmitText = (TextView) convertView.findViewById(R.id.main_tv_transmit_text);
            holder.iv_delete = (ImageView) convertView.findViewById(R.id.main_iv_delete);

            holder.iv_transmit = (ImageView) convertView.findViewById(R.id.main_iv_transmit);
            holder.iv_comment = (ImageView) convertView.findViewById(R.id.main_iv_comment);
            holder.iv_praise = (ImageView) convertView.findViewById(R.id.main_iv_praise);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //获取说说对象
        final AVObject content = contentList.get(position);
        Date time = content.getCreatedAt();
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
        holder.tv_text.setText(SpanableStringUtils.getInstace().getExpressionString(context, (String) (content.get(PublishContent.str_text))));

        //转发文本
        String transmitText = (String) content.get(PublishContent.str_transmit_text);
        holder.tv_transmitText.setVisibility(View.VISIBLE);
        holder.tv_transmitText.setText(transmitText + "");
        if ("".equals(transmitText) || transmitText == null) {
            holder.tv_transmitText.setVisibility(View.GONE);
        }
        //获取转发次数
        Integer transmitCount = (Integer) content.get(PublishContent.str_transmit_count);
        if (transmitCount == null)
            holder.tv_transmit.setText("0");
        else
            holder.tv_transmit.setText(transmitCount.toString());
        //获取评论次数
        Integer commentCount = (Integer) content.get(PublishContent.str_comment_count);
        if (commentCount == null)
            holder.tv_comment.setText("0");
        else
            holder.tv_comment.setText(commentCount.toString());

        ArrayList<String> pictureUrl = null;
        if (user != null) {
            Glide.with(context).load((String) user.get(User.str_photo)).into(holder.iv_photo);
            holder.tv_name.setText((String) user.get(User.str_accountName));
            if (user.getObjectId().equals(MyApplication.me.getObjectId())) {
                holder.iv_delete.setVisibility(View.VISIBLE);
                holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("删除确认");
                        builder.setMessage("确认删除这条动态吗");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                content.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            ((FriendMainPagerActivity) context).getContentData(FriendMainPagerActivity.REFRESH);
                                            //删除说说对应的remind
                                            deleteRemind(content);
                                            //删除说说对应的comment
                                            deleteComment(content);
                                            //删除说说对应的remind
                                            deletePraise(content);
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
            }
        }
        //获取每个人的多张图片
        pictureUrl = (ArrayList<String>) (content.get(PublishContent.str_picture));


        //将图片填充九宫格
        final ArrayList<String> finalPictureUrl = pictureUrl;
        holder.gv_picture.setUrls(finalPictureUrl);
        holder.gv_picture.setOnItemClickListener(new LGNineGrideView.OnItemClickListener() {
            @Override
            public void onClickItem(int position, View view) {
                Intent intent = new Intent(context, PhotoViewActivity.class);
                intent.putStringArrayListExtra(PhotoViewActivity.URL_LIST, (ArrayList<String>) finalPictureUrl);
                context.startActivity(intent);
            }
        });
        //转发点击事件监听
        holder.iv_transmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublishActivity.class);
                intent.putExtra(PublishActivity.str_publish_content_ser, content.toString());
                intent.putExtra(PublishActivity.str_type, PublishActivity.transmit);
                ((FriendMainPagerActivity) context).startActivity(intent);
            }
        });
        //评论点击事件监听
        holder.iv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublishPersionActivity.class);
                intent.putExtra(PublishPersionActivity.publishContentSer, content.toString());
                ((FriendMainPagerActivity) context).startActivity(intent);
            }
        });
        //点赞点击事件监听
        holder.iv_praise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judgeIsAlreadyPraise(user,content, holder);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublishPersionActivity.class);
                intent.putExtra(PublishPersionActivity.publishContentSer, content.toString());
                ((FriendMainPagerActivity) context).startActivity(intent);
            }
        });

        return convertView;
    }
    public void judgeIsAlreadyPraise(final AVObject praisedUser, final AVObject publish,
                                     final ViewHolder holder) {
        AVQuery<AVObject> query = new AVQuery<>(Praise.publishPraise);
        query.whereEqualTo(Praise.praiseUser, MyApplication.me);
        query.whereEqualTo(Praise.publish, publish);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {//获取数据成功
                    if (list.size() == 0)//关联对象不存在或者网络错误查询失败
                        savePraise(praisedUser, publish, holder);
                    else {
                        ToastUtils.showToast(context, "已赞过");
                    }
                } else if (e.getCode() == 101) {//若e.getCode = 101说明对象不存在，对象不存在，保存对象
                    savePraise(praisedUser, publish, holder);
                } else {
                    ToastUtils.showToast(context, "点赞失败");
                }
            }
        });

    }

    private void savePraise(AVObject praisedUser, final AVObject publish,
                            final ViewHolder holder) {
        //创建点赞对象
        AVObject object = new AVObject(Praise.publishPraise);
        //被点赞的用户
        object.put(Praise.praisedUser, praisedUser);
        //主动点赞的用户
        object.put(Praise.praiseUser, MyApplication.me);
        //点赞的动态
        object.put(Praise.publish, publish);
        //点赞是否被阅读
        object.put(Praise.isLook, false);
        //保存点赞
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    //更新点赞数量
                    String str_currentPraiseCount = (String) holder.tv_praise.getText();
                    int currentCount = 0;
                    if (str_currentPraiseCount != null && !"".equals(str_currentPraiseCount)) {
                        currentCount = Integer.parseInt(str_currentPraiseCount.toString().trim());
                    }
                    final int finalCurrentCount = currentCount;
                    //更新服务器端点赞的次数
                    publish.increment(PublishContent.str_praise_count);
                    publish.setFetchWhenSave(true);
                    publish.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            //更新说说中的点赞的次数
                            holder.tv_praise.setText((finalCurrentCount + 1) + "");

                        }
                    });
                    ToastUtils.showToast(context, "点赞成功");
                } else {
                    ToastUtils.showToast(context, "点赞失败" + e.getCode());
                }
            }
        });
    }
    private void deletePraise(AVObject content) {
        AVQuery<AVObject> query = new AVQuery(Praise.publishPraise);
        query.whereEqualTo(Praise.publish, content);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null){
                    for (int i = 0; i < list.size(); i++){
                        list.get(i).deleteInBackground();
                    }
                }
            }
        });
    }

    private void deleteComment(AVObject content) {
        AVQuery<AVObject> query = new AVQuery(Comment.str_comment);
        query.whereEqualTo(Comment.str_publish, content);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null){
                    for (int i = 0; i < list.size(); i++){
                        list.get(i).deleteInBackground();
                    }
                }
            }
        });
    }

    private void deleteRemind(AVObject content) {
        AVQuery<AVObject> query = new AVQuery(Remind.remind);
        query.whereEqualTo(Remind.remindPublish, content);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null){
                    for (int i = 0; i < list.size(); i++){
                        list.get(i).deleteInBackground();
                    }
                }
            }
        });
    }

    static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_text;
        public LGNineGrideView gv_picture;
        public TextView tv_transmit;
        public TextView tv_comment;
        public TextView tv_praise;
        public ImageView iv_transmit;
        public ImageView iv_comment;
        public ImageView iv_praise;
        public TextView tv_transmitText;
        public ImageView iv_delete;
    }
}

