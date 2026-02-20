package com.kds3393.just.viewer.Movie;

import android.text.TextUtils;

import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Utils.CUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SmiParser {
	private static final String TAG = "SmiParser";
	
	private static SmiParser mInstance = null;
	public static SmiParser getInstance() {
		if (mInstance == null)
			mInstance = new SmiParser();
		return mInstance;
	}
	
	private SmiParser(){}
	
	public ArrayList<ArrayList<SmiData>> parser(String path) {
		path = path.substring(0, path.lastIndexOf("."));
		CLog.e(TAG, "path = " + path);
		ArrayList<ArrayList<SmiData>> smiArrays = null;
		if (TextUtils.isEmpty(path))
			return null;
		File file = new File(path + ".smi");
		if (file.exists() && file.isFile() && file.canRead()) {
			ArrayList<SmiData> smi = null;
			try {
				String charset = CUtils.detectEncoding(file);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(file),charset));
				
        		String s;
        	    long time = -1;
        	    String text = null;
        	    String pTag = null;
        	    String classType = null;
        	    boolean smistart = false;
        	    
        	    while ((s = in.readLine()) != null) {
        	    	s = s.replace("&nbsp;", "");
        	    	if(s.contains("<SYNC") || s.contains("<Sync")) {
        	    		smistart = true;
        	    		if(time != -1 && smi != null) {
        	    			smi.add(new SmiData(time, text));
        	    		}
        	    		try {
        	    			time = Integer.parseInt(s.substring(s.indexOf("=")+1, s.indexOf(">")));
        	    		} catch (NumberFormatException e) {
        	    			continue;
        	    		}
        	    		pTag = s.substring(s.indexOf(">")+1, s.length());
        	    		if (TextUtils.isEmpty(pTag)) {
        	    			if ((s = in.readLine()) != null)
        	    				pTag = s.substring(s.indexOf("<"), s.indexOf(">") + 1);
        	    			else
        	    				break;
        	    		}
        	    		String lang = pTag.substring(pTag.indexOf("=")+1, pTag.indexOf(">"));
        	    		if (lang.equalsIgnoreCase("KRCC") ||
        	    				lang.equalsIgnoreCase("ENCC") ||
        	    				lang.equalsIgnoreCase("JPCC") ||
        	    				lang.equalsIgnoreCase("SUBTTL")) {//오타 방지
        	    			if (classType == null || !classType.equalsIgnoreCase(lang)) { // 언어가 다른 2개이상의 자막이 있을 경우
            	    			classType = lang;
            	    			if (smiArrays == null) {
            	    				smiArrays = new ArrayList<ArrayList<SmiData>>();
            	    			}
            	    			
//            	    			if (smi != null && smi.size() > 0) {
//           	    				for (SmiData data:smi)
//           	    					CLog.e(TAG, "smi " + classType + " Time = " + data.time + " text = " + data.text);
//           	    				CLog.e(TAG, "---------------------------------------------------------------");
//            	    			}
            	    			
            	    			smi = new ArrayList<SmiData>();
            	    			smiArrays.add(smi);
            	    		}
        	    		}
        	    		
        	    		
        	    		text = s.substring(s.indexOf(">")+1, s.length());
        	    	} else {
        	    		if(smistart == true) {
        	    			text += s;
        	    		}
        	    	}
        	    }
        	    
//        	    for (SmiData data:smi)
//					CLog.e(TAG, "smi " + classType + " Time = " + data.time + " text = " + data.text);
        	    in.close();
			} catch (Exception e) {
				CLog.e(TAG, e);
			}
			
		} else {
			return null;
		}
		return smiArrays;
	}
	
	public class SmiData {
		long time;
		String text;
		public SmiData(long time, String text) {
			this.time = time;
			this.text = text;
		}
		
		public long gettime() {
			return time;
		}
		
		public String gettext() {
			return text;
		}
	}
	
	
}
