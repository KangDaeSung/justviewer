package common.lib.utils;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import common.lib.debug.CLog;

public class SysUtils {
	private static final String TAG = "SystemInfoUtils";
	
	public static int getConnectedNetwork(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (wifiNetwork != null && wifiNetwork.isConnectedOrConnecting())
			return ConnectivityManager.TYPE_WIFI;
		else if (mobileNetwork != null && mobileNetwork.isConnectedOrConnecting())
			return ConnectivityManager.TYPE_MOBILE;
		else
			return -1;
	}
	
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if ((mobile != null && mobile.isConnected()) || (wifi != null && wifi.isConnected())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Unique id 생성
	 */
	public static String getUniqueId(Context context) {
		String deviceId = "";
		try {
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			
			final String tmDevice, tmSerial, androidId;
			androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			
			UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
			deviceId = deviceUuid.toString();
			return deviceId;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
		return deviceId;
	}
	
	public static int getAppVersionCode(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
		return -1;
	}
	
	public static String getAppVersion(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
		return "";
	}
	
	public static String getDeviceId(Context context) {
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tManager.getDeviceId();
	}
	
	public static int getSimState(Context context) {
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tManager.getSimState();
	}
	
	public static String getMCC(Context context) {
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tManager.getNetworkCountryIso();
	}
	
	public static String getMCCMNC(Context context) {
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tManager.getNetworkOperator();
	}
	
	public static boolean isPortrait(Context context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int deviceWidth = displayMetrics.widthPixels;
		int deviceHeight = displayMetrics.heightPixels;
		if (deviceHeight > deviceWidth) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isDevicePhone(Context context) {
		if (getRealScreenInch(context) < 7)
			return true;
		return false;
	}
	
	public static double getRealScreenInch(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		return Math.sqrt(x + y);
	}
	
	public static boolean isInstalledApplication(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			CLog.e(TAG, e);
			return false;
		}
		return true;
	}
	
	private static final String VERSIONCHECK_REGULAR_EXPRESSION = "[0-9]+(.([0-9])+)*";
	public static Integer versionCompare(String str1, String str2) {
		
		String[] vals1 = isValidVersion(str1).split("\\.");
		String[] vals2 = isValidVersion(str2).split("\\.");
		int i = 0;
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		return Integer.signum(vals1.length - vals2.length);
	}
	
	private static String isValidVersion(String s) {
		String result = s;
		if (!s.matches(VERSIONCHECK_REGULAR_EXPRESSION)) {
			result = "0";
		}
		return result;
	}
}
