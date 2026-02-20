package com.kds3393.just.viewer.Animation;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class LayoutTranslateAnimation implements Runnable {
	private static final String TAG = "LayoutScaleAnimation";
    public Scroller mScroller;

    private int mLastFlingY;
    private Context mContext;
    private Handler mHandler = new Handler();
    private View mView;
    
	private OnFlingListener mCheckedListener = null;
	public interface OnFlingListener {
		public void endFling(View view);
	};
	public void setOnFlingListener(OnFlingListener listnener) {
		mCheckedListener = listnener;
	}
	
    public LayoutTranslateAnimation(Context context) {
    	mContext = context;
        mScroller = new Scroller(mContext);
    }
    
    public LayoutTranslateAnimation(Context context, Interpolator interpolator) {
    	mContext = context;
        mScroller = new Scroller(mContext,interpolator);
    }
    
    private void startCommon() {
    	mHandler.removeCallbacks(this);
    }
    
    public boolean startUsingVelocity(View view, int initialVelocity, int distance,int delay, OnFlingListener listnener) {
    	mCheckedListener = listnener;
    	return startUsingVelocity(view,initialVelocity,distance,delay);
    }
    
    public boolean startUsingVelocity(View view, int initialVelocity, int distance,int delay) {
    	mView = view;
        startCommon();
        
        int min = -distance;
        int max = distance;
        
        mLastFlingY = 0;

        mScroller.fling(0, 0, 0, initialVelocity, 0, 0, min, max);
        mScroller.extendDuration(delay);
        mHandler.post(this);
        return true;
    }
    private boolean mIsVerti = true;
    public void startUsingDistance(View view, boolean isVerti, int distance, int delay, OnFlingListener listnener) {
    	mCheckedListener = listnener;
    	mView = view;
    	mIsVerti = isVerti;
        if (distance == 0) return;
        
        startCommon();
        
        mLastFlingY = 0;
        if (mIsVerti)
        	mScroller.startScroll(0, 0, 0, distance, delay);
        else
        	mScroller.startScroll(0, 0, distance, 0, delay);
        mHandler.post(this);
    }
    
    public void stop(boolean scrollIntoSlots) {
    	mHandler.removeCallbacks(this);
        endFling(scrollIntoSlots);
    }
    
    private void endFling(boolean scrollIntoSlots) {
        mScroller.forceFinished(true);
        if (mCheckedListener != null) {
        	mCheckedListener.endFling(mView);
        }
    }

    @Override
    public void run() {
        final Scroller scroller = mScroller;
        boolean more = scroller.computeScrollOffset();
        final int distance;
        if (mIsVerti) {
        	distance = scroller.getCurrY();
        } else {
        	distance = scroller.getCurrX();
        }
        // Flip sign to convert finger direction to list items direction
        // (e.g. finger moving down means list is moving towards the top)
        if (mLastFlingY != distance) {
            int offset = mLastFlingY - distance;
            
            ViewGroup.MarginLayoutParams params = (MarginLayoutParams) mView.getLayoutParams();
            
            if (mIsVerti){
            	params.topMargin = params.topMargin + offset;
        	} else {
            	params.leftMargin = params.leftMargin + offset;
        	}
            
            mView.setLayoutParams(params);
        }
        
        if (more) {
        	mLastFlingY = distance;
        	mHandler.post(this);
        } else {
            endFling(true);
        }
    }
}
