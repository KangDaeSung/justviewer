package com.kds3393.just.justviewer2.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import common.lib.utils.Size;
import common.lib.utils.Utils;

/**
 * @brief	프레프런스 설정관련 도우미 클래스
 * @author  PiaJang
 */
public class SharedPrefHelper {
	public final static String INI_FILENAME_CONFIG = "config";
	
	public final static String CONFIG_IMAGE_PAGE_TYPE = "image_page_type";

	public final static String CONFIG_IMAGE_VIEW_RIGHT_X = "image_view_right_x";
	public final static String CONFIG_IMAGE_VIEW_RIGHT_Y = "image_view_right_y";
	public final static String CONFIG_IMAGE_VIEW_LEFT_X = "image_view_left_x";
	public final static String CONFIG_IMAGE_VIEW_LEFT_Y = "image_view_left_y";
	
	public final static String CONFIG_IMAGE_VIEW_RIGHT_WIDTH = "image_view_right_width";
	public final static String CONFIG_IMAGE_VIEW_RIGHT_HEIGHT = "image_view_right_height";
	public final static String CONFIG_IMAGE_VIEW_LEFT_WIDTH = "image_view_left_width";
	public final static String CONFIG_IMAGE_VIEW_LEFT_HEIGHT = "image_view_left_height";

	public static void setImagePageType(Context _ctx, boolean isRight){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putBoolean(CONFIG_IMAGE_PAGE_TYPE, isRight);
		edit.commit();
	}
	
	public static boolean getImagePageType(Context _ctx) {
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getBoolean(CONFIG_IMAGE_PAGE_TYPE, true);
	}
	
	public static void setImageRightBtnPoint(Context _ctx, int x, int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_X, x);
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_Y, y);
		edit.commit();
	}
	
	public static Point getImageRightBtnPoint(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Point point = new Point();
		point.x = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_X, 100);
		point.y = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_Y, 500);
		return point;
	}
	
	
	public static void setImageLeftBtnPoint(Context _ctx, int x, int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_X, x);
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_Y, y);
		edit.commit();
	}
	
	public static Point getImageLeftBtnPoint(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Point point = new Point();
		point.x = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_X, Utils.dp2px(250f));
		point.y = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_Y, Utils.dp2px(250f));
		return point;
	}
	
	public static void setImageRightBtnSize(Context _ctx, int x, int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_WIDTH, x);
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_HEIGHT, y);
		edit.commit();
	}
	
	public static Size getImageRightBtnSize(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Size size = new Size();
		size.Width = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_WIDTH, Utils.dp2px(75f));
		size.Height = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_HEIGHT, Utils.dp2px(75f));
		if (size.Width == 0 || size.Height == 0) {
			size.Width = Utils.dp2px(75f);
			size.Height = Utils.dp2px(75f);
		}
		return size;
	}
	
	
	public static void setImageLeftBtnSize(Context _ctx, int x, int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_WIDTH, x);
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_HEIGHT, y);
		edit.commit();
	}
	
	public static Size getImageLeftBtnSize(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Size size = new Size();
		size.Width = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_WIDTH, Utils.dp2px(75f));
		size.Height = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_HEIGHT, Utils.dp2px(75f));
		if (size.Width == 0 || size.Height == 0) {
			size.Width = Utils.dp2px(75f);
			size.Height = Utils.dp2px(75f);
		}
		return size;
	}
}
