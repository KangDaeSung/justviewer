package com.common.utils.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.text.TextUtils;

/**
 * Debug json exemple
 * {
 *     "log":["yes"|"no"],
 *     "server":["dev"|"stg"|"real"],
 *     "serverurl":"url"
 * }
 */
public class DebugOptions {
	private static final String TAG = "DebugOptions";
	private static DebugOptions mInstance;
	
	private static final String DEBUG_JSON_FILE_NAME = "kwave.json";
	
	private static final String JSON_TAG_LOG = "log";
	private static final String JSON_TAG_SERVER = "server";
	private static final String JSON_TAG_SERVER_URL = "serverurl";
	
	String SERVER_TARGET[] = { "dev", "stg", "real" };
	
	private int mServer = 2;
	private String mSeverUrl = null;;
	
	public static DebugOptions getInstance() {
		if (mInstance == null) {
			mInstance = new DebugOptions();
		}
		return mInstance;
	}
	
	private DebugOptions() {
	}
	
	public int getServerTarget() {
		return mServer;
	}
	
	public String getServerUrl() {
		return mSeverUrl;
	}
	
	public void loadOptions() {
		loadOptions(Environment.getExternalStorageDirectory() + "/" + DEBUG_JSON_FILE_NAME);
	}
	
	private void init() {
		//CLog.SHOW_LOG = false;
		mServer = 2;
		mSeverUrl = null;
	}
	
	public void loadOptions(String filePath) {
		File debugFile = new File(filePath);
		if (debugFile.exists() == true) {
			FileInputStream input = null;
			byte[] inputBytes = new byte[1024];
			try {
				input = new FileInputStream(debugFile);
				
				if (-1 != input.read(inputBytes)) {
					String json = new String(inputBytes);
					parseOptions(json);
				}
			} catch (FileNotFoundException e) {
				CLog.e(TAG, e);
			} catch (IOException e) {
				CLog.e(TAG, e);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						CLog.e(TAG, e);
					}
				}
			}
		} else {
			init();
		}
	}
	
	private void parseOptions(String json) {
		try {
			JSONObject jsonObj = new JSONObject(json);
			setLog(jsonObj);
			setServerTarget(jsonObj);
			setServerUrl(jsonObj);
		} catch (JSONException e) {
			CLog.e(TAG, e);
		}
	}
	
	private void setLog(JSONObject jsonObj) {
		try {
			if (!jsonObj.isNull(JSON_TAG_LOG)) {
				String log = jsonObj.getString(JSON_TAG_LOG);
				if (log.equalsIgnoreCase("yes")) {
					CLog.SHOW_LOG = true;
				} else {
					//CLog.SHOW_LOG = false;
				}
			}
		} catch (JSONException e) {
			CLog.e(TAG, e);
		}
	}
	
	private void setServerTarget(JSONObject jsonObj) {
		try {
			if (!jsonObj.isNull(JSON_TAG_SERVER) && TextUtils.isEmpty(mSeverUrl)) {
				String server = jsonObj.getString(JSON_TAG_SERVER);
				for (int i = 0; i < SERVER_TARGET.length; i++) {
					if (server.equalsIgnoreCase(SERVER_TARGET[i])) {
						mServer = i;
						return;
					}
				}
			}
		} catch (JSONException e) {
			CLog.e(TAG, e);
		}
	}
	
	private void setServerUrl(JSONObject jsonObj) {
		try {
			if (!jsonObj.isNull(JSON_TAG_SERVER_URL)) {
				mSeverUrl = jsonObj.getString(JSON_TAG_SERVER_URL);
				mServer = -1;
			}
		} catch (JSONException e) {
			CLog.e(TAG, e);
		}
	}
	
}
