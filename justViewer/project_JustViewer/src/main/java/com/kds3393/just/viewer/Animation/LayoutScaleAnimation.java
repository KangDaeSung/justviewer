package com.kds3393.just.viewer.Animation;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Scroller;

import com.kds3393.just.viewer.Browser.BrowserView.OnFileCheckedListener;
import com.common.utils.debug.CLog;

public class LayoutScaleAnimation implements Runnable {
	private static final String TAG = "LayoutScaleAnimation";
    public Scroller mScroller;

    private int mLastFlingY;
    private Context mContext;
    private Handler mHandler = new Handler();
    private View mView;
    private LayoutParams mViewParams;
    private int mAnimationDuration = 1000;
    
	private OnFlingListener mCheckedListener = null;
	public interface OnFlingListener {
		public void endFling(View view);
	};
	public void setOnFlingListener(OnFlingListener listnener) {
		mCheckedListener = listnener;
	}
	
    public LayoutScaleAnimation(Context context) {
    	mContext = context;
        mScroller = new Scroller(mContext);
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
    	mViewParams = mView.getLayoutParams();
        startCommon();
        
        int min = -distance;
        int max = distance;
        
        mLastFlingY = 0;

        mScroller.fling(0, 0, 0, initialVelocity, 0, 0, min, max);
        mScroller.extendDuration(delay);
        mHandler.post(this);
        return true;
    }

    public void startUsingDistance(int distance) {
        if (distance == 0) return;
        
        startCommon();
        
        mLastFlingY = 0;
        mScroller.startScroll(0, 0, distance, 0, mAnimationDuration);
        mHandler.post(this);
    }
    
    public void stop(boolean scrollIntoSlots) {
    	mHandler.removeCallbacks(this);
        endFling(scrollIntoSlots);
    }
    
    private void endFling(boolean scrollIntoSlots) {
    	mView.setLayoutParams(mViewParams);
        mScroller.forceFinished(true);
        if (mCheckedListener != null) {
        	mCheckedListener.endFling(mView);
        }
    }

    @Override
    public void run() {
        final Scroller scroller = mScroller;
        boolean more = scroller.computeScrollOffset();
        final int y = scroller.getCurrY();

        // Flip sign to convert finger direction to list items direction
        // (e.g. finger moving down means list is moving towards the top)
        if (mLastFlingY != y) {
            int offset = mLastFlingY - y;
            
            
//            if (mGravity == Gravity.BOTTOM)
//            	mTop = mTop + offset;
//            if (mGravity == Gravity.TOP)
//            	mBottom = mBottom + offset;
            
//            mView.layout(mLeft, mTop, mRight, mBottom);
            mViewParams.height = mViewParams.height + offset;
            mView.layout(mView.getLeft(), mView.getTop(), mView.getRight(), mView.getTop() + mViewParams.height);
        }
        
        if (more) {
        	mLastFlingY = y;
        	mHandler.post(this);
        } else {
            endFling(true);
        }
    }
}
