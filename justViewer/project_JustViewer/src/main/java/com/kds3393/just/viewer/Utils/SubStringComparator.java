package com.kds3393.just.viewer.Utils;

import org.apache.tools.zip.ZipEntry;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

public class SubStringComparator implements Comparator<Object> {
	private final Collator collator = Collator.getInstance();
	private boolean mIsASC = true;
	
	public SubStringComparator() {
		mIsASC = true;
	}
	
	public SubStringComparator(boolean isASC) {
		mIsASC = isASC;
	}
	@Override
	public int compare(Object obj1, Object obj2) {
		int maxLength = 0;
		String str1 = getComparatorStr(obj1);
		String str2 = getComparatorStr(obj2);
		if (str1.length() != str2.length()) {
			maxLength = Math.max(str1.length(),str2.length());
			if (str1.length()<str2.length()) {
				str1 = getFitStringLength(str1,str2,maxLength);
			} else {
				str2 = getFitStringLength(str2,str1,maxLength);
			}
		}
		if (mIsASC)
			return collator.compare(str1, str2);
		else
			return collator.compare(str2, str1);
	}
	
	private String getComparatorStr(Object obj) {
		if (obj instanceof File) {
			return ((File)obj).getName();
		} else if (obj instanceof ZipEntry) {
			return ((ZipEntry)obj).getName();
		} else if (obj instanceof String) {
			return (String)obj;
		}
		return "";
	}
	private String getFitStringLength(String str1, String str2, int max) {
		String temp = "";
		for (int i=0;i<max - str1.length();i++) {
			temp+="0";
		}
		
		ArrayList<String> str1Array = new ArrayList<String>();
		ArrayList<String> str2Array = new ArrayList<String>();
		parseString(str1,str1Array);
		parseString(str2,str2Array);
		
		str1 = "";
		boolean isPass = false;
		for (int i=0;i<str1Array.size();i++) {
			if (!isPass && str2Array.size() > i && str1Array.get(i).length() != str2Array.get(i).length()) {
				if (str1Array.get(i).charAt(0) >= '0' && str1Array.get(i).charAt(0) <= '9') {
					str1 = str1 + temp + str1Array.get(i);
					isPass = true;
					continue;
				}
			}
			str1 += str1Array.get(i);
		}
		return str1;
	}

	private boolean parseString(String str, ArrayList<String> strArray) {
		if (str.length() <= 0)
			return true;
		boolean isNum = false;
		if (str.charAt(0) >= '0' && str.charAt(0) <= '9') {
			isNum = true;
		}
		for (int i=0;i<str.length();i++) {
			if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
				if (!isNum) {
					strArray.add(str.substring(0, i));
					if (i+1 < str.length())
						parseString(str.substring(i, str.length()), strArray);
					return true;
				}
			} else {
				if (isNum) {
					strArray.add(str.substring(0, i));
					if (i+1 < str.length())
						parseString(str.substring(i, str.length()), strArray);
					return true;
				}
			}
		}
		strArray.add(str);
		return true;
	}
}
