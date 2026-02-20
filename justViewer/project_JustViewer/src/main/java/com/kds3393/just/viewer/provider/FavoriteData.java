package com.kds3393.just.viewer.provider;

public class FavoriteData {
	private static final String TAG = "FavoriteData";

    public static final int INDEX_ID = 0;
    public static final int INDEX_PATH = 1;
    public static final int INDEX_TYPE = 2;

	public long mId;
	public String mPath;
	public int mViewType;
	
	public FavoriteData() {}
	

	public FavoriteData(String path, int viewType) {
		mPath = path;
		mViewType = viewType;
	}
}