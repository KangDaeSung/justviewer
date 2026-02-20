package com.kds3393.just.viewer.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.common.app.CApp;
import com.kds3393.just.viewer.Config.KConfig;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by android on 2016-09-13.
 */
public class DBMgr {
    private static final String TAG = "DBMgr";

    private DBProvider mDBProvider;
    private static volatile DBMgr mInstance = null;

    private DBMgr(Context context) {
        mDBProvider = new DBProvider(context, null, DBInfo.DATABASE_VERSION);
    }

    //Create DB
    private DBMgr() {
        mDBProvider = new DBProvider(CApp.getInstance(), null, DBInfo.DATABASE_VERSION);
    }

    public static DBMgr getInstance() {
        if (mInstance == null) {
            synchronized (DBMgr.class) {
                if (mInstance == null) {
                    mInstance = new DBMgr(CApp.getInstance());
                }
            }
        }
        return mInstance;
    }

    public static DBProvider getProvider() {
        return getInstance().mDBProvider;
    }

    public static DBProvider.DBHelper getDBHelper() {
        return getInstance().mDBProvider.getDBHelper();
    }

    public void clearMgr() {
        mDBProvider.close();
        mInstance = null;
    }

    /**
     * @brief rawQuery 실행
     * @param strQuery
     * @return
     */
    public Cursor rawQuery(String strQuery) {
        Log.e(TAG,"KDS3393_TEST_rawQuery = " + strQuery);
        return mDBProvider.rawQuery(strQuery);
    }

    /**
     * @brief execQuery 실행
     * @param strQuery
     * @return
     */
    public void execQuery(String strQuery) {
        mDBProvider.execSQL(strQuery);
    }

    /**
     * @brief Favorite 저장
     */
    public void insertFavorite(FavoriteData data) {
        ContentValues values = new ContentValues();
        values.put(DBInfo.FAVORITE_PATH, data.mPath);
        values.put(DBInfo.FAVORITE_TYPE, data.mViewType);
        mDBProvider.insertDB(DBInfo.IMAGE_FAVORITE_TABLE, values);
    }

    /**
     * @brief 모든 즐겨찾기 정보 Cursor 가져오기
     */
    public Cursor getFavoriteCursor(int type) {
        String where = "";
        if (type > KConfig.TYPE_FAVORITE_ALL) {
            where = " WHERE view_type = " + type;
        }
        return rawQuery("SELECT * FROM favorite_zip" + where + " ORDER BY _id ASC");
    }

