package com.kds3393.just.viewer.provider;

public class DBItemData {
	private static final String TAG = "ImageData";
	
	public long mId;
	public String mPath;
	public int mPageNum;	//현재 페이지
	public int mIsLeft;	//ImageViewer : 1 - left, 0 - right //MoviePlayer : 1 - portrait, 0 - landscape
	public int mZoomType;		//

    /**
     * ImageViewer : custom zoom인 경우 기준 height
     * TextViewer : Hide Position value
     */
	public int mZoomStandardHeight = 0;

	public DBItemData() {}
	

	public DBItemData(String path, int page, int isLeft) {
		mPath = path;
		mPageNum = page;
		mIsLeft = isLeft;
	}

	@Override
	public String toString() {
		return "path = " + mPath + " mPageNum = " + mPageNum;
	}
}