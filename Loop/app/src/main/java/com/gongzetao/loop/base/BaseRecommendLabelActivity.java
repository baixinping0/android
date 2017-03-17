package com.gongzetao.loop.base;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.adapter.RecommendLableAdapter;
import com.gongzetao.loop.bean.Label;
import com.gongzetao.loop.bean.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/9.
 */
public abstract class BaseRecommendLabelActivity extends BaseActivity {
    public GridView gv_list;
    public RecommendLableAdapter adapter;
    public List<Label> labels;
    public String[] label = new String[]
            {"", "服装", "自行车", "釜山行", "LOL", "火影", "跑步",
                    "飘扬过来来看你", "从你的全世界路过", "龚泽涛",
                    "喜羊羊", "安康", "英语", "活动"};
    public int[] labelColor = new int[]{R.color.qita, R.color.chuzu, R.color.chushou,
            R.color.hanju, R.color.youxi, R.color.dongman, R.color.yundong, R.color.yinyue,
            R.color.dianying, R.color.mingxing, R.color.dianshiju, R.color.jiaxiang, R.color.xuexi,
            R.color.huodong};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public abstract void  initUI();

    public void initData() {
        if (labels == null) {
            labels = new ArrayList<>();
            for (int i = 0; i < labelColor.length; i++) {
                labels.add(new Label(User.labelName[i], label[i], labelColor[i]));
            }
        }
        if (adapter == null)
            adapter = new RecommendLableAdapter(this, labels);
        gv_list.setAdapter(adapter);
        gv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initOnClickItem(position);
            }
        });
    }
   public void initOnClickItem(int position){};
}
