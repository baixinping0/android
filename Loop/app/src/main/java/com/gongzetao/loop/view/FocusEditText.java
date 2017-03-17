package com.gongzetao.loop.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by baixinping on 2016/8/6.
 */
public class FocusEditText extends EditText {
    public FocusEditText(Context context) {
        super(context);
    }

    public FocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
