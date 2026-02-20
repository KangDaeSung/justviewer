package com.kds3393.just.viewer.Image;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import com.kds3393.just.viewer.Utils.FileExtract;

import com.common.utils.ImageUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageDownloader {
	private static final String TAG = "ImageStoreDownloader";
	public static final int IMGAE_CACHE_LIMIT_SIZE = 30;
	
	public static ImageCacheManager mImageCache = new ImageCacheManager();
	public static ArrayList<ZipArchiveEntry> sEntryArray;
	public static String sZipPath;
	public static final int IMAGE_URL_TYPE = 1;
	public static final int IMAGE_FILE_TYPE = 2;
	
	public static final int IMAGE_PATH_BOX_MAIN = 1;
	public static final int IMAGE_PATH_BOX_SUB = 2;
	public static final int IMAGE_PATH_IMAGE_MAIN = 3;
	public static final int IMAGE_PATH_IMAGE_SUB = 4;
	
	public static boolean mutex = false;
	
	public static final String TYPE_TOOLBAR_IMAGE = "Toolbar";
	public static final String TYPE_BACKGROUND_IMAGE = "Background";

	public static boolean isNext(int index) {
		if (sEntryArray.size() - 1 > index) {
			return true;
		}
		return false;
	}
	
	public static boolean isPrev(int index) {
		if (index <= 0) {
			return false;
		}
		return true;
	}
	
	public static Bitmap getBitmapInCache(String url) {
		if (url == null || url.isEmpty())
			return null;
		Bitmap cachedImage = mImageCache.mCache.get(url);
		if (cachedImage == null)
			return null;
		return cachedImage;
	}
	
	public static BitmapDrawable getBitmapDrawableInCache(String url) {
		if (url == null || url.isEmpty())
			return null;
		Bitmap cachedImage = mImageCache.mCache.get(url);
		if (cachedImage == null)
			return null;
		return new BitmapDrawable(cachedImage);
	}
	
	public static Size sParentViewSize = new Size();
	
	public static void preload(int index) {
		if (index < 0 || index >= sEntryArray.size())
			return;
		Bitmap bmp = FileExtract.unzipTargetImage(sZipPath,sEntryArray.get(index));
		ImageDownloader.mImageCache.put(sEntryArray.get(index), null, bmp,true);
	}
	
	public static void download(Context context, int viewId, int index, View view, boolean isASync, OnImageDownloadCompleteListener listener) {
		ZipArchiveEntry key = sEntryArray.get(index);
		if (key == null)
			return;
		
		Bitmap cachedImage = mImageCache.mCache.get(key);
		
		if(cachedImage != null) {
			if (view != null) {
				float scale = ImageDownloaderTask.getScale(cachedImage);
				int w = (int)(cachedImage.getWidth() * scale);
				int h = (int)(cachedImage.getHeight() * scale);
				if (viewId >= 0)
					((PageView)view).setImageSize(w,h);
				view.setTag(key);
				view.setBackground(new BitmapDrawable(cachedImage));
				if (listener != null) {
					listener.onComplete(cachedImage, viewId, index);
				}
			}
		} else if(cancelPotentialDownload(key, view)) {
			if (isASync) {
				ImageDownloaderTask task = null;
				task = new ImageDownloaderTask(sZipPath, viewId,key, view,listener);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
				if (view != null) {
					view.setBackground(downloadedDrawable);
				}
				task.execute();
			} else {

			}
		}
	}
	
	public static void downloadThumbnail(Context context, int index, View view, boolean isASync, int maxWidth, int maxHeight) {
		ZipArchiveEntry key = sEntryArray.get(index);
		if (key == null)
			return;
		
		Bitmap cachedImage = mImageCache.mThumbCache.get(index);
		
		if(cachedImage != null) {
			if (view != null) {
				view.setTag(key);
				((ImageView)view).setImageBitmap(cachedImage);
			}
		} else if(cancelPotentialDownload(key, view)) {
			if (isASync) {
				ImageDownloaderTask task = null;
				task = new ImageDownloaderTask(sZipPath, index, key, view,maxWidth,maxHeight);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
				if (view != null) {
					view.setBackground(downloadedDrawable);
				}
				task.execute();
			} else {

			}
		}
	}
	
	public interface OnImageDownloadCompleteListener {
		public void onComplete(Bitmap bmp,int viewId, int index);
	}
	
	synchronized public static void doRecycle(int index) {
		if (!mutex) {
			Iterator<ZipArchiveEntry> iterator = mImageCache.iterator();
			ZipArchiveEntry prev = null;
			ZipArchiveEntry next = null;
			ZipArchiveEntry curr = sEntryArray.get(index);
			
			if (isPrev(index))
				prev = sEntryArray.get(index - 1);
			if (isNext(index))
				next = sEntryArray.get(index + 1);
			
			ArrayList<ZipArchiveEntry> removeArray = new ArrayList<ZipArchiveEntry>();
		    while (iterator.hasNext()) {
		    	ZipArchiveEntry key = (ZipArchiveEntry) iterator.next();
	        	if (mImageCache.mCache.get(key) != null) {
	        		if ((prev != null && !prev.getName().equalsIgnoreCase(key.getName())) &&
	        			(next != null && !next.getName().equalsIgnoreCase(key.getName())) &&
	        			(curr != null && !curr.getName().equalsIgnoreCase(key.getName()))) {
		        		removeArray.add(key);
	        		}
	        	}
		    }
		    
		    CLog.e(TAG, "KDS3393_view doRecycle index = " + index);
		    
		    for (ZipArchiveEntry key:removeArray) {
				mImageCache.mCache.get(key).recycle();
		        mImageCache.remove(key, mutex);
		    }
		    removeArray.clear();
		}
	}
	
	public static void doRecycleItem(ZipArchiveEntry key) {
		if (!mutex) {
			setMutex(true);
        	if (mImageCache.mCache.get(key) != null) {
        		mImageCache.mCache.get(key).recycle();
		        mImageCache.remove(key, mutex);
        	}
	        setMutex(false);
		}
	}
	
	public static void doDestroy() {
		if (!mutex) {
			setMutex(true);
			Iterator<ZipArchiveEntry> iterator = mImageCache.iterator();
			ArrayList<ZipArchiveEntry> removeArray = new ArrayList<ZipArchiveEntry>();
		    while (iterator.hasNext()) {
		    	ZipArchiveEntry key = (ZipArchiveEntry) iterator.next();
	        	if (mImageCache.mCache.get(key) != null) {
	        		removeArray.add(key);
	        	}
		    }
		    
		    for (ZipArchiveEntry key:removeArray) {
				mImageCache.mCache.get(key).recycle();
		        mImageCache.remove(key, mutex);
		    }
		    Log.e(TAG,"KDS339_doDestroy 1");
		    Iterator<Integer> thumbIter = mImageCache.mThumbCache.keySet().iterator();
		    while (thumbIter.hasNext()) {
		    	Log.e(TAG,"KDS339_doDestroy 2");
		    	int key = (int) thumbIter.next();
	        	if (mImageCache.mThumbCache.get(key) != null) {
	        		mImageCache.mThumbCache.get(key).recycle();
	        	}
		    }
		    removeArray.clear();
		    if (sEntryArray != null)
		    	sEntryArray.clear();
		    mImageCache.clear();
		    setMutex(false);
		}
	}

	public static Size getImageSize(String url) {
		if (mImageCache.mCache.get(url) == null) {
			return ImageUtils.getSizeOfBitmap(url);
		}
		return new Size(mImageCache.mCache.get(url));
	}
	
	public static Size getImageIntrinsicSize(String url) {
		if (mImageCache.mCache.get(url) == null) {
			return null;
		}
		BitmapDrawable drawable = new BitmapDrawable(mImageCache.mCache.get(url));
		Size size = new Size(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
		return size;
	}
	
	public static boolean hasImage(String url) {
		if (url != null && mImageCache.mCache.get(url) == null)
			return false;
		return true;
	}
	
	private static boolean cancelPotentialDownload(ZipArchiveEntry key, View view) {
		ImageDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(view);
		
		if(bitmapDownloaderTask != null) {
			if((bitmapDownloaderTask.mEntry == null) || bitmapDownloaderTask.mEntry != key) {
				bitmapDownloaderTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	private static ImageDownloaderTask getBitmapDownloaderTask(View view) {
		if(view != null) {
			Drawable drawable = null;
			drawable = view.getBackground();
			if(drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	static class DownloadedDrawable extends ColorDrawable {
		private final WeakReference<ImageDownloaderTask> bitmapDownloaderTaskReference;
		public DownloadedDrawable(ImageDownloaderTask bitmapDownloaderTask) {
			super(Color.TRANSPARENT);
			bitmapDownloaderTaskReference = new WeakReference<ImageDownloaderTask>(bitmapDownloaderTask);
		}

		public ImageDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}
	
	protected static void setMutex(boolean isMutex) {
		mutex = isMutex;
	}
}

