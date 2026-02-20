package com.common.utils.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import com.common.app.CApp;
import com.common.utils.FileUtils;
import com.common.utils.Size;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class CLog {
	public static boolean SHOW_LOG = false; // false이면 로그 출력 안함
	
	public static void e(String TAG, String log) {
		if (SHOW_LOG)
			Log.e(TAG, log);
	}
	
	public static void v(String TAG, String log) {
		if (SHOW_LOG)
			Log.v(TAG, log);
	}
	
	public static void d(String tag, String log) {
		if (SHOW_LOG)
			Log.d(tag, log);
	}
	
	public static void i(String tag, String log) {
		if (SHOW_LOG)
			Log.i(tag, log);
	}
	
	public static void w(String tag, String log) {
		if (SHOW_LOG)
			Log.w(tag, log);
	}
	
	public static void SetUseLog(boolean isUseLog) {
		SHOW_LOG = isUseLog;
	}
	
	public static void e(String TAG, Exception e) {
		Log.e(TAG, "catch Error");
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		Log.e(TAG, exceptionAsString);
	}
	
	public static String getClassAddress(Object obj) {
		return obj.toString().substring(obj.toString().lastIndexOf("@") + 5);
	}
	
	public static void layout(String tag, String desc, View view) {
		e(tag, desc + " l = " + view.getLeft() + " r = " + view.getRight() + " t = " + view.getTop() + " b = " + view.getBottom());
	}
	
	private static final String DEFAULT_SERVER_ADDRESS = "http://14.63.171.15/exception_log_api/API/v1_0/exceptionLogUpload?";
	
	public static void sendLog(Context context, String url) {
		if (TextUtils.isEmpty(url)) {
			url = DEFAULT_SERVER_ADDRESS;
		}
		if (CApp.LOG_PATH != null) {
			String appPackName;
			int appVer = 0;
			String appVerName = "";
			int screenWidth;
			int screenHeight;
			
			appPackName = context.getPackageName();
			
			Size size = Size.getDisplaySize((Activity) context);
			screenWidth = size.Width;
			screenHeight = size.Height;
			
			PackageInfo pinfo;
			
			try {
				pinfo = context.getPackageManager().getPackageInfo(appPackName, 0);
				appVer = pinfo.versionCode;
				appVerName = pinfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			UpLoadAsyncTask task = new UpLoadAsyncTask(url);
			task.setData(appPackName, screenWidth, screenHeight, appVer, appVerName);
			task.execute();
		}
	}
	
	public static String sContentUrl = "";
	public static int sContentId = -1;
	public static String sPageId = "";
	
	private static class UpLoadAsyncTask extends AsyncTask<Object, Object, Boolean> {
		private String mLogFilePath;
		private String mUrl;
		
		protected String mAppPackName;
		protected int mScreenWidth;
		protected int mScreenHeight;
		protected int mAppVer = -1;
		protected String mAppVerName;
		
		public StringBuilder getNetworkDefaultPacket() {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("app_id=" + mAppPackName);
			strBuilder.append("&model=" + android.os.Build.MODEL);
			strBuilder.append("&os_ver=" + android.os.Build.VERSION.RELEASE);
			strBuilder.append("&lcd_size=" + mScreenWidth + "x" + mScreenHeight);
			strBuilder.append("&app_ver=" + mAppVer);
			strBuilder.append("&app_ver_name=" + mAppVerName);
			strBuilder.append("&is_debug=n");
			return strBuilder;
		}
		
		public void setData(String packName, int screenWidth, int screenHeight, int appVer, String appVerName) {
			mAppPackName = packName;
			mScreenWidth = screenWidth;
			mScreenHeight = screenHeight;
			mAppVer = appVer;
			mAppVerName = appVerName;
		}
		
		public UpLoadAsyncTask(String url) {
			mUrl = url;
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			List<File> lists = FileUtils.getDirFileList(CApp.LOG_PATH);
			if (lists == null)
				return true;
			String contentUrl = null;
			String contentId = null;
			String pageId = null;
			String date = null;
			for (File file : lists) {
				if (!FileUtils.getExtension(file.getPath()).equalsIgnoreCase("log")) {
					continue;
				}
				HttpURLConnection conn = null;
				try {
					URL url = new URL(mUrl);
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setConnectTimeout(2000);
					// conn.setAllowUserInteraction(true);
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					// conn.addRequestProperty("Content-Encoding", "gzip");
					conn.addRequestProperty("Accept", "application/json");
					StringBuilder strBuilder = getNetworkDefaultPacket();
					
					OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
					PrintWriter writer = new PrintWriter(outStream);
					
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(file);
						BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));
						String strLine = null;
						
						contentUrl = null;
						contentId = null;
						pageId = null;
						date = null;
						try {
							strLine = bufferReader.readLine();
							if (!TextUtils.isEmpty(strLine) && strLine.indexOf("id:") >= 0) {
								strLine = strLine.trim();
								contentId = strLine.substring(3, strLine.length());
								
							}
							
							strLine = bufferReader.readLine();
							if (!TextUtils.isEmpty(strLine) && strLine.indexOf("url:") >= 0) {
								strLine = strLine.trim();
								contentUrl = strLine.substring(4, strLine.length());
							}
							
							strLine = bufferReader.readLine();
							if (!TextUtils.isEmpty(strLine) && strLine.indexOf("page:") >= 0) {
								strLine = strLine.trim();
								pageId = strLine.substring(5, strLine.length());
							}
							
							strLine = bufferReader.readLine();
							if (!TextUtils.isEmpty(strLine) && strLine.indexOf("date:") >= 0) {
								strLine = strLine.trim();
								date = strLine.substring(5, strLine.length());
							}
							strBuilder.append("&content_id=" + contentId);
							strBuilder.append("&content_url=" + contentUrl);
							strBuilder.append("&page_id=" + pageId);
							strBuilder.append("&date=" + pageId);
						} catch (Exception e1) {
						}
						
						String exceptionType = null;
						while ((strLine = bufferReader.readLine()) != null) {
							if (strLine.isEmpty())
								continue;
							if (exceptionType == null) {
								exceptionType = strLine;
								try {
									String split[] = exceptionType.split(" ");
									String s = split[0].substring(split[0].lastIndexOf(".") + 1, split[0].length());
									String log = "";
									for (int i = 0; i < s.length(); i++) {
										String c = "" + s.charAt(i);
										if (Pattern.matches("^[a-zA-Z]+$", c)) {
											log += c;
										}
									}
									strBuilder.append("&exception_log_type=" + log);
								} catch (Exception e2) {
								}
								strBuilder.append("&exception_log=");
							}
							strBuilder.append(strLine + "\n");
						}
						Log.e("log", "send_log_stat = " + mUrl + strBuilder.toString());
						bufferReader.close();
						fis.close();
					} catch (Exception e) {
						CLog.e("log", e);
					}
					writer.write(strBuilder.toString());
					writer.flush();
					writer.close();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					StringBuilder builder = new StringBuilder();
					String str;
					while ((str = reader.readLine()) != null) { // 서버에서 라인단위로
																// 보내줄 것이므로
																// 라인단위로 읽는다
						builder.append(str + "\n"); // View에 표시하기 위해 라인 구분자 추가
					}
					Log.e("log", "log_stat = " + builder.toString());
					reader.close();
					
					JSONObject jsonObj = new JSONObject(builder.toString());
					
					if (jsonObj.getInt("returnCode") != 0)
						return false;
					FileUtils.deleteFile(file.getPath());
				} catch (SocketTimeoutException e) {
					Log.e("log", "Error : SocketTimeoutException");
					return false;
				} catch (ConnectTimeoutException e) {
					Log.e("log", "Error : ConnectTimeoutException");
					return false;
				} catch (Exception e) {
					CLog.e("log", e);
					if (conn != null) {
						conn.disconnect();
						conn = null;
					}
					return false;
				}
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				FileUtils.deleteFile(mLogFilePath);
			}
		}
	}
}
