package com.kds3393.just.justviewer2.db

object DBInfo {
    var DB_PATH: String? = null
    var DB_NAME = "jv1.db"

    // 테이블 구조가 변경되었으므로 버전을 올려 기존 DB를 초기화합니다.
    const val DATABASE_VERSION = 3

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

    // 통합 책갈피 Table (이미지, 텍스트 공통)
    const val BOOKMARK_TABLE = "bookmark_table"

    // 책갈피 타입 구분용 (0: 이미지, 1: 텍스트)
    const val BOOK_TYPE = "book_type"

    // 공통/개별 데이터 컬럼
    const val _PAGE = "page" // 현재 페이지
    const val IS_LEFT = "is_left" // ImageViewer : 1 - left, 0 - right
    const val ZOOM_TYPE = "zoom_type"
    const val ZOOM_HEIGHT = "zoom_height"
    const val TEXT_HIDE_LINE = "hide_line" // TextViewer 전용

    // 하나의 테이블로 통합하여 생성
    const val BOOKMARK_TABLE_CREATE = " CREATE TABLE " + BOOKMARK_TABLE + " ( " +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            _PATH + " TEXT NOT NULL, " +
            BOOK_TYPE + " INTEGER NOT NULL, " +
            _PAGE + " INTEGER DEFAULT 0, " +
            IS_LEFT + " INTEGER DEFAULT 1, " +
            ZOOM_TYPE + " INTEGER DEFAULT 2, " +
            ZOOM_HEIGHT + " INTEGER DEFAULT 0, " +
            TEXT_HIDE_LINE + " INTEGER DEFAULT 0 " + " ) "
}