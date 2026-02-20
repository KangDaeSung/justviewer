package com.kds3393.just.viewer.Image;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.Size;
import com.common.utils.Utils;
import com.kds3393.just.dialog.ImageSlideShowTimeSetBuilder;
import com.kds3393.just.viewer.Browser.BrowserView;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Config.SettingImageViewer;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Image.ImageViewer.OnPageSelectedListener;
import com.kds3393.just.viewer.Music.MusicPlayerPanelView;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.LocalBinder;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnBatteryChangeListener;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.View.MoveScaleButton;
import com.kds3393.just.viewer.View.PageControlView;
import com.kds3393.just.viewer.provider.DBMgr;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;

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
        mContentPath = getIntent().getStringExtra(BrowserView.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY);

        if (TextUtils.isEmpty(mContentPath)) {
            finish();
        }

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
        DBMgr.getInstance().bookmarkOrganizeDB();
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
			mImageViewer.getImageData().mPageNum = index;
			mImageViewer.getImageData().mIsLeft = mImageViewer.getIsLeftCurrentPage();
			mImageViewer.getImageData().mZoomStandardHeight = KConfig.cStandardHeight;
			mImageViewer.getImageData().mZoomType = KConfig.cZoomLevel;
            DBMgr.getInstance().bookmarkUpdate(mImageViewer.getImageData());
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
		ImageDownloader.doDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
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
					mProgressBar.setProgress(index + 1);
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

            @Override
            public void onLoaded(int pageSize) {
                if (mProgressBar != null) {
                    mProgressBar.setMin(1);
                    mProgressBar.setMax(pageSize);
                }
            }
        });
    }

	private void setShowHideNaviBar() {
		if (mPageControlView.isMoveButtonEditMode())
			return;
		if (mNaviLayout.getVisibility() == View.GONE) {
			mNaviLayout.setVisibility(View.VISIBLE);
			if (mPageControlView != null) {
                mPageControlView.setMode(MoveScaleButton.MODE_SHOW);
			}
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			setHideNaviBar();
		}
	}

	private void setHideNaviBar() {
		mNaviLayout.setVisibility(View.GONE);
		if (mPageControlView != null) {
            mPageControlView.setMode(MoveScaleButton.MODE_FUNCTION);
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

	private PageControlView mPageControlView;
    private void createMoveButton() {
        mPageControlView = (PageControlView) findViewById(R.id.layout_move_edit);
        mPageControlView.setViewer(mImageViewer);
        mPageControlView.setOnPageControlListener(new PageControlView.OnPageControlListener() {
            @Override
            public void onPrev() {
                moveLeft();
            }

            @Override
            public void onNext() {
                moveRight();
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
                    mPageControlView.setMoveButtonEditMode(true);
					mNaviLayout.setVisibility(View.GONE);
					mProgressBar.setVisibility(View.INVISIBLE);
					Utils.hideKeyboard(mGoPageText);
				}
			});
		} else {
            findViewById(R.id.page_editmode).setVisibility(View.GONE);
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

    private DiscreteSeekBar mProgressBar;
    private void createSeekThumb() {
		mProgressBar = (DiscreteSeekBar) findViewById(R.id.page_seekbar);
        mProgressBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                mImageViewer.gotoPage(seekBar.getProgress() - 1);
                setHideNaviBar();
            }
        });
    }

    private Handler mSlideShowHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	mImageViewer.moveRight();
        	if (mSlideDelayMillsTime > 0) {
                sendMessageDelayed(obtainMessage(SLIDE_SHOW_HANDLER), mSlideDelayMillsTime);
            }
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
