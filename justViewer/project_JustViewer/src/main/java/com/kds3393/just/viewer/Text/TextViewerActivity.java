package com.kds3393.just.viewer.Text;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.kds3393.just.viewer.Browser.BrowserView;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.Music.MusicPlayerPanelView;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.LocalBinder;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Text.TextSettingPanel.OnChangeColorListener;
import com.kds3393.just.viewer.Utils.ReceiverManager.OnBatteryChangeListener;
import com.kds3393.just.viewer.View.BatteryView;
import com.kds3393.just.viewer.View.MoveScaleButton;
import com.kds3393.just.viewer.View.PageControlView;
import com.kds3393.just.viewer.View.TimeTextView;
import com.kds3393.just.viewer.provider.DBItemData;
import com.kds3393.just.viewer.provider.DBMgr;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextViewerActivity extends ParentActivity implements OnClickListener {
	private static final String TAG = "TextViewerActivity";
	
	private static final int MODE_SLIDE = 0;
	private static final int MODE_PAGING = 1;
	
	private String mContentPath;
	public ArrayList<String> mFilePaths;

    @BindView(R.id.txtviewer_viewer) TextViewer txtviewer_viewer;
    @BindView(R.id.txt_layout_move_edit) PageControlView txt_layout_move_edit;
    @BindView(R.id.txt_navi) RelativeLayout txt_navi;
    @BindView(R.id.txt_title) TextView txt_title;
    @BindView(R.id.txt_battery) BatteryView txt_battery;
    @BindView(R.id.txt_time) TimeTextView txt_time;
    @BindView(R.id.navi_btm_layout) LinearLayout navi_btm_layout;
    @BindView(R.id.navi_btm_setting) Button navi_btm_setting;
    @BindView(R.id.navi_btm_slide) Button navi_btm_slide;
    @BindView(R.id.navi_btm_editmode) Button navi_btm_editmode;
    @BindView(R.id.navi_btm_hide_line) ImageView navi_btm_hide_line;
    @BindView(R.id.navi_text_setting_panel) TextSettingPanel navi_text_setting_panel;
    @BindView(R.id.layout_navi) RelativeLayout layout_navi;
    @BindView(R.id.txt_music_panel) MusicPlayerPanelView txt_music_panel;

	protected DBItemData mTextData;
	
	private boolean mIsVolumeBtnMove = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentPath = getIntent().getStringExtra(BrowserView.EXTRA_BROWSER_PATH);
        mFilePaths = getIntent().getStringArrayListExtra(BrowserView.EXTRA_BROWSER_PATH_ARRAY);

        setContentView(R.layout.act_textviewer);
        ButterKnife.bind(this);

        if (TextUtils.isEmpty(mContentPath)) {
            finish();
        }

        mTextData = DBMgr.getInstance().bookmarkLoad(mContentPath);
    	if (mTextData == null) {
    		int isSliding = SettingTextViewer.getTextSlidingAndPaging(this)?MODE_SLIDE:MODE_PAGING;
    		mTextData = new DBItemData(mContentPath,0,isSliding);
            DBMgr.getInstance().bookmarkInsert(mTextData);
    	}
        createTextViewer();
        createMoveButton();

        txt_title.setText(FileUtils.getFileName(mContentPath));
        createNaviBottomMenu(txt_navi);

        navi_text_setting_panel.setTextView(txtviewer_viewer);
        navi_text_setting_panel.setOnChangeColorListener(new OnChangeColorListener(){
            @Override
            public void onChangeColor(int backColor) {
                txtviewer_viewer.setBackgroundColor(backColor);
            }
        });
        navi_text_setting_panel.setVisibility(View.GONE);

        txtviewer_viewer.runInitTask();
    }

    @Override
    public void onBackPressed() {
        if (txtviewer_viewer.isHideMode()) {
            txtviewer_viewer.setHideEditMode(false);
        } else {
            super.onBackPressed();
        }

    }

    @Override
	protected void onPause() {
        if (SettingTextViewer.getUsePageMoveBtn(this)) {
            txt_layout_move_edit.setSavePref();
        }
		mTextData.mPageNum = txtviewer_viewer.getFirstVisiblePosition();
        DBMgr.getInstance().bookmarkUpdate(mTextData);

		super.onPause();
	}
	
    @Override
	protected void onResume() {
    	mIsVolumeBtnMove = SettingActivity.getUseVolumeMoveBtn(this);
    	mReceiverManager.setOnBatteryChangeListener(this,new OnBatteryChangeListener(){
			@Override
			public void onReceiveBattery(int plugType, int level, int scale) {
                txt_battery.setBatteryInfo(plugType, level, scale);
			}
		});
    	txtviewer_viewer.setScrollSpeed(SettingTextViewer.getScrollSpeedValue(this));
		super.onResume();
	}


	@Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        DBMgr.getInstance().bookmarkOrganizeDB();
    }
    
	@Override
	protected void onStop() {
        txt_music_panel.clear();
		unbindService(mConnection);
		super.onStop();
	}
    
    private void createTextViewer() {
    	txtviewer_viewer.setContentPath(mContentPath);
    	txtviewer_viewer.setBackgroundColor(SettingTextViewer.sColors[SettingTextViewer.getTextColor(this)][0]);
    	txtviewer_viewer.setTextFont(SettingTextViewer.getTextFont(this));
    	txtviewer_viewer.setTextSize(SettingTextViewer.getTextSize(this));;
    	txtviewer_viewer.setTextData(mTextData);
    	txtviewer_viewer.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mTextData.mZoomStandardHeight > 0 && position == 0) {
                    txtviewer_viewer.setHideEditMode(true);
                } else {
                    setShowNaviBar();
                }
			}
		});
        txtviewer_viewer.setActivity(this);
    }
    
    public boolean mMoveButtonEditMode = false;;
    public void setShowNaviBar() {
		if (mMoveButtonEditMode)
			return;
		if (txt_navi.getVisibility() == View.GONE) {
            txt_navi.setVisibility(View.VISIBLE);
			if (txt_layout_move_edit != null) {
                txt_layout_move_edit.setMode(MoveScaleButton.MODE_SHOW);
			}
		} else {
            txt_navi.setVisibility(View.GONE);
            navi_text_setting_panel.setVisibility(View.GONE);
			
			if (txt_layout_move_edit != null) {
                txt_layout_move_edit.setMode(MoveScaleButton.MODE_FUNCTION);
			}

		}
    }

    private void createMoveButton() {
        txt_layout_move_edit.setViewer(null);
        txt_layout_move_edit.setOnPageControlListener(new PageControlView.OnPageControlListener() {
			@Override
			public void onPrev() {
				txtviewer_viewer.movePrev();
			}

			@Override
			public void onNext() {
				txtviewer_viewer.moveNext();
			}
		});
    }

	private boolean mIsAutoScroll = false;
	private static int AUTO_SCROLL_HANDLER = 0;
    private void createNaviBottomMenu(RelativeLayout main) {
        navi_btm_setting.setOnClickListener(this);

        navi_btm_slide.setOnClickListener(this);
		
		if (SettingTextViewer.getUsePageMoveBtn(this)) {
            navi_btm_editmode.setOnClickListener(this);
		} else {
            navi_btm_editmode.setVisibility(View.GONE);
		}
        navi_btm_hide_line.setOnClickListener(this);
    }
    
    private Handler mSlideShowHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (mIsAutoScroll) {
        		txtviewer_viewer.smoothScrollBy(1, 20);
        		sendMessageDelayed(obtainMessage(AUTO_SCROLL_HANDLER), 32);
        	}
        }
    };


    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (mIsVolumeBtnMove) {
    		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    			txtviewer_viewer.movePrev();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
    			txtviewer_viewer.moveNext();
    			return true;
    		} else if (keyCode == KeyEvent.KEYCODE_MENU){
    			setShowNaviBar();
    			return true;
    		}
    	}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    public void onClick(View v) {
        if (v == navi_btm_setting) {
            if (navi_text_setting_panel.getVisibility() == View.VISIBLE) {
                navi_text_setting_panel.setVisibility(View.GONE);
            } else {
                navi_text_setting_panel.setVisibility(View.VISIBLE);
            }
        } else if (v == navi_btm_slide) {
            if (mIsAutoScroll) {
                mIsAutoScroll = false;
                navi_btm_slide.setBackgroundResource(R.drawable.h_3d_media_play);
                Toast.makeText(TextViewerActivity.this, "Auto Scroll을 종료합니다.",Toast.LENGTH_SHORT).show();
                txtviewer_viewer.setOnTouchListener(null);
            } else {
                mIsAutoScroll = true;
                navi_btm_slide.setBackgroundResource(R.drawable.h_3d_media_stop);
                mSlideShowHandler.sendMessageDelayed(mSlideShowHandler.obtainMessage(AUTO_SCROLL_HANDLER), 30);
                Toast.makeText(TextViewerActivity.this, "Auto Scroll을 시작 합니다.\n",Toast.LENGTH_SHORT).show();
                setShowNaviBar();
                txtviewer_viewer.setOnTouchListener(new OnTouchListener(){
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            setShowNaviBar();
                        }
                        return true;
                    }
                });
            }
        } else if (v == navi_btm_editmode) {
            txt_layout_move_edit.setMoveButtonEditMode(true);
            txt_navi.setVisibility(View.GONE);
        } else if (v == navi_btm_hide_line) {
            boolean isHideMode = !txtviewer_viewer.isHideMode();
            if (isHideMode) {
                txt_navi.setVisibility(View.VISIBLE);
                setShowNaviBar();
            }
            txtviewer_viewer.setHideEditMode(isHideMode);
        }
    }

    // ----------------------------------- Music Service -------------------------------------
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            txt_music_panel.setMusicService(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
