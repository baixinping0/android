package com.gongzetao.loop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.bean.Label;

import java.util.List;

/**
 * Created by baixinping on 2016/10/5.
 */
public class RecommendLableAdapter extends BaseAdapter {
    Context context;
    List<Label> labels;

    public RecommendLableAdapter(Context context, List<Label> labels) {
        this.context = context;
        this.labels = labels;
    }


    @Override
    public int getCount() {
        return labels.size()+2;
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
//        if(position>14){
//            ImageView imageView = new ImageView(context);
//            imageView.setImageResource(R.drawable.);
//        }
        View view = View.inflate(context, R.layout.activity_recommend_label_item, null);
        TextView tv_name = (TextView) view.findViewById(R.id.activity_recommend_label_item_tv_name);
        if (position < 14) {

            LinearLayout ll_root = (LinearLayout) view.findViewById(R.id.activity_recommend_label_item_ll_root);
            tv_name.setText(labels.get(position).getName());
            tv_name.setBackgroundResource(labels.get(position).getColor());
        }
//        ll_root.setBackgroundColor();
        if (position == 14) {
            tv_name.setText("");
            tv_name.setBackgroundResource(R.drawable.official_search);
        }
        if(position ==15) {
            tv_name.setText("");
            tv_name.setBackgroundResource(R.drawable.merchant_search);
        }
        return view;
    }
}
