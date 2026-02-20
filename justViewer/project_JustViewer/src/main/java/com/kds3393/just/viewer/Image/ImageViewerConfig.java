package com.kds3393.just.viewer.Image;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.kds3393.just.viewer.provider.DBItemData;

public class ImageViewerConfig extends FrameLayout {
	
	public ImageViewerConfig(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewerConfig(Context context) {
		super(context);
	}
	
	protected DBItemData mImageData;
	
	protected int mSpacing = 10;
	protected int movePageHThreshold = 0;
	protected int mAnimationDuration = 300;
	
	protected int mPageCount = 0;
	
	public void setImageData(DBItemData data) {
		mImageData = data;
	}
	public DBItemData getImageData() {
		return mImageData;
	}
	public int getIndex() {
		return mImageData.mPageNum;
	}

	public void setPageCount(int count) {
		mPageCount = count;
	}

	public int getPageCount() {
		return mPageCount;
	}
}
