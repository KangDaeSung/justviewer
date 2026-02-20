package com.kds3393.just.viewer.Image;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.KConfig;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PageView extends ImageView {
	private static final String TAG = "PageView";
	
	int mIndex = -1;
	private int mWidth = 0;
	private int mHeight = 0;
	public PageView(Context context) {
		super(context);
	}

	public void setImageSize(int w, int h) {
		mWidth = w;
		mHeight = h;
		int topMargin = 0;
		if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_HEIGHT) {
			float scale = ((float)ImageDownloader.sParentViewSize.Height / (float)mHeight);
			w = (int) ((float)mWidth * scale);
			h = ImageDownloader.sParentViewSize.Height;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_FIT_SCREEN) {
			float scale = (float)mWidth / (float)ImageDownloader.sParentViewSize.Width ;
			w = ImageDownloader.sParentViewSize.Width;
			h = (int) ((float)mHeight / scale);
			topMargin = (ImageDownloader.sParentViewSize.Height - h) / 2;
			if (topMargin < 0)
				topMargin = 0;
		} else if (KConfig.cZoomLevel == KConfig.ZOOM_HARF_SCREEN) {
			topMargin = (Size.DisplayHeight - mHeight) / 2;
			if (topMargin < 0)
				topMargin = 0;
		} else {
			if (KConfig.cStandardHeight > 0) {
				float scale = ((float)KConfig.cStandardHeight / (float)mHeight);
				w = (int) ((float)mWidth * scale);
				h = KConfig.cStandardHeight;
				topMargin = (ImageDownloader.sParentViewSize.Height - h) / 2;
				if (topMargin < 0) {
					topMargin = 0;
				}
			} else {
				topMargin = (Size.DisplayHeight - mHeight) / 2;
				if (topMargin < 0) {
					topMargin = 0;
				}
				KConfig.cZoomLevel = KConfig.ZOOM_HARF_SCREEN;
			}
		}
		CLog.e(TAG, "KDS3393_i mIndex " + mIndex + " w = " + w + " h = " + h);
		LayoutUtils.setFrameLayoutParams(this, w, h,0,topMargin);
	}
	
	public int getW() {
		return mWidth;
	}

	public int getH() {
		return mHeight;
	}
	
	public void setInitLayout() {
		mWidth = 0;
		mHeight = 0;
		layout(0,0,0,0);
	}
	
	public void setInitLayout(boolean isRight, float distance) {
		int topMargin = (Size.DisplayHeight - mHeight) / 2;
		if (topMargin < 0)
			topMargin = 0;
		int left = 0;
		if (isRight) {
			
		} else {
			left = Size.DisplayWidth - mWidth;
		}
		layout((int)(left + distance), topMargin, (int)(left + mWidth + distance), mHeight + topMargin);
	}
	
	private Rect mRect = new Rect();
	public void storeRect() {
		mRect.left = this.getLeft();
		mRect.right = this.getRight();
		mRect.top = this.getTop();
		mRect.bottom = this.getBottom();
	}
	
	public void restoreRect() {
		if (mRect.left+mRect.top+mRect.right+mRect.bottom > 0)
			layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
	}
	@Override
	public void requestLayout() {
		
		super.requestLayout();
	}

	@Override
	public void offsetLeftAndRight(int offset) {
		super.offsetLeftAndRight(offset);
	}
	
	
}
