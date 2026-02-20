package com.kds3393.just.viewer.Config;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import com.common.utils.ResManager;
import com.common.utils.Size;
import com.kds3393.just.viewer.R;

/**
 * @brief	프레프런스 설정관련 도우미 클래스
 * @author  PiaJang
 */
public class SharedPrefHelper {
	
	public final static String INI_FILENAME_CONFIG = "config";
	
	public final static String CONFIG_LAST_PATH = "last_path";
	public final static String CONFIG_LAST_VIEW = "last_view";
	
	
	public final static String CONFIG_IMAGE_PAGE_TYPE = "image_page_type";
	public final static String CONFIG_IMAGE_VIEW_PAGE = "image_view_page";
	public final static String CONFIG_IMAGE_VIEW_PATH = "image_view_path";
	
	public final static String CONFIG_IMAGE_VIEW_RIGHT_X = "image_view_right_x";
	public final static String CONFIG_IMAGE_VIEW_RIGHT_Y = "image_view_right_y";
	public final static String CONFIG_IMAGE_VIEW_LEFT_X = "image_view_left_x";
	public final static String CONFIG_IMAGE_VIEW_LEFT_Y = "image_view_left_y";
	
	public final static String CONFIG_IMAGE_VIEW_RIGHT_WIDTH = "image_view_right_width";
	public final static String CONFIG_IMAGE_VIEW_RIGHT_HEIGHT = "image_view_right_height";
	public final static String CONFIG_IMAGE_VIEW_LEFT_WIDTH = "image_view_left_width";
	public final static String CONFIG_IMAGE_VIEW_LEFT_HEIGHT = "image_view_left_height";

	
	
	
	
	public final static String CONFIG_IMAGE_ROOT = "image_root";
	public final static String CONFIG_MOVIE_ROOT = "movie_root";
	public final static String CONFIG_TEXT_ROOT = "text_root";
	public final static String CONFIG_MUSIC_ROOT = "music_root";
	public final static String CONFIG_BROWSER_ROOT = "browser_root";
	
	public static void setLastPath(Context _ctx, String path){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_LAST_PATH, path);
		edit.commit();
	}
	
	public static String getLastPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_LAST_PATH, "/");
	}
	
	public static void setLastView(Context _ctx, int viewType) {
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_LAST_VIEW, viewType);
		edit.commit();
	}
	
	public static int getLastView(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getInt(CONFIG_LAST_VIEW, KConfig.TYPE_FAVORITE);
	}
	
	//-------------------- Image
	public static void setImageRootPath(Context _ctx, String date){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_IMAGE_ROOT, date);
		edit.commit();
	}
	
	public static String getImageRootPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_IMAGE_ROOT, KConfig.PATH_SDCARD_ROOT);
	}
	
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
	
	public static void setImageRightBtnPoint(Context _ctx, int x,int y){
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
	
	
	public static void setImageLeftBtnPoint(Context _ctx, int x,int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_X, x);
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_Y, y);
		edit.commit();
	}
	
	public static Point getImageLeftBtnPoint(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Point point = new Point();
		point.x = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_X, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_point));
		point.y = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_Y, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_point));
		return point;
	}
	
	public static void setImageRightBtnSize(Context _ctx, int x,int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_WIDTH, x);
		edit.putInt(CONFIG_IMAGE_VIEW_RIGHT_HEIGHT, y);
		edit.commit();
	}
	
	public static Size getImageRightBtnSize(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Size size = new Size();
		size.Width = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_WIDTH, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size));
		size.Height = pref.getInt(CONFIG_IMAGE_VIEW_RIGHT_HEIGHT, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size));
		if (size.Width == 0 || size.Height == 0) {
			size.Width = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size);
			size.Height = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size);
		}
		return size;
	}
	
	
	public static void setImageLeftBtnSize(Context _ctx, int x,int y){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_WIDTH, x);
		edit.putInt(CONFIG_IMAGE_VIEW_LEFT_HEIGHT, y);
		edit.commit();
	}
	
	public static Size getImageLeftBtnSize(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		Size size = new Size();
		size.Width = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_WIDTH, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size));
		size.Height = pref.getInt(CONFIG_IMAGE_VIEW_LEFT_HEIGHT, ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size));
		if (size.Width == 0 || size.Height == 0) {
			size.Width = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size);
			size.Height = ResManager.getDimen(R.dimen.imageviewer_page_move_btn_default_size);
		}
		return size;
	}
	//-------------------- Movie
	public static void setMovieRootPath(Context _ctx, String date){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_MOVIE_ROOT, date);
		edit.commit();
	}
	
	public static String getMovieRootPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_MOVIE_ROOT, KConfig.PATH_SDCARD_ROOT);
	}
	
	//-------------------- Browser
	public static void setBrowserRootPath(Context _ctx, String date){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_BROWSER_ROOT, date);
		edit.commit();
	}
	
	public static String getBrowserRootPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_BROWSER_ROOT, KConfig.PATH_SDCARD_ROOT);
	}
	
	//-------------------- Music
	public static void setMusicRootPath(Context _ctx, String date){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_MUSIC_ROOT, date);
		edit.commit();
	}
	
	public static String getMusicRootPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_MUSIC_ROOT, KConfig.PATH_SDCARD_ROOT);
	}
	
	//-------------------- Text
	public static void setTextRootPath(Context _ctx, String date){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(CONFIG_TEXT_ROOT, date);
		edit.commit();
	}
	
	public static String getTextRootPath(Context _ctx){
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_TEXT_ROOT, KConfig.PATH_SDCARD_ROOT);
	}
}
