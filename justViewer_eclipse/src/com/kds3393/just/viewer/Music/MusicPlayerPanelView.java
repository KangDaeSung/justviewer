package com.kds3393.just.viewer.Music;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.Utils;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Movie.CenterLineDrawable;
import com.kds3393.just.viewer.Music.MusicService.OnMediaPlayerListener;

public class MusicPlayerPanelView extends RelativeLayout {
	private static final String TAG = "MusicPlayerPanelView";
	
	private MusicService mService;
	
	private boolean mIsMusicPanelShow = false;
	
	private TextView mMp3TitleTxt;
	
	private TextView mMp3ArtistTxt;
	private Button mFavoriteBtn;
	
	private SeekBar mProgressBar;
	
	private TextView mCurrentTimeTxt;
	private TextView mEndTimeTxt;
	
	private Button mSleepTimerBtn;
	private Button mShuffleBtn;
	private Button mPrevBtn;
	private Button mPlayPauseBtn;
	private Button mNextBtn;
	private Button mDeleteBtn;
	
    private LayoutTranslateAnimation mShowAnimation;
    private LayoutTranslateAnimation mHideAnimation;
    
    private int mMoveDistances;						//Open Animation시 이동 거리
    
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}
	
	protected GestureDetector mOpenButtonGestureDetector;
	public MusicPlayerPanelView(Context context) {
		super(context);
		mShowAnimation = new LayoutTranslateAnimation(context,new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(context,new OvershootInterpolator());
		createView();
	}
	
	public MusicPlayerPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mShowAnimation = new LayoutTranslateAnimation(context,new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(context,new OvershootInterpolator());
		createView();
	}
	
	
	
	protected MusicLyricsView mMusicLyricsPanel;
	public void setLyricsView(MusicLyricsView lyrics) {
		mMusicLyricsPanel = lyrics;
	}
	
	protected MusicListView mMusicListPanel;
	public void setMusicList(MusicListView l) {
		mMusicListPanel = l;
	}
	
	private OnMediaPlayerListener mMediaListener;
	public void setMusicService(MusicService service) {
		mService = service;
		if (mMusicListPanel != null) {
			mMusicListPanel.setMusicService(service);
		}
		if (mService != null) {
			if (mService.getMediaPlayer().isPlaying() || mService.isPause()) {
				mProgressBar.setMax(mService.getMediaPlayer().getDuration());
				
				mEndTimeTxt.setText(Utils.getStringForDate(mService.getMediaPlayer().getDuration(),"mm:ss"));
				Mp3Id3Parser data = mService.getMusicMetaData();
				mMp3TitleTxt.setText(data.mTitle);
				mMp3ArtistTxt.setText(data.mArtist);
				this.setVisibility(View.VISIBLE);
				if (mMusicLyricsPanel != null) {
					if (isShow())
						mMusicLyricsPanel.setVisibility(View.VISIBLE);
					mMusicLyricsPanel.setLyricsText(data.mLyrics);
				}
				
	    		if (mMusicListPanel != null) {
	    			if (isShow())
	    				mMusicListPanel.setVisibility(View.VISIBLE);
	    			mMusicListPanel.setMusicListItem(mService.getMusicList());
	    		}
			}
			
			mMediaListener = new OnMediaPlayerListener() {
				@Override
				public void onStartCommanded(ArrayList<String> array) {
					if (mMusicListPanel != null)
						mMusicListPanel.setMusicListItem(mService.getMusicList());
				}
				
				@Override
				public void onPrepared(MediaPlayer mediaplsyer, Mp3Id3Parser data) {
					mMp3TitleTxt.setText(data.mTitle);
					mMp3ArtistTxt.setText(data.mArtist);
					mProgressBar.setProgress(0);
					mProgressBar.setMax(mService.getMediaPlayer().getDuration());
					mEndTimeTxt.setText(Utils.getStringForDate(mediaplsyer.getDuration(),"mm:ss"));
					setPlayButtonImage();
					if (MusicPlayerPanelView.this.getVisibility() != View.VISIBLE) {
						MusicPlayerPanelView.this.setVisibility(View.VISIBLE);
					}
					if (mMusicLyricsPanel != null) {
						if (isShow())
							mMusicLyricsPanel.setVisibility(View.VISIBLE);
						mMusicLyricsPanel.setLyricsText(data.mLyrics);
					}
					
		    		if (mMusicListPanel != null) {
		    			if (isShow())
		    				mMusicListPanel.setVisibility(View.VISIBLE);
		    			mMusicListPanel.onChangePlayMusic();
		    		}
		    			
				}

				@Override
				public void onCompletion(MediaPlayer mediaplsyer) {
					if (MusicPlayerPanelView.this.getVisibility() == View.VISIBLE) {
						MusicPlayerPanelView.this.setVisibility(View.GONE);
						if (mMusicLyricsPanel != null)
							mMusicLyricsPanel.setInvisibleHidePanel();
						if (mMusicListPanel != null)
							mMusicListPanel.setInvisibleHidePanel();
						setSwitchMusicPanel(false);
					}
				}

				@Override
				public void onPlay(MediaPlayer mediaplsyer, String playPath) {
					mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_pause);
				}
				
				@Override
				public void onPause(MediaPlayer mediaplsyer, String playPath) {
					mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_play);
				}

				@Override
				public void onDeleteMusicFile(String filePath) {
					if (mMusicListPanel != null) {
						mMusicListPanel.setMusicListItem(mService.getMusicList());
		    		}
				}

				@Override
				public void onSleepTimer(boolean isRun, String timeString) {
					if (isRun) {
						mSleepTimerBtn.setBackgroundResource(R.drawable.h_clock);
						Toast.makeText(getContext(), timeString + " 뒤에 음악을 종료합니다.",Toast.LENGTH_SHORT).show();
					} else {
						mSleepTimerBtn.setBackgroundResource(R.drawable.h_clock_off);
					}
				}
			};
			
			mService.setOnMediaPlayerListener(this,mMediaListener);
		}
		
	}
	
	public boolean isMusicPlaying() {
		if (mService != null && mService.getMediaPlayer() == null && mService.getMediaPlayer().isPlaying()) {
			return true;
		}
		return false;
	}
    public boolean isShow() {
    	return mIsMusicPanelShow;
    }
    
    public void setSwitchMusicPanel(boolean isShow) {
    	if (mIsMusicPanelShow == isShow)
    		return;
    	mIsMusicPanelShow = isShow;
    	if (isShow) {
    		mPanelHandler.sendEmptyMessage(SHOW_MUSIC_PANEL);
    		mShowAnimation.startUsingDistance(this, true, mMoveDistances, 400,null);
    		setPlayButtonImage();
    		if (mMusicLyricsPanel != null && mMusicLyricsPanel.isLyrics()) {
				mMusicLyricsPanel.setVisibility(View.VISIBLE);
			}
    		if (mMusicListPanel != null)
    			mMusicListPanel.setVisibility(View.VISIBLE);
    		if (mService != null && mService.isRunSleepTimer()) {
				mSleepTimerBtn.setBackgroundResource(R.drawable.h_clock);
			} else {
				mSleepTimerBtn.setBackgroundResource(R.drawable.h_clock_off);
			}
    	} else {
    		if (mMusicLyricsPanel != null) {
				mMusicLyricsPanel.setInvisibleHidePanel();
			}
    		if (mMusicListPanel != null)
    			mMusicListPanel.setInvisibleHidePanel();
    		mHideAnimation.startUsingDistance(this, true, -mMoveDistances, 400,null);
    	}
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	if (mMoveDistances == 0) {
        	mMoveDistances = t;
    	}
    }
    
	public void createView() {
		LinearLayout bgLayout = (LinearLayout) inflate(getContext(), R.layout.music_panel, null);
		addView(bgLayout);
		
			// line 0	// open btn
			Button btn = (Button) findViewById(R.id.panel_open);
			mOpenButtonGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
				@Override public boolean onDown(MotionEvent event) {return true;}
				@Override public void onLongPress(MotionEvent e) {}
				@Override public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {return true;}
				@Override public void onShowPress(MotionEvent e) {}

				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					if (!mIsMusicPanelShow && velocityY < 0)
						setSwitchMusicPanel(true);
					else if (mIsMusicPanelShow && velocityY > 0)
						setSwitchMusicPanel(false);
					return true;
				}
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					setSwitchMusicPanel(!mIsMusicPanelShow);
					return true;
				}
	        });
			btn.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mOpenButtonGestureDetector.onTouchEvent(event);
					return true;
				}
			});
				
			mMp3TitleTxt = (TextView) findViewById(R.id.panel_title);
			mMp3ArtistTxt = (TextView) findViewById(R.id.panel_artist);
			mFavoriteBtn = (Button) findViewById(R.id.panel_favo);
				
			mFavoriteBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					
				}
			});

			mCurrentTimeTxt = (TextView) findViewById(R.id.panel_current_time);
	        mEndTimeTxt = (TextView) findViewById(R.id.panel_duration_time);
			mProgressBar = (SeekBar) findViewById(R.id.panel_seekbar);
	        
			mSleepTimerBtn = (Button) findViewById(R.id.panel_sleep_timer);
			mSleepTimerBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mService.showSleepTimerDialog(getContext());
				}
			});
			
			mShuffleBtn = (Button) findViewById(R.id.panel_shuffle);
			mShuffleBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					boolean isMix = !SettingActivity.getMusicListShuffle(getContext());
					SettingActivity.setMusicListShuffle(getContext(),isMix);
					setSuppleButtonImage();
					if (mService != null) {
						mService.setShuffle(isMix);
					}
					
					if (mMusicListPanel != null)
						mMusicListPanel.setMusicListItem(mService.getMusicList());
				}
			});
			
			mPrevBtn = (Button) findViewById(R.id.panel_prev);
			mPrevBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mService.movePrev();
				}
			});
			
			mPlayPauseBtn = (Button) findViewById(R.id.panel_play);
			mPlayPauseBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					if (mService.getMediaPlayer().isPlaying()) {
						mService.pause();
						mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_play);
					} else {
						mService.play();
						mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_pause);
					}
				}
			});
			
			mNextBtn = (Button) findViewById(R.id.panel_next);
			mNextBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mService.moveNext(false);
				}
			});
			
			mDeleteBtn = (Button) findViewById(R.id.panel_del);
			mDeleteBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mService.deleteCurrentMp3();
				}
			});
	}
	
    public void setProgressColor(ProgressBar progressBar) {
	    //Progress
    	CenterLineDrawable fg1GradDrawable = new CenterLineDrawable();
    	fg1GradDrawable.setBackGroundColor(Color.TRANSPARENT);
    	fg1GradDrawable.setLineColor(Color.parseColor("#8ae3d6"));
    	
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
    
    private void setPlayButtonImage() {
    	if (mService != null) {
    		if (mService.getMediaPlayer().isPlaying())
    			mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_pause);
    		else
    			mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_play);
    	} else {
    		mPlayPauseBtn.setBackgroundResource(R.drawable.h_media_play);
    	}
    }
    
    private void setSuppleButtonImage() {
    	if (SettingActivity.getMusicListShuffle(getContext())) {
    		mShuffleBtn.setBackgroundResource(R.drawable.h_media_shuffle_on);
    	} else {
    		mShuffleBtn.setBackgroundResource(R.drawable.h_media_shuffle_off);
    	}
    }
    
	private boolean mDragging = false;
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        	mDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        	if (!fromuser) {
                return;
            }
        	if (mService.getMediaPlayer().isPlaying()) {
        		mService.seekTo(progress);
        	}
        }

        public void onStopTrackingTouch(SeekBar bar) {
        	mDragging = false;
        }
    };
    
    private static final int SHOW_MUSIC_PANEL = 0;
    private Handler mPanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos = 0;
            switch (msg.what) {
                case SHOW_MUSIC_PANEL:
                	if (mIsMusicPanelShow && mService.getMediaPlayer() != null) {
            			pos = mService.getMediaPlayer().getCurrentPosition();
            			if (!mDragging) {
            				mProgressBar.setProgress(pos);
            			}
                    	msg = obtainMessage(SHOW_MUSIC_PANEL);
                   		sendMessageDelayed(msg, 1000 - (pos % 1000));
            		}
                	mCurrentTimeTxt.setText(Utils.getStringForDate(pos,"mm:ss"));
                	break;
            }
        }
    };
    
    public void setMusicPause() {
    	if (mService != null) {
    		mService.pause();
    	}
    }
    
    public void onResume() {
    	if (mService != null) {
    		setPlayButtonImage();
    		mService.setOnMediaPlayerListener(this,mMediaListener);
    	}
    	setSuppleButtonImage();
    }
    
    public void clear() {
    	if (mService != null) {
    		mService.setOnMediaPlayerListener(this,null);
    	}
    }
    
    
}
