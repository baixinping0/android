package com.gongzetao.loop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.base.BaseRecommendLabelActivity;
import com.gongzetao.loop.utils.DialogUtils;

/**
 * Created by baixinping on 2016/10/5.
 */
public class RecommendLableActivity extends BaseRecommendLabelActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_recommend_label);
        super.onCreate(savedInstanceState);
        initUI();
        initData();
    }

    @Override
    public void initUI() {
        gv_list = (GridView) findViewById(R.id.recommend_label_gv);
    }

    @Override
    public void initOnClickItem(final int position) {
        final DialogUtils utils = new DialogUtils();
        utils.setOnButtonClickListener(new DialogUtils.OnButtonClickListener() {
            @Override
            public void clickCancel() {
            }
            @Override
            public void clickOk() {
                String lable = utils.ev_value.getText().toString().trim();
                if (lable == null)
                    lable = "";
                Intent intent = new Intent();
                intent.putExtra(RegisterActivity.label, lable);
                intent.putExtra("position", position);
                RecommendLableActivity.this.setResult(RESULT_OK, intent);
                RecommendLableActivity.this.finish();
            }
        });
        utils.openUpdateDialog(RecommendLableActivity.this);
//            utils.ev_value.setText(label[position]);
    }
}
