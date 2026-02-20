package com.kds3393.just.viewer.View;

import java.util.Date;

import com.common.utils.LayoutUtils;
import com.common.utils.debug.CLog;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TimeTextView extends TextView {
	private static final String TAG = "TimeTextView";
	
	public static TimeTextView make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static TimeTextView make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static TimeTextView make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		TimeTextView view = new TimeTextView(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	public TimeTextView(Context context) {
		super(context);
		mTimeUpdateHandler.sendEmptyMessage(UPDATE_TIME);
		
	}
	
	public TimeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTimeUpdateHandler.sendEmptyMessage(UPDATE_TIME);
	}
	
	@Override
	public void setVisibility(int visibility) {
		if (visibility == View.VISIBLE)
			mTimeUpdateHandler.sendEmptyMessage(UPDATE_TIME);
		super.setVisibility(visibility);
	}



	private static final int UPDATE_TIME = 1;
    private Handler mTimeUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME:
                	TimeTextView.this.setText(DateFormat.format("aa hh:mm", new Date()).toString());
                	if (TimeTextView.this.getVisibility() == View.VISIBLE) {
                		mTimeUpdateHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                	}
                	break;
            }
        }
    };
}
