package com.kds3393.just.viewer.Movie;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Browser.BrowserView;
import com.kds3393.just.viewer.Movie.MovieController.OnControlFunctionListener;
import com.kds3393.just.viewer.Movie.SmiParser.SmiData;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Utils.ReceiverManager;
import com.kds3393.just.viewer.provider.DBItemData;
import com.kds3393.just.viewer.provider.DBMgr;

import java.util.ArrayList;

public class MoviePlayerActivity extends ParentActivity implements OnPreparedListener, OnErrorListener, OnCompletionListener {
	private static final String TAG = "MoviePlayerActivity";

	public static final int MODE_PORTRAIT = 0;
	public static final int MODE_LANDSCAPE = 1;
	public static final int MODE_NULL = 2;

	private String mContentPath;
	private ArrayList<String> mFilePaths;
	private int mMovieIndex;
	
	private MovieViewer mVideoView;
	private MovieController mMoiveController;
	
	private DBItemData mMovieData;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentPath = getIntent().getStringExtra(BrowserView.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY);
        mMovieIndex = mFilePaths.indexOf(mContentPath);
        if (TextUtils.isEmpty(mContentPath)) {
			finish();
		}
    	
        mMovieData = loadMovieData(mContentPath);
        setContentView(R.layout.act_movie_viewer);
		initMovieLayout();

        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
    }
	
	private DBItemData loadMovieData(String path) {
		DBItemData data = DBMgr.getInstance().bookmarkLoad(mContentPath);
    	if (data == null) {
    		data = new DBItemData(mContentPath,0,MODE_NULL);
            DBMgr.getInstance().bookmarkInsert(data);
    	}
    	return data;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void setOrientation() {
		if (mMovieData.mIsLeft == MoviePlayerActivity.MODE_LANDSCAPE &&
				getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
			Log.e(TAG,"KDS3393_TEST_delay orienation SCREEN_ORIENTATION_SENSOR_LANDSCAPE");
			Size.InitScreenSize(this, false, true);
		} else if (mMovieData.mIsLeft == MoviePlayerActivity.MODE_PORTRAIT &&
				getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
			Log.e(TAG,"KDS3393_TEST_delay orienation SCREEN_ORIENTATION_SENSOR_PORTRAIT");
			Size.InitScreenSize(this, true, true);
		}
	}

	private TextView mSmiTextView;
	private void initMovieLayout() {
		mVideoView = (MovieViewer) findViewById(R.id.movie_viewer);
		mMoiveController = (MovieController) findViewById(R.id.movie_controller);
		mMoiveController.setUseFastForward(true);

		mSmiTextView = (TextView) findViewById(R.id.movie_smi);
		ArrayList<ArrayList<SmiData>> smiArray = SmiParser.getInstance().parser(mContentPath);
		if (smiArray != null) {
			mMoiveController.setSmi(smiArray, mSmiTextView);
		}

		mVideoView.setMediaController(mMoiveController);
		mMoiveController.setMovieData(mMovieData);
        mMoiveController.setParentView(findViewById(R.id.movie_root_view));
		mMoiveController.setOnControlFunctionListener(new OnControlFunctionListener(){
			@Override
			public void onChangeMovie(boolean isNext) {
				changeMovie(isNext);
			}

			@Override
			public void onDeleteCurrentMovie() {
				AlertDialog.Builder aDialog = new AlertDialog.Builder(new ContextThemeWrapper(MoviePlayerActivity.this, android.R.style.Theme_Holo_Dialog));
				aDialog.setTitle("삭제");
				aDialog.setMessage("현재 보고 있는 동영상을 삭제하시겠습니까?\n(자막파일도 같이 삭제됩니다.)");
				aDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton("확인", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						stopMovie();
                        DBMgr.getInstance().bookmarkRemove(mContentPath);
						FileUtils.deleteFile(mContentPath);
						FileUtils.deleteFile(mContentPath.substring(0, mContentPath.lastIndexOf(".")) + ".smi");

						mFilePaths.remove(mMovieIndex);
						if (mMovieIndex >= mFilePaths.size()) {
							MoviePlayerActivity.this.finish();
						} else {
							mContentPath = mFilePaths.get(mMovieIndex);
							ArrayList<ArrayList<SmiData>> smiArray = SmiParser.getInstance().parser(mContentPath);
							if (smiArray != null) {
								mMoiveController.setSmi(smiArray, mSmiTextView);
							}
							startMovie(mContentPath);
						}
					}
				}).show();
			}
		});

		mVideoView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        if (mVideoView != null && mVideoView.getCurrentPosition() > 0 && !mVideoView.isPlaying()) {
		        	mVideoView.start();
		        	mVideoState = VIDEO_PLAYING;
		        }
			}
		});
    }
	
	private void initMovie() {
		mVideoState = VIDEO_STOP;
    	try {
			mVideoView.setVideoPath(mContentPath.replace("%20", " "));
			mMoiveController.setTitle(FileUtils.getName(mContentPath));
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
    }
	
	private void changeMovie(boolean isNext) {
		int index;
		if (isNext) {
			index = mMovieIndex + 1;
		} else {
			index = mMovieIndex - 1;
		}
		
		if (index < 0) {
			Toast.makeText(this, "첫번째 동영상입니다.\n이전 동영상으로 넘어갈 수 없습니다.",Toast.LENGTH_SHORT).show();
		} else if (index >= mFilePaths.size()){
			Toast.makeText(this, "마지막 동영상입니다.\n다음 동영상으로 넘어갈 수 없습니다.",Toast.LENGTH_SHORT).show();
		} else {
			if (mVideoView.getMediaPlayer() != null) {
            	mMovieData.mPageNum = mVideoView.getMediaPlayer().getCurrentPosition();
            }
            DBMgr.getInstance().bookmarkUpdate(mMovieData);
			mContentPath = mFilePaths.get(index);
			mMovieData = loadMovieData(mContentPath);
			mMovieIndex = index;
			ArrayList<ArrayList<SmiData>> smiArray = SmiParser.getInstance().parser(mContentPath);
			mMoiveController.setSmi(smiArray, mSmiTextView);
			stopMovie();
			startMovie(mContentPath);
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		mVideoView.requestLayout();
		if (mMovieData.mPageNum > 0) {
			mp.seekTo(mMovieData.mPageNum);
		}
		mVideoView.postDelayed(new Runnable(){
			@Override
			public void run() {
				mMoiveController.startSmi();
				mVideoView.show();
			}
		}, 10);
		if (mMovieData.mIsLeft != MODE_NULL) {
			if (mp.getVideoWidth() > mp.getVideoHeight()) {
				mMovieData.mIsLeft = MODE_LANDSCAPE;
			} else {
				mMovieData.mIsLeft = MODE_PORTRAIT;
			}
		}
		setOrientation();
	}
	
	private static int VIDEO_INIT = 1;
	private static int VIDEO_STOP = 2;
	private static int VIDEO_PAUSE = 3;
	private static int VIDEO_PLAYING = 4;
	private int mVideoState = VIDEO_INIT;
	private int mCurrentPosition = 0;
    @Override
    public void onPause() {
    	super.onPause();
    	if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
            	mCurrentPosition = mVideoView.getCurrentPosition();
            	mVideoView.pause();
            	mVideoState = VIDEO_PAUSE;
            }
            if (mVideoView.getMediaPlayer() != null) {
            	mMovieData.mPageNum = mVideoView.getMediaPlayer().getCurrentPosition();
            }
            DBMgr.getInstance().bookmarkUpdate(mMovieData);
    	}

        
    }

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
    public void onResume() {
    	super.onResume();
    	KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    	if (!keyguardManager.inKeyguardRestrictedInputMode()) {
        	if (mVideoState == VIDEO_INIT) {
				initMovie();
			}
    		mVideoView.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				playVideo();
    			}
    		}, 10);
    	} else {
    		mReceiverManager.setOnUserPresentListener(this, new ReceiverManager.OnUserPresentListener(){
				@Override
				public void onUserPresent() {
		        	if (mVideoState == VIDEO_INIT) {
                        initMovie();
					}
		    		mVideoView.postDelayed(new Runnable(){
		    			@Override
		    			public void run() {
		    				playVideo();
		    			}
		    		}, 10);
				}
			});
    	}

    	mReceiverManager.setOnBatteryChangeListener(this,new ReceiverManager.OnBatteryChangeListener(){
			@Override
			public void onReceiveBattery(int plugType, int level, int scale) {
				mMoiveController.mBatteryView.setBatteryInfo(plugType, level, scale);
			}
		});
    }
    
    private void stopMovie() {
    	if (mVideoView != null && mVideoView.isPlaying()) {
    		mVideoState = VIDEO_STOP;
			mVideoView.stopPlayback();
		}
    }
    private void startMovie(String path) {
    	try {
			mVideoView.setVideoPath(path.replace("%20", " "));
			mMoiveController.setTitle(FileUtils.getName(path));
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
		mVideoView.postDelayed(new Runnable(){
			@Override
			public void run() {
				playVideo();
			}
		}, 10);
    }
	@Override
	protected void onStop() {
		super.onStop();
	}

	private void playVideo() {
        try {
            Runnable r = new Runnable() {
                public void run() {
                	if (mVideoView != null) {
                		if (mVideoState == VIDEO_PAUSE) {
                			//mVideoView.seekTo(mCurrentPosition);
                			mVideoView.start();
                		} else if (mVideoState == VIDEO_STOP) {
                			mVideoView.start();
                		}
                        mVideoState = VIDEO_PLAYING;
                	} else {
                		return;
                	}
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
        	CLog.e(TAG,e);
            if (mVideoView != null) {
            	stopMovie();
            	mVideoView = null;
            }
        }
    }
	
	
	
	@Override
	protected void onDestroy() {
		stopMovie();
		super.onDestroy();
	}

	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		if (mediaPlayer != null) {
		    mediaPlayer.stop();
		    mediaPlayer.release();
		}
    	Intent result = new Intent();
    	setResult(RESULT_OK, result);
		Log.e(TAG,"KDS3393_TEST_delay onError = " + what);
        finish();
		return false;
	}
	
	public void onCompletion(MediaPlayer arg0) {
		mMovieData.mPageNum = 0;
    	Intent result = new Intent();
    	setResult(RESULT_OK, result);
		Log.e(TAG,"KDS3393_TEST_delay onCompletion");
        finish(); 
	}
}
