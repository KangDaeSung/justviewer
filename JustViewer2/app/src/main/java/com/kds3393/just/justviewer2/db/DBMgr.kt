package com.kds3393.just.justviewer2.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.kds3393.just.justviewer2.CApp
import com.kds3393.just.justviewer2.data.BookInfo
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.data.TextItemData
import com.kds3393.just.justviewer2.db.DBProvider.DBHelper
import java.io.File
import kotlin.collections.set

class DBMgr {
    var provider: DBProvider
    private constructor(context: Context) {
        if (DBInfo.DB_PATH == null) {
            val dbFile = context.getDatabasePath(DBInfo.DB_NAME)
            DBInfo.DB_PATH = dbFile.parent //            DB_PATH = Environment.getExternalStorageDirectory().getPath() + "/";
        }
        provider = DBProvider(context, DBInfo.DB_PATH, null, DBInfo.DATABASE_VERSION)
    }

    fun clearMgr() {
        provider.close()
        mInstance = null
    }

    /**
     * rawQuery 실행
     * @param strQuery
     * @return
     */
    fun rawQuery(strQuery: String): Cursor? { //        Log.e(TAG,"KDS3393_TEST_rawQuery strQuery = " + strQuery);
        return provider.rawQuery(strQuery)
    }

    fun deleteItem(table: String, id: Long) {
        provider.deleteDB(table, DBInfo._ID + " = \"" + id + "\"")
    }

    fun deleteAll(table: String) {
        provider.deleteDB(table, null)
    }

    //------------------------------------------------------- Text 책갈피 Table -------------------------------------------------------------
    //Image 책갈피 정보 Insert
    fun insertImageData(data: BookInfo) {
        val values = ContentValues()
        values.put(DBInfo._PATH, data.targetPath)
        values.put(DBInfo._PAGE, data.currentPage)
        values.put(DBInfo.IS_LEFT, data.isLeft)
        values.put(DBInfo.ZOOM_TYPE, data.zoomType)
        values.put(DBInfo.ZOOM_HEIGHT, data.zoomStandardHeight)
        val result = provider.insertDB(DBInfo.IMAGE_BOOKMARK_TABLE, values)
        Log.e(TAG, "KDS3393_TEST_insertImageData result = $result")
    }

    //Image 책갈피 정보 업데이트
    fun updateImageData(data: BookInfo) {
        val bookId = if (data.id > 0) {
            data.id
        } else {
            val cursor = rawQuery("SELECT ${DBInfo._ID} FROM ${DBInfo.IMAGE_BOOKMARK_TABLE} WHERE ${DBInfo._PATH}=\"${data.targetPath}\"" + " ORDER BY _id ASC")
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                DBUtils.obtainLong(cursor, DBInfo._ID)
            } else {
                -1
            }
        }

