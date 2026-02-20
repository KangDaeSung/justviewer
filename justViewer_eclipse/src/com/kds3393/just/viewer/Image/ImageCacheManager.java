package com.kds3393.just.viewer.Image;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import com.kds3393.just.viewer.Image.ImageDownloader.DownloadedDrawable;
import com.common.utils.debug.CLog;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

public class ImageCacheManager {
	private static final String TAG = "ImageCacheManager";
	public HashMap<ZipArchiveEntry, Bitmap> mCache = new HashMap<ZipArchiveEntry, Bitmap>();
	public HashMap<Integer, Bitmap> mThumbCache = new HashMap<Integer, Bitmap>();
	public static HashMap<ZipArchiveEntry, View> mViewCache = new HashMap<ZipArchiveEntry, View>();
	
	public boolean put(ZipArchiveEntry key, View view, Bitmap value, boolean mutex) {
		mCache.put(key, value);
		mViewCache.put(key, view);
		return true;
	}
	
	public int size() {
		return mCache.size();
	}
	
	public Iterator<ZipArchiveEntry> iterator(){
		return mCache.keySet().iterator();
	}

	public void remove(ZipArchiveEntry key, boolean mutex) {
		mCache.remove(key);
		View view = mViewCache.get(key);
		if (view != null && view.getBackground() != null && !(view.getBackground() instanceof DownloadedDrawable)) {
			if (((ZipArchiveEntry)view.getTag()).getName().equalsIgnoreCase(key.getName()))
				view.setBackground(null);
			if (view instanceof PageView) {
				CLog.e(TAG, "KDS3393_view remove index = " + ((PageView)view).mIndex + " :: " + view.getBackground());
			}
		}
	}
	
	public void clear() {
		mCache.clear();
		mViewCache.clear();
		mThumbCache.clear();
	}
}
