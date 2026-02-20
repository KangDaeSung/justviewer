package com.kds3393.just.viewer.Config;

import java.util.ArrayList;

import android.os.Environment;

public class KConfig {
	public static final int TYPE_FAVORITE = -1;
	public static final int TYPE_IMAGE = 0;
	public static final int TYPE_MOVIE = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_MUSIC = 3;
	public static final int TYPE_BROWSER = 4;
	
	
	public static String PATH_SDCARD_ROOT = Environment.getExternalStorageDirectory().getParent();
	public static String PATH_TEMP;
	public static String PATH_LOG = Environment.getExternalStorageDirectory() + "/log";
	
	
	// ---------------------------- Setting --------------------------------------------
	
	// ImageViewer
	
	
	public static int ZOOM_HARF_SCREEN = 0;
	public static int ZOOM_FIT_HEIGHT = 1;
	public static int ZOOM_FIT_SCREEN = 2;
	public static int ZOOM_USER_CUSTOM = 3;
	public static int cZoomLevel = ZOOM_HARF_SCREEN;  //zoom Type
	public static int cStandardHeight;
	
}
