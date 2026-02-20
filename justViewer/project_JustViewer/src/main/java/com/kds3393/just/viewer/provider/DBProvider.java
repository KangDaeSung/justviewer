package com.kds3393.just.viewer.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.common.utils.debug.CLog;

/**
 * Created by android on 2016-09-13.
 */
public class DBProvider {
    private static final String TAG = "DBProvider";

    private SQLiteDatabase m_db;
    private static DBHelper m_dbHelper;

    public DBProvider(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        m_dbHelper = new DBHelper(context, context.getDatabasePath(DBInfo.DB_NAME).getPath(), null, DBInfo.DATABASE_VERSION);
        m_db = m_dbHelper.getWritableDatabase();
    }

    public class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(DBInfo.IMAGE_FAVORITE_TABLE_CREATE);
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                CLog.e(TAG, e);
            } finally {
                db.endTransaction();
            }

            db.beginTransaction();
            try {
                db.execSQL(DBInfo.BOOKMARK_TABLE_CREATE);
                db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                CLog.e(TAG, e);
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public DBHelper getDBHelper() {
        return m_dbHelper;
    }

    public boolean isOpened() {
        if (m_db == null) {
            return false;
        }
        return m_db.isOpen();
    }

    public void close() {
        if (m_db != null) {
            m_db.close();
        }
    }

    /**
     * @brief DB에 data insert
     * @param strTableName
     * @param values
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertDB(String strTableName, ContentValues values) {
        if (isOpened()) {
            return m_db.insert(strTableName, null, values);
        }
        return -1;
    }

    /**
     * @brief DB에 data update
     * @param strTableName
     * @param values
     * @param strWhere
     * @return
     */
    public int updateDB(String strTableName, ContentValues values, String strWhere) {
        if (isOpened()) {
            Log.i(TAG, "DB UPDATE : " + "strTableName= " + strTableName + values.toString() + " where=" + strWhere);
            return m_db.update(strTableName, values, strWhere, null);
        }
        return -1;
    }

    /**
     * @brief DB data delete
     * @param strTableName
     * @param strWhere
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
     *         To remove all rows and get a count pass "1" as the whereClause.
     */
    public long deleteDB(String strTableName, String strWhere) {
        if (strWhere == null) {
            strWhere = "";
        }
        if (isOpened()) {
            Log.i(TAG, "DB DELETE : " + "strTableName= " + strTableName + " where=" + strWhere);
            return m_db.delete(strTableName, strWhere, null);
        }
        return -1;
    }

    /**
     * @brief DB에서 원하는 Field를 query 한다.
     * @param strTable
     * @param strSelect
     * @param strWhere
     * @return
     */
    public Cursor getDBData(String strTable, String[] strSelect, String strWhere) {
        if (isOpened()) {
            Cursor cursor = m_db.query(strTable, strSelect, strWhere, null, null, null, null);
            return cursor;
        }
        return null;
    }

    /**
     * @brief DB에서 원하는 Field를 정렬 query 한다.
     * @param strTable
     * @param strSelect
     * @param strWhere
     * @return
     */
    public Cursor getDBData(String strTable, String[] strSelect, String strWhere, String orderby) {
        if (isOpened()) {
            Cursor cursor = m_db.query(strTable, strSelect, strWhere, null, null, null, orderby);
            return cursor;
        }

        return null;
    }

    /**
     * @brief rawQuery 실행
     * @param strQuery
     * @return
     */
    public Cursor rawQuery(String strQuery) {
        if (isOpened()) {
            Cursor cursor = m_db.rawQuery(strQuery, null);
            return cursor;
        }
        return null;
    }

    /**
     * @brief execSQL 실행
     * @param strQuery
     * @return
     */
    public void execSQL(String strQuery) {
        if (isOpened()) {
            m_db.execSQL(strQuery);
        }
    }

    /**
     * @brief 테이블 존재여부 검사
     * @param strTableName
     *            검사할 테이블 명
     * @return 존재여부 (true : 존재, false : 존재하지 않음)
     */
    public boolean isExistTable(String strTableName) {
        if (isOpened()) {
            boolean bRet = false;
            String temp = "SELECT name FROM sqlite_master WHERE name='" + strTableName + "'";
            Cursor cursor = m_db.rawQuery(temp, null);

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    bRet = true;
                }
                cursor.close();
            }

            return bRet;
        }
        return false;
    }

    public boolean isExistData(String strTableName, String idFieldName, int id) {
        if (isOpened()) {
            boolean bRet = false;
            String temp = "SELECT * FROM " + strTableName + " WHERE " + idFieldName + "='" + id + "'";
            Cursor cursor = m_db.rawQuery(temp, null);

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    bRet = true;
                }
                cursor.close();
            }

            return bRet;
        }
        return false;
    }

    public boolean isExistData(String strTableName, String idFieldName, int id, String userfieldName, String userid) {
        if (isOpened()) {
            boolean bRet = false;
            String temp = "SELECT * FROM " + strTableName + " WHERE " + idFieldName + "='" + id + "' AND " + userfieldName + "='" + userid + "'";
            Cursor cursor = m_db.rawQuery(temp, null);

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    bRet = true;
                }
                cursor.close();
            }

            return bRet;
        }
        return false;
    }

    public void startTransaction() {
        if (isOpened()) {
            m_db.beginTransaction();
        }
    }

    public void transactionSuccess() {
        if (isOpened()) {
            m_db.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (isOpened()) {
            m_db.endTransaction();
        }
    }

    public SQLiteStatement compileStatement(String sql) {
        return m_db.compileStatement(sql);
    }
}
