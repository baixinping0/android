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
import com.gongzetao.loop.bean.Remind;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.facedutils.SpanableStringUtils;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.List;

/**
 * Created by baixinping on 2016/9/4.
 */
public class RemindAdapter extends BaseAdapter {
    List<AVObject> reminds;
    Context context;

    public void setReminds(List<AVObject> reminds) {
        this.reminds = reminds;
    }

    public RemindAdapter(Context context, List<AVObject> reminds) {
        this.context = context;
        this.reminds = reminds;
    }

    @Override
    public int getCount() {
        return reminds.size();
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
            convertView = View.inflate(context, R.layout.activity_remind_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.activity_remind_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.activity_remind_item_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.activity_remind_item_tv_time);
            holder.tv_reply = (TextView) convertView.findViewById(R.id.activity_remind_item_tv_remind);
            holder.tv_remindText = (TextView) convertView.findViewById(R.id.activity_remind_item_tv_remind_text);
            holder.iv_publishPicture = (ImageView) convertView.findViewById(R.id.activity_remind_item_iv_remind_publish_picture);
            holder.tv_publishText = (TextView) convertView.findViewById(R.id.activity_remind_item_tv_remind_publish_text);
            holder.ll_publish = (LinearLayout) convertView.findViewById(R.id.activity_remind_item_ll_publish);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //获取评论对象
        AVObject remind = reminds.get(position);
        //获取被提醒的用户
        AVUser remindedUser = (AVUser) remind.get(Remind.remindedUser);
        //获取主动提醒的用户
        final AVUser remindUser = (AVUser) remind.get(Remind.remindUser);
        //获提醒的动态
        final AVObject publish = (AVObject) remind.get(Remind.remindPublish);

        //主动动提醒人主的头像
        String remindUserPhoto = null;
        if(remindUser!=null)
            remindUserPhoto = (String) remindUser.get(User.str_photo);
        if (remindUserPhoto != null)
            Glide.with(context).load(remindUserPhoto).into(holder.iv_photo);
        else
            Glide.with(context).load(R.drawable.persion_default_photo).into(holder.iv_photo);
        //动提醒人的姓名
        holder.tv_name.setText((CharSequence) remindUser.get(User.str_accountName));
        //提醒时间
        holder.tv_time.setText(DataUtils.getDataTimeText(remind.getCreatedAt()));
        //提醒内容
        holder.tv_remindText.setText((CharSequence) remind.get(Remind.remindText));
        //提醒类型
        int remindType = -1;
        if (remind.get(Remind.remindType) != null)
            remindType = (Integer) remind.get(Remind.remindType);
        String baseRemindText = "@" + (String) remindedUser.get(User.str_accountName) + " ";
        holder.tv_remindText.setText(SpanableStringUtils.getInstace().getExpressionString(context, (String)remind.get(Remind.remindText)));

        List<String> pictureUrls = null;
        if (publish != null) {
            pictureUrls = (List<String>) publish.get(PublishContent.str_picture);
            if (pictureUrls != null && pictureUrls.size() > 0) {
                Glide.with(context).load(pictureUrls.get(0)).into(holder.iv_publishPicture);
            } else {
                Glide.with(context).load(R.drawable.default_picture).into(holder.iv_publishPicture);
            }
            holder.tv_publishText.setText((String) publish.get(PublishContent.str_text));
        }

        holder.tv_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishToCommentReply(context, publish, remindUser);
            }
        });
        holder.ll_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishPersionActivity(context, publish);
            }
        });
        holder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterFriendMainPager(context, remindUser);
            }
        });

        holder.tv_remindText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishToCommentReply(context, publish, remindUser);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_reply;
        public TextView tv_remindText;
        public ImageView iv_publishPicture;
        public TextView tv_publishText;
        public LinearLayout ll_publish;
    }
}
