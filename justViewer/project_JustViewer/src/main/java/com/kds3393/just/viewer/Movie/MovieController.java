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
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.Size;
import com.common.utils.Utils;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Movie.SmiParser.SmiData;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.provider.DBItemData;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public class MovieController extends RelativeLayout implements View.OnClickListener{
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
    
    private ImageButton        mPrevButton;
    private ImageButton        mRewButton;
    private ImageButton        mPauseButton;
    private ImageButton        mFfwdButton;
    private ImageButton        mNextButton;
    private ImageButton        mDeleteButton;
    private ImageButton        mChangeSmiButton;
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

    public MovieController(Context context) {
        super(context);
        init();
    }

    public MovieController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

	private void init() {
		setFocusable(true);
		setFocusableInTouchMode(true);
		setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		requestFocus();

		initFloatingWindowLayout();
		this.setVisibility(View.INVISIBLE);
	}

	public void setUseFastForward(boolean useFastForward) {
		mUseFastForward = useFastForward;
	}

    private RelativeLayout mTitlePanel;
	public BatteryView mBatteryView;
	private TextView mTimeTextView;
	
    private LinearLayout mContolPanel;
    public void initFloatingWindowLayout() {
		inflate(getContext(),R.layout.v_movie_controller,this);

    	mTitlePanel = (RelativeLayout) findViewById(R.id.title_panel);
		mTitleView = (TextView) findViewById(R.id.txt_title);
		mBatteryView = (BatteryView) findViewById(R.id.txt_battery);
		mTimeTextView = (TextView) findViewById(R.id.txt_time);
		findViewById(R.id.orientation_btn).setOnClickListener(this);
		mContolPanel = (LinearLayout) findViewById(R.id.control_panel);

		mProgress = (SeekBar) findViewById(R.id.movie_seekbar);
		mProgress.setOnSeekBarChangeListener(mSeekListener);

		mCurrentTime = (TextView) findViewById(R.id.movie_current_time);
		mEndTime = (TextView) findViewById(R.id.movie_max_time);

		mChangeSmiButton = (ImageButton) findViewById(R.id.movie_smi_choice);

		if (mSmiArray == null || mSmiArray.size() <= 1)
			mChangeSmiButton.setVisibility(View.INVISIBLE);

		mPrevButton = (ImageButton) findViewById(R.id.movie_prev);
		mRewButton = (ImageButton) findViewById(R.id.movie_rew);
		mPauseButton = (ImageButton) findViewById(R.id.movie_pause);
		mFfwdButton = (ImageButton) findViewById(R.id.movie_ffwd);
		mNextButton = (ImageButton) findViewById(R.id.movie_next);
		mDeleteButton = (ImageButton) findViewById(R.id.movie_del);

		mChangeSmiButton.setOnClickListener(this);
		mPrevButton.setOnClickListener(this);
		mRewButton.setOnClickListener(this);
		mPauseButton.setOnClickListener(this);
		mFfwdButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mDeleteButton.setOnClickListener(this);

		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }
    
    private DBItemData mMovieData;
    public void setMovieData(DBItemData data) {
    	mMovieData = data;
    }

	public void setActivityOrientation() {
		Activity act = (Activity) getContext();
		if (mMovieData.mIsLeft == MoviePlayerActivity.MODE_LANDSCAPE &&
				act.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
			Log.e(TAG,"KDS3393_TEST_delay_SCREEN_ORIENTATION_SENSOR_LANDSCAPE = " + act.getRequestedOrientation());
			Size.InitScreenSize(getContext(), false, true);
		} else if (mMovieData.mIsLeft == MoviePlayerActivity.MODE_PORTRAIT &&
				act.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
			Log.e(TAG,"KDS3393_TEST_delay_SCREEN_ORIENTATION_SENSOR_PORTRAIT = " + act.getRequestedOrientation());
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
    public void setParentView(View parent) {
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

		parent.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mParentTouchGestureDetector.onTouchEvent(event);
				return true;
			}
		});
    }

	@Override
	public void onClick(View view) {
		if (view == mChangeSmiButton) {
			mSmiLangIndex = (mSmiLangIndex+1) % mSmiArray.size();
			mSmiHandler.sendEmptyMessage(SHOW_SMI_CAPTION);
		} else if (view == mPrevButton) {
			if (mOnControlFunctionListener != null)
				mOnControlFunctionListener.onChangeMovie(false);
		} else if (view == mRewButton) {
			doMovePos(false);
			show(sDefaultTimeout);
		} else if (view == mPauseButton) {
			doPauseResume();
			show(sDefaultTimeout);
		} else if (view == mFfwdButton) {
			doMovePos(true);
			show(sDefaultTimeout);
		} else if (view == mNextButton) {
			if (mOnControlFunctionListener != null)
				mOnControlFunctionListener.onChangeMovie(true);
		} else if (view == mDeleteButton) {
			if (mOnControlFunctionListener != null)
				mOnControlFunctionListener.onDeleteCurrentMovie();
		} else if (view.getId() == R.id.orientation_btn) {
			int screenWidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
			int screenHeight = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
			if (screenWidth > screenHeight) {
				mMovieData.mIsLeft = MoviePlayerActivity.MODE_PORTRAIT;
			} else {
				mMovieData.mIsLeft = MoviePlayerActivity.MODE_LANDSCAPE;
			}
			setActivityOrientation();
		}
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
    	if (mSmiArray != null && mSmiArray.size() > 0 && mSmiArray.get(mSmiLangIndex).size() > 0) {
			mSmiHandler.sendEmptyMessage(SHOW_SMI_CAPTION);
		}
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
        	mPauseButton.setImageResource(com.kds3393.just.viewer.R.drawable.h_media_pause);
        } else {
        	mPauseButton.setImageResource(com.kds3393.just.viewer.R.drawable.h_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
        	mPauseButton.setImageResource(com.kds3393.just.viewer.R.drawable.h_media_pause);
            mPlayer.pause();
        } else {
        	mPauseButton.setImageResource(com.kds3393.just.viewer.R.drawable.h_media_play);
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
