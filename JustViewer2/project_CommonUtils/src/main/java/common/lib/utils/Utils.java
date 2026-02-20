package common.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AndroidException;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import common.lib.debug.CLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
	public static boolean isNotDeveloped = false;
	private static final String TAG = "Utils";
	public static final String KEY_IS_FIRST = "isFirst";
	
	public static class ContextNullException extends AndroidException {
		private static final long serialVersionUID = -7745709505071042910L;
		
		public ContextNullException() {
			super();
		}
	}
	
	/**
	 * 가상 키보드를 숨긴다.
	 */
	public static void hideKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static int dp2px(float dp)  {
		Resources resources = Resources.getSystem();
		float px = dp * resources.getDisplayMetrics().density;
		return (int) Math.ceil(px);
	}

	/**
	 * 가상 키보드를 보여준다.
	 */
	public static void showKeyboard(Activity act, View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager)act.getSystemService(Activity.INPUT_METHOD_SERVICE);
			view.requestFocus();
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	/**
	 * 현재 날짜를 보여준다. (yyyyMMddHHmmssSS)
	 */
	public static String getTodayDateFormat() {
		return getTodayDateFormat("yyyyMMddHHmmssSS");
	}
	
	/**
	 * 현재 날짜를 보여준다.
	 * 
	 * @param format string format (yyyyMMddHHmmssSS)
	 */
	public static String getTodayDateFormat(String format) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdfNow = new SimpleDateFormat(format);
		return sdfNow.format(date);
	}
	
	/**
	 * 인자로 넘겨준 ms 값을 기준으로 날짜와 시간값을 반환한다.
	 * 
	 * @param timeMs 시간 ms
	 * @param format string format (yyyyMMddHHmmssSS)
	 */
	public static String getStringForDate(long timeMs, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dd = new Date(timeMs);
		return sdf.format(dd);
	}
	
	/**
	 * 스트링을 int로 변환 숫자형 문자가 아닐 경우에 대한 예외처리가 되어 있다.
	 * 
	 * @param def 변환 실패시 반환 값
	 */
	public static int parseInt(String value, int def) {
		int result = def;
		try {
			if (value != null && value.length() > 0) {
				if (value.lastIndexOf('.') < 0)
					result = Integer.parseInt(value);
				else
					result = (int) Float.parseFloat(value);
			}
			
		} catch (Exception e) {
			result = def;
			CLog.e(TAG, e);
		}
		getStringForDate(1, "");
		return result;
	}
	
	/**
	 * 스트링을 long로 변환 숫자형 문자가 아닐 경우에 대한 예외처리가 되어 있다.
	 * 
	 * @param def 변환 실패시 반환 값
	 */
	public static long parseLong(String value, long def) {
		long result = def;
		try {
			if (value != null && value.length() > 0)
				result = Long.parseLong(value);
		} catch (Exception e) {
			result = def;
			CLog.e(TAG, e);
		}
		return result;
	}
	
	/**
	 * 스트링을 float로 변환 숫자형 문자가 아닐 경우에 대한 예외처리가 되어 있다.
	 * 
	 * @return 실패시 0을 반환
	 */
	public static float parseFloat(String value) {
		return parseFloat(value, 0);
	}
	
	/**
	 * 스트링을 float로 변환 숫자형 문자가 아닐 경우에 대한 예외처리가 되어 있다.
	 * 
	 * @param def 변환 실패시 반환 값
	 */
	public static float parseFloat(String value, float def) {
		float result = def;
		try {
			if (value != null && value.length() > 0)
				result = Float.parseFloat(value);
		} catch (Exception e) {
			result = def;
			CLog.e(TAG, e);
		}
		return result;
	}
	
	/**
	 * 스트링을 double로 변환 숫자형 문자가 아닐 경우에 대한 예외처리가 되어 있다.
	 * 
	 * @return 실패시 0을 반환
	 */
	public static double parseDouble(String value) {
		double result = 0.0f;
		try {
			if (value != null && value.length() > 0)
				result = Double.parseDouble(value);
		} catch (Exception e) {
			result = 0.0;
			CLog.e(TAG, e);
		}
		return result;
	}
}