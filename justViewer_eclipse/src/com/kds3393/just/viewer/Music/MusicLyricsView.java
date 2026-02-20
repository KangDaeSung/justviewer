package com.kds3393.just.viewer.Music;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.utils.ImageUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation.OnFlingListener;

public class MusicLyricsView extends RelativeLayout {
	private static final String TAG = "MusicLyricsView";
	
	private TextView mLyricsTextView;
	private Button mOpenButton;
	private boolean mIsOpen = false;
	private OnFlingListener mOnFlingListener;
    public static MusicLyricsView makeLyricsPanel(Context context,ViewGroup parent) {
    	MusicLyricsView view = new MusicLyricsView(context);
    	parent.addView(view);
 
    	LayoutUtils.setLayoutParams(parent, view, 
    			view.mMainSize.Width, view.mMainSize.Height, 
    			view.mMainPoint.x, view.mMainPoint.y, 
    			-view.mMainSize.Width, 0);
    	view.setVisibility(View.GONE);
    	return view;
    }
	
	public MusicLyricsView(Context context) {
		super(context);
		initSize();
		createView();
		mOnFlingListener = new OnFlingListener(){
			@Override
			public void endFling(View view) {
				MusicLyricsView.this.setVisibility(View.INVISIBLE);
			}
		};
	}
	
	public MusicLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSize();
		createView();
		mOnFlingListener = new OnFlingListener(){
			@Override
			public void endFling(View view) {
				MusicLyricsView.this.setVisibility(View.INVISIBLE);
			}
		};
	}
	
	public Size mMainSize;
	public Point mMainPoint;		//main Panel 좌표
	
	public Size mShowButtonSize;
	public Size mLyricsScrollViewSize;
	
	private float TextSize;
	private int mBlinkHeight;
	
	private int mMoveDistances;						//Open Animation시 이동 거리
	
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mMainSize = new Size(800,805);
			mShowButtonSize = new Size(69,174);
			mMainPoint = new Point(Size.DisplayWidth - mShowButtonSize.Width - 1,94);
			mLyricsScrollViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 40,mMainSize.Height);
			TextSize = 25;
			mBlinkHeight = 140;
		} else if (Size.ScreenType == Size.S1080X1920X3) { 
			mMainSize = new Size(1010,1300);
			mShowButtonSize = new Size(69,174);
			mMainPoint = new Point(Size.DisplayWidth - mShowButtonSize.Width - 1,94);
			mLyricsScrollViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 5,mMainSize.Height);
			TextSize = 14;
			mBlinkHeight = 200;
		} else {
			mMainSize = new Size(720,855);
			mShowButtonSize = new Size(69,174);
			mMainPoint = new Point(Size.DisplayWidth - mShowButtonSize.Width - 1,94);
			mLyricsScrollViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 10,mMainSize.Height);
			TextSize = 15;
			mBlinkHeight = 70;
		}
		
		mMoveDistances = mMainSize.Width - mShowButtonSize.Width;
	}
	
    private LayoutTranslateAnimation mShowAnimation;
    private LayoutTranslateAnimation mHideAnimation;
	protected GestureDetector mOpenButtonGestureDetector;
	private void createView() {
		mShowAnimation = new LayoutTranslateAnimation(getContext(),new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(getContext(),new OvershootInterpolator());
		
		mOpenButton = ViewMaker.ButtonMaker(getContext(), this, mShowButtonSize.Width, mShowButtonSize.Height, 1, 0);
		mOpenButton.setBackgroundResource(R.drawable.right_open);
		LayoutUtils.setRelativeRule(mOpenButton, RelativeLayout.CENTER_VERTICAL);
		mOpenButtonGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
			@Override public boolean onDown(MotionEvent event) {return true;}
			@Override public void onLongPress(MotionEvent e) {}
			@Override public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {return true;}
			@Override public void onShowPress(MotionEvent e) {}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				if (!mIsOpen && velocityX < 0)
					setSwitchPanel(true);
				else if (mIsOpen && velocityX > 0)
					setSwitchPanel(false);
				return true;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				setSwitchPanel(!mIsOpen);
				return true;
			}
        });
		mOpenButton.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mOpenButtonGestureDetector.onTouchEvent(event);
				return true;
			}
		});
		
		
		ScrollView scrollview = new ScrollView(getContext());
		this.addView(scrollview);
		LayoutUtils.setRelativeLayoutParams(scrollview, mLyricsScrollViewSize.Width, mLyricsScrollViewSize.Height, mShowButtonSize.Width + 1, 0, -1);
		scrollview.setBackground(ImageUtils.makeRoundDrawable(180));
		scrollview.setSmoothScrollingEnabled(true);
		
			LinearLayout scrollLayout = new LinearLayout(getContext());
			scrollLayout.setOrientation(LinearLayout.VERTICAL);
			scrollview.addView(scrollLayout);
			LayoutUtils.setLayoutParams(scrollview,scrollLayout,mLyricsScrollViewSize.Width - 30,LayoutParams.WRAP_CONTENT);
			
				mLyricsTextView = ViewMaker.TextViewMaker(getContext(), scrollLayout, "", LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 15, 10);
				mLyricsTextView.setGravity(Gravity.CENTER_HORIZONTAL);
				mLyricsTextView.setTextColor(Color.WHITE);
				mLyricsTextView.setShadowLayer(1.0f, 1.0f, 1.0f, Color.BLACK);
				mLyricsTextView.setTextSize(TextSize);
				
				View blinkView = new View(getContext());
				scrollLayout.addView(blinkView);
				LayoutUtils.setLayoutParams(scrollLayout,blinkView,LayoutParams.MATCH_PARENT,mBlinkHeight);
	}

    public void setSwitchPanel(boolean isShow) {
    	if (mIsOpen == isShow)
    		return;
    	mIsOpen = isShow;
    	if (isShow) {
    		bringToFront();
    		mShowAnimation.startUsingDistance(this, false, mMoveDistances, 400,null);
    	} else {
    		mHideAnimation.startUsingDistance(this, false, -mMoveDistances, 400,null);
    	}
    }
    
    public void setInvisibleHidePanel() {
    	if (mIsOpen) {
    		mIsOpen = false;
    		mHideAnimation.startUsingDistance(this, false, -mMoveDistances, 400,mOnFlingListener);
    	} else {
    		this.setVisibility(View.INVISIBLE);
    	}
    }
    
    public void setLyricsText(String text) {
    	if (TextUtils.isEmpty(text)) {
    		setInvisibleHidePanel();
    		mLyricsTextView.setText("");
    	} else {
          	mLyricsTextView.setText(text);
    	}
    }
    
    public boolean isLyrics() {
    	if (TextUtils.isEmpty(mLyricsTextView.getText())) {
    		return false;
    	}
    	return true;
    }
}
