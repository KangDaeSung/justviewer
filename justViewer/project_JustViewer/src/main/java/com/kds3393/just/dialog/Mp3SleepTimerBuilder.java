package com.kds3393.just.dialog;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.Music.MusicService;

public class Mp3SleepTimerBuilder {
    private static final String TAG = "PageWheelSelector";
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    
    private WheelView mWheel[];
    private ImageView mThumbImg;
    private TextView mMessage;
    private Context mContext;
    public Mp3SleepTimerBuilder(Context context) {
    	initSize();
    	mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog));
    	mContext = context;
    }
    
    private int mMessageViewHeight;			//상단 Message view의 높이
	private float mMessageTextSize;				//상단 Icon Size width,height 동일

	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mMessageViewHeight = 92;
			mMessageTextSize = 20;
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mMessageViewHeight = 168;
			mMessageTextSize = 12;
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mMessageViewHeight = 80;
			mMessageTextSize = 15;
		}

		
	}
	
    public Context getContext() {
    	return mContext;
    }
    
    public AlertDialog.Builder getBuilder() {
        return mBuilder;
    }
    
    public AlertDialog getDialog() {
    	return mDialog;
    }
    
    private MusicService mService;
    public void setCurrentTime(long time) {
    	if (time == 0) {
    		mMessage.setText("Timer가 설정되어 있지 않습니다.");
    		return;
    	}
    		
    	long sec = (time / 1000) % 60;
    	long min = (time / (1000 * 60)) % 60;
    	long hour = (time / (1000 * 60 * 60)) % 24;

    	String msg = "현재 sleep time까지 ";
    	if (hour > 0) {
    		msg += hour + "시간 ";
    	}
    	if (hour > 0 || min > 0) {
    		msg += min + "분 ";
    	}
    	if (hour > 0 || min > 0 || sec > 0) {
    		msg += sec + "초 ";
    	}
    	mMessage.setText(msg + "남았습니다.");
    }
    
    public long getSleepTime() {
    	long hour = mWheel[0].getCurrentItem() * 60 *60 * 1000;
    	long min = mWheel[1].getCurrentItem() * 60 * 1000 * 10;
    	return (hour + min);
    }
    
    public String getStringSleepTime() {
    	String timeString = "";
    	int hour = mWheel[0].getCurrentItem();
    	int min = mWheel[1].getCurrentItem() * 10;
    	if (hour > 0) {
    		timeString = hour + "시간";
    	}
    	if (min > 0) {
    		timeString = " " + min + "분";
    	}
    	return timeString;
    }
    public void setPageText(int index) {

    }
    
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {}
        public void onScrollingFinished(WheelView wheel) {
        }
    };
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
   			
        }
    };
	
    public void makeView(Context context) {
    	LinearLayout dialoglayout = new LinearLayout(context);
    	dialoglayout.setOrientation(LinearLayout.VERTICAL);
    	dialoglayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    	dialoglayout.setGravity(Gravity.CENTER);
    		
		    mMessage = ViewMaker.TextViewMaker(context, dialoglayout, "", LayoutParams.WRAP_CONTENT, mMessageViewHeight, 0, 10);
			mMessage.setTextSize(mMessageTextSize);
	    		
	        LinearLayout layout = new LinearLayout(context);
	        layout.setOrientation(LinearLayout.HORIZONTAL);
	        dialoglayout.addView(layout);
	        LayoutUtils.setLinearLayoutParams(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	        
	        	mWheel = new WheelView[2];
	        	for (int i=0;i<mWheel.length;i++) {
	        		mWheel[i] = new WheelView(context);
	        		layout.addView(mWheel[i]);
		        	mWheel[i].addScrollingListener(scrolledListener);
		        	mWheel[i].addChangingListener(changedListener);
		        	mWheel[i].setCyclic(true);
		        	mWheel[i].setInterpolator(new AnticipateOvershootInterpolator());
		        	
		        	mWheel[i].setCurrentItem(0);
	        	}
	    		
	        	mWheel[0].setViewAdapter(new NumericWheelAdapter(context, 0, 12));
	        	LayoutUtils.setLinearLayoutParams(mWheel[0],174,250,0,0,0,0);
	        	Integer minArray[] = new Integer[6];
	        	for (int i=0;i<minArray.length;i++) {
	        		minArray[i] = i * 10;
	        	}
	        	mWheel[1].setViewAdapter(new ArrayWheelAdapter<Integer>(context, minArray));
	        	LayoutUtils.setLinearLayoutParams(mWheel[1],174,250,30,0,0,0);
	        	 
        mDialog = mBuilder.create();
        mDialog.setView(dialoglayout);
        mDialog.setTitle("Sleep timer setting");
    }
    
    
}
