/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kds3393.just.viewer.Movie;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Image.PageView;
import com.kds3393.just.viewer.Image.ImageViewer.OnPageSelectedListener;
import com.kds3393.just.viewer.Movie.SmiParser.SmiData;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.View.TimeTextView;
import com.kds3393.just.viewer.provider.DBItemData;
import com.common.utils.ImageUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.Utils;
import com.common.utils.ViewMaker;
import com.common.utils.debug.CLog;
import com.common.utils.ResManager;

public class MovieController extends RelativeLayout {
	private static final String TAG = "MovieController";
	
    private OnControlFunctionListener mOnControlFunctionListener = null;
	public void setOnControlFunctionListener(OnControlFunctionListener listener) {
		mOnControlFunctionListener = listener;
	}
	
	public interface OnControlFunctionListener {
		public void onChangeMovie(boolean isNext);
		public void onDeleteCurrentMovie();
	}
	
	
    private MediaPlayerControl  mPlayer;
    private SeekBar             mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 3000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private static final int    SHOW_SMI_CAPTION = 3;
    private static final int    MOVE_POSITION = 4;
    
    private boolean             mUseFastForward;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    
    private Button         		mPrevButton;
    private Button         		mRewButton;
    private Button         		mPauseButton;
    private Button         		mFfwdButton;
    private Button         		mNextButton;
    private Button         		mDeleteButton;
    private Button         		mChangeSmiButton;
    private TextView 			mTitleView;
    public float mScale = 1.0f;
    
    private static Animation sFadeInAni;
    private static Animation sFadeOutAni;
    static {
    	sFadeInAni = new AlphaAnimation(0.0f,1.0f);
    	sFadeInAni.setDuration(700);
    	sFadeOutAni = new AlphaAnimation(1.0f,0.0f);
    	sFadeOutAni.setDuration(700);
    }
    
    public MovieController(Context context,boolean useFastForward) {
        super(context);
        initSize();
        mUseFastForward = useFastForward;
        initFloatingWindow();
        this.setVisibility(View.INVISIBLE);
    }

	private Size mTitleLayoutSize; 			//타이틀과 TopOption Layout이 들어가는 Layout Size
	private Size mTitleTextViewSize;		//타이틀 텍스트 View의 Size
	private Size mInfoLayoutSize;			//배터리, 시계 layout size
	private float mClockTextSize;			//time text size
	
	private Size mTopOptionLayoutSize;	//TopOption의 Layout Size
	private Size mOrientationButtonSize;	//화면 회전 버튼의 Size
	
	private Size mControlPanelSize;			//하단 Control Panel의 Size
	
	private Size mSeekBarSize;				//Seekbar의 Size
	private int mProgressThumbSize;		//Seekbar의 Thumb의 Size
	
	private Size mTimeTextLayoutSize;		//시간이 표시되는 Layout Size
	private Size mFunctionLayoutSize;			//function button이 들어가는 Layout Size
	
	private float mTitleTextSize;				//Title text size
	private float mTimeTextSize;			//time text size
	
	
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mTitleLayoutSize = new Size(LayoutParams.MATCH_PARENT,192);
			mInfoLayoutSize = new Size(120,64);
			mClockTextSize = 14;
			mTopOptionLayoutSize = new Size(LayoutParams.MATCH_PARENT, 128);
			mOrientationButtonSize = new Size(128, 128);
			
			mControlPanelSize = new Size(LayoutParams.MATCH_PARENT, 200);
			
			mSeekBarSize = new Size(LayoutParams.MATCH_PARENT,64);
			mProgressThumbSize = 62;
			
			mTimeTextLayoutSize = new Size(LayoutParams.MATCH_PARENT, 35);
			mFunctionLayoutSize = new Size(LayoutParams.MATCH_PARENT, 80);
			
			mTitleTextSize = 30;
			mTimeTextSize = 22;
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mTitleLayoutSize = new Size(LayoutParams.MATCH_PARENT,296);
			mInfoLayoutSize = new Size(180,128);
			mClockTextSize = 10;
			mTopOptionLayoutSize = new Size(LayoutParams.MATCH_PARENT, 168);
			mOrientationButtonSize = new Size(168, 168);
			
			mControlPanelSize = new Size(LayoutParams.MATCH_PARENT, 350);
			mSeekBarSize = new Size(LayoutParams.MATCH_PARENT,96);
			mProgressThumbSize = 286;
			
