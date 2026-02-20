package com.kds3393.just.viewer.provider;

import java.io.File;
import java.util.ArrayList;

import com.common.utils.debug.CLog;
import com.common.utils.debug.DevUtils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class FavoriteData {
	private static final String TAG = "FavoriteData";
	
	public long mId;
	public String mPath;
	public int mViewType;
	
	public FavoriteData() {}
	

	public FavoriteData(String path, int viewType) {
		mPath = path;
		mViewType = viewType;
	}
	
	public static final String[] LIST_PROJECTION = new String[] {
        Provider.IMAGE_FAVORITE_ID,
        Provider.IMAGE_FAVORITE_PATH,
        Provider.IMAGE_FAVORITE_TYPE
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_PATH = 1;
    public static final int INDEX_TYPE = 2;
    
    static public void save(FavoriteData data ,ContentResolver resolver) {
    	String where = Provider.IMAGE_FAVORITE_PATH + "=\"" + data.mPath + "\"";
        Cursor cursor = resolver.query(Provider.IMAGE_FAVORITE_URI, FavoriteData.LIST_PROJECTION, where , null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	return;
        }
        
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Provider.IMAGE_FAVORITE_PATH, data.mPath);
        values.put(Provider.IMAGE_FAVORITE_TYPE, data.mViewType);
        resolver.insert(Provider.IMAGE_FAVORITE_URI, values);
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
    }
    
    static public void update(FavoriteData data ,ContentResolver resolver) {
        ContentValues values = new ContentValues();
        String where = Provider.IMAGE_FAVORITE_PATH + "=\"" + data.mPath + "\"";
        values.clear();
        values.put(Provider.IMAGE_FAVORITE_PATH, data.mPath);
        values.put(Provider.IMAGE_FAVORITE_TYPE, data.mViewType);
        resolver.update(Provider.IMAGE_FAVORITE_URI, values, where, null);
        if (mChangeViewListener != null)
        	mChangeViewListener.onDBChange();
    }

    static public FavoriteData load(String path, ContentResolver resolver) {
        String where = Provider.IMAGE_FAVORITE_PATH + "=\"" + path + "\"";
        Cursor cursor = resolver.query(Provider.IMAGE_FAVORITE_URI, FavoriteData.LIST_PROJECTION, where , null, null);
        if (cursor != null && cursor.getCount() > 0) {
        	FavoriteData loadData = new FavoriteData();
            try {
            	cursor.moveToFirst();
            	loadData.mId = cursor.getLong(FavoriteData.INDEX_ID);
            	loadData.mPath = cursor.getString(FavoriteData.INDEX_PATH);
            	loadData.mViewType = cursor.getInt(FavoriteData.INDEX_TYPE);
            } finally {
            	cursor.close();
            }
            return loadData;
        }
        if (cursor != null)
        	cursor.close();
        return null;
    }
    
    static public void removeItem(int id,ContentResolver  resolver) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String where = Provider.IMAGE_FAVORITE_ID + " =?";
        
        String[] args = new String[] { String.valueOf(id) };
        Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_FAVORITE_URI);
        
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
    
    static public void removeItem(String path,ContentResolver  resolver) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String where = Provider.IMAGE_FAVORITE_PATH + " =?";
        
        String[] args = new String[] { path };
        Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_FAVORITE_URI);
        
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
        Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_FAVORITE_URI);
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
    	String where  = Provider.IMAGE_FAVORITE_ID + " =?";
    	
    	Cursor cursor = resolver.query(Provider.IMAGE_FAVORITE_URI, LIST_PROJECTION, null , null, null);
    	File file;
    	while (cursor.moveToNext()) {
    		file = new File(cursor.getString(INDEX_PATH));
    		if (!file.exists()) {
    			Builder deleteBuild = ContentProviderOperation.newDelete(Provider.IMAGE_FAVORITE_URI);
    			deleteBuild.withSelection(where, new String[] { String.valueOf(cursor.getInt(INDEX_ID)) });
    			ops.add(deleteBuild.build());
    			CLog.e(TAG, "KDS3393_DB Search = " + cursor.getInt(INDEX_ID));
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