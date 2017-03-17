package com.gongzetao.loop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.activity.ChatActivity;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.User;

import java.util.List;

/**
 * Created by baixinping on 2016/8/22.
 */
public class FriendsListAdapter extends BaseAdapter {
    Context context;
    List<AVUser> userLists;
    MyApplication app;

    public void setApp(MyApplication app) {
        this.app = app;
    }

    public FriendsListAdapter(List<AVUser> usersList, Context context) {
        this.userLists = usersList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return userLists.size();
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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.layout_friends_list_item, null);
            holder.iv_photo = (ImageView) convertView.findViewById(R.id.friends_item_iv_photo);
            holder.tv_name = (TextView) convertView.findViewById(R.id.friends_item_tv_name);
            holder.tv_table = (TextView) convertView.findViewById(R.id.friends_item_tv_table);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final AVUser user = userLists.get(position);
        holder.iv_photo.setImageResource(R.drawable.ad1);
        holder.tv_name.setText((String) user.get(User.str_accountName));
        holder.tv_table.setText((String) user.get(User.str_table));
        Glide.with(context).load(user.get(User.str_photo)).into(holder.iv_photo);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.FRIEND_INFO, (String) user.toString());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        public ImageView iv_photo;
        public TextView tv_name;
        public TextView tv_table;
    }
}