    /**
     * @brief 모든 즐겨찾기 정보 Array 가져오기
     */
    public ArrayList<FavoriteData> getFavoriteData(int type) {
        ArrayList<FavoriteData> resultList = new ArrayList<FavoriteData>();
        Cursor cursor = getFavoriteCursor(type);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                FavoriteData temp = makeFavoriteData(cursor);
                resultList.add(temp);
            }
            cursor.close();
        }
        return resultList;
    }

    /**
     * @brief 모든 즐겨찾기 정보 삭제
     */
    public void removeFavorite(long id) {
        execQuery("DELETE FROM favorite_zip WHERE _id = " + id + ";");
    }

    /**
     * @brief 모든 즐겨찾기 패스 정리(정보가 변경된 즐겨찾기 제거)
     */
    public void OrganizeDB() {
        ArrayList<FavoriteData> array = getFavoriteData(KConfig.TYPE_FAVORITE_ALL);
        File file;
        for (FavoriteData data:array) {
            file = new File(data.mPath);
            if (!file.exists()) {
                removeFavorite(data.mId);
            }
        }
    }

    private FavoriteData makeFavoriteData(Cursor cursor) {
        FavoriteData temp = new FavoriteData();
        temp.mId = cursor.getLong(cursor.getColumnIndex(DBInfo._ID));
        temp.mPath = cursor.getString(cursor.getColumnIndex(DBInfo.FAVORITE_PATH));
        temp.mViewType = cursor.getInt(cursor.getColumnIndex(DBInfo.FAVORITE_TYPE));
        return temp;
    }



    public void bookmarkInsert(DBItemData data) {
        ContentValues values = new ContentValues();
        values.clear();
        values.put(DBInfo.BOOKMARK_PATH, data.mPath);
        values.put(DBInfo.BOOKMARK_PAGE, data.mPageNum);
        values.put(DBInfo.BOOKMARK_ISLEFT, data.mIsLeft);
        values.put(DBInfo.BOOKMARK_ZOOM, data.mZoomType);
        values.put(DBInfo.BOOKMARK_IMAGE_STANDARD_HEIGHT, data.mZoomStandardHeight);

        mDBProvider.insertDB(DBInfo.BOOKMARK_TABLE, values);
    }

    public void bookmarkUpdate(DBItemData data) {
        ContentValues values = new ContentValues();
        String where = DBInfo.BOOKMARK_PATH + "=\"" + data.mPath + "\"";
        values.clear();
        values.put(DBInfo.BOOKMARK_PATH, data.mPath);
        values.put(DBInfo.BOOKMARK_PAGE, data.mPageNum);
        values.put(DBInfo.BOOKMARK_ISLEFT, data.mIsLeft);
        values.put(DBInfo.BOOKMARK_ZOOM, data.mZoomType);
        values.put(DBInfo.BOOKMARK_IMAGE_STANDARD_HEIGHT, data.mZoomStandardHeight);
        mDBProvider.updateDB(DBInfo.BOOKMARK_TABLE, values,where);
    }

    public DBItemData bookmarkLoad(String path) {
        String where = DBInfo.BOOKMARK_PATH + "=\"" + path + "\"";
        Cursor cursor = rawQuery("SELECT * FROM bookmark_table WHERE " + where + " ORDER BY _id ASC");
        if (cursor != null && cursor.getCount() > 0) {
            DBItemData loadData = new DBItemData();
            try {
                cursor.moveToFirst();
                loadData.mId = cursor.getLong(cursor.getColumnIndex(DBInfo.BOOKMARK_ID));
                loadData.mPath = cursor.getString(cursor.getColumnIndex(DBInfo.BOOKMARK_PATH));
                loadData.mPageNum = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_PAGE));
                loadData.mIsLeft = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_ISLEFT));
                loadData.mZoomType = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_ZOOM));
                loadData.mZoomStandardHeight = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_IMAGE_STANDARD_HEIGHT));
            } finally {
                cursor.close();
            }
            return loadData;
        }
        if (cursor != null)
            cursor.close();
        return null;
    }

    public void bookmarkRemove(String path) {
        String where = DBInfo.BOOKMARK_PATH + "=\"" + path + "\"";
        execQuery("DELETE FROM bookmark_table WHERE " + where);
    }

    public void bookmarkOrganizeDB() {
        ArrayList<DBItemData> array = new ArrayList<DBItemData>();
        Cursor cursor = rawQuery("SELECT * FROM bookmark_table");
        if (cursor != null && cursor.getCount() > 0) {
            DBItemData loadData = new DBItemData();
            try {
                cursor.moveToFirst();
                loadData.mId = cursor.getLong(cursor.getColumnIndex(DBInfo.BOOKMARK_ID));
                loadData.mPath = cursor.getString(cursor.getColumnIndex(DBInfo.BOOKMARK_PATH));
                loadData.mPageNum = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_PAGE));
                loadData.mIsLeft = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_ISLEFT));
                loadData.mZoomType = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_ZOOM));
                loadData.mZoomStandardHeight = cursor.getInt(cursor.getColumnIndex(DBInfo.BOOKMARK_IMAGE_STANDARD_HEIGHT));
                array.add(loadData);
            } finally {
                cursor.close();
            }
        }

        for (DBItemData data:array) {
            File file = new File(data.mPath);
            if (!file.exists()) {
                bookmarkRemove(data.mPath);
            }
        }
    }
}