			mTimeTextLayoutSize = new Size(LayoutParams.MATCH_PARENT, 60);
			mFunctionLayoutSize = new Size(LayoutParams.MATCH_PARENT, 168);
			
			mTitleTextSize = 24;
			mTimeTextSize = 16;
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mTitleLayoutSize = new Size(LayoutParams.MATCH_PARENT,(int) (128 * ((0.5 * Size.Density) + 0.5)));
			mInfoLayoutSize = new Size(120,(int) (50 * ((0.5 * Size.Density) + 0.5)));
			mClockTextSize = (float) (18 / ((0.5 * Size.Density) + 0.5));
			mTopOptionLayoutSize = new Size(LayoutParams.MATCH_PARENT, 138);
			mOrientationButtonSize = new Size(128,128);
			
			mControlPanelSize = new Size(LayoutParams.MATCH_PARENT, (int) (154 * ((0.5 * Size.Density) + 0.5)));
			
			mSeekBarSize = new Size(LayoutParams.MATCH_PARENT, (int) (36 * Size.Density));
			mProgressThumbSize = (int) (70 * Size.Density);
			mTimeTextLayoutSize = new Size(LayoutParams.MATCH_PARENT, (int) (35 * ((0.5 * Size.Density) + 0.5)));
			mFunctionLayoutSize = new Size(LayoutParams.MATCH_PARENT, (int) (70 * ((0.5 * Size.Density) + 0.5)));
			
