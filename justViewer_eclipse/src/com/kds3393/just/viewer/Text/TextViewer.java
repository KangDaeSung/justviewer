package com.kds3393.just.viewer.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.net.io.Util;
import org.mozilla.universalchardet.UniversalDetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.provider.DBItemData;

public class TextViewer extends RelativeLayout implements TextSettingInterface {
	private static final String TAG = "TextViewer";
	private static int NONE = 0;
	private static int PREV = 1;
	private static int NEXT = 2;
	private VTextListView mTextView;
	private ImageView mCaptureView;
	private String mContentPath;
	
	private TranslateAnimation mAniLeftIn;
	private TranslateAnimation mAniLeftOut;
	private TranslateAnimation mAniRightIn;
	private TranslateAnimation mAniRightOut;
	private int mAniDuration = 500;
	private int mPageDirectionMode = SettingTextViewer.DIRECTION_VERTICAL;
	public TextViewer(Context context,String path) {
		super(context);
		init(path);
	}
	
	public TextViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(null);
	}
	
	private void init(String path) {
		mContentPath = path;
		initAnimation();
		mPageDirectionMode = SettingTextViewer.getPageDirection(getContext());
		
		mTextView = new VTextListView(getContext());
    	addView(mTextView);
    	LayoutUtils.setLayoutParams(this, mTextView, Size.DisplayWidth - 20, LayoutParams.MATCH_PARENT);
    	LayoutUtils.setRelativeRule(mTextView, RelativeLayout.CENTER_IN_PARENT);
		setBackgroundColor(SettingTextViewer.sColors[SettingTextViewer.getTextColor(getContext())][0]);
		mTextView.setOnScrollListener(new OnScrollListener(){
			private int mChangeBook = NONE;
			private boolean isFling = false;
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					if (mTextView.getLastVisiblePosition() == textArray.size() - 1) {
						mChangeBook = NEXT;
					} else if (mTextView.getFirstVisiblePosition() == 0){
						mChangeBook = PREV;
					}
				} else if (mChangeBook != NONE && scrollState == OnScrollListener.SCROLL_STATE_FLING){
					isFling = true;
				} else if (mChangeBook != NONE && isFling && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					if (mChangeBook == NEXT && mTextView.getLastVisiblePosition() == textArray.size() - 1) {
						changeBook(true);
					} else if (mChangeBook == PREV && mTextView.getFirstVisiblePosition() == 0){
						changeBook(false);
					} 
					isFling = false;
					mChangeBook = NONE;
				}
			}
		});
		
		mCaptureView = new ImageView(getContext());
		this.addView(mCaptureView);
		LayoutUtils.setRelativeLayoutParams(mCaptureView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, -1);
		mCaptureView.setVisibility(View.GONE);
	}
	
	public void setContentPath(String path) {
		mContentPath = path;
	}
	
	private void initAnimation() {
		mAniLeftIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF,-1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniLeftIn.setDuration(mAniDuration);
		mAniLeftIn.setFillEnabled(true);
		mAniLeftIn.setAnimationListener(mAnimationListener);
		mAniLeftOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,-1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniLeftOut.setDuration(mAniDuration);
		mAniLeftOut.setFillEnabled(true);
		mAniLeftOut.setFillAfter(true);
		mAniLeftOut.setAnimationListener(mAnimationListener);
		mAniRightIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniRightIn.setDuration(mAniDuration);
		mAniRightIn.setFillEnabled(true);
		mAniRightOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,1.0f,
				Animation.RELATIVE_TO_SELF,0.0f,
				Animation.RELATIVE_TO_SELF,0.0f);
		mAniRightOut.setDuration(mAniDuration);
		mAniRightOut.setFillEnabled(true);
		mAniRightOut.setFillAfter(true);
		mAniRightOut.setAnimationListener(mAnimationListener);
	}
	
	private boolean mAnimation = false;
	private AnimationListener mAnimationListener = new AnimationListener(){
		@Override public void onAnimationRepeat(Animation arg0) {}
		@Override public void onAnimationStart(Animation arg0) {}
		@Override
		public void onAnimationEnd(Animation arg0) {
			mCaptureView.setVisibility(View.GONE);
			mAnimation = false;
		}
	};
	
	DBItemData mTextData;
	public void setTextData(DBItemData data) {
		mTextData = data;
	}
	
	private ContentReadFileAsyncTask mReadBookAsyncTask = new ContentReadFileAsyncTask();
	public void runInitTask() {
		mReadBookAsyncTask.execute(true);
	}
	
	public int getFirstVisiblePosition() {
		return mTextView.getFirstVisiblePosition();
	}
	
	public void setScrollSpeed(int speed) {
		mTextView.setScrollSpeed(speed);
	}
	
	public void setOnItemClickListener(OnItemClickListener l) {
		mTextView.setOnItemClickListener(l);
	}
	
	public void smoothScrollBy(int distance, int duration) {
		mTextView.smoothScrollBy(distance, duration);
	}
	
	public void movePrev() {
		if (mTextView.getFirstVisiblePosition() == 0) {
			changeBook(false);
		} else {
			if (mPageDirectionMode == SettingTextViewer.DIRECTION_VERTICAL) {
				moveScrollUp();
			} else {
				captureView();
				mCaptureView.startAnimation(mAniRightOut);
				mCaptureView.setVisibility(View.VISIBLE);
				
				mTextView.startAnimation(mAniLeftIn);
				this.smoothScrollBy(-Size.DisplayHeight, 1);
			}
		}
	}
	
	public void moveNext() {
		Log.e(TAG,"KDS3393_last = " + mTextView.getLastVisiblePosition() + " last line = " + textArray.size());
		if (mTextView.getLastVisiblePosition() == textArray.size() - 1) {
			changeBook(true);
		} else {
			if (mPageDirectionMode == SettingTextViewer.DIRECTION_VERTICAL) {
				moveScrollDown();
			} else {
				captureView();
				mCaptureView.startAnimation(mAniLeftOut);
				mCaptureView.setVisibility(View.VISIBLE);

				mTextView.startAnimation(mAniRightIn);
				this.smoothScrollBy(Size.DisplayHeight, 1);
			}
		}
	}
	
	private void changeBook(boolean isNext) {
		if (isNext) {
			Toast.makeText(getContext(), "마지막 입니다.",Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getContext(), "처음 입니다.",Toast.LENGTH_SHORT).show();
		}
	}
	
	private void captureView() {
		this.buildDrawingCache();
		Bitmap bitmap = this.getDrawingCache();
		mCaptureView.setBackground(new BitmapDrawable(bitmap));
	}
	public void moveScrollUp() {
		mTextView.moveScrollUp();
	}
	
	public void moveScrollDown() {
		mTextView.moveScrollDown();
	}
	
	protected class ContentReadFileAsyncTask extends AsyncTask<Boolean, Object, Boolean>{
		@Override
		protected Boolean doInBackground(Boolean... params) {
			setTextBook(mContentPath);
			//initConfig();
			return true; 
		}
		
		@Override
		protected void onPostExecute(Boolean isReadText) {
			mTextView.setBook(textArray);
			mTextView.setSelection(mTextData.mIndex);
		}
	}

	private static String detectEncoding(File file) throws IOException {
		byte[] buf = new byte[4096];

		FileInputStream  fis = new FileInputStream(file);
		UniversalDetector detector = new UniversalDetector(null);
		
	    int nread;
	    int total = 0;
	    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	    	total += nread;
	    	detector.handleData(buf, 0, nread);
	    	if (total > 1024*64) {
	    		break;
	    	}
	    }

	    fis.close();
	    
	    detector.dataEnd();
	    String encoding = detector.getDetectedCharset();
	    
	    if (encoding == null) encoding = "EUC-KR";
		return encoding;
	}
	
	private ArrayList<String> textArray = new ArrayList<String>();
	public boolean setTextBook(String contentPath) {
		File file = new File(contentPath);
		if (file.exists() && file.isFile() && file.canRead()) {
			
			try {
				String charset = detectEncoding(file);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file),charset));
				String s;
				StringBuilder strBuf = new StringBuilder();
				while ((s = in.readLine()) != null) {
        	    	s = s.replace("&nbsp;", "");
       	    		strBuf.append(s);
       	    		textArray.add(strBuf.toString());
       	    		strBuf.setLength(0);
        	    }
        	    in.close();
        	    return true;
			} catch (Exception e) {
				CLog.e(TAG, e);
			} 
		}
		return false;
	}
	
	@Override
	public void setTextFont(int index) {
		mTextView.setTextFont(index);

	}

	@Override
	public void setTextColor(int index) {
		setBackgroundColor(SettingTextViewer.sColors[index][0]);
		mTextView.setTextColor(index);
	}

	@Override
	public void setTextSize(int index) {
		mTextView.setTextSize(index);

	}

	@Override
	public void setLineSpacing(int index) {
		mTextView.setLineSpacing(index);

	}
}
