package com.common.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;

import com.common.utils.ViewMaker;
import com.common.view.ProgressCircle;

/**
 * <p>A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.</p>
 * <p>The dialog can be made cancelable on back key press.</p>
 * <p>The progress range is 0..10000.</p>
 */
public class ProgressCircleDialog extends AlertDialog {
    
    private ProgressCircle mProgress;
    private TextView mMessageView;
    
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    
    private int mMax;
    private int mProgressVal;
    private CharSequence mMessage;
    
    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    
    public ProgressCircleDialog(Context context) {
        super(context);
        initFormats();
    }

    public ProgressCircleDialog(Context context, int theme) {
        super(context, theme);
        initFormats();
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }
    
    public static ProgressCircleDialog show(Context context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, false);
    }

    public static ProgressCircleDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static ProgressCircleDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static ProgressCircleDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
    	ProgressCircleDialog dialog = new ProgressCircleDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    	int size = 0;
    	if (displayMetrics.widthPixels>displayMetrics.heightPixels) {
    		size = displayMetrics.heightPixels;
    	} else {
    		size = displayMetrics.widthPixels;
    	}
    	size = (int) (size * 0.2); 
    	LinearLayout layout = new LinearLayout(getContext());
        layout.setBackgroundColor(Color.BLACK);
        setView(layout);
        
	        mProgress = ProgressCircle.make(getContext(), layout, size, size, 0, 0);
	        mProgress.setBGCircleColor(Color.GRAY);
	        mProgress.setPersentColor(Color.GRAY);
	        mProgress.setStrokeWidth(4);
	        
	        mMessageView = ViewMaker.TextViewMaker(getContext(), layout, "", (int) (size*2f), size, 0, 0);
	        mMessageView.setGravity(Gravity.CENTER_VERTICAL);
	        mMessageView.setTextColor(Color.WHITE);
	        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX,size * 0.14f);
        
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        
        mProgress.setInfinity(mIsInfinity);
        mProgress.setShowPersent(mIsShowPersent);
        
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(getWindow().getAttributes());
//        lp.width = (int) (size*2.5f);
//        lp.height = size;
//
//        getWindow().setAttributes(lp);
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    public void setProgress(int value) {
    	mProgressVal = value;
        if (mHasStarted) {
            mProgress.setProgress(mProgressVal);
        }
    }

    public float getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public float getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
    	mMax = max;
        if (mProgress != null) {
            mProgress.setMax(mMax);
        }
    }
    
    private boolean mIsInfinity = true;
    private boolean mIsShowPersent = true;
    public void setInfinity(boolean isInfinity) {
    	mIsInfinity = isInfinity;
    	if (mProgress != null) {
    		mProgress.setInfinity(mIsInfinity);
    	}
    		
	}
	
	public boolean isInfinity() {
		if (mProgress != null)
			return mProgress.isInfinity();
		return mIsInfinity;
	}
	
	public void setShowPersent(boolean isShow) {
		mIsShowPersent = isShow;
		if (mProgress != null)
			mProgress.setShowPersent(mIsShowPersent);
		
	}
    @Override
    public void setMessage(CharSequence message) {
    	mMessage = message;
        if (mProgress != null) {
            mMessageView.setText(mMessage);
        }
    }
}
