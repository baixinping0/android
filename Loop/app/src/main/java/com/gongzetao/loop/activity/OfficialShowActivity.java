package com.gongzetao.loop.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.gongzetao.loop.R;
import com.gongzetao.loop.base.BaseActivity;
import com.gongzetao.loop.utils.LogUtils;

/**
 * Created by baixinping on 2016/10/15.
 */
public class OfficialShowActivity extends BaseActivity{
    WebView webView;
    ImageView iv_last;
    TextView tv_title;
    public static final String str_title = "title";
    public static final String str_url = "url";
    String title;
    String urls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_show);
        initDefaultDate();
        initUI();
        initDate();
    }

    private void initDate() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        LogUtils.MyLog(urls);
        webView.loadUrl(urls);
    }

    private void initDefaultDate() {
        title = getIntent().getStringExtra(str_title);
        urls = getIntent().getStringExtra(str_url);
    }

    private void initUI() {
        iv_last = (ImageView) findViewById(R.id.title_icon_last);
        tv_title = (TextView) findViewById(R.id.title_tv_title);
        tv_title.setText(title);
        iv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        webView = (WebView) findViewById(R.id.official_show_wv);
    }
}
