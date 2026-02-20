package com.kds3393.just.viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Config.KConfig;
import com.kds3393.just.viewer.Config.SettingActivity;
import com.kds3393.just.viewer.Utils.ReceiverManager;

import java.io.File;
import java.io.FileOutputStream;

@SuppressLint("Registered")
public class ParentActivity extends Activity {
	private static final String TAG = "ParentActivity";
	private int mPrevTimeout = -1;
	protected ReceiverManager mReceiverManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initDeviceInfo();
		int timeout = SettingActivity.getScreenTimeOut(this);
		if (timeout > 0) {
			try {
				mPrevTimeout = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT);
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
			} catch (SettingNotFoundException e) {
				CLog.e(TAG, e);
			}
		} else {
			mPrevTimeout = -999;
		}
		mReceiverManager = new ReceiverManager();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		mReceiverManager.unRegister(this);
		super.onPause();
	}



	@Override
	protected void onStop() {
		if (mPrevTimeout != -999)
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mPrevTimeout);
		super.onStop();
	}
	
	private void initDeviceInfo() {
		Size.InitScreenSize(this);
		KConfig.PATH_TEMP = getExternalFilesDir(null) + "/temp/";
		File path = new File(KConfig.PATH_TEMP);
		if(!path.exists()) {
			path.mkdirs();
			File nomedia = new File(KConfig.PATH_TEMP + ".nomedia");
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(nomedia);
				fos.write(1);
				fos.close();
			} catch (Exception e) {
				CLog.e(TAG, e);
			}
		}
	}
}
