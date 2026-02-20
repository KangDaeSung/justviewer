package com.kds3393.just.justviewer2.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import common.lib.debug.CLog

class DBProvider(context: Context?, path: String?, factory: CursorFactory?, version: Int) {
    val db: SQLiteDatabase
    var dbHelper: DBHelper
    init {
        dbHelper = DBHelper(context, path + "/" + DBInfo.DB_NAME, null, DBInfo.DATABASE_VERSION)
        db = dbHelper.writableDatabase
    }

    inner class DBHelper internal constructor(context: Context?, name: String?, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(db: SQLiteDatabase) {
            CLog.e("KDS3393_TEST_DB_onCreate")
            db.beginTransaction()
            try {
                db.execSQL(DBInfo.TEXT_BOOKMARK_TABLE_CREATE)
                db.execSQL(DBInfo.FAVO_BOOKMARK_TABLE_CREATE)
                db.execSQL(DBInfo.IMAGE_BOOKMARK_TABLE_CREATE)
                db.setTransactionSuccessful()
            } catch (e: SQLiteException) {
                CLog.e(e)
            } finally {
                db.endTransaction()
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.FAVO_BOOKMARK_TABLE)
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.IMAGE_BOOKMARK_TABLE)
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TEXT_BOOKMARK_TABLE)
            onCreate(db)
        }
    }

    val isOpened: Boolean
        get() = if (db == null) {
            false
        } else db.isOpen

    fun close() {
        if (db != null) {
            db.close()
        }
    }

    /**
     * @brief DB에 data insert
     * @param strTableName
     * @param values
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    fun insertDB(strTableName: String, values: ContentValues?): Long {
        return if (isOpened) {
            db.insert(strTableName, null, values)
        } else -1
    }

    /**
     * @brief DB에 data update
     * @param strTableName
     * @param values
     * @param strWhere
     * @return
     */
    fun updateDB(strTableName: String, values: ContentValues, strWhere: String): Int {
        if (isOpened) {
            CLog.i("DB UPDATE : strTableName = $strTableName$values where = $strWhere")
            return db.update(strTableName, values, strWhere, null)
        }
        return -1
    }

    /**
     * @brief DB data delete
     * @param strTableName
     * @param strWhere
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     * To remove all rows and get a count pass "1" as the whereClause.
     */
    fun deleteDB(strTableName: String, strWhere: String?): Long {
        var strWhere = strWhere
        if (strWhere == null) {
            strWhere = ""
        }
        if (isOpened) {
            CLog.i("DB DELETE : strTableName= $strTableName where=$strWhere")
            return db.delete(strTableName, strWhere, null).toLong()
        }
        return -1
    }

    /**
     * @brief DB에서 원하는 Field를 query 한다.
     * @param strTable
     * @param strSelect
     * @param strWhere
     * @return
     */
    fun getDBData(strTable: String, strSelect: Array<String?>?, strWhere: String?): Cursor? {
        return if (isOpened) {
            db.query(strTable, strSelect, strWhere, null, null, null, null)
        } else null
    }

    /**
     * @brief DB에서 원하는 Field를 정렬 query 한다.
     * @param strTable
     * @param strSelect
     * @param strWhere
     * @return
     */
    fun getDBData(strTable: String, strSelect: Array<String?>?, strWhere: String?, orderby: String?): Cursor? {
        return if (isOpened) {
            db.query(strTable, strSelect, strWhere, null, null, null, orderby)
        } else null
    }

    /**
     * @brief rawQuery 실행
     * @param strQuery
     * @return
     */
    fun rawQuery(strQuery: String): Cursor? {
        return if (isOpened) {
            db.rawQuery(strQuery, null)
        } else null
    }

    /**
     * @brief execSQL 실행
     * @param strQuery
     * @return
     */
    fun execSQL(strQuery: String?) {
        if (isOpened) {
            db.execSQL(strQuery)
        }
    }

    /**
     * @brief 테이블 존재여부 검사
     * @param strTableName
     * 검사할 테이블 명
     * @return 존재여부 (true : 존재, false : 존재하지 않음)
     */
    fun isExistTable(strTableName: String): Boolean {
        if (isOpened) {
            var bRet = false
            val temp = "SELECT name FROM sqlite_master WHERE name='$strTableName'"
            val cursor = db.rawQuery(temp, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    bRet = true
                }
                cursor.close()
            }
            return bRet
        }
        return false
    }

    fun isExistData(strTableName: String, idFieldName: String, id: Int): Boolean {
        if (isOpened) {
            var bRet = false
            val temp = "SELECT * FROM $strTableName WHERE $idFieldName='$id'"
            val cursor = db.rawQuery(temp, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    bRet = true
                }
                cursor.close()
            }
            return bRet
        }
        return false
    }

    fun isExistData(strTableName: String, idFieldName: String, id: Int, userfieldName: String, userid: String): Boolean {
        if (isOpened) {
            var bRet = false
            val temp = "SELECT * FROM $strTableName WHERE $idFieldName='$id' AND $userfieldName='$userid'"
            val cursor = db.rawQuery(temp, null)
            if (cursor != null) {
                if (cursor.count > 0) {
                    bRet = true
                }
                cursor.close()
            }
            return bRet
        }
        return false
    }

    fun startTransaction() {
        if (isOpened) {
            db.beginTransaction()
        }
    }

    fun transactionSuccess() {
        if (isOpened) {
            db.setTransactionSuccessful()
        }
    }

    fun endTransaction() {
        if (isOpened) {
            db.endTransaction()
        }
    }

    fun compileStatement(sql: String?): SQLiteStatement {
        return db.compileStatement(sql)
    }
}