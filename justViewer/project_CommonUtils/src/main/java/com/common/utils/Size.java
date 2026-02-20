package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

public class Size {
    public static final int S720X1280X2 = 1; // 갤럭시 노트1,2, 갤럭시 S3
    public static final int S800X1280X1 = 2; // 갤럭시 탭 10.1, 갤럭시 탭 8.9
    public static final int S800X1280X1_7 = 3; // 갤럭시 노트8.0
    public static final int S800X1232X1 = 4; // 갤럭시 노트 10.1(하단 Bar가 있는 case)
    public static final int S1080X1920X3 = 5;    //갤럭시 S4
    public static final int S1600X2560X2 = 6;    //갤럭시 노트 10.1 신규 버전
    public static int ScreenType = S720X1280X2;
    
	private static final String TAG = "Size";
	public static int DisplayWidth;
	public static int DisplayHeight;
	public static float Density;
	public static int mSwipeMinDistance = 0;
	public static int mSwipeMinVelocity = 0;
	public int Width;
	public int Height;
	
	public Size() {
		Width = 0;
		Height = 0;
	}
	
	public Size(int width, int height) {
		Width = width;
		Height = height;
	}
	
	public Size(Bitmap bitmap) {
		if (bitmap != null) {
			Width = (int) (bitmap.getWidth());
			Height = (int) (bitmap.getHeight());
		} else {
			Log.e(TAG,"Error : Bitmap is Null");
			Width = 0;
			Height = 0;
		}
	}
	
	public String toString() {
		return "Size:Width[" + Width + "] Height[" + Height + "]";
	}
	
	public static Size getDisplaySize(Activity activity) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return new Size(displayMetrics.widthPixels,displayMetrics.heightPixels);
	}
	
	public static float getDensity(Activity activity) {
		DisplayMetrics metrics = new DisplayMetrics(); 
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics); 
		return metrics.density;
	}
	
	public static void initDensity(Activity activity) {
		Density = getDensity(activity);
	}
	
	public static void InitScreenSize(Activity activity) {
		Size size = getDisplaySize(activity);
		initDensity(activity);
		
		int orientation = activity.getRequestedOrientation();
		if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			if (size.Width > size.Height) {
				int temp = size.Width;
				size.Width = size.Height;
				size.Height = temp;
			}
		} else if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			if (size.Width < size.Height) {
				int temp = size.Width;
				size.Width = size.Height;
				size.Height = temp;
			}
		}
		
		DisplayWidth = size.Width;
		DisplayHeight = size.Height;
		ScreenType = setScreenType();
		Log.d(TAG,"Info : ScreenSize = " + DisplayWidth + " X " + DisplayHeight + " Density = " + Density + " : orientation = " + orientation);
	}
	
	public static void InitScreenSize(Context context, boolean isPortrait, boolean isSensor) {
		Activity activity = (Activity) context;
		if (isSensor) {
			if (isPortrait) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}
		} else {
			if (isPortrait) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		}
		InitScreenSize(activity);
	}
	
	private static int setScreenType() {
		if (((DisplayWidth == 720 && DisplayHeight == 1280) ||
				(DisplayWidth == 1280 && DisplayHeight == 720) ||
				(DisplayWidth == 800 && DisplayHeight == 1280) ||
				(DisplayWidth == 1280 && DisplayHeight == 800))
				 && Density == 2) {
			return S720X1280X2;
		} else if (((DisplayWidth == 800 && DisplayHeight == 1280) ||
				(DisplayWidth == 800 && DisplayHeight == 1232) ||
				(DisplayWidth == 1280 && DisplayHeight == 800) ||
				(DisplayWidth == 1280 && DisplayHeight == 752))
				&& Density == 1) {
			return S800X1280X1;
		} else if (((DisplayWidth == 1080 && DisplayHeight == 1920) ||
				(DisplayWidth == 1920 && DisplayHeight == 1080))
				&& Density == 3) {
			return S1080X1920X3;
		} else if ((DisplayWidth == 1600 && DisplayHeight == 2560) ||
				(DisplayWidth == 2560 && DisplayHeight == 1600)
				&& Density == 2) {
			return S1600X2560X2;
		} else if (((DisplayWidth == 800 && DisplayHeight == 1280) ||
				(DisplayWidth == 800 && DisplayHeight == 1232))
				&& Density < 1.7) {
			return S800X1280X1_7;
		} else if (DisplayWidth == 1440 && DisplayHeight == 2560 ||
				DisplayWidth == 2560 && DisplayHeight == 1440) {
			return S1600X2560X2;
		}
		return S800X1280X1;
	}
}
