package com.kds3393.just.justviewer2.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.kds3393.just.justviewer2.CApp
import com.kds3393.just.justviewer2.data.BookmarkData
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.utils.Event
import common.lib.utils.SharedBus
import java.io.File
import kotlin.collections.set

class DBMgr {
    var provider: DBProvider
    private constructor(context: Context) {
        if (DBInfo.DB_PATH == null) {
            val dbFile = context.getDatabasePath(DBInfo.DB_NAME)
            DBInfo.DB_PATH = dbFile.parent
        }
        provider = DBProvider(context, DBInfo.DB_PATH)
    }

    fun rawQuery(strQuery: String): Cursor? {
        return provider.rawQuery(strQuery)
    }

    fun deleteItem(table: String, id: Long) {
        provider.deleteDB(table, DBInfo._ID + " = \"" + id + "\"")
    }

    @Suppress("unused")
    fun clearMgr() {
        provider.close()
        mInstance = null
    }

    @Suppress("unused")
    fun deleteAll(table: String) {
        provider.deleteDB(table, null)
    }

    //------------------------------------------------------- 통합 책갈피 (이미지 & 텍스트) -------------------------------------------------------------
    fun insertBookmark(data: BookmarkData): Long {
        val values = ContentValues()
        values.put(DBInfo._PATH, data.targetPath)
        values.put(DBInfo.BOOK_TYPE, data.bookType)
        values.put(DBInfo._PAGE, data.currentPage)
        values.put(DBInfo.IS_LEFT, data.isLeft)
        values.put(DBInfo.ZOOM_TYPE, data.zoomType)
        values.put(DBInfo.ZOOM_HEIGHT, data.zoomStandardHeight)
        values.put(DBInfo.TEXT_HIDE_LINE, data.hideLine)
        val result = provider.insertDB(DBInfo.BOOKMARK_TABLE, values)
        data.id = result
        SharedBus.post(Event.Bookmark(data, isAdd = true))
        return result
    }

    fun updateBookmark(data: BookmarkData) {
        val bookId = if (data.id > 0) {
            data.id
        } else {
            val cursor = rawQuery("SELECT ${DBInfo._ID} FROM ${DBInfo.BOOKMARK_TABLE} WHERE ${DBInfo._PATH}=\"${data.targetPath}\" AND ${DBInfo.BOOK_TYPE}=${data.bookType} ORDER BY _id ASC")
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                val id = DBUtils.obtainLong(cursor, DBInfo._ID)
                cursor.close()
                id
            } else {
                cursor?.close()
                -1L
            }
        }

        if (bookId > 0) {
            val values = ContentValues()
            values.put(DBInfo._PATH, data.targetPath)
            values.put(DBInfo.BOOK_TYPE, data.bookType)
            values.put(DBInfo._PAGE, data.currentPage)
            values.put(DBInfo.IS_LEFT, data.isLeft)
            values.put(DBInfo.ZOOM_TYPE, data.zoomType)
            values.put(DBInfo.ZOOM_HEIGHT, data.zoomStandardHeight)
            values.put(DBInfo.TEXT_HIDE_LINE, data.hideLine)
            provider.updateDB(DBInfo.BOOKMARK_TABLE, values, DBInfo._ID + " = " + bookId)
            data.id = bookId
        } else {
            insertBookmark(data)
        }
    }

    fun loadBookmark(path: String, type: Int): BookmarkData? {
        val where = DBInfo._PATH + "=\"" + path + "\" AND " + DBInfo.BOOK_TYPE + "=" + type
        val cursor = rawQuery("SELECT * FROM " + DBInfo.BOOKMARK_TABLE + " WHERE " + where + " ORDER BY _id ASC")
        if (cursor != null && cursor.count > 0) {
            cursor.use { c ->
                c.moveToFirst()
                val data = BookmarkData(DBUtils.obtainString(c, DBInfo._PATH), type)
                data.id = DBUtils.obtainLong(c, DBInfo._ID)
                data.currentPage = DBUtils.obtainInt(c, DBInfo._PAGE)
                data.isLeft = DBUtils.obtainInt(c, DBInfo.IS_LEFT)
                data.zoomType = DBUtils.obtainInt(c, DBInfo.ZOOM_TYPE)
                data.zoomStandardHeight = DBUtils.obtainInt(c, DBInfo.ZOOM_HEIGHT)
                data.hideLine = DBUtils.obtainInt(c, DBInfo.TEXT_HIDE_LINE)
                return data
            }
        }
        cursor?.close()
        return null
    }

    fun getBookmarkList(type: Int = -1): ArrayList<BookmarkData> {
        val wbList = ArrayList<BookmarkData>()
        val where = if (type >= 0) " WHERE " + DBInfo.BOOK_TYPE + "=" + type else ""

        val cursor = rawQuery("SELECT * FROM " + DBInfo.BOOKMARK_TABLE + where)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val data = BookmarkData(DBUtils.obtainString(cursor, DBInfo._PATH), type)
                data.id = DBUtils.obtainLong(cursor, DBInfo._ID)
                data.currentPage = DBUtils.obtainInt(cursor, DBInfo._PAGE)
                data.isLeft = DBUtils.obtainInt(cursor, DBInfo.IS_LEFT)
                data.zoomType = DBUtils.obtainInt(cursor, DBInfo.ZOOM_TYPE)
                data.zoomStandardHeight = DBUtils.obtainInt(cursor, DBInfo.ZOOM_HEIGHT)
                data.hideLine = DBUtils.obtainInt(cursor, DBInfo.TEXT_HIDE_LINE)
                wbList.add(data)
            }
            cursor.close()
        }
        return wbList
    }

    //------------------------------------------------------- Favorite Table (유지) -------------------------------------------------------------
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

    @Suppress("unused")
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
                val path = DBUtils.obtainString(cursor, DBInfo._PATH)
                map[path] = true
            }
            cursor.close()
        }
        return map
    }

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
                data.mOrderIndex = DBUtils.obtainInt(cursor, DBInfo.FAVO_ORDER_INDEX)
                wbList.add(data)
            }
            cursor.close()
        }
        return wbList
    }

    companion object {
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
    }
}