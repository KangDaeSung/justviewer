package com.kds3393.just.viewer.Music;

import java.util.ArrayList;

import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation.OnFlingListener;
import com.kds3393.just.viewer.View.CheckButton;
import com.common.utils.FileUtils;
import com.common.utils.ImageUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.common.utils.debug.CLog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MusicListView extends RelativeLayout {
	private static final String TAG = "MusicListView";
	
	private Button mOpenButton;
	private boolean mIsOpen = false;
	private OnFlingListener mOnFlingListener;
	private MusicService mService;
	private ListView mListView;
    public static MusicListView make(Context context,ViewGroup parent) {
    	MusicListView view = new MusicListView(context);
    	parent.addView(view);
 
    	LayoutUtils.setLayoutParams(parent, view, 
    			view.mMainSize.Width, view.mMainSize.Height, 
    			view.mMainPoint.x, view.mMainPoint.y, 
    			-view.mMainSize.Width, 0);
    	view.setVisibility(View.GONE);
    	return view;
    }
	
    public MusicListView(Context context) {
		super(context);
		initSize();
		createView();
		mOnFlingListener = new OnFlingListener(){
			@Override
			public void endFling(View view) {
				MusicListView.this.setVisibility(View.INVISIBLE);
			}
		};
	}
	
    public MusicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSize();
		createView();
		mOnFlingListener = new OnFlingListener(){
			@Override
			public void endFling(View view) {
				MusicListView.this.setVisibility(View.INVISIBLE);
			}
		};
	}
    
	public void setMusicService(MusicService s) {
		mService = s;
	}
	public Size mMainSize;
	public Point mMainPoint;				//main Panel 좌표
	
	public Size mShowButtonSize;
	public Size mMusicListViewSize;			// music list view의 size
	private int mItemSize; 					//리스트 item Height
	private Size mTitleTextViewSize;		//mp3 name이 들어가는 TextView size
	private Size mStatusImageSize;		//play중인 음악의 상태 이미지 size
	
	private float TextSize;					//mp3 파일 이름 text의 size
	
	private int mMoveDistances;						//Open Animation시 이동 거리
	
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mMainSize = new Size(800,805);
			mShowButtonSize = new Size(69,174);
			mMusicListViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 60,mMainSize.Height - 20);
			mMainPoint = new Point(-(mMusicListViewSize.Width + 20),94);
			mItemSize = 68;
			mStatusImageSize = new Size(8,8);
			TextSize = 25;
		} else if (Size.ScreenType == Size.S1080X1920X3) { 
			mMainSize = new Size(1010,1300);
			mShowButtonSize = new Size(69,174);
			mMusicListViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 25,mMainSize.Height - 20);
			mMainPoint = new Point(-(mMusicListViewSize.Width + 20),94);
			mItemSize = 132;
			mStatusImageSize = new Size(52,52);
			TextSize = 14;
		} else {
			mMainSize = new Size(720,855);
			mShowButtonSize = new Size(69,174);
			mMusicListViewSize = new Size(mMainSize.Width-mShowButtonSize.Width - 30,mMainSize.Height - 20);
			mMainPoint = new Point(-(mMusicListViewSize.Width + 20),94);
			mItemSize = (int) (24 * Size.Density);
			mStatusImageSize = new Size((int) (8 * Size.Density),(int) (8 * Size.Density));
			TextSize = 15;
		}
		mTitleTextViewSize = new Size(mMusicListViewSize.Width - mStatusImageSize.Width , LayoutParams.MATCH_PARENT);
		mMoveDistances = mMainSize.Width - mShowButtonSize.Width;
	}
	
    private LayoutTranslateAnimation mShowAnimation;
    private LayoutTranslateAnimation mHideAnimation;
	protected GestureDetector mOpenButtonGestureDetector;
	private MP3Adapter mAdapter;
	private void createView() {
		mShowAnimation = new LayoutTranslateAnimation(getContext(),new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(getContext(),new OvershootInterpolator());
		
		LinearLayout mLayout = ViewMaker.LinearMaker(getContext(), this, mMusicListViewSize.Width + 20, mMusicListViewSize.Height + 20, 0, 0);
		mLayout.setBackground(ImageUtils.makeRoundDrawable(180));
		
			mListView = new ListView(getContext());
			mLayout.addView(mListView);
			LayoutUtils.setLinearLayoutParams(mListView, mMusicListViewSize.Width, mMusicListViewSize.Height, 10, 10);
			mListView.setFadingEdgeLength(0);
			mListView.setCacheColorHint(0);
			mAdapter = new MP3Adapter(getContext());
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mService.moveIndex(position);
				}
			});
			
			
		mOpenButton = ViewMaker.ButtonMaker(getContext(), this, mShowButtonSize.Width, mShowButtonSize.Height, mMusicListViewSize.Width + 20, 0);
		mOpenButton.setBackgroundResource(R.drawable.left_open);
		LayoutUtils.setRelativeRule(mOpenButton, RelativeLayout.CENTER_VERTICAL);
		mOpenButtonGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
			@Override public boolean onDown(MotionEvent event) {return true;}
			@Override public void onLongPress(MotionEvent e) {}
			@Override public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {return true;}
			@Override public void onShowPress(MotionEvent e) {}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				if (!mIsOpen && velocityX > 0)
					setSwitchPanel(true);
				else if (mIsOpen && velocityX < 0)
					setSwitchPanel(false);
				return true;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				setSwitchPanel(!mIsOpen);
				return true;
			}
        });
		mOpenButton.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mOpenButtonGestureDetector.onTouchEvent(event);
				return true;
			}
		});
	}
	
	public void setMusicListItem(ArrayList<String> array) {
		mAdapter.clear();
		mAdapter.addAll(array);
		mAdapter.notifyDataSetChanged();
		
	}
	
	public void onChangePlayMusic() {
		mAdapter.notifyDataSetChanged();
		int first = mListView.getFirstVisiblePosition();
		int last = mListView.getLastVisiblePosition();
		int visibleCount = last - first;
		
		int index = mService.getPlayIndex();
		if (index < first || index > last) {
			index = index - (visibleCount / 2);
			if (index < 0)
				index = 0;
			else if (index >= mAdapter.getCount())
				index = mAdapter.getCount() -1;
			mListView.setSelection(index);
		}
	}
	
    public void setSwitchPanel(boolean isShow) {
    	if (mIsOpen == isShow)
    		return;
    	mIsOpen = isShow;
    	if (isShow) {
    		bringToFront();
    		mShowAnimation.startUsingDistance(this, false, -mMoveDistances, 400,null);
    	} else {
    		mHideAnimation.startUsingDistance(this, false, mMoveDistances, 400,null);
    	}
    }
    
    public void setInvisibleHidePanel() {
    	if (mIsOpen) {
    		mIsOpen = false;
    		mHideAnimation.startUsingDistance(this, false, mMoveDistances, 400,mOnFlingListener);
    	} else {
    		this.setVisibility(View.INVISIBLE);
    	}
    }
    
	public class MP3Adapter extends ArrayAdapter<String> {
	    private static final String TAG = "MP3Adapter";
	    public MP3Adapter(Context context) {
	    	super(context,0);
	    }

        
	    class ViewHolder extends LinearLayout {
			public TextView mTitleView = null;
	    	public ImageView mStatusView = null;
	    	public ViewHolder(Context context) {
				super(context);
				setOrientation(LinearLayout.HORIZONTAL);
				setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,mItemSize));
				setGravity(Gravity.CENTER_VERTICAL);
				mStatusView = ViewMaker.ImageViewMaker(getContext(), this, mStatusImageSize.Width, mStatusImageSize.Height, 0, 0);
				mStatusView.setBackgroundResource(R.drawable.h_speaker);
				mStatusView.setVisibility(View.INVISIBLE);

				mTitleView = ViewMaker.TextViewMaker(getContext(), this, "", mTitleTextViewSize.Width, mTitleTextViewSize.Height, 0, 0);
				mTitleView.setSingleLine(true);
				mTitleView.setEllipsize(TruncateAt.END);
				mTitleView.setSelected(true);
				mTitleView.setGravity(Gravity.CENTER_VERTICAL);
			}
	    	public void setPlay(boolean isPlay) {
	    		if (isPlay) {
	    			mStatusView.setVisibility(View.VISIBLE);
	    			mTitleView.setBackgroundColor(Color.WHITE);
	    			mTitleView.setTextColor(Color.BLACK);
	    		} else {
	    			mStatusView.setVisibility(View.INVISIBLE);
	    			mTitleView.setBackground(null);
	    			mTitleView.setTextColor(Color.WHITE);
	    		}
	    	}
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	return getContent(position,convertView,parent);
	    }
	    
	    
	    private View getContent(int position, View convertView, ViewGroup parent) {
	    	ViewHolder listMain = (ViewHolder) convertView;
	    	
	    	String text = null;
	    	text = (String) getItem(position);
	    	
    		if (listMain == null) {
    			listMain = new ViewHolder(getContext());
    		} 
    		listMain.mTitleView.setText(FileUtils.getFileName(text));
    		int index = mService.getPlayIndex();
    		listMain.setPlay(index >= 0 && position == index);
	        return listMain;
	    }
	    

	}
}
