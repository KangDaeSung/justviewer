package com.kds3393.just.viewer.Music;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.debug.CLog;
import com.kds3393.just.dialog.Mp3SleepTimerBuilder;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Activity.ActJustViewer;
import com.kds3393.just.viewer.Config.SettingActivity;

public class MusicService extends Service {
	private static final String TAG = "MusicService";
	
	public static final String EXTRA_MUSIC_FILE_LIST = "just_music_file_list";
	public static final String EXTRA_MUSIC_FOLDER_PATH = "just_music_folder_path";
		
	private ArrayList<String> mOriMP3PathList = new ArrayList<String>();
	private ArrayList<String> mMP3PathList = new ArrayList<String>();
	private String mFolderPath;
	private int mIndex;
	MediaPlayer mMediaPlayer;
	
	private HashMap<View,OnMediaPlayerListener> mOnMediaPlayerListener = new HashMap<View,OnMediaPlayerListener>();
	
	public void setOnMediaPlayerListener(View view, OnMediaPlayerListener listener) {
		mOnMediaPlayerListener.put(view, listener);
	}
	
	public interface OnMediaPlayerListener {
		public void onStartCommanded(ArrayList<String> array);
		public void onPrepared(MediaPlayer mediaplsyer, Mp3Id3Parser data);
		public void onCompletion(MediaPlayer mediaplsyer);
		public void onPlay(MediaPlayer mediaplsyer, String playPath);
		public void onPause(MediaPlayer mediaplsyer, String playPath);
		public void onDeleteMusicFile(String filePath);
		public void onSleepTimer(boolean isRun,String time);
	}
	
	
	private NotificationManager mNM;
	private int mNotificationID = 100;
	public static final String MUSIC_NOTIFICATION_CLICK = "music_notifaction_click";
	private void showNotification(String title, boolean isPlay) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        // tickerText를 null로 하여 아이콘만 갱신되게 한다.
        Notification notification = new Notification(R.drawable.icon, null, System.currentTimeMillis());
        // 해당 notification을 클릭했을 실행할 PendingIntent 생성
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActJustViewer.class), 0);

        PendingIntent prevIntent = PendingIntent.getBroadcast(this, R.id.prev_btn, new Intent(MUSIC_NOTIFICATION_CLICK).putExtra("ID", R.id.prev_btn), 0);
        PendingIntent playIntent = PendingIntent.getBroadcast(this, R.id.play_pause, new Intent(MUSIC_NOTIFICATION_CLICK).putExtra("ID", R.id.play_pause), 0);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, R.id.next_btn, new Intent(MUSIC_NOTIFICATION_CLICK).putExtra("ID", R.id.next_btn), 0);
        
        RemoteViews remoteView = new RemoteViews(getPackageName(),R.layout.noti_layout);
        notification.contentView = remoteView;
        
        remoteView.setOnClickPendingIntent(R.id.app_icon, contentIntent);
        remoteView.setOnClickPendingIntent(R.id.prev_btn, prevIntent);
        remoteView.setOnClickPendingIntent(R.id.play_pause, playIntent);
        remoteView.setOnClickPendingIntent(R.id.next_btn, nextIntent);
        if (title != null)
        	remoteView.setTextViewText(R.id.music_titls, title);
        if (isPlay) {
        	remoteView.setImageViewResource(R.id.play_pause, R.drawable.h_media_pause);
        } else {
        	remoteView.setImageViewResource(R.id.play_pause, R.drawable.h_media_play);
        }
        remoteView.setImageViewResource(R.id.prev_btn, R.drawable.h_media_prev);
        remoteView.setImageViewResource(R.id.next_btn, R.drawable.h_media_next);
        // notification 송신
        mNM.notify(mNotificationID, notification);
    }
	
	private BroadcastReceiver mRecevier = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MUSIC_NOTIFICATION_CLICK)) {
				if (intent.getIntExtra("ID", 0) == R.id.prev_btn) {
					movePrev();
				} else if (intent.getIntExtra("ID", 0) == R.id.play_pause) {
					if (mMediaPlayer.isPlaying()) {
						pause();
					} else {
						play();
					}
					showNotification(null,mMediaPlayer.isPlaying());
				} else if (intent.getIntExtra("ID", 0) == R.id.next_btn) {
					moveNext(false);
				}
			}
		}
	};
	
	private Mp3Id3Parser mCurrentMusicMeta;
	private boolean mIsErrorCompletion = false; //error -38로 인해 음악이 중도 끝난 이후 onCompletion이 발생하였을 경우 true
	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		mMediaPlayer = new MediaPlayer();
		//곡 재생이 완료하면 서비스를 종료시킨다
		mMediaPlayer.setOnPreparedListener(new OnPreparedListener(){
			@Override
			public void onPrepared(MediaPlayer mediaplsyer) {
				if (mMP3PathList.size() <= 0)
					return;
				mCurrentMusicMeta = Mp3Id3Parser.mp3HeaderParser(mMP3PathList.get(mIndex));
				showNotification(mCurrentMusicMeta.mTitle,true);
				if (mOnMediaPlayerListener.size() > 0) {
					Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
					while (iterator.hasNext()) {
						View view = iterator.next();
						OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
						if (listener != null)
							listener.onPrepared(mediaplsyer, mCurrentMusicMeta);
					}
				}
			}
		});
		
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mediaplsyer) {
				if (mIsErrorCompletion) {
					mMediaPlayer.reset();
					playMusic(mIndex,true);
					mIsErrorCompletion = false;
				} else {
					moveNext(true);
				}
			}
		});
		
		mMediaPlayer.setOnErrorListener(new OnErrorListener(){
			@Override
			public boolean onError(MediaPlayer mediaplsyer, int arg1, int arg2) {
				if (arg1 == -38) {
					mIsErrorCompletion = true;
				}
				CLog.e(TAG, "KDS3393_mp3Path onError arg1 = " + arg1 + " arg2 = " + arg2);
				return false;
			}
		});

		TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telMgr.listen(new PhoneStateListener() {
			private boolean mIsCallPause = false;
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					case TelephonyManager.CALL_STATE_IDLE:
						if (mIsCallPause) {
							mIsCallPause = false;
							play();
						}
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						if (mMediaPlayer.isPlaying()) {
							mIsCallPause = true;
							pause();
						}
						break;
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MUSIC_NOTIFICATION_CLICK);
		registerReceiver(mRecevier, intentFilter);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String folderPath = intent.getStringExtra(EXTRA_MUSIC_FOLDER_PATH);
			if (mMediaPlayer != null && mFolderPath != null && mFolderPath.equalsIgnoreCase(folderPath)) {
				if (isPause()) {
					play();
					return START_STICKY;
				} else if (mMediaPlayer.isPlaying()){
					pause();
					return START_STICKY;
				}
			}
			mFolderPath = folderPath;
			
			String beforePath = "";
			if (mMP3PathList.size() == 1) {
				beforePath = mMP3PathList.get(0);
			}
			mMP3PathList = intent.getStringArrayListExtra(EXTRA_MUSIC_FILE_LIST);
			if (mMP3PathList.size() == 1 && beforePath.equalsIgnoreCase(mMP3PathList.get(0))) { //동일한 파일을 연속으로 누를 경우 무시
				return START_STICKY;
			}
		}
		
		if (mMP3PathList == null || mMP3PathList.size() <= 0)
			return START_STICKY;
		
		mOriMP3PathList.clear();
		mOriMP3PathList.addAll(mMP3PathList);
		if (SettingActivity.getMusicListShuffle(this)) {
			shuffleArray(mMP3PathList,false);
		}
		mMediaPlayer.reset();
		
		if (mOnMediaPlayerListener.size() > 0) {
			Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
			while (iterator.hasNext()) {
				View view = iterator.next();
				OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
				if (listener != null)
					listener.onStartCommanded(mMP3PathList);
			}
		}
		
		playMusic(0,true);
		return START_STICKY;
	}

	private void shuffleArray(ArrayList<String> array, boolean isFirst) {
		Random rnd = new Random();
		String curPath = null;
		if (isFirst) {
			curPath = array.get(mIndex);
		}
		
		for (int i = array.size() - 1; i >= 0; i--) {
		    int index = rnd.nextInt(i + 1);
		    
		    String a = array.get(index);
		    array.set(index, array.get(i));
		    array.set(i,a);
		}
		
		if (isFirst) {
			int index = array.indexOf(curPath);
			array.set(index, array.get(0));
			array.set(0, curPath);
			CLog.e(TAG, " curPath = " + curPath);
		}
		mIndex = 0;
	}
	
	public boolean mIsMusicPlaying = true;
	private void playMusic(int index, boolean isPlay) {
		mIsMusicPlaying = isPlay;
		mIndex = index;
		
		if (mMP3PathList.size() <= index) //때때로 mMp3PathList의 size가 0인경우 있음
			return;
		
		File mp3file = new File(mMP3PathList.get(index));
		if(mp3file.exists()){
			new Thread(mRun).start();
		}
	}
	
	Runnable mRun = new Runnable() {
		public void run() {
			try{
				FileInputStream fs = new FileInputStream(mMP3PathList.get(mIndex));
				FileDescriptor fd = fs.getFD();
				mMediaPlayer.setDataSource(fd);
				mMediaPlayer.prepare();
				if (mIsMusicPlaying)
					mMediaPlayer.start();
			}catch(Exception e){
				CLog.e(TAG, e);
			}
		}
	};

	@Override
	public void onDestroy() {
		if(mMediaPlayer!= null && mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer=null;
		}
		unregisterReceiver(mRecevier);
		super.onDestroy();
	}
	
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
    public String getPlayPath() {
    	return mFolderPath;
    }
    
    public boolean isFolderPlaying(String path) {
    	if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mFolderPath.equalsIgnoreCase(path)) {
   			return true;
    	}
    	return false;
    }
    
    public int getCurrentPosition() {
    	return mMediaPlayer.getCurrentPosition();
    }
    
    public int getDuration() {
    	return mMediaPlayer.getDuration();
    }
    
    public Mp3Id3Parser getMusicMetaData() {
    	return mCurrentMusicMeta;
    }

    public MediaPlayer getMediaPlayer() {
    	return mMediaPlayer;
    }
    
    public ArrayList<String> getMusicList() {
    	return mMP3PathList;
    }
    
    public int getPlayIndex() {
    	if (mMP3PathList.size() > 0)
    		return mIndex;
    	return -1;
    }
    
    private Mp3SleepTimerBuilder mPageSelectorDialog;
    private CountDownTimer mSleepTimer;
    public void showSleepTimerDialog(Context context ) {
    	if (mPageSelectorDialog == null || context != mPageSelectorDialog.getContext()) {
			mPageSelectorDialog = new Mp3SleepTimerBuilder(context);
			mPageSelectorDialog.getBuilder().setPositiveButton(android.R.string.ok, new Dialog.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
					long time = mPageSelectorDialog.getSleepTime();
					String timeString = mPageSelectorDialog.getStringSleepTime();
					if (time <= 0)
						return;
					if (mSleepTimer != null)
						mSleepTimer.cancel();
					mSleepTimer = new CountDownTimer(time, 1000) {
						@Override
						public void onFinish() {
							MusicService.this.stop();
							if (mPageSelectorDialog != null && mPageSelectorDialog.getDialog().isShowing())
								mPageSelectorDialog.getDialog().dismiss();
							mSleepTimer = null;
						}
						@Override
						public void onTick(long millisUntilFinished) {
							if (mPageSelectorDialog != null && mPageSelectorDialog.getDialog().isShowing()) {
								mPageSelectorDialog.setCurrentTime(millisUntilFinished);
							}
						}
					};
					
					mSleepTimer.start();
					if (mOnMediaPlayerListener.size() > 0) {
						Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
						while (iterator.hasNext()) {
							View view = iterator.next();
							OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
							if (listener != null)
								listener.onSleepTimer(true,timeString);
						}
					}
				}
			}).setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			}).setNeutralButton("Timer Cancel", new Dialog.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
					cancelSleepTimer();
				}
			});;
			
			mPageSelectorDialog.makeView(context);
    	}
    	if (!mPageSelectorDialog.getDialog().isShowing()) {
    		mPageSelectorDialog.getDialog().show();
    		if (mSleepTimer == null)
    			mPageSelectorDialog.setCurrentTime(0);
    	}
    }
    
    private void cancelSleepTimer() {
    	if (mSleepTimer != null) {
			mSleepTimer.cancel();
			mSleepTimer = null;
			if (mOnMediaPlayerListener.size() > 0) {
				Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
				while (iterator.hasNext()) {
					View view = iterator.next();
					OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
					if (listener != null)
						listener.onSleepTimer(false,"");
				}
			}
		}
    }
    public boolean isRunSleepTimer() {
    	return mSleepTimer != null;
    }
    public void setShuffle(boolean isShuffle) {
    	if (isShuffle) {
    		shuffleArray(mMP3PathList,mMediaPlayer.isPlaying());
    	} else {
    		String curPath = mMP3PathList.get(mIndex);
    		mMP3PathList.clear();
    		mMP3PathList.addAll(mOriMP3PathList);
    		mIndex = mMP3PathList.indexOf(curPath);
    	}
    }
    public void deleteCurrentMp3() {
    	String deletePath = mMP3PathList.get(mIndex);
    	moveNext(false);
    	FileUtils.deleteFile(deletePath);
		mMP3PathList.remove(deletePath);
		mOriMP3PathList.remove(deletePath);
		mIndex--;
		
		if (mOnMediaPlayerListener.size() > 0) {
			Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
			while (iterator.hasNext()) {
				View view = iterator.next();
				OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
				if (listener != null)
					listener.onDeleteMusicFile(deletePath);
			}
		}
    }
    
    public void moveIndex(int index) {
    	if (mIndex != index) {
    		boolean isPlaying = mMediaPlayer.isPlaying();
    		mMediaPlayer.reset();
    		playMusic(index,isPlaying);
    	}
    }
    
    public void movePrev() {
    	if (mIndex > 0) {
    		boolean isPlaying = mMediaPlayer.isPlaying();
			mMediaPlayer.reset();
			playMusic(mIndex - 1,isPlaying);
		} else {
			Toast.makeText(MusicService.this, "처음입니다.", Toast.LENGTH_LONG).show();
		}
    	
    }
    
    public void moveNext(boolean isForcePlay) {
		if (mMP3PathList.size() > mIndex + 1) {
			boolean isPlaying = true;
			if (!isForcePlay)
				isPlaying = mMediaPlayer.isPlaying();
			
			mMediaPlayer.reset();
			playMusic(mIndex + 1,isPlaying);
		} else {
			stop();
		}
    }
    
    public boolean isPause() {
    	if (mMediaPlayer == null || mMediaPlayer.isPlaying())
    		return false;
    	if (mMP3PathList.size() > 0)
    		return true;
    	else
    		return false;
    }
    
    public void pause() {
    	if (mMediaPlayer.isPlaying()) {
    		mMediaPlayer.pause();
			if (mFolderPath != null && mOnMediaPlayerListener.size() > 0) {
				Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
				while (iterator.hasNext()) {
					View view = iterator.next();
					OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
					if (listener != null)
						listener.onPause(mMediaPlayer, mFolderPath);
				}
			}
    	}
    }
    
    public void seekTo(int pos) {
    	mMediaPlayer.seekTo(pos);
    }
    
    public void play() {
    	if (mMediaPlayer.getDuration() > 0) {
    		mMediaPlayer.start();
			if (mFolderPath != null && mOnMediaPlayerListener.size() > 0) {
				Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
				while (iterator.hasNext()) {
					View view = iterator.next();
					OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
					if (listener != null)
						listener.onPlay(mMediaPlayer, mFolderPath);
				}
			}
    	}
    }
    
    public void stop() {
		mMediaPlayer.reset();
		mMP3PathList.clear();
		mFolderPath = null;
		stopSelf();	//서비스 종료
		cancelSleepTimer();
		mNM.cancel(mNotificationID);
		if (mOnMediaPlayerListener.size() > 0) {
			Iterator<View> iterator = mOnMediaPlayerListener.keySet().iterator();
			while (iterator.hasNext()) {
				View view = iterator.next();
				OnMediaPlayerListener listener = mOnMediaPlayerListener.get(view);
				if (listener != null)
					listener.onCompletion(mMediaPlayer);
			}
		}
    }
}