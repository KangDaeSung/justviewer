package com.common.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import com.common.utils.SysUtils;
import com.common.utils.Utils;
import com.common.utils.debug.CLog;
import com.common.utils.debug.DevUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

@SuppressLint("Registered")
public class CApp extends Application {
	private static final String TAG = "CApp";
	
	private static CApp application;
	public static Context mAppContext = null;;
	
	UncaughtExceptionHandler mExceptionHandler;
	public static String LOG_PATH;
	
	public static int APP_VERSION_CODE = 0;	//버전 코드
	public static String APP_VERSION_NAME = "0.0.0"; // 버전 이름
	public static String APP_PACK_NAME; // 패키지명
	public static String APP_NAME; // 앱 이름
	
	public String mDeviceUniqueId = null;
	
	public static CApp getInstance() {
		return application;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mAppContext = getApplicationContext();
		LOG_PATH = getExternalFilesDir(null) + "/log";
		init();
	}
	
	protected void init() {
		mDeviceUniqueId = SysUtils.getUniqueId(this);
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			APP_VERSION_CODE = pinfo.versionCode;
			APP_VERSION_NAME = pinfo.versionName;
			APP_NAME = pinfo.applicationInfo.name;
		} catch (NameNotFoundException e) {
			CLog.e(TAG, e);
		}
		APP_PACK_NAME = getPackageName();
		LOG_PATH = Environment.getExternalStorageDirectory() + "/log";
	}
	
	public CApp() {
		super();
		application = this;
		mExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, final Throwable ex) {
				boolean isDebug = true;
				if (0 == (CApp.this.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
					isDebug = DevUtils.isDebuggable(CApp.this);
				}
				if (!isDebug) {
					// 파일로 로그 작성
					try {
						String today = Utils.getTodayDateFormat();
						String filename = LOG_PATH + "/Log_" + APP_NAME + "_" + today + ".txt";
						File file = new File(LOG_PATH);
						if (!file.exists()) {
							file.mkdirs();
						}
						StringWriter sw = new StringWriter();
						ex.printStackTrace(new PrintWriter(sw));
						String exceptionAsString = sw.toString();
						
						FileOutputStream fos;
						file = new File(filename);
						fos = new FileOutputStream(file);
						BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
						buw.write("id:" + CLog.sContentId + "\n");
						buw.write("url:" + CLog.sContentUrl + "\n");
						buw.write("page:" + CLog.sPageId + "\n");
						buw.write("date:" + Utils.getTodayDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초") + "\n");
						buw.write(exceptionAsString);
						buw.close();
						fos.close();
					} catch (Exception e) {
						CLog.e(TAG, e);
					}
				}
				mExceptionHandler.uncaughtException(thread, ex);
			}
		});
	}
	
	private static String APP_UUID = null;
	

	
	private static final String INI_FILENAME_CONFIG = "app_config";
	private static final String CONFIG_UUID = "config_uuid";
	
	private static String getUUID(Context _ctx) {
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		return pref.getString(CONFIG_UUID, null);
	}
	
	private static void setUUID(Context _ctx, String uuid) {
		SharedPreferences pref = _ctx.getSharedPreferences(INI_FILENAME_CONFIG, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(CONFIG_UUID, uuid);
		
		if (Build.VERSION.SDK_INT > 9) {
			editor.apply();
		} else {
			editor.commit();
		}
	}
}