package com.kds3393.just.viewer.Image;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.ResManager;
import com.common.utils.Size;
import com.common.utils.Utils;
import com.kds3393.just.dialog.ImageSlideShowTimeSetBuilder;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Browser.FileBrowserActivity;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Config.SettingImageViewer;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Image.ImageViewer.OnPageSelectedListener;
import com.kds3393.just.viewer.Movie.CenterLineDrawable;
import com.kds3393.just.viewer.Music.MusicPlayerPanelView;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.LocalBinder;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnBatteryChangeListener;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.View.MoveScaleButton;
import com.kds3393.just.viewer.View.ResizeButton;
import com.kds3393.just.viewer.provider.DBItemData;

public class ImageViewerActivity extends ParentActivity {
	private static final String TAG = "ImageViewerActivity";
	
	private String mContentPath;
	public ArrayList<String> mFilePaths;
	
	protected ProgressDialog mProgressDialog;
	
	private ImageViewer mImageViewer;
	private TextView mPageTextView;
	private BatteryView mBatteryView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentPath = getIntent().getStringExtra(FileBrowserActivity.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(FileBrowserActivity.EXTRA_BROWSER_PATH_ARRAY);
        
        if (TextUtils.isEmpty(mContentPath))
        	finish();
        setContentView(R.layout.act_imageviewer);
        createMainView();
        InitProgress();
        
        mImageViewer.runInitTask(mProgressDialog);
    }
    
	private boolean mIsVolumeBtnMove = true;
    @Override
	protected void onResume() {
    	mIsVolumeBtnMove = SettingActivity.getUseVolumeMoveBtn(this);
    	mReceiverManager.setOnBatteryChangeListener(this,new OnBatteryChangeListener(){
			@Override
			public void onReceiveBattery(int plugType, int level, int scale) {
				mBatteryView.setBatteryInfo(plugType, level, scale);
			}
		});
		super.onResume();
	}

    

