package com.kds3393.just.viewer.Music;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.common.utils.FileUtils;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation;
import com.kds3393.just.viewer.Animation.LayoutTranslateAnimation.OnFlingListener;
import com.kds3393.just.viewer.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicListView extends LinearLayout {
	private static final String TAG = "MusicListView";

	private boolean mIsOpen = false;
	private OnFlingListener mOnFlingListener;
	private MusicService mService;

    @BindView(R.id.music_list) ListView music_list;
    @BindView(R.id.side_open_btn) ImageButton side_open_btn;
    public static MusicListView make(Context context,ViewGroup parent) {
    	MusicListView view = new MusicListView(context);
    	parent.addView(view);
 
    	view.setVisibility(View.GONE);
    	return view;
    }
	
    public MusicListView(Context context) {
		super(context);
		init();
	}
	
    public MusicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.v_music_list, this);
        ButterKnife.bind(this, this);
		setOrientation(LinearLayout.HORIZONTAL);
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

	private int mMoveDistances;						//Open Animation시 이동 거리

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mMoveDistances = getLayoutParams().width - side_open_btn.getWidth();
    }

    private LayoutTranslateAnimation mShowAnimation;
    private LayoutTranslateAnimation mHideAnimation;
	protected GestureDetector mOpenButtonGestureDetector;
	private MP3Adapter mAdapter;
	private void createView() {
		mShowAnimation = new LayoutTranslateAnimation(getContext(),new AnticipateInterpolator());
		mHideAnimation = new LayoutTranslateAnimation(getContext(),new OvershootInterpolator());

		mAdapter = new MP3Adapter(getContext());
        music_list.setAdapter(mAdapter);
        music_list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mService.moveIndex(position);
			}
		});


        side_open_btn = (ImageButton) findViewById(R.id.side_open_btn);
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
        side_open_btn.setOnTouchListener(new OnTouchListener(){
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
		int first = music_list.getFirstVisiblePosition();
		int last = music_list.getLastVisiblePosition();
		int visibleCount = last - first;
		
		int index = mService.getPlayIndex();
		if (index < first || index > last) {
			index = index - (visibleCount / 2);
			if (index < 0)
				index = 0;
			else if (index >= mAdapter.getCount())
				index = mAdapter.getCount() -1;
            music_list.setSelection(index);
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

        
	    class ViewHolder  {
			public TextView mTitleView = null;
	    	public ImageView mStatusView = null;
	    	public ViewHolder(View v) {
				mStatusView = (ImageView) v.findViewById(R.id.status_icon);
				mTitleView = (TextView) v.findViewById(R.id.music_name);
			}

			public void setData(String name) {
				mTitleView.setText(name);
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
	    	String text = null;
			ViewHolder holder = null;
    		if (convertView == null) {
				convertView = inflate(getContext(), R.layout.v_music_list_item, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
    		} else {
				holder = (ViewHolder) convertView.getTag();
			}
            text = (String) getItem(position);
            Log.e(TAG,"KDS3393_TEST_text = " + text);
			holder.setData(FileUtils.getFileName(text));
    		int index = mService.getPlayIndex();
			holder.setPlay(index >= 0 && position == index);
	        return convertView;
	    }
	}
}
