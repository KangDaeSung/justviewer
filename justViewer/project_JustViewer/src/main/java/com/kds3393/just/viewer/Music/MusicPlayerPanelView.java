package com.kds3393.just.viewer.Music;

import android.content.Context;
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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.Utils;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Music.MusicService.OnMediaPlayerListener;
import com.kds3393.just.viewer.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicPlayerPanelView extends LinearLayout {
	private static final String TAG = "MusicPlayerPanelView";
	
	private MusicService mService;
	
	private boolean mIsMusicPanelShow = false;

    @BindView(R.id.panel_open) Button panel_open;
    @BindView(R.id.panel_title) TextView panel_title;
    @BindView(R.id.panel_artist) TextView panel_artist;
    @BindView(R.id.panel_favo) Button panel_favo;
    @BindView(R.id.panel_current_time) TextView panel_current_time;
    @BindView(R.id.panel_seekbar) SeekBar panel_seekbar;
    @BindView(R.id.panel_duration_time) TextView panel_duration_time;
    @BindView(R.id.panel_sleep_timer) Button panel_sleep_timer;
    @BindView(R.id.panel_shuffle) Button panel_shuffle;
    @BindView(R.id.panel_prev) Button panel_prev;
    @BindView(R.id.panel_play) Button panel_play;
    @BindView(R.id.panel_next) Button panel_next;
    @BindView(R.id.panel_stop) Button panel_stop;
    @BindView(R.id.panel_del) Button panel_del;


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
		init();
	}
	
	public MusicPlayerPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mShowAnimation = new LayoutTranslateAnimation(getContext(),new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(getContext(),new OvershootInterpolator());

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER_HORIZONTAL);
		setBackgroundResource(R.drawable.z_music_panel);

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
                panel_seekbar.setMax(mService.getMediaPlayer().getDuration());
                panel_duration_time.setText(Utils.getStringForDate(mService.getMediaPlayer().getDuration(),"mm:ss"));
				Mp3Id3Parser data = mService.getMusicMetaData();
                panel_title.setText(data.mTitle);
                panel_artist.setText(data.mArtist);
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
                    panel_title.setText(data.mTitle);
                    panel_artist.setText(data.mArtist);
                    panel_seekbar.setProgress(0);
                    panel_seekbar.setMax(mService.getMediaPlayer().getDuration());
                    panel_duration_time.setText(Utils.getStringForDate(mediaplsyer.getDuration(),"mm:ss"));
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
                    panel_play.setBackgroundResource(R.drawable.h_media_pause);
				}
				
				@Override
				public void onPause(MediaPlayer mediaplsyer, String playPath) {
                    panel_play.setBackgroundResource(R.drawable.h_media_play);
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
                        panel_sleep_timer.setBackgroundResource(R.drawable.h_clock);
						Toast.makeText(getContext(), timeString + " 뒤에 음악을 종료합니다.",Toast.LENGTH_SHORT).show();
					} else {
                        panel_sleep_timer.setBackgroundResource(R.drawable.h_clock_off);
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
                panel_sleep_timer.setBackgroundResource(R.drawable.h_clock);
			} else {
                panel_sleep_timer.setBackgroundResource(R.drawable.h_clock_off);
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
		inflate(getContext(), R.layout.music_panel, this);
        ButterKnife.bind(this, this);

        panel_seekbar.setOnSeekBarChangeListener(mSeekListener);
		// line 0	// open btn
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
        panel_open.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mOpenButtonGestureDetector.onTouchEvent(event);
				return true;
			}
		});

        panel_favo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

			}
		});

        panel_sleep_timer.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mService.showSleepTimerDialog(getContext());
			}
		});

        panel_shuffle.setOnClickListener(new OnClickListener(){
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
        panel_prev.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mService.movePrev();
			}
		});
        panel_play.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (mService.getMediaPlayer().isPlaying()) {
					mService.pause();
                    panel_play.setBackgroundResource(R.drawable.h_media_play);
				} else {
					mService.play();
                    panel_play.setBackgroundResource(R.drawable.h_media_pause);
				}
			}
		});

        panel_next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mService.moveNext(false);
			}
		});
        panel_stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mService.stop();
			}
		});
        panel_del.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mService.deleteCurrentMp3();
			}
		});
	}
    
    private void setPlayButtonImage() {
    	if (mService != null) {
    		if (mService.getMediaPlayer().isPlaying())
                panel_play.setBackgroundResource(R.drawable.h_media_pause);
    		else
                panel_play.setBackgroundResource(R.drawable.h_media_play);
    	} else {
            panel_play.setBackgroundResource(R.drawable.h_media_play);
    	}
    }
    
    private void setSuppleButtonImage() {
    	if (SettingActivity.getMusicListShuffle(getContext())) {
            panel_shuffle.setBackgroundResource(R.drawable.h_media_shuffle_on);
    	} else {
            panel_shuffle.setBackgroundResource(R.drawable.h_media_shuffle_off);
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
                            panel_seekbar.setProgress(pos);
            			}
                    	msg = obtainMessage(SHOW_MUSIC_PANEL);
                   		sendMessageDelayed(msg, 1000 - (pos % 1000));
            		}
                    panel_current_time.setText(Utils.getStringForDate(pos,"mm:ss"));
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
