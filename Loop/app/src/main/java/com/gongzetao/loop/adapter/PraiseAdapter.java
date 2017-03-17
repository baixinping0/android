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
import com.gongzetao.loop.bean.Praise;
import com.gongzetao.loop.bean.PublishContent;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.DataUtils;
import com.gongzetao.loop.utils.EnterActivityUtils;
import com.gongzetao.loop.view.CircleImageView;

import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class PraiseAdapter extends BaseAdapter {

    List<AVObject> praises;
    Context context;

    public void setPraises(List<AVObject> praises) {
        this.praises = praises;
    }

    public PraiseAdapter(Context context, List<AVObject> praises) {
        this.context = context;
        this.praises = praises;
    }

    @Override
    public int getCount() {
        return praises.size();
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
            convertView = View.inflate(context, R.layout.activity_praise_item, null);
            holder.iv_photo = (CircleImageView) convertView.findViewById(R.id.activity_praise_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.activity_praise_item_tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.activity_praise_item_tv_time);
            holder.tv_reply = (TextView) convertView.findViewById(R.id.activity_praise_item_tv_reply);
            holder.tv_praiseText = (TextView) convertView.findViewById(R.id.activity_praise_item_tv_praise_text);
            holder.iv_publishPicture = (ImageView) convertView.findViewById(R.id.activity_praise_item_iv_praise_publish_picture);
            holder.tv_publishText = (TextView) convertView.findViewById(R.id.activity_praise_item_tv_praise_publish_text);
            holder.ll_publish = (LinearLayout) convertView.findViewById(R.id.activity_praise_item_ll_publish);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        //获取点赞对象
        AVObject praise = praises.get(position);
        //获取被点赞的用户
        AVUser praisedUser = (AVUser) praise.get(Praise.praisedUser);
        //获取点赞的用户
        final AVUser praiseUser = (AVUser) praise.get(Praise.praiseUser);
        //获取点赞的动态
        final AVObject publish = (AVObject) praise.get(Praise.publish);

        //点赞人的头像
        Glide.with(context).load(praiseUser.get(User.str_photo)).into(holder.iv_photo);
        //点赞人的姓名
        holder.tv_name.setText((CharSequence) praiseUser.get(User.str_accountName));
        //点赞时间
        holder.tv_time.setText(DataUtils.getDataTimeText(praise.getCreatedAt()));
        //点赞内容
        holder.tv_praiseText.setText((String)praiseUser.get(User.str_accountName) + "赞了你");
        List<String> pictureUrls = null;
        if (publish != null){
            pictureUrls = (List<String>) publish.get(PublishContent.str_picture);
            holder.tv_publishText.setText((String) publish.get(PublishContent.str_text));
        }
        if (pictureUrls != null && pictureUrls.size() > 0){
            Glide.with(context).load(pictureUrls.get(0)).into(holder.iv_publishPicture);
        }else {
            Glide.with(context).load(R.drawable.default_picture).into(holder.iv_publishPicture);
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
                EnterActivityUtils.enterFriendMainPager(context, praiseUser);
            }
        });
        holder.tv_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterActivityUtils.enterPublishToCommentReply(context, publish, praiseUser);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        public CircleImageView iv_photo;
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_reply;
        public TextView tv_praiseText;
        public ImageView iv_publishPicture;
        public TextView tv_publishText;
        public LinearLayout ll_publish;
    }
}
