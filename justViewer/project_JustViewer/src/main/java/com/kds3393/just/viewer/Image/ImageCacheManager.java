package com.kds3393.just.viewer.Image;

import android.graphics.Bitmap;
import android.view.View;

import com.common.utils.debug.CLog;
import com.kds3393.just.viewer.Image.ImageDownloader.DownloadedDrawable;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.tools.zip.ZipEntry;

public class ImageCacheManager {
	private static final String TAG = "ImageCacheManager";
	public HashMap<ZipEntry, Bitmap> mCache = new HashMap<ZipEntry, Bitmap>();
	public HashMap<Integer, Bitmap> mThumbCache = new HashMap<Integer, Bitmap>();
	public static HashMap<ZipEntry, View> mViewCache = new HashMap<ZipEntry, View>();
	
	public boolean put(ZipEntry key, View view, Bitmap value, boolean mutex) {
		mCache.put(key, value);
		mViewCache.put(key, view);
		return true;
	}
	
	public int size() {
		return mCache.size();
	}
	
	public Iterator<ZipEntry> iterator(){
		return mCache.keySet().iterator();
	}

	public void remove(ZipEntry key, boolean mutex) {
		mCache.remove(key);
		View view = mViewCache.get(key);
		if (view != null && view.getBackground() != null && !(view.getBackground() instanceof DownloadedDrawable)) {
			if (((ZipEntry)view.getTag()).getName().equalsIgnoreCase(key.getName()))
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
