package com.kds3393.just.viewer.provider;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.common.utils.debug.CLog;

public class DBItemData {
	private static final String TAG = "ImageData";
	
	public long mId;
	public String mPath;
	public int mIndex;
	public int mIsLeft;	//ImageViewer : 1 - left, 0 - right //MoviePlayer : 1 - portrait, 0 - landscape
	public int mZoomType;		//
	public int mZoomStandardHeight;		//custom zoom인 경우 기준 height
	public DBItemData() {}
	

	public DBItemData(String path, int page, int isLeft) {
		mPath = path;
		mIndex = page;
		mIsLeft = isLeft;
	}
	
	public static final String[] LIST_PROJECTION = new String[] {
        Provider.IMAGE_ZIP_ID,
        Provider.IMAGE_ZIP_PATH,
        Provider.IMAGE_ZIP_PAGE,
        Provider.IMAGE_ZIP_ISLEFT,
        Provider.IMAGE_ZIP_ZOOM,
        Provider.IMAGE_STANDARD_HEIGHT,
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_PATH = 1;
    public static final int INDEX_PAGE = 2;
    public static final int INDEX_ISLEFT = 3;
    public static final int INDEX_ZOOM_TYPE = 4;
    public static final int INDEX_STANDARD_HEIGHT = 5;
    
    static public void save(DBItemData data ,ContentResolver resolver) {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Provider.IMAGE_ZIP_PATH, data.mPath);
        values.put(Provider.IMAGE_ZIP_PAGE, data.mIndex);
        values.put(Provider.IMAGE_ZIP_ISLEFT, data.mIsLeft);
        values.put(Provider.IMAGE_ZIP_ZOOM, data.mZoomType);
        values.put(Provider.IMAGE_STANDARD_HEIGHT, data.mZoomStandardHeight);
        
        resolver.insert(Provider.IMAGE_ZIP_URI, values);
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
    }
    
    static public void update(DBItemData data ,ContentResolver resolver) {
        ContentValues values = new ContentValues();
        String where = Provider.IMAGE_ZIP_PATH + "=\"" + data.mPath + "\"";
        values.clear();
        values.put(Provider.IMAGE_ZIP_PATH, data.mPath);
        values.put(Provider.IMAGE_ZIP_PAGE, data.mIndex);
        values.put(Provider.IMAGE_ZIP_ISLEFT, data.mIsLeft);
        values.put(Provider.IMAGE_ZIP_ZOOM, data.mZoomType);
        values.put(Provider.IMAGE_STANDARD_HEIGHT, data.mZoomStandardHeight);
        resolver.update(Provider.IMAGE_ZIP_URI, values, where, null);
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
    }
    
    static public DBItemData load(String path, ContentResolver resolver) {
        String where = Provider.IMAGE_ZIP_PATH + "=\"" + path + "\"";
        Cursor cursor = resolver.query(Provider.IMAGE_ZIP_URI, DBItemData.LIST_PROJECTION, where , null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	DBItemData loadData = new DBItemData();
            try {
            	cursor.moveToFirst();
            	loadData.mId = cursor.getLong(DBItemData.INDEX_ID);
            	loadData.mPath = cursor.getString(DBItemData.INDEX_PATH);
            	loadData.mIndex = cursor.getInt(DBItemData.INDEX_PAGE);
            	loadData.mIsLeft = cursor.getInt(DBItemData.INDEX_ISLEFT);
            	loadData.mZoomType = cursor.getInt(DBItemData.INDEX_ZOOM_TYPE);
            	loadData.mZoomStandardHeight = cursor.getInt(DBItemData.INDEX_STANDARD_HEIGHT);
            	Log.e(TAG,"KDS3393_mZoomType = " + loadData.mZoomType);
            } finally {
            	cursor.close();
            }
            return loadData;
        }
        if (cursor != null)
        	cursor.close();
        return null;
    }
    
    static public void removeItem(String path,ContentResolver  resolver) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String where = Provider.IMAGE_ZIP_PATH + " =?";
        
        String[] args = new String[] { path };
        Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_ZIP_URI);
        
        deleteBuild.withSelection(where, args);
        ops.add(deleteBuild.build());
        try {
            resolver.applyBatch(Provider.AUTHORITY, ops);
        } catch (Exception e) {
        	CLog.e(TAG,e);
        }
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
    }

    static public void removeItemAll(ContentResolver  resolver) {
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_ZIP_URI);
        deleteBuild.withSelection(null, null);
        ops.add(deleteBuild.build());
        try {
            resolver.applyBatch(Provider.AUTHORITY, ops);
        } catch (Exception e) {
        	CLog.e(TAG,e);
        }
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
        
    }

    static public void OrganizeDB(ContentResolver resolver) {
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	String where  = Provider.IMAGE_ZIP_ID + " =?";
    	
    	Cursor cursor = resolver.query(Provider.IMAGE_ZIP_URI, LIST_PROJECTION, null , null, null);
    	File file;
    	while (cursor.moveToNext()) {
    		file = new File(cursor.getString(INDEX_PATH));
    		if (!file.exists()) {
    			Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_ZIP_URI);
    			deleteBuild.withSelection(where, new String[] { String.valueOf(cursor.getInt(INDEX_ID)) });
    			ops.add(deleteBuild.build());
    		}
    	}
    	cursor.close();
    	if (ops.size() > 0) {
	        try {
	            resolver.applyBatch(Provider.AUTHORITY, ops);
	        } catch (Exception e) {
	        	CLog.e(TAG,e);
	        }
    	}
    }
    
    private static ChangeDBListener mChangeViewListener;
    
    public interface ChangeDBListener {
        public abstract void onDBChange();
    }
    
    public static void setOnDBChangeListener(ChangeDBListener obs) {
        mChangeViewListener = obs;
    }
}