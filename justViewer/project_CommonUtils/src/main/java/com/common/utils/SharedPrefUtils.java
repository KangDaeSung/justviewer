package com.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefUtils {
	private static final String TAG = "PreferencesUtils";
	
	public static void setPref(Context context, String tag, String key, String value) {
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(key, value);
		prefEditor.commit();
	}
	
	public static void setPref(Context context, String tag, String key, int value) {
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putInt(key, value);
		prefEditor.commit();
	}
	
	public static void setPref(Context context, String tag, String key, boolean value) {
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putBoolean(key, value);
		prefEditor.commit();
	}
	
	public static void setPref(Context context, String tag, String key, float value) {
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putFloat(key, value);
		prefEditor.commit();
	}
	
	public static String getPref(Context context, String tag, String key, String def) {
		String returnValue;
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		returnValue = pref.getString(key, def);
		return returnValue;
	}
	
	public static int getPref(Context context, String tag, String key, int def) {
		int returnValue = -1;
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		returnValue = pref.getInt(key, def);
		return returnValue;
	}
	
	public static boolean getPref(Context context, String tag, String key, boolean def) {
		boolean returnValue = false;
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		returnValue = pref.getBoolean(key, def);
		return returnValue;
	}
	
	public static float getPref(Context context, String tag, String key, float def) {
		float returnValue = -1;
		SharedPreferences pref = null;
		pref = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
		returnValue = pref.getFloat(key, def);
		return returnValue;
	}
}
