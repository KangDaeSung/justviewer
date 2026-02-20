package com.kds3393.just.justviewer2.db

import android.database.Cursor

object DBUtils {
    //--------------------- Cursor에서 data 가져오기 -------------------------------
    //on[1]/off[0]
    fun obtainBool(cursor:Cursor, column:String, def:Boolean = true) : Boolean {
        val index = cursor.getColumnIndex(column)
        if (index >= 0) {
            return cursor.getInt(index) == 1
        }
        return def
    }

    fun obtainInt(cursor:Cursor, column:String, def:Int = -1) : Int {
        val index = cursor.getColumnIndex(column)
        if (index >= 0) {
            return cursor.getInt(index)
        }
        return def
    }

    fun obtainLong(cursor:Cursor, column:String, def:Long = -1) : Long {
        val index = cursor.getColumnIndex(column)
        if (index >= 0) {
            return cursor.getLong(index)
        }
        return def
    }

    fun obtainString(cursor:Cursor, column:String, def:String = "") : String {
        val index = cursor.getColumnIndex(column)
        if (index >= 0) {
            return cursor.getString(index)?:def
        }
        return def
    }
}