			mTitleTextSize = (float) (24 / ((0.5 * Size.Density) + 0.5));
			mTimeTextSize = (float) (20 / ((0.5 * Size.Density) + 0.5));
		}
		
		mTitleTextViewSize = new Size(Size.DisplayWidth - mInfoLayoutSize.Width - 30,mInfoLayoutSize.Height);
	}
	
    private void initFloatingWindow() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }
    
    private LinearLayout mTitlePanel;
	public BatteryView mBatteryView;
	private TextView mTimeTextView;
	
    private LinearLayout mContolPanel;
    public void initFloatingWindowLayout() {
    	mTitlePanel = ViewMaker.LinearMaker(getContext(), this, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	mTitlePanel.setOrientation(LinearLayout.VERTICAL);
    	
    		LinearLayout infoLayout = ViewMaker.LinearMaker(getContext(), mTitlePanel, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		infoLayout.setBackgroundColor(Color.parseColor("#aa000000"));
    		infoLayout.setOrientation(LinearLayout.HORIZONTAL);
    		
	    		mTitleView = ViewMaker.TextViewMaker(getContext(), infoLayout, "", mTitleTextViewSize.Width, mTitleTextViewSize.Height, 10, 0);
	    		mTitleView.setSingleLine();
	    		mTitleView.setEllipsize(TruncateAt.MARQUEE);
	    		mTitleView.setSelected(true);
	    		mTitleView.setTextSize(mTitleTextSize);
	    		mTitleView.setGravity(Gravity.CENTER);
	    		mTitleView.setShadowLayer(1.0f, 1.0f, 1.0f, Color.BLACK);
	    		
				LinearLayout statusLayout = ViewMaker.LinearMaker(getContext(), infoLayout, mInfoLayoutSize.Width, mInfoLayoutSize.Height, 10, 0, 10, 0);
				statusLayout.setOrientation(LinearLayout.VERTICAL);
				statusLayout.setGravity(Gravity.CENTER);
				
					mBatteryView = BatteryView.make(getContext(), statusLayout, LayoutParams.MATCH_PARENT, (mInfoLayoutSize.Height - 20)/2);
					mTimeTextView = TimeTextView.make(getContext(), statusLayout, LayoutParams.MATCH_PARENT, (mInfoLayoutSize.Height - 20)/2);
					mTimeTextView.setTextSize(mClockTextSize);
					mTimeTextView.setGravity(Gravity.CENTER);

    		LinearLayout optionLayout = ViewMaker.LinearMaker(getContext(), mTitlePanel, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    		optionLayout.setOrientation(LinearLayout.HORIZONTAL);
    		optionLayout.setGravity(Gravity.RIGHT);
					
	    		Button orientationBtn = ViewMaker.ButtonMaker(getContext(), optionLayout, mOrientationButtonSize.Width, mOrientationButtonSize.Height, 0, 10, 10, 0);
	    		orientationBtn.setBackgroundResource(R.drawable.z_rotation);
	    		orientationBtn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View view) {
						int screenWidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
						int screenHeight = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
						if (screenWidth > screenHeight) {
							mMovieData.mIsLeft = MoviePlayerActivity.MODE_PORTRAIT;
							Size.InitScreenSize(getContext(), true, true);
						} else {
							mMovieData.mIsLeft = MoviePlayerActivity.MODE_LANDSCAPE;
							Size.InitScreenSize(getContext(), false, true);
						}
					}
				});
	    		
    	mContolPanel = new LinearLayout(getContext());
    	this.addView(mContolPanel);
    	LayoutUtils.setRelativeLayoutParams(mContolPanel, mControlPanelSize.Width, mControlPanelSize.Height, RelativeLayout.ALIGN_PARENT_BOTTOM);
    	mContolPanel.setOrientation(LinearLayout.VERTICAL);
    	mContolPanel.setBackgroundColor(Color.parseColor("#aa000000"));
    	
        initControllerView();
    }
    
    private DBItemData mMovieData;
    public void setMovieData(DBItemData data) {
    	mMovieData = data;
    	if (mMovieData.mIsLeft == MoviePlayerActivity.MODE_LANDSCAPE) {
    		Size.InitScreenSize(getContext(), false, true);
    	} else {
    		Size.InitScreenSize(getContext(), true, true);
    	}
    }
    
    public void setTitle(String title) {
    	mTitleView.setText(title);
    }
    
    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    protected GestureDetector mParentTouchGestureDetector;
    public void setParentView(RelativeLayout layout) {
    	layout.setSoundEffectsEnabled(false);
    	mParentTouchGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
			@Override public void onLongPress(MotionEvent e) {}
			@Override public void onShowPress(MotionEvent e) {}
			@Override public boolean onFling(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {return true;}
			
			private int mBrightnessValue = 0;
			private int mMaxVolume = 0;
			private int mVolumeValue = 0;
			private AudioManager mAudioManager;
			private Toast toast;
			
			private float mDistance = 0;
			private float mStandardValue = 0;
			@Override 
			public boolean onDown(MotionEvent event) {
				if (!mShowing) {
					if (event.getX() < (Size.DisplayWidth / 2)) { // brightness
						try {
							mBrightnessValue = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
						} catch (Exception e) {
							CLog.e(TAG, e);
						}
						mStandardValue = Size.DisplayHeight / 255.0f;
					} else { // volume
						mAudioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
						mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						mVolumeValue = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
						mStandardValue = Size.DisplayHeight / mMaxVolume;
					}
					mDistance = 0;
					toast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
					
				}
				return true;
			}
			
			@Override 
			public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
				if (!mShowing) {
					if (e1.getX() < (Size.DisplayWidth / 2)) { // brightness
						mDistance += distanceY;
						Log.e(TAG,"brightness KDS3393_distanceY = " + mDistance);
						int value = (int) (mBrightnessValue + (mDistance / mStandardValue));
						if (value < 26)
							value = 26;
						else if (value > 255)
							value = 255;
						WindowManager.LayoutParams layoutParams = ((Activity) getContext()).getWindow().getAttributes();
				        layoutParams.screenBrightness = value/255.0f;
						Log.e(TAG,"KDS3393 layoutParams.screenBrightness = " + layoutParams.screenBrightness + " brightnessValue = " + value);
						
						Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
						((Activity) getContext()).getWindow().setAttributes(layoutParams);
						toast.setText("Brightness(max:255) : " + value);
						//toast = Toast.makeText(getContext(), "Brightness(max:255) : " + brightnessValue, 500);
						toast.show();
					} else { // volume
						Log.e(TAG,"volume KDS3393_distanceY = " + distanceY);
						mDistance += distanceY;
						int value  = (int) (mVolumeValue + (mDistance / mStandardValue));
						if (value < 0)
							value = 0;
						else if (value > mMaxVolume)
							value = mMaxVolume;
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_PLAY_SOUND);
						toast.setText("Volume(max:" + mMaxVolume + ") : " + value);
						toast.show();
					}
				}
				return true;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (mScale > 1.0f)
					return true;
                if (mShowing) {
                    hide();
                } else {
                	show();
                }
				return true;
			}
        });
    	
    	layout.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mParentTouchGestureDetector.onTouchEvent(event);
				return true;
			}
		});
    }
    
    private void initControllerView() {
    	// Start --------------------------- Seek bar ---------------------------------------------
        mProgress = new SeekBar(getContext());
        mContolPanel.addView(mProgress);
        LayoutUtils.setLinearLayoutParams(mProgress, mSeekBarSize.Width, mSeekBarSize.Height, 0, 0, 0, 0);
        
		Bitmap bm = ResManager.getResourceBitmap(getContext(),"seekbar_thumb");
    	Bitmap bmp = Bitmap.createScaledBitmap(bm, mProgressThumbSize, mProgressThumbSize, true);
    	if (bmp != bm) {
	    	bm.recycle();
    	}
    	bm = null;
    	Drawable d = ImageUtils.BitmapToDrawable(bmp);
        mProgress.setThumb(d);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);
        setProgressColor(mProgress);          //value2
        
    	// End   --------------------------- Seek bar ---------------------------------------------
        
        RelativeLayout timeLayout = new RelativeLayout(getContext());
        mContolPanel.addView(timeLayout);
    	LayoutUtils.setLinearLayoutParams(timeLayout, mTimeTextLayoutSize.Width, mTimeTextLayoutSize.Height);
    	
	        mCurrentTime = new TextView(getContext());
	        LayoutUtils.setRelativeLayoutParams(mCurrentTime, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 20, 0, 0, 0 ,RelativeLayout.ALIGN_PARENT_LEFT);
	        mCurrentTime.setGravity(Gravity.CENTER);
	        mCurrentTime.setTextColor(Color.WHITE);
	        mCurrentTime.setTextSize(mTimeTextSize);
	        timeLayout.addView(mCurrentTime);
	
	        
	        
	        mEndTime = new TextView(getContext());
	        LayoutUtils.setRelativeLayoutParams(mEndTime, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 0, 0, 20, 0 ,RelativeLayout.ALIGN_PARENT_RIGHT);
	        mEndTime.setGravity(Gravity.CENTER);
	        mEndTime.setTextColor(Color.WHITE);
	        mEndTime.setTextSize(mTimeTextSize);
	        timeLayout.addView(mEndTime);
    	
	    
        LinearLayout functionLayout = new LinearLayout(getContext());
        mContolPanel.addView(functionLayout);
    	LayoutUtils.setLinearLayoutParams(functionLayout, mFunctionLayoutSize.Width,mFunctionLayoutSize.Height);
    	
    	
	    	mChangeSmiButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, 10, 0);
	    	mChangeSmiButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_smi_choice);
	    	mChangeSmiButton.requestFocus();
	    	mChangeSmiButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	mSmiLangIndex = (mSmiLangIndex+1) % mSmiArray.size();
	            	mSmiHandler.sendEmptyMessage(SHOW_SMI_CAPTION);
	            }
	        });
	    	if (mSmiArray == null || mSmiArray.size() <= 1)
	    		mChangeSmiButton.setVisibility(View.INVISIBLE);
	    	
	    	int leftMargin = ((Size.DisplayWidth - (mFunctionLayoutSize.Height * 2)) - ((mFunctionLayoutSize.Height - 10) * 5) - 40) / 2;
	        
	    	mPrevButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, leftMargin, 0);
	    	mPrevButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_prev);
	    	mPrevButton.requestFocus();
	    	mPrevButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	if (mOnControlFunctionListener != null)
	            		mOnControlFunctionListener.onChangeMovie(false);
	            }
	        });
	        
	        mRewButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, 10, 0);
	        mRewButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_rewind);
	        mRewButton.requestFocus();
	        mRewButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	doMovePos(false);
	                show(sDefaultTimeout);
	            }
	        });

        	mPauseButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, 10, 0);
        	mPauseButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_pause);
	        mPauseButton.requestFocus();
	        mPauseButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                doPauseResume();
	                show(sDefaultTimeout);
	            }
	        });
	        
	        mFfwdButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, 10, 0);
	        mFfwdButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_foward);
	        mFfwdButton.requestFocus();
	        mFfwdButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	doMovePos(true);
	                show(sDefaultTimeout);
	            }
	        });
	        
	        mNextButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, 10, 0);
	        mNextButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_next);
	        mNextButton.requestFocus();
	        mNextButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	if (mOnControlFunctionListener != null)
	            		mOnControlFunctionListener.onChangeMovie(true);
	            }
	        });
	        
	        mDeleteButton = ViewMaker.ButtonMaker(getContext(), functionLayout, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, leftMargin, 0);
	        mDeleteButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_delete);
	        mDeleteButton.requestFocus();
	        mDeleteButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	if (mOnControlFunctionListener != null)
	            		mOnControlFunctionListener.onDeleteCurrentMovie();
	            }
	        });
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public void onChangeOrientation(Configuration newConfig) {
    	int width = Size.getDisplaySize((Activity) getContext()).Width;
    	int leftMargin = ((width - (mFunctionLayoutSize.Height * 2)) - ((mFunctionLayoutSize.Height - 10) * 5) - 40) / 2;
		LayoutUtils.setLinearLayoutParams(mPrevButton, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, leftMargin, 0);
		LayoutUtils.setLinearLayoutParams(mDeleteButton, mFunctionLayoutSize.Height - 10, mFunctionLayoutSize.Height - 10, leftMargin, 0);
		LayoutUtils.setLinearLayoutParams(mTitleView, width - mInfoLayoutSize.Width - 30, mTitleTextViewSize.Height, 10, 0, 10, 0);
    }
    
    public void setProgressColor(ProgressBar progressBar) {
	    //Progress
    	CenterLineDrawable fg1GradDrawable = new CenterLineDrawable();
    	fg1GradDrawable.setBackGroundColor(Color.TRANSPARENT);
    	fg1GradDrawable.setLineColor(Color.BLUE);
    	
    	CenterLineDrawable bg1GradDrawable = new CenterLineDrawable();
    	bg1GradDrawable.setBackGroundColor(Color.TRANSPARENT);
    	bg1GradDrawable.setLineColor(Color.GRAY);
    	
    	fg1GradDrawable.getPaint().setColor(Color.TRANSPARENT);
	    ClipDrawable fg1clip = new ClipDrawable(fg1GradDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
	    
    	ClipDrawable bgclip = new ClipDrawable(bg1GradDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
    	bgclip.setLevel(10000);
	    //Setup LayerDrawable and assign to progressBar
	    Drawable[] progressDrawables = {bgclip, fg1clip};
	    LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);     
	    progressLayerDrawable.setId(0, android.R.id.background);
	    progressLayerDrawable.setId(1, android.R.id.progress);

	    //Copy the existing ProgressDrawable bounds to the new one.
	    Rect bounds = progressBar.getProgressDrawable().getBounds();
	    progressBar.setProgressDrawable(progressLayerDrawable);     
	    progressBar.getProgressDrawable().setBounds(bounds);

	    //now force a redraw
	    progressBar.invalidate();
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        try {
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {
        mShowing = true;
    	setProgress();
    	disableUnsupportedButtons();
        updatePausePlay();
        setVisibility(View.VISIBLE);
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        
        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
        if (getVisibility() != View.VISIBLE) {
	        setVisibility(View.VISIBLE);
	        this.clearAnimation();
	        this.startAnimation(sFadeInAni);
        }
    }

    /**
     * Remove the controller from the screen.
     */
    public boolean hide() {
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if (getVisibility() == View.VISIBLE) {
	                setVisibility(View.INVISIBLE);
	                this.clearAnimation();
	                this.startAnimation(sFadeOutAni);
                }
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
            return true;
        }
        return false;
    }
    
    public void forceHide() {
    	if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                setVisibility(View.INVISIBLE);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }
    
    private TextView mSmiTextView;
    private ArrayList<ArrayList<SmiData>> mSmiArray;
    
    public void setSmi(ArrayList<ArrayList<SmiData>> smiArray,TextView view) {
    	mSmiTextView = view;
    	mSmiArray = smiArray;
    }
    
    public void startSmi() {
    	if (mSmiArray != null && mSmiArray.size() > 0 && mSmiArray.get(mSmiLangIndex).size() > 0)
    		mSmiHandler.sendEmptyMessage(SHOW_SMI_CAPTION);
    }
    
    private int mSmiLangIndex = 0;
    private Handler mSmiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case SHOW_SMI_CAPTION:
                	if (mSmiArray == null)
                		return;
                	pos = mPlayer.getCurrentPosition();
                	int countSmi = getSyncIndex(pos);
                	mSmiTextView.setText(Html.fromHtml(mSmiArray.get(mSmiLangIndex).get(countSmi).gettext()));
                	//CLog.e(TAG, "KDS3393_SMI pos = " + pos + " delay = " + (1000 - (pos % 1000)));
                	msg = obtainMessage(SHOW_SMI_CAPTION);
               		sendMessageDelayed(msg, 1000 - (pos % 1000));
                	
                	break;
            }
        }
    };

    private Handler mHandler = new Handler() {
    	int oldPos = 0;
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case MOVE_POSITION:
                	int moveLength = msg.arg1;
                	boolean isNext = (msg.arg2==0);
                	CLog.e(TAG, "KDS3393_Position = " + mPlayer.getCurrentPosition() + " moveLength = " + moveLength + " oldPos = " + oldPos);
                	if (isNext) {
                		CLog.e(TAG, "KDS3393_duration = " + mPlayer.getDuration() + " : " + (oldPos + moveLength) );
                		if (oldPos == 0 || mPlayer.getCurrentPosition() < (oldPos + moveLength - 2000)) {
                			if ((oldPos + moveLength) >= mPlayer.getDuration()) {
                				oldPos = 0;
                				return;
                			}
                			oldPos = mPlayer.getCurrentPosition();
                			mPlayer.seekTo(oldPos + moveLength);
                    		msg = obtainMessage(MOVE_POSITION,moveLength + 1000,msg.arg2);
                    		sendMessageDelayed(msg, 100);
                    		return;
                		}
                	} else {
                		if (oldPos == 0 || mPlayer.getCurrentPosition() > (oldPos + moveLength + 2000)) {
                			oldPos = mPlayer.getCurrentPosition();
                			if ((oldPos + moveLength) < 0) {
                				oldPos = 0;
                				mPlayer.seekTo(0);
                				return;
                			}
                			mPlayer.seekTo(oldPos + moveLength);
                    		msg = obtainMessage(MOVE_POSITION,moveLength - 1000,msg.arg2);
                    		sendMessageDelayed(msg, 100);
                    		return;
                		}
                	}
                	
                	oldPos = 0;

                	
                	
                	break;
            }
        }
    };

    
    
    private int getSyncIndex(long playTime) {
    	int l=0,m,h=mSmiArray.get(mSmiLangIndex).size();
    	
    	while(l <= h) {
    		m = (l+h)/2;
    		if(mSmiArray.get(mSmiLangIndex).get(m).gettime() <= playTime && mSmiArray.get(mSmiLangIndex).size() > (m+1) && playTime < mSmiArray.get(mSmiLangIndex).get(m+1).gettime()) {
    			return m;
    		}
    		if(mSmiArray.get(mSmiLangIndex).size() > (m+1) && playTime > mSmiArray.get(mSmiLangIndex).get(m+1).gettime()) {
    			l=m+1;
    		} else {
    			h=m-1;
    		}
    	}
    	return 0;
    }
    
    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int persent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(persent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(Utils.getStringForDate(duration,"HH:mm:ss"));
        if (mCurrentTime != null)
            mCurrentTime.setText(Utils.getStringForDate(position,"HH:mm:ss"));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mShowing)
                hide();
            else
            	show(sDefaultTimeout);
    	}
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                if (hide())
                	return true;
            }
            return super.dispatchKeyEvent(event);
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private void updatePausePlay() {
        if (mPauseButton == null)
            return;
        if (mPlayer.isPlaying()) {
        	mPauseButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_pause);
        } else {
        	mPauseButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
        	mPauseButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_pause);
            mPlayer.pause();
        } else {
        	mPauseButton.setBackgroundResource(com.kds3393.just.viewer.R.drawable.h_media_play);
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doMovePos(boolean isNext) {
    	Message msg;
    	if (isNext) {
    		msg = mHandler.obtainMessage(MOVE_POSITION,5000,0);
    	} else {
    		msg = mHandler.obtainMessage(MOVE_POSITION,-5000,1);
    	}
    	mHandler.sendMessage(msg);
    }
    
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);
            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(Utils.getStringForDate(newposition,"HH:mm:ss"));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }

        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

//    @Override
//    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
//        super.onInitializeAccessibilityEvent(event);
//        event.setClassName(MovieController.class.getName());
//    }
//
//    @Override
//    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
//        super.onInitializeAccessibilityNodeInfo(info);
//        info.setClassName(MovieController.class.getName());
//    }
}
