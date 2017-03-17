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
import com.avos.avoscloud.FollowCallback;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.FriendMainPagerActivity;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.utils.AttentionUtils;
import com.gongzetao.loop.utils.ToastUtils;

import java.util.List;

/**
 * Created by baixinping on 2016/9/3.
 */
public class FriendAttentionAdapter extends BaseAdapter {

    List<AVUser> friendList;
    List<AVUser> myfolloweeList;
    Context context;
    public FriendAttentionAdapter(Context context, List<AVUser> searchList,
                                 List<AVUser> followeeList){
        this.context = context;
        this.friendList = searchList;
        this.myfolloweeList = followeeList;
    }

    @Override
    public int getCount() {
        return friendList.size();
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
        if (convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.activity_search_friend_list_item, null);
            holder.iv_photo = (ImageView) convertView.findViewById(R.id.search_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.search_item_tv_name);
            holder.tv_table = (TextView) convertView.findViewById(R.id.search_item_tv_table);
            holder.tv_attestation = (TextView) convertView.findViewById(R.id.search_item_tv_attestation);
            holder.iv_attention = (ImageView) convertView.findViewById(R.id.search_item_tv_attention);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AVUser user = friendList.get(position);
        holder.tv_name.setText((CharSequence) user.get(User.str_accountName));
        holder.tv_table.setText((CharSequence) user.get(User.str_table));
        Glide.with(context).load((String) user.get(User.str_photo)).into(holder.iv_photo);
        holder.tv_attestation.setText((CharSequence) user.get(User.str_attestation));
        final ViewHolder finalHolder = holder;

        if (AttentionUtils.isAttention(user, myfolloweeList)){
            holder.iv_attention.setEnabled(false);
            holder.iv_attention.setImageResource(R.drawable.alreaday_attention);
        }else {
            holder.iv_attention.setImageResource(R.drawable.no_attention);
            attention(holder.iv_attention, user);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendMainPagerActivity.class);
                intent.putExtra(FriendMainPagerActivity.ser_user_info, user.toString());
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    static class ViewHolder{
        public ImageView iv_photo;
        public TextView tv_name;
        public TextView tv_table;
        public TextView tv_attestation;
        public ImageView iv_attention;
    }
    /**
     * 关注好友
     * @param iv_attention
     * @param user
     */
    private void attention(final ImageView iv_attention, final AVUser user) {
        iv_attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //关注好友
            AVUser.getCurrentUser().followInBackground(user.getObjectId(), new FollowCallback() {
                @Override
                public void done(AVObject object, AVException e) {
                    if (e == null) {
                        ToastUtils.showToast(context, "关注成功");
                        iv_attention.setImageResource(R.drawable.alreaday_attention);
                    } else if (e.getCode() == AVException.DUPLICATE_VALUE) {
                        ToastUtils.showToast(context, "关注失败");
                    }
                }
            });
            }
        });
    }

    /**
     * 取消关注
     * @param view
     * @param user
     */
    private void cancelAttention(final ImageView view, final AVUser user) {

        AVUser.getCurrentUser().unfollowInBackground(user.getObjectId(), new FollowCallback() {
            @Override
            public void done(AVObject object, AVException e) {
                if (e == null) {
                    ToastUtils.showToast(context, "取消关注成功");
                    view.setImageResource(R.drawable.no_attention);
                    view.setEnabled(true);
                } else {
                    ToastUtils.showToast(context, "取消关注失败");
                }
            }
        });
    }
}