	@Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        DBItemData.OrganizeDB(getContentResolver());
    }
    
    private void createMainView() {
		createViewer();
		createMoveButton();
		createSeekThumb();
		createNavi();
    }

    @Override
	protected void onPause() {
		int index = 0;
		if (mImageViewer.getImageData() != null) {
			if (SettingImageViewer.getIsPageRight(this)) {
				index = mImageViewer.getIndex();
			} else {
				index = mImageViewer.getPageCount() - 1 - mImageViewer.getIndex();
			}
			mImageViewer.getImageData().mIndex = index;
			mImageViewer.getImageData().mIsLeft = mImageViewer.getIsLeftCurrentPage();
			mImageViewer.getImageData().mZoomStandardHeight = KConfig.cStandardHeight;
			mImageViewer.getImageData().mZoomType = KConfig.cZoomLevel;
			DBItemData.update(mImageViewer.getImageData(), getContentResolver());
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		mMusicPanel.clear();
		unbindService(mConnection);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mThumbImageView.setImageBitmap(null);
		ImageDownloader.doDestroy();
		super.onDestroy();
	}
    
    private void InitProgress() {
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setCancelable(false);
    	mProgressDialog.setCanceledOnTouchOutside(false);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mProgressDialog.setMessage("Viewer 초기화 중");
    }
    
	private void createViewer() {
    	mImageViewer = (ImageViewer) findViewById(R.id.imageviewer);
    	mImageViewer.setZipPath(mContentPath);
    	mImageViewer.setContentFileList(mFilePaths);
    	mImageViewer.setViewSize(Size.DisplayWidth,Size.DisplayHeight);

		mImageViewer.setOnPageSelectedListener(new OnPageSelectedListener(){
			@Override
			public void onPageSelected(PageView CurrentPage, int index) {
				setPageText(index);
				if (mProgressBar != null) {
					mProgressBar.setProgress(index);
				}
			}

			@Override
			public void onBookSelected(String path) {
				mTitleText.setText(FileUtils.getFileName(path));
			}

			@Override
			public void onSingleTab() {
				setShowHideNaviBar();
			}
		});
    }
	
	private boolean mMoveButtonEditMode = false;;
	private void setShowHideNaviBar() {
		if (mMoveButtonEditMode)
			return;
		if (mNaviLayout.getVisibility() == View.GONE) {
			mNaviLayout.setVisibility(View.VISIBLE);
			if (mPrevPageBtn != null) {
				mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_SHOW);
				mNextPageBtn.setMoveMode(MoveScaleButton.MODE_SHOW);
			}
			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.setMax(mImageViewer.getPageCount() - 1);
		} else {
			setHideNaviBar();
		}
	}
	
	private void setHideNaviBar() {
		mNaviLayout.setVisibility(View.GONE);
		if (mPrevPageBtn != null) {
			mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
			mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
		}
		mProgressBar.setVisibility(View.INVISIBLE);
		Utils.hideKeyboard(mGoPageText);
	}
	
	private TextView mTitleText;
	private RelativeLayout mNaviLayout;
	protected MusicPlayerPanelView mMusicPanel;
    private void createNavi() {
    	mNaviLayout = (RelativeLayout) findViewById(R.id.layout_navi);
		mPageTextView = (TextView) findViewById(R.id.txt_navi_page);
		mTitleText = (TextView) findViewById(R.id.txt_navi_title);
		mBatteryView = (BatteryView) findViewById(R.id.battery);
		createNaviBottomMenu();
	    mMusicPanel = (MusicPlayerPanelView) findViewById(R.id.image_music_panel);
    }
    
	private MoveScaleButton mPrevPageBtn;
	private MoveScaleButton mNextPageBtn;
	private ResizeButton mPrevResizeBtn;
	private ResizeButton mNextResizeBtn;
	private Button mEditModeFinish;
    private void createMoveButton() {
    	if (!SettingImageViewer.getUsePageMoveBtn(this)) {
    		findViewById(R.id.layout_move_edit).setVisibility(View.GONE);
    		return;
		}
    	
    	Size prevSize = SharedPrefHelper.getImageLeftBtnSize(this);
    	Point prevPoint = SharedPrefHelper.getImageLeftBtnPoint(this);
    	mPrevPageBtn = (MoveScaleButton) findViewById(R.id.btn_left_page_move);
		LayoutUtils.setRelativeLayoutParams(mPrevPageBtn,prevSize.Width, prevSize.Height, prevPoint.x,prevPoint.y,-1);
		mPrevPageBtn.setText("이전 페이지");
		mPrevPageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				moveLeft();
			}
		});
		
		
		Size nextSize = SharedPrefHelper.getImageRightBtnSize(this);
		Point nextPoint = SharedPrefHelper.getImageRightBtnPoint(this);
		mNextPageBtn = (MoveScaleButton) findViewById(R.id.btn_right_page_move);
		LayoutUtils.setRelativeLayoutParams(mNextPageBtn,nextSize.Width, nextSize.Height, nextPoint.x,nextPoint.y,-1);
		mNextPageBtn.setText("다음 페이지");
		mNextPageBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				moveRight();
			}
		});
		
		mPrevPageBtn.setBoundView(mNextPageBtn);
		mNextPageBtn.setBoundView(mPrevPageBtn);
		int resizeBtnSize = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_resize_size);
		int x = (int) (prevPoint.x + prevSize.Width) - resizeBtnSize;
		int y = (int) (prevPoint.y + prevSize.Height) - resizeBtnSize;
		mPrevResizeBtn = (ResizeButton) findViewById(R.id.btn_left_resize);
		LayoutUtils.setRelativeLayoutParams(mPrevResizeBtn,resizeBtnSize, resizeBtnSize, x, y,-1);
		mPrevResizeBtn.setScaleButton(mPrevPageBtn);
		mPrevResizeBtn.setMaxScaleSize(Size.DisplayWidth,Size.DisplayHeight);
		
		x = (int) (nextPoint.x + nextSize.Width) - resizeBtnSize;
		y = (int) (nextPoint.y + nextSize.Height) - resizeBtnSize;
		mNextResizeBtn = (ResizeButton) findViewById(R.id.btn_right_resize);
		LayoutUtils.setRelativeLayoutParams(mNextResizeBtn,resizeBtnSize, resizeBtnSize, x, y,-1);
		mNextResizeBtn.setScaleButton(mNextPageBtn);
		mNextResizeBtn.setMaxScaleSize(Size.DisplayWidth,Size.DisplayHeight);
		
		mPrevPageBtn.setFunctionView(mImageViewer,mPrevResizeBtn);
		mNextPageBtn.setFunctionView(mImageViewer,mNextResizeBtn);
		mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
		mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
		
		mEditModeFinish = (Button) findViewById(R.id.btn_edit_confirm);
		mEditModeFinish.setText("설정 완료");
		mEditModeFinish.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mMoveButtonEditMode = false;
				mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
				mNextPageBtn.setMoveMode(MoveScaleButton.MODE_FUNCTION);
				mEditModeFinish.setVisibility(View.INVISIBLE);
				SharedPrefHelper.setImageLeftBtnPoint(ImageViewerActivity.this, mPrevPageBtn.getLeft(),mPrevPageBtn.getTop());
				SharedPrefHelper.setImageRightBtnPoint(ImageViewerActivity.this, mNextPageBtn.getLeft(),mNextPageBtn.getTop());
				SharedPrefHelper.setImageLeftBtnSize(ImageViewerActivity.this, mPrevPageBtn.getWidth(),mPrevPageBtn.getHeight());
				SharedPrefHelper.setImageRightBtnSize(ImageViewerActivity.this, mNextPageBtn.getWidth(),mNextPageBtn.getHeight());
			}
		});
    }
    
    private void setPageText(int index) {
		if (!SettingImageViewer.getIsPageRight(this)) {
			index = mImageViewer.getPageCount() - 1 - index;
		}
    	String str = (index + 1) + "/" + mImageViewer.getPageCount();
    	mPageTextView.setText(str);
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (mIsVolumeBtnMove) {
    		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    			moveLeft();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
    			moveRight();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_MENU){
    			setShowHideNaviBar();
    			return true;
    		}
    	}
		return super.onKeyDown(keyCode, event);
	}

    private void moveLeft() {
    	mImageViewer.moveLeft();
    }
    
    private void moveRight() {
    	mImageViewer.moveRight();
    }
    
	private EditText mGoPageText;
	private ImageView mSlideShow;
	private long mSlideDelayMillsTime = 0;
	private static int SLIDE_SHOW_HANDLER = 1;
	private ImageSlideShowTimeSetBuilder mSlideTimeDialog;
    private void createNaviBottomMenu() {
    	ImageView PageDirection = (ImageView) findViewById(R.id.page_direction);
		if (SharedPrefHelper.getImagePageType(ImageViewerActivity.this)) {
			PageDirection.setBackgroundResource(R.drawable.h_book_driection_right);
		} else {
			PageDirection.setBackgroundResource(R.drawable.h_book_driection_left);
		}
		PageDirection.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				boolean isRight = !SharedPrefHelper.getImagePageType(ImageViewerActivity.this);
				mImageViewer.debugLogViews();
				if (isRight) {
					view.setBackgroundResource(R.drawable.h_book_driection_right);
					Toast.makeText(ImageViewerActivity.this, "오른쪽으로 넘기도록 설정되었습니다.",Toast.LENGTH_SHORT).show();
				} else {
					view.setBackgroundResource(R.drawable.h_book_driection_left);
					Toast.makeText(ImageViewerActivity.this, "왼쪽으로 넘기도록 설정되었습니다.",Toast.LENGTH_SHORT).show();
				}
				mImageViewer.setDirection(isRight);
			}
		});
	    		
		mSlideShow = (ImageView) findViewById(R.id.page_slideshow);
		mSlideShow.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				if (mSlideDelayMillsTime > 0) {
					mSlideDelayMillsTime = 0;
					mSlideShow.setBackgroundResource(R.drawable.h_3d_media_play);
					mSlideDelayMillsTime = 0;
					Toast.makeText(ImageViewerActivity.this, "Slide show를 종료합니다.",Toast.LENGTH_SHORT).show();
				} else {
					if (mSlideTimeDialog == null) {
						mSlideTimeDialog = new ImageSlideShowTimeSetBuilder(ImageViewerActivity.this);
						mSlideTimeDialog.getBuilder().setPositiveButton(android.R.string.ok, new Dialog.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								dialog.dismiss();
								mSlideShow.setBackgroundResource(R.drawable.h_3d_media_stop);
								mSlideDelayMillsTime = mSlideTimeDialog.getTimeMills();
								mSlideShowHandler.sendMessageDelayed(mSlideShowHandler.obtainMessage(SLIDE_SHOW_HANDLER), mSlideDelayMillsTime);
								String text = "Slide show를 시작 합니다.\n" + (mSlideDelayMillsTime / 1000) + "초 마다 페이지를 넘깁니다.";
								Toast.makeText(ImageViewerActivity.this, text,Toast.LENGTH_SHORT).show();
							}
						}).setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								dialog.dismiss();
								mSlideDelayMillsTime = 0;
							}
						});
						mSlideTimeDialog.makeView(ImageViewerActivity.this);
					}
					mSlideTimeDialog.getDialog().show();
				}
			}
		});
    		
		if (SettingImageViewer.getUsePageMoveBtn(this)) {
    		ImageView editMode = (ImageView) findViewById(R.id.page_editmode);
    		editMode.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					mMoveButtonEditMode = true;
					mNaviLayout.setVisibility(View.GONE);
					mPrevPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
					mNextPageBtn.setMoveMode(MoveScaleButton.MODE_EDIT);
					mEditModeFinish.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.INVISIBLE);
					Utils.hideKeyboard(mGoPageText);
				}
			});
		}
	    	
		mGoPageText = (EditText) findViewById(R.id.page_gopage);
		mGoPageText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	String str = mGoPageText.getText().toString();
					if (TextUtils.isEmpty(str))
						return true;
					int index = Integer.parseInt(str) - 1;
					if (index >= 0 && index <= mImageViewer.getPageCount()) {
						mImageViewer.gotoPage(index);
						Toast.makeText(ImageViewerActivity.this, (index + 1) + " 페이지로 이동하였습니다.",Toast.LENGTH_SHORT).show();
						Utils.hideKeyboard(mGoPageText);
					} else {
						Toast.makeText(ImageViewerActivity.this, "페이지 범위가 아닙니다. (1~" + mImageViewer.getPageCount() + ")",Toast.LENGTH_SHORT).show();
					}
					mGoPageText.setText("");
                    return true;  
                 }
                 return false;
             }
        });
    }
    
    private SeekBar mProgressBar;
    private LinearLayout mThumbLayout;
    private ImageView mThumbImageView;
    private TextView mThumbPageTextView;
    private void createSeekThumb() {
		mProgressBar = (SeekBar) findViewById(R.id.page_seekbar);
        mProgressBar.setOnSeekBarChangeListener(mSeekListener);
        mProgressBar.setVisibility(View.INVISIBLE);
        setProgressColor(mProgressBar);
        
        mThumbLayout = (LinearLayout) findViewById(R.id.layout_thumb);
    	mThumbLayout.setVisibility(View.GONE);
    	mThumbLayout.setOrientation(LinearLayout.VERTICAL);
    	
    	mThumbLayout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mProgressBar.getProgress();
			}
		});
    	
        mThumbImageView = (ImageView) findViewById(R.id.img_thumb);
        mThumbPageTextView = (TextView) findViewById(R.id.txt_page);
    }
    
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        	mThumbLayout.setVisibility(View.VISIBLE);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        	if (!fromuser) {
                return;
            }
        	mThumbImageView.setImageBitmap(null);
        	int width = ResManager.getDimen(R.dimen.imageviewer_thumb_image_width);
        	int height = ResManager.getDimen(R.dimen.imageviewer_thumb_image_height);
        	ImageDownloader.downloadThumbnail(ImageViewerActivity.this, progress, mThumbImageView, true, width, height);
            if (!SettingImageViewer.getIsPageRight(ImageViewerActivity.this))
                progress = mImageViewer.getPageCount() - 1 - progress;
        	mThumbPageTextView.setText((progress + 1) + " / " + mImageViewer.getPageCount());
        	
        }

        public void onStopTrackingTouch(SeekBar bar) {
        	mImageViewer.gotoPage(bar.getProgress());
        	mThumbLayout.setVisibility(View.GONE);
        	setHideNaviBar();
        }
    };
    
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
    
    private Handler mSlideShowHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	mImageViewer.moveRight();
        	if (mSlideDelayMillsTime > 0)
        		sendMessageDelayed(obtainMessage(SLIDE_SHOW_HANDLER), mSlideDelayMillsTime);
        }
    };
    
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
