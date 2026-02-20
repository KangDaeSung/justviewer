package com.kds3393.just.viewer.Image;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.Toast;

import com.common.utils.FileUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SettingImageViewer;
import com.kds3393.just.viewer.Config.SharedPrefHelper;
import com.kds3393.just.viewer.Image.ImageDownloader.OnImageDownloadCompleteListener;
import com.kds3393.just.viewer.Utils.SubStringComparator;
import com.kds3393.just.viewer.provider.DBItemData;
import com.kds3393.just.viewer.provider.DBMgr;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class ImageViewer extends ImageViewerConfig implements GestureDetector.OnGestureListener , GestureDetector.OnDoubleTapListener{
	private static final String TAG = "ImageViewer";

    private Context mContext;
	private Size ViewSize = new Size();
    
    private PageView[] Views = new PageView[3];
    public ArrayList<String> mFilePaths;
    public int mBookIndex;
    
    private boolean mIsFirstView = true;
    private OnPageSelectedListener mOnPageSelectedListener = null;
	public void setOnPageSelectedListener(OnPageSelectedListener listener) {
		mOnPageSelectedListener = listener;
	}
	
	public interface OnPageSelectedListener {
		public void onPageSelected(PageView CurrentPage, int index);
		public void onBookSelected(String path);
		public void onSingleTab();
        public void onLoaded(int pageSize);
	}
	
	public ImageViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	
	public ImageViewer(Context context) {
		super(context);
		init(context);
	}

    private DBMgr mDBMgr;
	private void init(Context context) {
        mContext = context;
        mDBMgr = DBMgr.getInstance();
		mGestureDetector = new GestureDetector(context, this);
		Views[0] = PageViewMaker(getContext(),this,0,0,0,0);
		Views[1] = PageViewMaker(getContext(),this,0,0,0,0);
		Views[2] = PageViewMaker(getContext(),this,0,0,0,0);
	}
	
	public void setZipPath(String path) {
		ImageDownloader.sZipPath = path;
	}
	public void setContentFileList(ArrayList<String> file) {
		if (file.size() > 1) {
			mFilePaths = file;
			mBookIndex = mFilePaths.indexOf(ImageDownloader.sZipPath);
		} else
			mFilePaths = null;
	}
	
	public void setViewSize(int width, int height) {
		ViewSize.Width = ImageDownloader.sParentViewSize.Width = width;
		ViewSize.Height = ImageDownloader.sParentViewSize.Height = height;
		movePageHThreshold = (int) (ViewSize.Width * 0.1); 
	}
	
	public static PageView PageViewMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		PageView view = new PageView(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView, view, width, height, x, y, 0, 0);
		return view;
	}
	
	public void moveRight() {
		CLog.e(TAG, "KDS3393_movebook Views[1].getRight() = " + Views[1].getRight() + " ViewSize.Width = " + ViewSize.Width);
		if (mFlingRunnable.isFinished()) {
			if (!ImageDownloader.isNext(mImageData.mPageNum) && Views[1].getRight() <= ViewSize.Width) {
				MoveBook(true);	//Next Book
				return;
			}
			int distance = 0;
			boolean isViewWidthDistance = false;
			if (KConfig.cZoomLevel == KConfig.ZOOM_USER_CUSTOM) {
				if (Views[1].getWidth() > (ViewSize.Width * 2))
					isViewWidthDistance = true;
			}
			if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT || isViewWidthDistance) {
				//distance = (Views[1].getWidth() / 2) + mSpacing;
				int harf = getHarfPage(Views[1]);
				
				int skipMargin = (harf - ViewSize.Width) / 2;
				int left = Math.abs(Views[1].getLeft());
				int offset = 0;
				if (left < (Views[1].getWidth() / 2)) {	//오른쪽 페이지로 이동
					offset = left - skipMargin;
					distance = harf - offset;
				} else { // 다음 이미지의 왼쪽 페이지로 이동
					if (ImageDownloader.isNext(mImageData.mPageNum)) {
						int page2Margin = (getHarfPage(Views[2]) - ViewSize.Width) / 2;
						offset = left - harf - skipMargin;
						distance = ViewSize.Width + skipMargin + mSpacing + page2Margin - offset; 
					} else {
						distance = Views[1].getWidth() - (left + ViewSize.Width);
					}

				}
			} else if (Views[1].getRight() < ViewSize.Width) {
				mFlingRunnable.startUsingVelocity(-8000);
				return;
			} else if (KConfig.cZoomLevel == KConfig.ZOOM_USER_CUSTOM) {
				if (Views[1].getRight() <= ViewSize.Width) {
					distance = ViewSize.Width + mSpacing;
				} else {
					distance = Views[1].getRight() - ViewSize.Width;
				}
			} else {
				distance = ViewSize.Width + mSpacing;
			}
			mFlingRunnable.startUsingDistance(-distance);
		}
	}
	
	public void moveLeft() {
		CLog.e(TAG, "KDS3393_movebook Views[1].getLeft() = " + Views[1].getLeft());
		if (mFlingRunnable.isFinished()) {
			if (!ImageDownloader.isPrev(mImageData.mPageNum) && Views[1].getLeft() >= 0) {
				MoveBook(false); //Prev Book
				return;
			}
			int distance = 0;
			boolean isViewWidthDistance = false;
			if (KConfig.cZoomLevel == KConfig.ZOOM_USER_CUSTOM) {
				if (Views[1].getWidth() > (ViewSize.Width * 2))
					isViewWidthDistance = true;
			}
			if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT || isViewWidthDistance) {
				//distance = (Views[1].getWidth() / 2) + mSpacing;
				int harf = getHarfPage(Views[1]);
				int skipMargin = (harf - ViewSize.Width) / 2;
				int left = Math.abs(Views[1].getLeft());
				int offset = 0;
				if (left > getHarfPage(Views[1])) {	//왼쪽 페이지로 이동
					offset = left - harf - skipMargin;
					distance = harf + offset;
				} else { // 이전 이미지의 오른쪽 페이지로 이동
					if (ImageDownloader.isPrev(mImageData.mPageNum)) {
						int page0Margin = (getHarfPage(Views[0]) - ViewSize.Width) / 2;
						offset = left - skipMargin;
						distance = ViewSize.Width + page0Margin + mSpacing + skipMargin + offset; 
					} else {
						distance = left;
					}
				}
			} else if (Views[1].getLeft() > 0) {
				mFlingRunnable.startUsingVelocity(8000);
				return;
			} else if (KConfig.cZoomLevel == KConfig.ZOOM_USER_CUSTOM) {
				if (Views[1].getLeft() < 0) {
					distance = Math.abs(Views[1].getLeft());
				} else {
					distance = ViewSize.Width + mSpacing;
				}
			} else {
				distance = ViewSize.Width + mSpacing;
			}
			mFlingRunnable.startUsingDistance(distance);
				
		}
	}
	
	private int getHarfPage(View view) {
		int harf;
		if (view.getWidth() > view.getHeight()) {
			harf = (view.getWidth() / 2);
		} else {
			harf = view.getWidth();
		}
		return harf;
	}
	
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

	public void gotoPage(int index) {
		if (mImageData.mPageNum == index)
			return;
		mImageData.mPageNum = index;
		ImageDownloader.doRecycle(mImageData.mPageNum);
		mDistance = 0;
		setView(1,mImageData.mPageNum);
		
		setSelected(mImageData.mPageNum);
		invalidate();
	}
	
	public int getIsLeftCurrentPage() {
		int offset = Views[1].getW() / 4;
		if (Math.abs(Views[1].getLeft()) <= offset) {
			return 1;
		} else {
			return 0;
		}
	}
	private void setSelected(int index) {
		setView(0,index - 1);
		setView(2,index + 1);
		setVisibility(Views[0],View.INVISIBLE);
		setVisibility(Views[2],View.INVISIBLE);
		if (mOnPageSelectedListener != null)
			mOnPageSelectedListener.onPageSelected(Views[1], mImageData.mPageNum);
	}
	
	private boolean isBlockTouch = false;
	public void setDirection(boolean isRight) {
		if (SettingImageViewer.getIsPageRight(getContext()) != isRight) {
			isBlockTouch = true;
			Collections.sort(ImageDownloader.sEntryArray, new SubStringComparator(isRight));
			SharedPrefHelper.setImagePageType(getContext(), isRight);
			SettingImageViewer.setIsPageRight(getContext(), isRight);
			mImageData.mPageNum = mPageCount - 1 - mImageData.mPageNum;
			mImageData.mIsLeft = getIsLeftCurrentPage();
			mIsFirstView = true;
			mDistance = 0;
			setView(1,mImageData.mPageNum);
			
			setSelected(mImageData.mPageNum);
			invalidate();
			isBlockTouch = false;
		}
	}
	
	private void setView(int viewId,int index) {
		if (index < 0 || index >= ImageDownloader.sEntryArray.size())
			return;
		Views[viewId].mIndex = index;
		ImageDownloader.download(getContext(), viewId, index, Views[viewId], true, new OnImageDownloadCompleteListener(){
			@Override
			public void onComplete(Bitmap bmp, int viewId, int index) {
				if (viewId == 0) {
					setPrevLayout();
				} else if (viewId == 2) {
					CLog.e(TAG, "KDS3393_view complete 2 = " + Views[2].mIndex + " :: " + Views[2].getBackground());
					setNextLayout();
				} else if (viewId == 1) {
					int topMargin = 0;
					int width = 0;
					int height = 0;
					if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT) {
						float scale = ((float)ViewSize.Height / (float)Views[viewId].getH());
						width = (int) ((float)Views[viewId].getW() * scale);
						height = ViewSize.Height;
					} else if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_SCREEN) {
						float scale = (float)Views[viewId].getW() / (float)ViewSize.Width ;
						height = (int) ((float)Views[viewId].getH() / scale);
						width = ViewSize.Width;
						topMargin = (ViewSize.Height - height) / 2;
						if (topMargin < 0)
							topMargin = 0;
					} else if (KConfig.cZoomLevel == KConfig.ZOOM_HARF_SCREEN) {
						topMargin = getTopMargin(Views[viewId]);
						width = Views[viewId].getW();
						height = Views[viewId].getH();
					} else {
						float scale = ((float)KConfig.cStandardHeight / (float)Views[viewId].getH());
						width = (int) ((float)Views[viewId].getW() * scale);
						height = KConfig.cStandardHeight;
						topMargin = (ViewSize.Height - height) / 2;
						if (topMargin < 0)
							topMargin = 0;
					}
					
					
					if (mIsFirstView) {
						mIsFirstView = false;
						if (mImageData.mIsLeft == 0) {
							mDistance = ViewSize.Width - width;
						}
					}
					Views[1].layout(mDistance, topMargin, mDistance + width, topMargin + height);
					invalidate();
				}
			}
		});
	}
	
	private void setVisibility(View view, int visibility) {
		if (view != null)
			view.setVisibility(visibility);
	}
	
	private int mDistance = 0;
	
	private GestureDetector mGestureDetector;
	
	private FlingRunnable mFlingRunnable = new FlingRunnable();
	private boolean mBlockLayoutRequests = false;
	private float beforeMultiTouchDistance = 0;
	private boolean mIsScaling = false;
	private int mOldWidth = 0;
	private int mOldHeight = 0;
	private int mOldLeft = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isBlockTouch)
			return true;
        int action = event.getAction();
        if (event.getPointerCount() > 1) {
            float distance = spacing(event);
            if (action > MotionEvent.ACTION_MASK) {
        		if (action == MotionEvent.ACTION_POINTER_2_DOWN) {
        			beforeMultiTouchDistance = distance;
                	mOldWidth = Views[1].getWidth();
                	mOldHeight = Views[1].getHeight();
                	mOldLeft = Views[1].getLeft();
        		} else if (action == MotionEvent.ACTION_POINTER_2_UP) {
        			
        		}
        		return true;
        	}
            switch (action) {
            case MotionEvent.ACTION_DOWN:
            	beforeMultiTouchDistance = distance;
            	mOldWidth = Views[1].getWidth();
            	mOldHeight = Views[1].getHeight();
            	mOldLeft = Views[1].getLeft();
            	break;
            case MotionEvent.ACTION_MOVE:
            	if (mIsScaling) {
            		return true;
            	}
            	mIsScaling = true;
            	float movDistance = distance - beforeMultiTouchDistance;
            	
            	if (Math.abs(movDistance) >= 1) {
            		
            		int height = (int) (mOldHeight + movDistance);
            		if (height > ViewSize.Height)
            			height = ViewSize.Height;
            		float scale = (float)height / (float)mOldHeight;
            		int width = (int) ((float)mOldWidth * scale);
            		if (width < ViewSize.Width) {
            			scale = (float)ViewSize.Width / (float) mOldWidth;
            			width = ViewSize.Width;
            			height = (int) ((float)mOldHeight * scale);
            			CLog.e(TAG,"KDS3393_1old = " + mOldWidth + ":" + mOldHeight + " new = " + width + " : " + height);
            		}
            		int left = (int) (mOldLeft - (movDistance / 2));
            		if (left > 0)
            			left = 0;
            		int right = left + width;
            		if (right < ViewSize.Width) {
            			right = ViewSize.Width;
            			left = right - width;
            		}
            		CLog.e(TAG,"KDS3393_2old = " + mOldWidth + ":" + mOldHeight + " new = " + width + " : " + height);
        			int topMargin = (ViewSize.Height - height) / 2;
        			if (topMargin < 0)
        				topMargin = 0;
            		Views[1].layout(left, topMargin, right, topMargin + height);
            		invalidate();
            		KConfig.cStandardHeight = height;
            		KConfig.cZoomLevel = KConfig.ZOOM_USER_CUSTOM;
            	}
            	mIsScaling = false;
            	break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	break;
            }
        } else {
            switch (action) {
            case MotionEvent.ACTION_DOWN:
            	if (!mFlingRunnable.isFinished()) {
            		onFinishedMovement();
            		mFlingRunnable.stop(false);
            	}
            	break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	if (beforeMultiTouchDistance == 0) {
                	boolean fling = mGestureDetector.onTouchEvent(event);
                	if (!fling)
                		onUp();
            	} else {
            		beforeMultiTouchDistance = 0;
            	}
                return true;
            }
            if (beforeMultiTouchDistance == 0)
            	mGestureDetector.onTouchEvent(event);
        }
		return true;
	}
	
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    } 
    
	private void setInitLayout() {
		setPrevLayout();
		setNextLayout();
	}

	private void setPrevLayout() {
		int topMargin = 0;
		int left = 0;
		int right = 0;
		int height = 0;
		if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT) {
			float scale = ((float)ViewSize.Height / (float)Views[0].getH());
			int width = (int) ((float)Views[0].getW() * scale);
			left = (int) (Views[1].getLeft() - width - mSpacing);
			right = left + width;
			height = ViewSize.Height;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_SCREEN) {
			float scale = (float)Views[0].getW() / (float)ViewSize.Width ;
			height = (int) ((float)Views[0].getH() / scale);
			right = (int) (Views[1].getLeft() - mSpacing);
			left = right - ViewSize.Width;
			topMargin = (ViewSize.Height - height) / 2;
			if (topMargin < 0)
				topMargin = 0;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_HARF_SCREEN) {
			topMargin = getTopMargin(Views[0]);
			left = Views[1].getLeft() - Views[0].getW() - mSpacing;
			right = left + Views[0].getW();
			height = Views[0].getH();
		} else {
			float scale = ((float)KConfig.cStandardHeight / (float)Views[0].getH());
			int width = (int) ((float)Views[0].getW() * scale);
			left = (int) (Views[1].getLeft() - width - mSpacing);
			right = left + width;
			height = KConfig.cStandardHeight;
			topMargin = (ViewSize.Height - height) / 2;
			if (topMargin < 0)
				topMargin = 0;
		}
		
		Views[0].layout(left, topMargin, right, topMargin + height);
		CLog.e(TAG, "KDS3393_i setPrevLayout w = " + Views[0].getWidth() + " h = " + Views[0].getHeight() + " left = " + left + " right = " + right);
	}
	
	private void setNextLayout() {
		int topMargin = 0;
		int left = 0;
		int right = 0;
		int height = 0;
		if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT) {
			float scale = ((float)ViewSize.Height / (float)Views[2].getH());
			int width = (int) ((float)Views[2].getW() * scale);
			left = Views[1].getRight() + mSpacing;
			right = left + width;
			height = ViewSize.Height;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_SCREEN) {
			float scale = (float)Views[1].getW() / (float)ViewSize.Width ;
			height = (int) ((float)Views[1].getH() / scale);
			left = Views[1].getRight() + mSpacing;
			right = left + ViewSize.Width;
			topMargin = (ViewSize.Height - height) / 2;
			if (topMargin < 0)
				topMargin = 0;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_HARF_SCREEN) {
			topMargin = getTopMargin(Views[2]);
			left = Views[1].getRight() + mSpacing;
			right = left + Views[2].getW();
			height = Views[2].getH();
		} else {
			float scale = ((float)KConfig.cStandardHeight / (float)Views[2].getH());
			int width = (int) ((float)Views[2].getW() * scale);
			left = Views[1].getRight() + mSpacing;
			right = left + width;
			height = KConfig.cStandardHeight;
			topMargin = (ViewSize.Height - height) / 2;
			if (topMargin < 0)
				topMargin = 0;
		}

		Views[2].layout(left, topMargin, right, topMargin + height);
	}
	private int getTopMargin(PageView view) {
		CLog.e(TAG, "KDS3393_getTopMargin ViewSize.Height = " + ViewSize.Height + " view.getH() = " + view.getH());
		int topMargin = (ViewSize.Height - view.getH()) / 2;
		if (topMargin < 0)
			topMargin = 0;
		return topMargin;
	}
	
    private void scrollIntoSlots() {
        if (getChildCount() == 0) return;
        
        int rightpos = ViewSize.Width - Views[1].getRight() - mSpacing;
        //CLog.e(TAG, "KDS3393_scrollIntoSlots rightpos = " + rightpos + " cR = " + Views[1].getRight() + " cL = " + Views[1].getLeft());
        if (Views[1].getRight() > 0 && ViewSize.Width != rightpos && rightpos > -mSpacing) { // next View와 화면 공유 중
        	int center = rightpos - movePageHThreshold;
        	if (center < 0) { //원래 위치
				if (Math.abs(rightpos + mSpacing) == 1) {
					onFinishedMovement();
				} else {
					mFlingRunnable.startUsingDistance(rightpos + mSpacing);
				}
        	} else { //next로 이동
        		mFlingRunnable.startUsingDistance(-Views[2].getLeft());
        	}
        	return;
        } else if (Views[1].getLeft() > 0 && Views[0].getRight() < ViewSize.Width) { //prev View와 화면 공유 중
        	if (Views[1].getLeft() < movePageHThreshold) { //원 위치
				if (Math.abs(Views[1].getLeft()) == 1) {
					onFinishedMovement();
				} else {
					mFlingRunnable.startUsingDistance(-Views[1].getLeft());
				}
        	} else { //prev로 이동
        		mFlingRunnable.startUsingDistance(ViewSize.Width - Views[0].getRight());
        	}
        	return;
        } else {
        	CLog.e(TAG, "KDS3393_view 0 = " + Views[0].mIndex + " :: " + Views[0].getBackground());
        	CLog.e(TAG, "KDS3393_view 1 = " + Views[1].mIndex + " :: " + Views[1].getBackground());
        	CLog.e(TAG, "KDS3393_view 2 = " + Views[2].mIndex + " :: " + Views[2].getBackground());
        	onFinishedMovement(); // 이동 종료
        }
    }
    
    private void onFinishedMovement() {
    	if (Views[1].getRight() >= ViewSize.Width && Views[1].getLeft() <= 0) {
    		return;
    	}
    	PageView cView = null;
    	Rect r = new Rect();
    	int centerOffset = 0;
    	
    	for (int i=0;i<Views.length;i++) {
    		if (Views[i].getW() == 0)
    			continue;
    		if (Views[i].getLeft() > 0) {
    			centerOffset = ViewSize.Width - Views[i].getLeft();
    		} else if (Views[i].getRight() > 0) {
    			centerOffset = Views[i].getRight();
    		} else {
    			if (Views[i].getLeft() < 0 && Views[i].getRight() < 0)
    				centerOffset = -1;
    			else if (Views[i].getLeft() < 0 && Views[i].getRight() < 0)
    				centerOffset = -1;
    		}
    		CLog.e(TAG, "KDS3393_i = " + i + " w = " + Views[i].getWidth() + " h = " + Views[i].getHeight());
    		CLog.e(TAG, "KDS3393_i = " + i + " centerOffset = " + centerOffset + " ViewSize.Width = " + ViewSize.Width);
    		CLog.e(TAG, "KDS3393_i = " + i + " 1 = " + (centerOffset > (ViewSize.Width / 2)));
    		CLog.e(TAG, "KDS3393_i = " + i + " 2 = " + ((i == 0 && Views[i].getRight() >= ViewSize.Width)));
    		CLog.e(TAG, "KDS3393_i = " + i + " 3 = " + (i == 2 && Views[i].getLeft() < 0));
    		if (centerOffset > (ViewSize.Width / 2) ||
    				(i == 0 && Views[i].getRight() >= ViewSize.Width) ||
    				(i == 2 && Views[i].getLeft() < 0)) {
    			if (mImageData.mPageNum == Views[i].mIndex) {
    				CLog.layout(TAG, "onFinishedMovement return ", Views[i]);
    				return;
    			}
    			mImageData.mPageNum = Views[i].mIndex;
    			cView = Views[i];
    			r.left = cView.getLeft();
    			r.right = cView.getRight();
    			r.top = cView.getTop();
    			r.bottom = cView.getBottom();
    			break;
    		}
    	}
    	
    	if (cView != null) {
    		setView(1,mImageData.mPageNum);
    		Views[1].layout(r.left, r.top, r.right, r.bottom);
    		CLog.layout(TAG, "Views[1] ", Views[1]);
    		mDistance = r.left;
    		setSelected(mImageData.mPageNum);
    		this.post(new Runnable(){
				@Override
				public void run() {
					ImageDownloader.doRecycle(mImageData.mPageNum);
				}
			});
    	}
    }
    
	void onUp() {
        if (mFlingRunnable.isFinished()) {
            scrollIntoSlots();
        }
    }

	@Override
	public boolean onDown(MotionEvent arg0) {
		setInitLayout();
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
		return mFlingRunnable.startUsingVelocity((int) (velocityX * 0.7));
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		doScroll(-distanceX);
		return false;
	}
	
	private boolean doScroll(float distanceX) {
		int topMargin = 0;
		Views[1].offsetLeftAndRight((int) distanceX);
		CLog.layout(TAG, "doScroll 1", Views[1]);
		if (distanceX > 0 && !ImageDownloader.isPrev(mImageData.mPageNum) && Views[1].getLeft() > 0) {
			topMargin = getTopMargin(Views[1]);
			Views[1].layout(0, topMargin, Views[1].getW(), topMargin + Views[1].getH());
			if (mFlingRunnable.isFinished())
				return MoveBook(false); //Prev Book
		} else if (distanceX < 0 && !ImageDownloader.isNext(mImageData.mPageNum) && Views[1].getRight() < ViewSize.Width) {
			topMargin = getTopMargin(Views[1]);
			Views[1].layout(ViewSize.Width - Views[1].getW(), topMargin, ViewSize.Width, topMargin + Views[1].getH());
			if (mFlingRunnable.isFinished())
				return MoveBook(true);	//Next Book
		}
		mDistance = Views[1].getLeft();
		setPrevLayout();
		setNextLayout();
		if (Views[0].getRight() > 0) {
			if (Views[0].getVisibility() == View.INVISIBLE)
				Views[0].setVisibility(View.VISIBLE);
		} else {
			if (Views[0].getVisibility() == View.VISIBLE)
				Views[0].setVisibility(View.INVISIBLE);
		}
		if (Views[2].getLeft() < ViewSize.Width) {
			if (Views[2].getVisibility() == View.INVISIBLE)
				Views[2].setVisibility(View.VISIBLE);
		} else {
			if (Views[2].getVisibility() == View.VISIBLE)
				Views[2].setVisibility(View.INVISIBLE);
		}
		invalidate();
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		KConfig.cZoomLevel = (KConfig.cZoomLevel + 1) % 3;
		if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT) {
			setZoomFitHeight(Views[0]);
			setZoomFitHeight(Views[1]);
			setZoomFitHeight(Views[2]);
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_HARF_SCREEN) {
			setZoomHarfScreen(Views[0]);
			setZoomHarfScreen(Views[1]);
			setZoomHarfScreen(Views[2]);
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_SCREEN) {
			setZoomFitScreen(Views[0]);
			setZoomFitScreen(Views[1]);
			setZoomFitScreen(Views[2]);
		}
		
		CLog.e(TAG, "KDS3393_zoome = " + KConfig.cZoomLevel + " 0 = " + Views[0].getHeight() + " 1 = " + Views[1].getHeight() + " 2 = " + Views[2].getHeight());
		return false;
	}

	private void setZoomFitHeight(PageView view) {
		if (view.getHeight() > 0 && ViewSize.Height > view.getHeight()) {
			float scale = (float)ViewSize.Height / (float)view.getHeight();
			int l = (int) ((float)view.getLeft() * scale);
			int r = (int) ((float)view.getRight() * scale);
			view.layout(l, 0, r, Size.DisplayHeight);
		}
	}
	
	private void setZoomHarfScreen(PageView view) {
		float scale = (float)view.getH() / (float)view.getHeight();
		int l = (int) ((float)view.getLeft() * scale);
		int r = (int) ((float)view.getRight() * scale);
		int top = getTopMargin(view);
		view.layout(l, top, r, top + view.getH());
		invalidate();
	}
	
	private void setZoomFitScreen(PageView view) {
		float scale = (float)view.getW() / (float)ViewSize.Width ;
		int h = (int) ((float)view.getH() / scale);
		int top = (ViewSize.Height - h) / 2;
		if (top < 0)
			top = 0;
		view.layout(0, top, ViewSize.Width, top + h);
		invalidate();
	}
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (mOnPageSelectedListener != null)
			mOnPageSelectedListener.onSingleTab();
		return false;
	}
	
    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (PageView view : Views) {
			if (view != null)
				view.storeRect();
		}
		super.onLayout(changed,l, t, r, b);
		for (PageView view : Views) {
			if (view != null)
				view.restoreRect();
		}
	}

    private class FlingRunnable implements Runnable {
    	private boolean isFinished = true;
        private Scroller mScroller;

        private int mLastFlingX;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        public boolean isFinished() {
        	return isFinished;
        }
        
        private void startCommon() {
            removeCallbacks(this);
        }
        
        public boolean startUsingVelocity(int initialVelocity) {
            isFinished = false;
            if (initialVelocity == 0) {
            	isFinished = true;
            	return false;
            }
            startCommon();

            int min = Views[1].getLeft();
            int max = ViewSize.Width;
            int limitMax = Views[1].getRight() - ViewSize.Width;
            if (limitMax < max)
            	max = limitMax;
            
            mLastFlingX = 0;
            if ((initialVelocity < 0 && max < 0)) { //페이지 이동 우측
            	startUsingDistance(-(ViewSize.Width + max + mSpacing));
            	return true;
            } else if  (initialVelocity > 0 && min > 0) { //페이지 이동 우측
            	startUsingDistance(ViewSize.Width - min + mSpacing);
            	return true;
            }
            mScroller.fling(0, 0, -initialVelocity, 0,min, max, 0, 0);
            mScroller.extendDuration(mAnimationDuration);
            post(this);
            return true;
        }

        public void startUsingDistance(int distance) {
            isFinished = false;
            if (distance == 0) {
            	isFinished = true;
            	return;
            }
            startCommon();
            
            mLastFlingX = 0;
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration);
            post(this);
        }
        
        public void stop(boolean scrollIntoSlots) {
            removeCallbacks(this);
            endFling(scrollIntoSlots);
        }
        
        private void endFling(boolean scrollIntoSlots) {
            /*
             * Force the scroller's status to finished (without setting its
             * position to the end)
             */
            mScroller.forceFinished(true);
            
            if (scrollIntoSlots) scrollIntoSlots();
            isFinished = true;
        }

        @Override
        public void run() {
            final Scroller scroller = mScroller;
            boolean more = scroller.computeScrollOffset();
            final int x = scroller.getCurrX();

            // Flip sign to convert finger direction to list items direction
            // (e.g. finger moving down means list is moving towards the top)
            if (mLastFlingX != x) {
                int offset = mLastFlingX - x;
                CLog.e(TAG, "KDS3393_delta = " + offset + " mLastFlingX = " + mLastFlingX + " x = " + x);
                CLog.e(TAG, "KDS3393_run = " + mFlingRunnable.isFinished());
                if (doScroll(offset)) { // 책 이동으로 인해 Fling 중단
                	removeCallbacks(this);
                	mScroller.forceFinished(true);
                	return;
                } else if (Math.abs(offset) == 1) {
                	more = false;
				}
            }
            
            if (more) {
            	mLastFlingX = x;
                post(this);
            } else {
                endFling(true);
            }
        }
    }
    
    //-------------------------------------------- Book Manage ------------------------------------------
    protected ProgressDialog mProgressDialog;
    private ContentInitAsyncTask mInitAsyncTask = new ContentInitAsyncTask();
    public void runInitTask(ProgressDialog dialog) {
    	mProgressDialog = dialog;
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    	mInitAsyncTask.execute();
    }
    
    private boolean isBookChangeing = false;
    protected class ContentInitAsyncTask extends AsyncTask<Object, Object, Boolean>{
		@Override
		protected Boolean doInBackground(Object... params) {
			isBookChangeing = true;
			ImageDownloader.doDestroy();

            //ParserContent
            ZipArchiveInputStream zipFile;
            try {
                ZipFile zFile = new ZipFile(ImageDownloader.sZipPath,"EUC-KR");
                Enumeration zipEntries = zFile.getEntries();

                if (ImageDownloader.sEntryArray == null) {
                    ImageDownloader.sEntryArray = new ArrayList<ZipEntry>();
                } else {
                    ImageDownloader.sEntryArray.clear();
                }

                while (zipEntries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) zipEntries.nextElement();
                    String extension = FileUtils.getExtension(entry.getName());
                    if (extension.equalsIgnoreCase("jpg") ||
                            extension.equalsIgnoreCase("jpeg") ||
                            extension.equalsIgnoreCase("png") ||
                            extension.equalsIgnoreCase("bmp") ||
                            extension.equalsIgnoreCase("gif")) {
                        ImageDownloader.sEntryArray.add(entry);
                    }
                }
                zFile.close();
                Collections.sort(ImageDownloader.sEntryArray, new SubStringComparator(SettingImageViewer.getIsPageRight(mContext)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //initConfig
            mImageData = mDBMgr.bookmarkLoad(ImageDownloader.sZipPath);
            if (mImageData == null) {
                int isLeftView = 1;
                if (!SettingImageViewer.getIsPageRight(mContext))
                    isLeftView = 0;
                mImageData = new DBItemData(ImageDownloader.sZipPath,0,isLeftView);
                mDBMgr.bookmarkInsert(mImageData);
            }
            KConfig.cZoomLevel = mImageData.mZoomType;
            KConfig.cStandardHeight = mImageData.mZoomStandardHeight;

			return false; 
		}
		
		@Override
		protected void onPostExecute(Boolean isError) {
            setPageCount(ImageDownloader.sEntryArray.size());
			if (mProgressDialog != null && mProgressDialog.isShowing())
	    		mProgressDialog.dismiss();

			if (mImageData != null) {
				setImageData(mImageData);
				int index = mImageData.mPageNum;
				if (mOnPageSelectedListener != null) {
                    mOnPageSelectedListener.onLoaded(mPageCount);
					mOnPageSelectedListener.onPageSelected(Views[1], index);
					mOnPageSelectedListener.onBookSelected(ImageDownloader.sZipPath);
				}
				mImageData.mPageNum = -1;
		        if (!SettingImageViewer.getIsPageRight(getContext()))
		            index = mPageCount - 1 - index;
				gotoPage(index);
			} else {
				((Activity) getContext()).finish();
			}
			isBlockTouch = false;
			isBookChangeing = true;
		}
	}
    
    public boolean MoveBook(boolean isNext) {
    	if (getContext() == null) {
    		return false;
		}
        if (!SettingImageViewer.getIsPageRight(getContext())) {
            isNext = !isNext;
        }
        
    	if (mFilePaths == null || mProgressDialog.isShowing())
    		return false;
    	int bookindex = mBookIndex;
    	String moveBookPath = null;
    	if (isNext) {
    		if (mFilePaths.size() > (mBookIndex + 1)){
        		moveBookPath = mFilePaths.get(mBookIndex + 1);
        		bookindex++;
    		} else {
    			Toast.makeText(getContext(), "마지막 책입니다.\n다음 권으로 넘어갈 수 없습니다.",Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	} else {
    		if ((mBookIndex - 1) >= 0) {
    			moveBookPath = mFilePaths.get(mBookIndex - 1);
        		bookindex--;
    		} else {
    			Toast.makeText(getContext(), "첫번째 책입니다.\n이전 권으로 넘어갈 수 없습니다.",Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	}
    	
    	File file = new File(moveBookPath);
    	if (file.exists() && file.isFile() && FileUtils.getExtension(moveBookPath).equalsIgnoreCase("zip")) {
    		mBookIndex = bookindex;
    		isBlockTouch = true;
    		mProgressDialog.setMessage(FileUtils.getFileName(moveBookPath) + "으로 이동합니다.");
    		mProgressDialog.show();
    		mImageData.mZoomStandardHeight = KConfig.cStandardHeight;
    		mImageData.mZoomType = KConfig.cZoomLevel;
    		DBMgr.getInstance().bookmarkUpdate(mImageData);
    		ImageDownloader.sZipPath = moveBookPath;
    		mInitAsyncTask = new ContentInitAsyncTask();
			if(Build.VERSION.SDK_INT > 16) {
                Views[0].setBackground(null);
                Views[1].setBackground(null);
                Views[2].setBackground(null);
			} else {
				Views[0].setBackgroundDrawable(null);
				Views[1].setBackgroundDrawable(null);
				Views[2].setBackgroundDrawable(null);
			}

    		mInitAsyncTask.execute();
    		return true;
    	}
    	return false;
    }

    //-------------------------------------------- DEBUG Code  ------------------------------------------
    public void debugLogViews() {
    	debugLogView(Views[0]);
    	debugLogView(Views[1]);
    	debugLogView(Views[2]);
    }

    public void debugLogView(PageView view) {
    	CLog.e(TAG, "[" + view.mIndex + "] v = " + view.getVisibility() + 
				" size = [" + view.getW() + "," + view.getH() + "]" + 
				" rect = [" + view.getLeft() + "," + view.getRight() + "][" + view.getTop() + "," + view.getBottom() + "]");
    }
}