        if (bookId > 0) {
            val values = ContentValues()
            values.put(DBInfo._PATH, data.targetPath)
            values.put(DBInfo._PAGE, data.currentPage)
            values.put(DBInfo.IS_LEFT, data.isLeft)
            values.put(DBInfo.ZOOM_TYPE, data.zoomType)
            values.put(DBInfo.ZOOM_HEIGHT, data.zoomStandardHeight)
            provider.updateDB(DBInfo.IMAGE_BOOKMARK_TABLE, values, DBInfo._ID + " = " + bookId)
        } else {
            insertImageData(data)
        }
    }

    //Image Item 정보 가져오기
    fun imageDataLoad(path: String): BookInfo? {
        val where = DBInfo._PATH + "=\"" + path + "\""
        val cursor = rawQuery("SELECT * FROM " + DBInfo.IMAGE_BOOKMARK_TABLE + " WHERE " + where + " ORDER BY _id ASC")
        if (cursor != null && cursor.count > 0) {
            var book: BookInfo?
            cursor.use {
                cursor.moveToFirst()
                book = BookInfo(DBUtils.obtainString(it, DBInfo._PATH))
                book.id = DBUtils.obtainLong(it, DBInfo._ID)
                book.currentPage = DBUtils.obtainInt(it, DBInfo._PAGE)
                book.isLeft = DBUtils.obtainInt(it, DBInfo.IS_LEFT)
                book.zoomType = DBUtils.obtainInt(it, DBInfo.ZOOM_TYPE)
                book.zoomStandardHeight = DBUtils.obtainInt(it, DBInfo.ZOOM_HEIGHT)
            }
            return book
        }
        cursor?.close()
        return null
    }

    //모든 Text Item 정보 가져오기
    val imageList: ArrayList<BookInfo>
        get() {
            val wbList = ArrayList<BookInfo>()
            val cursor = rawQuery("SELECT * FROM " + DBInfo.IMAGE_BOOKMARK_TABLE)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val loadData = BookInfo(DBUtils.obtainString(cursor, DBInfo._PATH))
                    loadData.id = DBUtils.obtainLong(cursor, DBInfo._ID)
                    loadData.currentPage = DBUtils.obtainInt(cursor, DBInfo._PAGE)
                    loadData.isLeft = DBUtils.obtainInt(cursor, DBInfo.IS_LEFT)
                    loadData.zoomType = DBUtils.obtainInt(cursor, DBInfo.ZOOM_TYPE)
                    loadData.zoomStandardHeight = DBUtils.obtainInt(cursor, DBInfo.ZOOM_HEIGHT)
                    wbList.add(loadData)
                }
                cursor.close()
            }
            return wbList
        }

    //------------------------------------------------------- Text 책갈피 Table -------------------------------------------------------------
    //Text 책갈피 정보 Insert
    fun insertTextData(data: TextItemData) : Long {
        val values = ContentValues()
        values.put(DBInfo._PATH, data.mPath)
        values.put(DBInfo._PAGE, data.mPageNum)
        values.put(DBInfo.TEXT_HIDE_LINE, data.mHighLine)
        return provider.insertDB(DBInfo.TEXT_BOOKMARK_TABLE, values)
    }

    //Text 책갈피 정보 업데이트
    fun updateTextData(data: TextItemData) {
        val d = bookmarkLoad(data.mPath)
        if (d == null) {
            insertTextData(data)
        } else {
            val values = ContentValues()
            values.put(DBInfo._PATH, data.mPath)
            values.put(DBInfo._PAGE, data.mPageNum)
            values.put(DBInfo.TEXT_HIDE_LINE, data.mHighLine)
            provider.updateDB(DBInfo.TEXT_BOOKMARK_TABLE, values, DBInfo._ID + " = " + d.mId)
        }
    }

    //Text Item 정보 가져오기
    fun bookmarkLoad(path: String): TextItemData? {
        val where = DBInfo._PATH + "=\"" + path + "\""
        val cursor = rawQuery("SELECT * FROM " + DBInfo.TEXT_BOOKMARK_TABLE + " WHERE " + where + " ORDER BY _id ASC")
        if (cursor != null && cursor.count > 0) {
            cursor.use { c ->
                c.moveToFirst()
                val loadData = TextItemData()
                loadData.mId = DBUtils.obtainLong(c, DBInfo._ID)
                loadData.mPath = DBUtils.obtainString(c, DBInfo._PATH)
                loadData.mPageNum = DBUtils.obtainInt(c, DBInfo._PAGE)
                loadData.mHighLine = DBUtils.obtainInt(c, DBInfo.TEXT_HIDE_LINE) //Log.e(TAG,"KDS3393_TEST_bookmarkload = " + loadData);
                return loadData
            }
        }
        cursor?.close()
        return null
    }

    //모든 Text Item 정보 가져오기
    val textList: ArrayList<TextItemData>
        get() {
            val wbList = ArrayList<TextItemData>()
            val cursor = rawQuery("SELECT * FROM " + DBInfo.TEXT_BOOKMARK_TABLE)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val data = TextItemData()
                    data.mId = DBUtils.obtainLong(cursor, DBInfo._ID)
                    data.mPath = DBUtils.obtainString(cursor, DBInfo._PATH)
                    data.mPageNum = DBUtils.obtainInt(cursor, DBInfo._PAGE)
                    data.mHighLine = DBUtils.obtainInt(cursor, DBInfo.TEXT_HIDE_LINE)
                    wbList.add(data)
                }
                cursor.close()
            }
            return wbList
        }

    //------------------------------------------------------- Favorite Table -------------------------------------------------------------
    //Favorite 정보 Insert
    fun insertFavoriteData(data: FileData) {
        val values = ContentValues()
        values.put(DBInfo._PATH, data.mPath)
        values.put(DBInfo.FAVO_TYPE, data.mType)
        values.put(DBInfo.FAVO_NAME, data.mDisplayName)
        values.put(DBInfo.FAVO_ORDER_INDEX, data.mOrderIndex)
        values.put(DBInfo.FAVO_ID, data.mNetId)
        values.put(DBInfo.FAVO_PASS, data.mNetPass)
        provider.insertDB(DBInfo.FAVO_BOOKMARK_TABLE, values)
    }

    fun deleteFavoriteData(id: Long) {
        deleteItem(DBInfo.FAVO_BOOKMARK_TABLE, id)
    }

    //Favorite 정보 업데이트
    fun updateFavoriteData(data: FileData) {
        val values = ContentValues()
        values.put(DBInfo._PATH, data.mPath)
        values.put(DBInfo.FAVO_TYPE, data.mType)
        values.put(DBInfo.FAVO_NAME, data.mDisplayName)
        values.put(DBInfo.FAVO_ORDER_INDEX, data.mOrderIndex)
        values.put(DBInfo.FAVO_ID, data.mNetId)
        values.put(DBInfo.FAVO_PASS, data.mNetPass)
        provider.updateDB(DBInfo.FAVO_BOOKMARK_TABLE, values, DBInfo._ID + " = " + data.mId)
    }

    //Favorite 가져오기
    fun loadFavorite(path: String?): FileData? {
        if (path == null) return null

        val where = DBInfo._PATH + "=\"" + path + "\""
        val cursor = rawQuery("SELECT * FROM " + DBInfo.FAVO_BOOKMARK_TABLE + " WHERE " + where + " ORDER BY _id ASC")
        if (cursor != null && cursor.count > 0) {
            val loadData = FileData()
            try {
                cursor.moveToFirst()
                loadData.mId = DBUtils.obtainLong(cursor, DBInfo._ID)
                loadData.mType = DBUtils.obtainInt(cursor, DBInfo.FAVO_TYPE)
                loadData.mPath = DBUtils.obtainString(cursor, DBInfo._PATH)
                if (loadData.mType == FileData.TYPE_LOCAL_DIR && loadData.mPath.isNotEmpty()) {
                    loadData.mIsDirectory = File(loadData.mPath).isDirectory
                }
                loadData.mDisplayName = DBUtils.obtainString(cursor, DBInfo.FAVO_NAME)
                loadData.mNetId = DBUtils.obtainString(cursor, DBInfo.FAVO_ID)
                loadData.mNetPass = DBUtils.obtainString(cursor, DBInfo.FAVO_PASS)
                loadData.mOrderIndex = DBUtils.obtainInt(cursor, DBInfo.FAVO_ORDER_INDEX)
            } finally {
                cursor.close()
            }
            return loadData
        }
        cursor?.close()
        return null
    }

    fun getFileFavoriteList(parentPath: String?): HashMap<String, Boolean> {
        val map = HashMap<String, Boolean>()
        if (parentPath.isNullOrEmpty()) return map

        val where = " WHERE " + DBInfo.FAVO_TYPE + " = " + FileData.TYPE_LOCAL_FILE + " AND " + DBInfo.FAVO_NAME + " = \"" + parentPath + "\""
        val cursor = rawQuery("SELECT * FROM " + DBInfo.FAVO_BOOKMARK_TABLE + where + " ORDER BY " + DBInfo.FAVO_ORDER_INDEX + " ASC")
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = DBUtils.obtainString(cursor, DBInfo._PATH) //                Log.e(TAG,"KDS3393_TEST_getFavoriteList data.mPath = " + path);
                map[path] = true
            }
            cursor.close()
        }
        return map
    }

    //Favorite 정보 가져오기
    fun getFavoriteList(type: Int): ArrayList<FileData> {
        val wbList = ArrayList<FileData>()
        val where = " WHERE " + DBInfo.FAVO_TYPE + " = " + type
        val cursor = rawQuery("SELECT * FROM " + DBInfo.FAVO_BOOKMARK_TABLE + where + " ORDER BY " + DBInfo.FAVO_ORDER_INDEX + " ASC")
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val data = FileData()
                data.mId = DBUtils.obtainLong(cursor, DBInfo._ID)
                data.mType = DBUtils.obtainInt(cursor, DBInfo.FAVO_TYPE)
                data.mPath = DBUtils.obtainString(cursor, DBInfo._PATH)
                if (data.mType == FileData.TYPE_LOCAL_DIR && data.mPath.isNotEmpty()) {
                    data.mIsDirectory = File(data.mPath).isDirectory
                }
                data.mDisplayName = DBUtils.obtainString(cursor, DBInfo.FAVO_NAME)
                data.mNetId = DBUtils.obtainString(cursor, DBInfo.FAVO_ID)
                data.mNetPass = DBUtils.obtainString(cursor, DBInfo.FAVO_PASS)
                data.mOrderIndex = DBUtils.obtainInt(cursor, DBInfo.FAVO_ORDER_INDEX) //                Log.e(TAG,"KDS3393_TEST_getFavoriteList data.mPath = " + data.mPath);
                wbList.add(data)
            }
            cursor.close()
        }
        return wbList
    }

    companion object {
        private const val TAG = "DBMgr"

        @Volatile
        private var mInstance: DBMgr? = null
        val instance: DBMgr
            get() {
                if (mInstance == null) {
                    synchronized(DBMgr::class.java) {
                        if (mInstance == null) {
                            mInstance = DBMgr(CApp.get())
                        }
                    }
                }
                return mInstance!!
            }
        val dBHelper: DBHelper
            get() = instance.provider.dbHelper
    }
}