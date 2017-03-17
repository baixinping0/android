package com.gongzetao.loop.view.sortlistview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.gongzetao.loop.R;

public class ClearEditText extends AutoCompleteTextView implements
        OnFocusChangeListener, TextWatcher {

    public Drawable mClearDrawable;

    private OnClearSucceed onClearSucceed;

    public void setOnClearSucceed(OnClearSucceed onClearSucceed) {
        this.onClearSucceed = onClearSucceed;
    }

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {

        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        //去掉删除的x
//        mClearDrawable = getCompoundDrawables()[2];
//        if (mClearDrawable == null) {
//            mClearDrawable = getResources()
//                    .getDrawable(R.drawable.sortlistview_emotionstore_progresscancelbtn);
//        }
//        mClearDrawable.setBounds(-20, 0, mClearDrawable.getIntrinsicWidth() - 20, mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);

        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth()
                        - getPaddingRight() - mClearDrawable.getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }


    public void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int count,
                              int after) {
        setClearIconVisible(s.length() > 0);
        if (onClearSucceed != null && (s == null || "".equals(s.toString())))
            onClearSucceed.clearSucceed();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    public void setShakeAnimation() {
        this.setAnimation(shakeAnimation(5));
    }


    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }

    public interface OnClearSucceed {
        void clearSucceed();
    }

    @Override
    public boolean hasFocus() {
        return true;
    }
}
