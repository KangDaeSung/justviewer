package com.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.text.TextUtils;

import com.common.utils.debug.CLog;

public class SharedBackup {
	private static final String TAG = "SharedBackup";
	
	public static final void putInt(String path, int value) {
		File file;
		file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		
		file = new File(path);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
			buw.write(String.valueOf(value));
			buw.close();
			fos.close();
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
	}
	
	public static final int getInt(String path, int value) {
		FileInputStream fis;
		if (FileUtils.fileIsLive(path)) {
			try {
				fis = new FileInputStream(path);
				BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
				String str = bufferReader.readLine();
				return Utils.parseInt(str, value);
			} catch (Exception e) {
				CLog.e(TAG, e);
			}
		}
		return value;
	}
}
