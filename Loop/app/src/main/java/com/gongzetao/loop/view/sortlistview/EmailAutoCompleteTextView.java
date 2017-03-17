package com.gongzetao.loop.view.sortlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gongzetao.loop.R;

/**
 * Created by yulinbin on 2016/10/17.
 */
public class EmailAutoCompleteTextView extends ClearEditText {
    private static final String TAG = "EmailAutoCompleteTextView";

    private String[] emailSufixs = new String[] { "@qq.com","@163.com",
            "@gmail.com", "@126.com" };

    public EmailAutoCompleteTextView(Context context) {
        super(context);
        init(context);
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmailAutoCompleteTextView(Context context, AttributeSet attrs,
                                     int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setAdapterString(String[] es) {
        if(es != null && es.length > 0)
            this.emailSufixs = es;
    }

    private void init(final Context context) {
        //adapter中使用默认的emailSufixs中的数据，可以通过setAdapterString来更改
        this.setAdapter(new EmailAutoCompleteAdapter(context, R.layout.auto_complete_item, emailSufixs));

        //使得在输入3个字符之后便开启自动完成
        this.setThreshold(3);

        this.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    String text = EmailAutoCompleteTextView.this.getText().toString();
                    //当该文本域重新获得焦点后，重启自动完成
                    if(!"".equals(text))
                        performFiltering(text, 0);
                } else {
                    EmailAutoCompleteTextView ev = (EmailAutoCompleteTextView) v;
                    String text = ev.getText().toString();
                    if(text != null && text.matches("^[a-zA-Z0-9_]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
                    } else {
                        Toast toast = Toast.makeText(context, "邮件地址格式不正确", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 50);
                        toast.show();
                    }
                }
            }
        });
    }



    @Override
    protected void replaceText(CharSequence text) {
        String t = this.getText().toString();
        int index = t.indexOf("@");
        if(index != -1)
            t = t.substring(0, index);
        super.replaceText(t + text);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        String t = text.toString();

        int index = t.indexOf("@");
        if(index == -1) {

            if(t.matches("^[a-zA-Z0-9_]+$")) {
                super.performFiltering("@", keyCode);
            }
            else
                this.dismissDropDown();//当用户中途输入非法字符时，关闭下拉提示框
        } else {
            if(this.getText().toString().contains(".com"))
                dismissDropDown();
            super.performFiltering(t.substring(index), keyCode);
        }
    }


    private class EmailAutoCompleteAdapter extends ArrayAdapter<String> {

        public EmailAutoCompleteAdapter(Context context, int textViewResourceId, String[] email_s) {
            super(context, textViewResourceId, email_s);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(
                        R.layout.auto_complete_item, null);
            TextView tv = (TextView) v.findViewById(R.id.tv_main);

            String t = EmailAutoCompleteTextView.this.getText().toString();
            int index = t.indexOf("@");
            if(index != -1)
                t = t.substring(0, index);
            tv.setText(t + getItem(position));
            return v;
        }
    }
}