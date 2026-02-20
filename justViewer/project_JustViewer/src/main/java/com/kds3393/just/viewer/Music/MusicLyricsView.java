package com.kds3393.just.viewer.Music;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation.OnFlingListener;
import com.kds3393.just.viewer.R;

public class MusicLyricsView extends LinearLayout {
	private static final String TAG = "MusicLyricsView";
	
	private ScrollView mLyricsScrollView;
	private TextView mLyricsTextView;
	private ImageButton mOpenButton;
	private boolean mIsOpen = false;
	private OnFlingListener mOnFlingListener;
	public MusicLyricsView(Context context) {
		super(context);
		init();
	}
	
	public MusicLyricsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.v_music_lyrics, this);
		setOrientation(LinearLayout.HORIZONTAL);
		createView();
		mOnFlingListener = new OnFlingListener(){
			@Override
			public void endFling(View view) {
				MusicLyricsView.this.setVisibility(View.INVISIBLE);
			}
		};
	}

	private int mMoveDistances;						//Open Animation시 이동 거리

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mMoveDistances = getLayoutParams().width - mOpenButton.getWidth();
	}

    private LayoutTranslateAnimation mShowAnimation;
    private LayoutTranslateAnimation mHideAnimation;
	protected GestureDetector mOpenButtonGestureDetector;
	private void createView() {
		mShowAnimation = new LayoutTranslateAnimation(getContext(),new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(getContext(),new OvershootInterpolator());
		
		mOpenButton = (ImageButton) findViewById(R.id.side_open_btn);
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

		mLyricsScrollView = (ScrollView) findViewById(R.id.side_lyrics_scroll);
		mLyricsTextView = (TextView) findViewById(R.id.side_lyrics_txt);
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
