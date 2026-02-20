package com.kds3393.just.viewer.Movie;

import java.util.ArrayList;

import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.Browser.FileBrowserActivity;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Movie.MovieController.OnControlFunctionListener;
import com.kds3393.just.viewer.Movie.SmiParser.SmiData;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnBatteryChangeListener;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnUserPresentListener;
import com.kds3393.just.viewer.provider.DBItemData;
import com.common.utils.FileUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.common.utils.debug.CLog;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoviePlayerActivity extends ParentActivity implements OnPreparedListener, OnErrorListener, OnCompletionListener {
	private static final String TAG = "MoviePlayerActivity";
	
	public static final int MODE_PORTRAIT = 0;
	public static final int MODE_LANDSCAPE = 1;
	
	private String mContentPath;
	private ArrayList<String> mFilePaths;
	private int mMovieIndex;
	
	private MovieViewer mVideoView;
	private MovieController mMoiveController;
	
	private DBItemData mMovieData;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSize();
        mContentPath = getIntent().getStringExtra(FileBrowserActivity.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(FileBrowserActivity.EXTRA_BROWSER_PATH_ARRAY);
        mMovieIndex = mFilePaths.indexOf(mContentPath);
        if (TextUtils.isEmpty(mContentPath))
        	finish();
    	
        mMovieData = loadMovieData(mContentPath);
        setContentView(createMainView());
    }
	
	private DBItemData loadMovieData(String path) {
		DBItemData data = DBItemData.load(mContentPath, getContentResolver());
    	if (data == null) {
    		int orientation = SettingActivity.getMovieOrientation(this)?MODE_LANDSCAPE:MODE_PORTRAIT;
    		data = new DBItemData(mContentPath,0,orientation);
    		DBItemData.save(data, getContentResolver());
    	}
    	return data;
	}
	
	private float mSmiTextSize;
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mSmiTextSize = 32;
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mSmiTextSize = 22;
		} else {
			mSmiTextSize = (float) (32 / ((0.5 * Size.Density) + 0.5));
			
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mMoiveController.onChangeOrientation(newConfig);
		super.onConfigurationChanged(newConfig);
	}



	private RelativeLayout createMainView() {
    	RelativeLayout main = new RelativeLayout(this);
    		createMovieViewer(main);
		return main;
    }
	
	private TextView mSmiTextView;
	private void createMovieViewer(RelativeLayout main) {
		mVideoView = new MovieViewer(this);
		main.addView(mVideoView);
		LayoutUtils.setRelativeRule(mVideoView, RelativeLayout.CENTER_IN_PARENT);
		
		mMoiveController = new MovieController(this,true);
		
		mSmiTextView = ViewMaker.TextViewMaker(this, main, "", LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0, 0, 0, 100);
		LayoutUtils.setRelativeRule(mSmiTextView, RelativeLayout.ALIGN_PARENT_BOTTOM);
		mSmiTextView.setGravity(Gravity.CENTER);
		mSmiTextView.setTextSize(mSmiTextSize);
		mSmiTextView.setTextColor(Color.WHITE);
		mSmiTextView.setShadowLayer(1.0f, 1.0f, 1.0f, Color.BLACK);
		ArrayList<ArrayList<SmiData>> smiArray = SmiParser.getInstance().parser(mContentPath);
		if (smiArray != null) {
			mMoiveController.setSmi(smiArray, mSmiTextView);
		}
		
		main.addView(mMoiveController);
		mMoiveController.setParentView(main);
		mVideoView.setMediaController(mMoiveController);
		mMoiveController.setMovieData(mMovieData);
		mMoiveController.initFloatingWindowLayout();
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
						DBItemData.removeItem(mContentPath,getContentResolver());
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
	
	private void init() {
		if (mVideoView == null) {
    		mVideoView = new MovieViewer(this);
    	} 
		
		mVideoState = VIDEO_STOP;

		mVideoView.setOnPreparedListener(this);
    	mVideoView.setOnErrorListener(this);
    	mVideoView.setOnCompletionListener(this);

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
            	mMovieData.mIndex = mVideoView.getMediaPlayer().getCurrentPosition();
            }
			DBItemData.update(mMovieData, getContentResolver());
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
		if (mMovieData.mIndex > 0) {
			mp.seekTo(mMovieData.mIndex);
		}
		mVideoView.postDelayed(new Runnable(){
			@Override
			public void run() {
				mMoiveController.startSmi();
				mVideoView.show();
			}
		}, 10);
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
            	mMovieData.mIndex = mVideoView.getMediaPlayer().getCurrentPosition();
            }
            DBItemData.update(mMovieData, getContentResolver());
    	}
        
        
    }
    
    @Override
    public void onResume() {
    	super.onResume(); 
    	KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    	if (!keyguardManager.inKeyguardRestrictedInputMode()) {
        	if (mVideoState == VIDEO_INIT)
        		init();
    		mVideoView.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				playVideo();
    			}
    		}, 10);
    	} else {
    		mReceiverManager.setOnUserPresentListener(this, new OnUserPresentListener(){
				@Override
				public void onUserPresent() {
		        	if (mVideoState == VIDEO_INIT)
		        		init();
		    		mVideoView.postDelayed(new Runnable(){
		    			@Override
		    			public void run() {
		    				playVideo();
		    			}
		    		}, 10);
				}
			});
    	}
    	
    	mReceiverManager.setOnBatteryChangeListener(this,new OnBatteryChangeListener(){
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
        finish();
		return false;
	}
	
	public void onCompletion(MediaPlayer arg0) {
		mMovieData.mIndex = 0;
    	Intent result = new Intent();
    	setResult(RESULT_OK, result);
        finish(); 
	}
}
