package com.kds3393.just.viewer.Text;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Browser.FileBrowserActivity;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Music.MusicPlayerPanelView;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.LocalBinder;
import com.kds3393.just.viewer.Text.TextSettingPanel.OnChangeColorListener;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnBatteryChangeListener;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.View.MoveScaleButton;
import com.kds3393.just.viewer.View.ResizeButton;
import com.kds3393.just.viewer.View.TimeTextView;
import com.kds3393.just.viewer.provider.DBItemData;

public class TextViewerActivity extends ParentActivity {
	private static final String TAG = "TextViewerActivity";
	
	private static final int MODE_SLIDE = 0;
	private static final int MODE_PAGING = 1;
	
	private String mContentPath;
	public ArrayList<String> mFilePaths;
	
	private TextViewer mTextView;
	
	private BatteryView mBatteryView;
	
	protected DBItemData mTextData;
	
	private boolean mIsVolumeBtnMove = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSize();
        mContentPath = getIntent().getStringExtra(FileBrowserActivity.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(FileBrowserActivity.EXTRA_BROWSER_PATH_ARRAY);
        
        if (TextUtils.isEmpty(mContentPath))
        	finish();
        
        mTextData = DBItemData.load(mContentPath, getContentResolver());
    	if (mTextData == null) {
    		int isSliding = SettingTextViewer.getTextSlidingAndPaging(this)?MODE_SLIDE:MODE_PAGING;
    		mTextData = new DBItemData(mContentPath,0,isSliding);
    		DBItemData.save(mTextData, getContentResolver());
    	}
    	
        setContentView(R.layout.act_textviewer);
        initMainView();
        mTextView.runInitTask();
    }
    
    private int mBottomMenuLayoutHeight;				//하단 layout height
    
    private int mSettingPanelTopMargin;			//text setting panel top margin
    private Size mSettingPanel;						//text setting panel Size
    
    private Size mResizeBtnSize;			//이동버튼을 resize하기 위한 버튼 size
    
    private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mBottomMenuLayoutHeight = 82;
			mSettingPanel = new Size(Size.DisplayWidth - 20,410);
			
			mResizeBtnSize = new Size(80,80);
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mBottomMenuLayoutHeight = 180;
			mSettingPanel = new Size(Size.DisplayWidth - 20,600);
			
			mResizeBtnSize = new Size(100,100);
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mBottomMenuLayoutHeight = (int)(Size.DisplayHeight * 0.08);
			mSettingPanel = new Size(Size.DisplayWidth - 20,(int)(Size.DisplayHeight * 0.33));
			
