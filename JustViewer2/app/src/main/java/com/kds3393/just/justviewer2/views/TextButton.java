package com.kds3393.just.justviewer2.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import common.lib.utils.ViewMaker;

public class TextButton extends RelativeLayout {
    private TextView mTextView;

    public TextButton(Context context) {
        super(context);
        init();
    }

    public TextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mTextView = ViewMaker.TextViewMaker(getContext(), this, null, RelativeLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 7, 7, 7, 7);
        mTextView.setGravity(Gravity.CENTER);
    }

    public void setText(String txt) {
        mTextView.setText(txt);
    }

    public void setColor(int back, int text) {
        mTextView.setBackgroundColor(back);
        mTextView.setTextColor(text);
    }

    public void setTextBackgroundColor(int back) {
        mTextView.setBackgroundColor(back);
    }

    public void setSelectDrawable(boolean isSelected) {
        if (isSelected)
            setBackgroundColor(Color.RED);
        else
            setBackgroundColor(Color.BLACK);
    }

    public void setTextSize(int size) {
        mTextView.setTextSize(size);
    }

    public void setTypeface(Typeface tf) {
        mTextView.setTypeface(tf);
    }
}
