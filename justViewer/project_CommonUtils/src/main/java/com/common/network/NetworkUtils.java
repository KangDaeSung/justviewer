package com.common.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AndroidException;

import com.common.utils.Utils;
import com.common.utils.debug.CLog;

public class NetworkUtils {
	private static final String TAG = "NetworkUtils";
	
	public static final int NETWORK_DISCONNECTED = -100;
	public static final int NETWORK_CONNECTED_3G = 0;
	public static final int NETWORK_CONNECTED_WIFI = 1;
	public static final int NETWORK_CONNECTED_ETC = 2;
	
	public static boolean isWIFI(Context context) {
		context = Utils.checkContext(context);
		return NetworkStateCheck(context) == NETWORK_CONNECTED_WIFI;
	}
	
	public static boolean isMOBILE(Context context) {
		context = Utils.checkContext(context);
		return NetworkStateCheck(context) == NETWORK_CONNECTED_3G;
	}
	
	public static boolean isAirplaneMode(Context context) {
		int result;
		context = Utils.checkContext(context);
		try {
			result = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
			return result != 0;
		} catch (SettingNotFoundException e) {
			return false;
		}
	}
	
	public static boolean isRoaming(Context context) {
		context = Utils.checkContext(context);
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		
		return info.isRoaming();
	}
	
	public static boolean isConnected(Context context) {
		context = Utils.checkContext(context);
		boolean bRet = false;
		int status = NetworkStateCheck(context);
		if (status == NETWORK_CONNECTED_WIFI || status == NETWORK_CONNECTED_3G) {
			bRet = true;
		}
		CLog.d(TAG, "isConnected: " + bRet + ", status = " + status);
		return bRet;
	}
	
	public static int NetworkStateCheck(Context context) {
		context = Utils.checkContext(context);
		
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return NETWORK_DISCONNECTED;
		}
		
		switch (info.getType()) {
		case ConnectivityManager.TYPE_WIFI:
			return NETWORK_CONNECTED_WIFI;
		case ConnectivityManager.TYPE_MOBILE:
			return NETWORK_CONNECTED_3G;
		default:
			return NETWORK_CONNECTED_ETC;
		}
	}
}