			mResizeBtnSize = new Size(80,80);
		}
		mSettingPanelTopMargin = Size.DisplayHeight - mSettingPanel.Height - mBottomMenuLayoutHeight - 10;
    }
    
	@Override
	protected void onPause() {
		mTextData.mIndex = mTextView.getFirstVisiblePosition();
		DBItemData.update(mTextData, getContentResolver());
		
		if (SettingTextViewer.getUsePageMoveBtn(this)) {
			SharedPrefHelper.setImageLeftBtnPoint(this, mPrevPageBtn.getLeft(),mPrevPageBtn.getTop());
			SharedPrefHelper.setImageRightBtnPoint(this, mNextPageBtn.getLeft(),mNextPageBtn.getTop());
			SharedPrefHelper.setImageLeftBtnSize(this, mPrevPageBtn.getWidth(),mPrevPageBtn.getHeight());
			SharedPrefHelper.setImageRightBtnSize(this, mNextPageBtn.getWidth(),mNextPageBtn.getHeight());
		}
		
		super.onPause();
	}
	
    @Override
	protected void onResume() {
    	mIsVolumeBtnMove = SettingActivity.getUseVolumeMoveBtn(this);
    	mReceiverManager.setOnBatteryChangeListener(this,new OnBatteryChangeListener(){
			@Override
			public void onReceiveBattery(int plugType, int level, int scale) {
				mBatteryView.setBatteryInfo(plugType, level, scale);
			}
		});
    	mTextView.setScrollSpeed(SettingTextViewer.getScrollSpeedValue(this));
		super.onResume();
	}


	@Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        DBItemData.OrganizeDB(getContentResolver());
    }
    
	@Override
	protected void onStop() {
		mMusicPanel.clear();
		unbindService(mConnection);
		super.onStop();
	}
	
    private void initMainView() {
   		createTextViewer();
		createMoveButton();
		createNavi();
    }
    
    private void createTextViewer() {
    	mTextView = (TextViewer) findViewById(R.id.txtviewer_viewer);
    	mTextView.setContentPath(mContentPath);
    	mTextView.setBackgroundColor(SettingTextViewer.sColors[SettingTextViewer.getTextColor(this)][0]);
    	mTextView.setTextFont(SettingTextViewer.getTextFont(this));
    	mTextView.setTextSize(SettingTextViewer.getTextSize(this));;
    	mTextView.setTextData(mTextData);
    	mTextView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setShowNaviBar();
			}
		});
    }
    
    private boolean mMoveButtonEditMode = false;;
    private void setShowNaviBar() {
		if (mMoveButtonEditMode)
			return;
		if (mNaviLayout.getVisibility() == View.GONE) {
			mNaviLayout.setVisibility(View.VISIBLE);
			if (mPrevPageBtn != null) {
				mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_SHOW);
				mNextPageBtn.setMoveMode(MoveScaleButton.MODE_SHOW);
			}
		} else {
			mNaviLayout.setVisibility(View.GONE);
			mTextSettingPanel.setVisibility(View.GONE);
			
			if (mPrevPageBtn != null) {
				mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
				mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
			}

		}
    }
	private TextView mTitleText;
	private RelativeLayout mNaviLayout;
	protected MusicPlayerPanelView mMusicPanel;
    private void createNavi() {
    	mNaviLayout = (RelativeLayout) findViewById(R.id.txt_navi);
		
    	mTitleText = (TextView) findViewById(R.id.txt_title);
    	mTitleText.setText(FileUtils.getFileName(mContentPath));
		mBatteryView = (BatteryView) findViewById(R.id.txt_battery);
		createNaviBottomMenu(mNaviLayout);
		mMusicPanel = (MusicPlayerPanelView) findViewById(R.id.txt_music_panel);
		createSettingPanel(mNaviLayout);
    }
    
    private RelativeLayout mMoveBtnLayout;
	private MoveScaleButton mPrevPageBtn;
	private MoveScaleButton mNextPageBtn;
	private ResizeButton mPrevResizeBtn;
	private ResizeButton mNextResizeBtn;
	private Button mEditModeFinish;
    private void createMoveButton() {
    	mMoveBtnLayout = (RelativeLayout) findViewById(R.id.txtviewer_move_btn_layout);
    	if (!SettingTextViewer.getUsePageMoveBtn(this)) {
    		mMoveBtnLayout.setVisibility(View.GONE);
			return;
		}
    	mMoveBtnLayout.setVisibility(View.VISIBLE);
    	mPrevPageBtn = (MoveScaleButton) findViewById(R.id.txtviewer_move_prev);
    	mNextPageBtn = (MoveScaleButton) findViewById(R.id.txtviewer_move_next);
    	mPrevResizeBtn = (ResizeButton) findViewById(R.id.txtviewer_move_prev_resize);
    	mNextResizeBtn = (ResizeButton) findViewById(R.id.txtviewer_move_next_resize);
    	
    	Size prevSize = SharedPrefHelper.getImageLeftBtnSize(this);
    	Point prevPoint = SharedPrefHelper.getImageLeftBtnPoint(this);
		
    	LayoutUtils.setRelativeLayoutParams(mPrevPageBtn, prevSize.Width, prevSize.Height, 
    			prevPoint.x,prevPoint.y, -1);
    	mPrevPageBtn.setText("이전 페이지");
		mPrevPageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mTextView.movePrev();
			}
		});
		
		Size nextSize = SharedPrefHelper.getImageRightBtnSize(this);
		Point nextPoint = SharedPrefHelper.getImageRightBtnPoint(this);
		
    	LayoutUtils.setRelativeLayoutParams(mNextPageBtn, 
    			nextSize.Width, nextSize.Height, 
    			nextPoint.x,nextPoint.y, -1);
    	mNextPageBtn.setText("다음 페이지");
		mNextPageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mTextView.moveNext();
			}
		});
		
		mPrevPageBtn.setBoundView(mNextPageBtn);
		mNextPageBtn.setBoundView(mPrevPageBtn);
		
		int x = (int) (prevPoint.x + prevSize.Width) - mResizeBtnSize.Width;
		int y = (int) (prevPoint.y + prevSize.Height) - mResizeBtnSize.Height;
		
		LayoutUtils.setRelativeLayoutParams(mPrevResizeBtn, 
				mResizeBtnSize.Width, mResizeBtnSize.Height, 
				x, y, -1);
		mPrevResizeBtn.setScaleButton(mPrevPageBtn);
		mPrevResizeBtn.setMaxScaleSize(Size.DisplayWidth,Size.DisplayHeight);
		mPrevResizeBtn.setBackgroundResource(R.drawable.h_resize);
		mPrevResizeBtn.setVisibility(View.INVISIBLE);
		
		x = (int) (nextPoint.x + nextSize.Width) - mResizeBtnSize.Width;
		y = (int) (nextPoint.y + nextSize.Height) - mResizeBtnSize.Height;
		
		LayoutUtils.setRelativeLayoutParams(mNextResizeBtn, 
				mResizeBtnSize.Width, mResizeBtnSize.Height, 
				x, y, -1);
		mNextResizeBtn.setScaleButton(mNextPageBtn);
		mNextResizeBtn.setMaxScaleSize(Size.DisplayWidth,Size.DisplayHeight);
		mNextResizeBtn.setVisibility(View.INVISIBLE);
		
		mPrevPageBtn.setFunctionView(null,mPrevResizeBtn);
		mNextPageBtn.setFunctionView(null,mNextResizeBtn);
		mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
		mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
			
		mEditModeFinish = (Button) findViewById(R.id.txtviewer_edit_close);
		mEditModeFinish.setVisibility(View.INVISIBLE);
		mEditModeFinish.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mMoveButtonEditMode = false;
				mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
				mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
				mEditModeFinish.setVisibility(View.INVISIBLE);
			}
		});
    }
    
	private Button mSlideShow;
	private boolean mIsAutoScroll = false;
	private static int AUTO_SCROLL_HANDLER = 0;
    private void createNaviBottomMenu(RelativeLayout main) {
		Button textSettingBtn = (Button) findViewById(R.id.navi_btm_setting);
		textSettingBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (mTextSettingPanel.getVisibility() == View.VISIBLE)
					mTextSettingPanel.setVisibility(View.GONE);
				else
					mTextSettingPanel.setVisibility(View.VISIBLE);
			}
		});
		
		mSlideShow = (Button) findViewById(R.id.navi_btm_slide);
		mSlideShow.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if (mIsAutoScroll) {
					mIsAutoScroll = false;
					mSlideShow.setBackgroundResource(R.drawable.h_3d_media_play);
					Toast.makeText(TextViewerActivity.this, "Auto Scroll을 종료합니다.",Toast.LENGTH_SHORT).show();
					mTextView.setOnTouchListener(null);
				} else {
					mIsAutoScroll = true;
					mSlideShow.setBackgroundResource(R.drawable.h_3d_media_stop);
					mSlideShowHandler.sendMessageDelayed(mSlideShowHandler.obtainMessage(AUTO_SCROLL_HANDLER), 30);
					Toast.makeText(TextViewerActivity.this, "Auto Scroll을 시작 합니다.\n",Toast.LENGTH_SHORT).show();
					setShowNaviBar();
					mTextView.setOnTouchListener(new OnTouchListener(){
						@Override
						public boolean onTouch(View view, MotionEvent event) {
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								setShowNaviBar();
							}
							return true;
						}
					});
				}
			}
		});
		
		Button editMode = (Button) findViewById(R.id.navi_btm_editmode);
		if (SettingTextViewer.getUsePageMoveBtn(this)) {
    		editMode.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					mMoveButtonEditMode = true;
					mNaviLayout.setVisibility(View.GONE);
					mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
					mNextPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
					mEditModeFinish.setVisibility(View.VISIBLE);
					
					mPrevResizeBtn.setViewPoint(mPrevPageBtn.getWidth(), mPrevPageBtn.getHeight(), mPrevPageBtn.getLeft(), mPrevPageBtn.getTop());
					mNextResizeBtn.setViewPoint(mNextPageBtn.getWidth(), mNextPageBtn.getHeight(), mNextPageBtn.getLeft(), mNextPageBtn.getTop());
				}
			});
		} else {
			editMode.setVisibility(View.GONE);
		}
    }
    
    private Handler mSlideShowHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (mIsAutoScroll) {
        		mTextView.smoothScrollBy(1, 20);
        		sendMessageDelayed(obtainMessage(AUTO_SCROLL_HANDLER), 32);
        	}
        }
    };
    
    private TextSettingPanel mTextSettingPanel;
    private void createSettingPanel(RelativeLayout main) {
    	mTextSettingPanel = TextSettingPanel.make(this, main, mSettingPanel.Width, mSettingPanel.Height, 10, mSettingPanelTopMargin);
    	mTextSettingPanel.setTextView(mTextView);
    	mTextSettingPanel.setOnChangeColorListener(new OnChangeColorListener(){
			@Override
			public void onChangeColor(int backColor) {
				mTextView.setBackgroundColor(backColor);
			}
		});
    	mTextSettingPanel.setVisibility(View.GONE);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (mIsVolumeBtnMove) {
    		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    			mTextView.movePrev();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
    			mTextView.moveNext();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_MENU){
    			setShowNaviBar();
    			return true;
    		}
    	}
		return super.onKeyDown(keyCode, event);
	}
    // ----------------------------------- Music Service -------------------------------------
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mMusicPanel.setMusicService(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
