package com.kds3393.just.viewer.provider;

/**
 * Created by android on 2016-09-13.
 */
public class DBInfo {
    public static String DB_NAME = "justviewer.db";
    public static final int DATABASE_VERSION = 1;

    public static final String IMAGE_FAVORITE_TABLE = "favorite_zip";

    public static final String _ID = "_id";

    public static final String FAVORITE_PATH = "favorite_path";
    public static final String FAVORITE_TYPE = "view_type";

    protected static final String IMAGE_FAVORITE_TABLE_CREATE =
            " CREATE TABLE " + IMAGE_FAVORITE_TABLE + " ( " +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FAVORITE_PATH + " TEXT NOT NULL," +
                    FAVORITE_TYPE + " INTEGER NOT NULL " +
                    " ) ";


    //bookmark 정보 저장
    public static final String BOOKMARK_TABLE = "bookmark_table";

    public static final String BOOKMARK_ID = "_id";
    public static final String BOOKMARK_PATH = "path";
    public static final String BOOKMARK_PAGE = "page";
    public static final String BOOKMARK_ISLEFT = "Page_Number";
    public static final String BOOKMARK_ZOOM = "zoom_type";
    public static final String BOOKMARK_IMAGE_STANDARD_HEIGHT = "standard_height";

    public static final String BOOKMARK_TABLE_CREATE =
            " CREATE TABLE " + BOOKMARK_TABLE + " ( " +
                    BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BOOKMARK_PATH + 				" TEXT NOT NULL," +
                    BOOKMARK_PAGE + 				" INTEGER, " +
                    BOOKMARK_ISLEFT + 			" INTEGER, " +
                    BOOKMARK_ZOOM + 				" INTEGER, " +
                    BOOKMARK_IMAGE_STANDARD_HEIGHT + 	    " INTEGER " +
                    " ) ";

    public static final String TEXT_BOOKMARK_TABLE = "txt_bookmark_table";
    public static final String TEXT_BOOKMARK_TABLE_CREATE =
            " CREATE TABLE " + TEXT_BOOKMARK_TABLE + " ( " +
                    BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BOOKMARK_PATH + 				" TEXT NOT NULL," +
                    BOOKMARK_PAGE + 				" INTEGER, " +
                    BOOKMARK_ISLEFT + 			" INTEGER, " +
                    BOOKMARK_ZOOM + 				" INTEGER, " +
                    BOOKMARK_IMAGE_STANDARD_HEIGHT + 	    " INTEGER " +
                    " ) ";
}
