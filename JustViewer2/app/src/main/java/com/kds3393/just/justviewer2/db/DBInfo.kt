package com.kds3393.just.justviewer2.db

object DBInfo {
    private const val TAG = "DBInfo"
    var DB_PATH: String? = null
    var DB_NAME = "jv1.db"
    const val DATABASE_VERSION = 1
    const val _ID = "_id"
    const val _PATH = "path"

    //즐겨찾기 Table
    const val FAVO_TYPE = "_type"
    const val FAVO_NAME = "_name"
    const val FAVO_ID = "net_id"
    const val FAVO_PASS = "net_pw"
    const val FAVO_ORDER_INDEX = "_index"
    const val FAVO_BOOKMARK_TABLE = "favo_table"
    const val FAVO_BOOKMARK_TABLE_CREATE = " CREATE TABLE " + FAVO_BOOKMARK_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVO_TYPE + " INTEGER NOT NULL," + _PATH + " TEXT NOT NULL," + FAVO_NAME + " TEXT NOT NULL, " + FAVO_ID + " TEXT, " + FAVO_PASS + " TEXT, " + FAVO_ORDER_INDEX + " INTEGER " + " ) "

    //ImageViewer 값 저장
    const val _PAGE = "page" //현재 페이지
    const val IS_LEFT = "is_left" //ImageViewer : 1 - left, 0 - right //MoviePlayer : 1 - portrait, 0 - landscape
    const val ZOOM_TYPE = "zoom_type"
    const val ZOOM_HEIGHT = "zoom_height"
    const val IMAGE_BOOKMARK_TABLE = "image_bookmark_table"
    const val IMAGE_BOOKMARK_TABLE_CREATE = " CREATE TABLE " + IMAGE_BOOKMARK_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + _PATH + " TEXT NOT NULL," + _PAGE + " INTEGER, " + IS_LEFT + " INTEGER, " + ZOOM_TYPE + " INTEGER, " + ZOOM_HEIGHT + " INTEGER " + " ) "

    //TextViewer 값 저장
    const val TEXT_HIDE_LINE = "hide_line"
    const val TEXT_BOOKMARK_TABLE = "txt_bookmark_table"
    const val TEXT_BOOKMARK_TABLE_CREATE = " CREATE TABLE " + TEXT_BOOKMARK_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + _PATH + " TEXT NOT NULL," + _PAGE + " INTEGER, " + TEXT_HIDE_LINE + " INTEGER " + " ) "
}