package com.kds3393.just.viewer.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class Provider extends ContentProvider{

    private static final String TAG = "Provider";

    public static final String AUTHORITY = "com.kds3393.just.viewer.provider";
    
    public static final String IMAGE_ZIP_ID = "_id";
    public static final String IMAGE_ZIP_PATH = "path";
    public static final String IMAGE_ZIP_PAGE = "page";
    public static final String IMAGE_ZIP_ISLEFT = "Page_Number";
    public static final String IMAGE_ZIP_ZOOM = "zoom_type";
    public static final String IMAGE_STANDARD_HEIGHT = "standard_height";
    
    public static final String IMAGE_FAVORITE_ID = "_id";
    public static final String IMAGE_FAVORITE_PATH = "favorite_path";
    public static final String IMAGE_FAVORITE_TYPE = "view_type";
    
    public static final Uri IMAGE_ZIP_URI = Uri.parse("content://" + AUTHORITY + "/" + MainTable.IMAGE_ZIP_TABLE);
    public static final Uri IMAGE_FAVORITE_URI = Uri.parse("content://" + AUTHORITY + "/" + MainTable.IMAGE_FAVORITE_TABLE);
    
    private static final String DATABASE_NAME = "viewer_manager";
    private static final int DATABASE_VERSION = 3;
    private static final String IMAGE_ZIP_TABLE_CREATE =
        " CREATE TABLE " + MainTable.IMAGE_ZIP_TABLE + " ( " +
        		IMAGE_ZIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        		IMAGE_ZIP_PATH + 				" TEXT NOT NULL," +
        		IMAGE_ZIP_PAGE + 				" INTEGER, " +
        		IMAGE_ZIP_ISLEFT + 			" INTEGER, " +
        		IMAGE_ZIP_ZOOM + 				" INTEGER, " +
        		IMAGE_STANDARD_HEIGHT + 	" INTEGER " +
        " ) ";
    
    private static final String IMAGE_FAVORITE_TABLE_CREATE =
            " CREATE TABLE " + MainTable.IMAGE_FAVORITE_TABLE + " ( " +
            		IMAGE_ZIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            		IMAGE_FAVORITE_PATH + 			" TEXT NOT NULL," +
            		IMAGE_FAVORITE_TYPE + 			" INTEGER NOT NULL " +
            " ) ";
    private static final int IMAGE_ZIP_FULL_QUERY = 1;
    private static final int IMAGE_ZIP_ZIP_PATH_SEARCH = 2;
    private static final int IMAGE_FAVORITE_FULL_QUERY = 3;
    private static final int IMAGE_FAVORITE_VIEW_TYPE_SEARCH = 4;
    
    private static final String DATABASE_DROP_IMAGE = " DROP TABLE IF EXISTS " + MainTable.IMAGE_ZIP_TABLE;
    private static final String DATABASE_DROP_FAVORITE = " DROP TABLE IF EXISTS " + MainTable.IMAGE_ZIP_TABLE;
    private static final UriMatcher uriMatcher;

    private SQLiteDatabase mViewerDB;
    
    public static final class MainTable implements BaseColumns {
        private MainTable() {}
        public static final String IMAGE_ZIP_TABLE = "image_zip";
        public static final String IMAGE_FAVORITE_TABLE = "favorite_zip";
        
        public static final Uri CONTENT_ZIP_URI =  Uri.parse("content://" + AUTHORITY + "/" + IMAGE_ZIP_TABLE);
        public static final Uri CONTENT_ZI_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/" + IMAGE_ZIP_TABLE +"/");
        public static final String ZIP_DEFAULT_SORT_ORDER = IMAGE_ZIP_PAGE + " COLLATE LOCALIZED DESC";
        public static final Uri CONTENT_FAVORITE_URI =  Uri.parse("content://" + AUTHORITY + "/" + IMAGE_FAVORITE_TABLE);
        public static final Uri CONTENT_FAVORITE_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/" + IMAGE_FAVORITE_TABLE +"/");
        public static final String FAVORITE_DEFAULT_SORT_ORDER = IMAGE_FAVORITE_TYPE + " COLLATE LOCALIZED DESC";
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MainTable.IMAGE_ZIP_TABLE, IMAGE_ZIP_FULL_QUERY);
        uriMatcher.addURI(AUTHORITY, MainTable.IMAGE_ZIP_TABLE + "/#", IMAGE_ZIP_ZIP_PATH_SEARCH);
        uriMatcher.addURI(AUTHORITY, MainTable.IMAGE_FAVORITE_TABLE, IMAGE_FAVORITE_FULL_QUERY);
        uriMatcher.addURI(AUTHORITY, MainTable.IMAGE_FAVORITE_TABLE + "/#", IMAGE_FAVORITE_VIEW_TYPE_SEARCH);

    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "DatabaseHelper";
       
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
       
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(IMAGE_ZIP_TABLE_CREATE);
            db.execSQL(IMAGE_FAVORITE_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("Upgrading database from version %d to %d which will destroy all old data", oldVersion, newVersion));
            if (oldVersion == 1) {
            	db.execSQL("ALTER TABLE " + MainTable.IMAGE_ZIP_TABLE + " ADD COLUMN " + IMAGE_ZIP_ZOOM + " INTEGER");
            	db.execSQL("ALTER TABLE " + MainTable.IMAGE_ZIP_TABLE + " ADD COLUMN " + IMAGE_STANDARD_HEIGHT + " INTEGER");
            } else if (oldVersion == 2) {
            	db.execSQL("ALTER TABLE " + MainTable.IMAGE_ZIP_TABLE + " ADD COLUMN " + IMAGE_STANDARD_HEIGHT + " INTEGER");
            }
        }
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete......START");
        if (selection != null)
        	Log.d(TAG, "delete selection = " + selection);
        
        int count = 0;
        if (uri == IMAGE_ZIP_URI)
        	count = mViewerDB.delete(MainTable.IMAGE_ZIP_TABLE, selection, selectionArgs);
        else if (uri == IMAGE_FAVORITE_URI) 
        	count = mViewerDB.delete(MainTable.IMAGE_FAVORITE_TABLE, selection, selectionArgs);
        
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "delete...END");
        return count;
    }
    
    @Override
    public String getType(Uri arg0) {
        Log.d(TAG, "getType");
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues value) {
        Log.d(TAG, "insert...START");
        long rowID = 0;
        if (uri == IMAGE_ZIP_URI)
            rowID = mViewerDB.insert(MainTable.IMAGE_ZIP_TABLE, "", value);
        else if (uri == IMAGE_FAVORITE_URI) 
        	rowID = mViewerDB.insert(MainTable.IMAGE_FAVORITE_TABLE, "", value);
        
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.d(TAG, "insert...END uri = " + uri);
            return _uri;
        }
        
        return null;
        //throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        mViewerDB = dbHelper.getWritableDatabase();
        return false;
    } 

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query...START uri = " + uri);
        
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        
        switch(uriMatcher.match(uri)) {
        case IMAGE_ZIP_FULL_QUERY:
            sqlBuilder.setTables(MainTable.IMAGE_ZIP_TABLE);
            break;
        case IMAGE_ZIP_ZIP_PATH_SEARCH:
            sqlBuilder.setTables(MainTable.IMAGE_ZIP_TABLE);
            sqlBuilder.appendWhere( String.format("%d = %d", IMAGE_ZIP_PATH, uri.getPathSegments().get(1)) );
            break;
        case IMAGE_FAVORITE_FULL_QUERY:
            sqlBuilder.setTables(MainTable.IMAGE_FAVORITE_TABLE);
        	break;
        case IMAGE_FAVORITE_VIEW_TYPE_SEARCH:
        	sqlBuilder.appendWhere( String.format("%d = %d", IMAGE_FAVORITE_PATH, uri.getPathSegments().get(1)) );
        	break;
        }
   
        
        Cursor c = sqlBuilder.query(mViewerDB, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "query...END");
       
        return c;
    }

   
    @Override
    public int update(Uri uri, ContentValues value, String arg2, String[] arg3) {
        Log.d(TAG, "update......uri = " + uri);
        long rowID = 0;
        if (uri == IMAGE_ZIP_URI)
            rowID = mViewerDB.update(MainTable.IMAGE_ZIP_TABLE, value, arg2, arg3);
        else if (uri == IMAGE_ZIP_URI) 
        	rowID = mViewerDB.update(MainTable.IMAGE_FAVORITE_TABLE, value, arg2, arg3);
        
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.d(TAG, "update...END uri = " + uri);
            return 1;
        }
        
        return 0;
    }
}
