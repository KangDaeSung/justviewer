package com.kds3393.just.viewer.Image;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.kds3393.just.viewer.Image.ImageDownloader.DownloadedDrawable;
import com.kds3393.just.viewer.Image.ImageDownloader.OnImageDownloadCompleteListener;
import com.kds3393.just.viewer.Utils.FileExtract;

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "ImageDownloaderTask";
	
	private int mViewId;
	public ZipArchiveEntry mEntry;
	public String mZipPath;
	private WeakReference<View> imageViewReference;
	private int mLoadType = -1;
	private int mImagePathType;
	private OnImageDownloadCompleteListener mOnImageDownloadCompleteListener; 
	public ImageDownloaderTask(String zipPath, int viewId, ZipArchiveEntry key, View view, OnImageDownloadCompleteListener listener) {
		mZipPath = zipPath;
		mEntry = key;
		mViewId = viewId;
		imageViewReference = new WeakReference<View>(view);
		mOnImageDownloadCompleteListener = listener;
	}

	private int mMaxWidth = 0;
	private int mMaxHeight = 0;
	private int mPos = -1;
	public ImageDownloaderTask(String zipPath, int pos, ZipArchiveEntry key, View view, int maxWidth, int maxHeight) {
		mZipPath = zipPath;
		mEntry = key;
		mViewId = -1;
		mPos = pos;
		imageViewReference = new WeakReference<View>(view);
		mOnImageDownloadCompleteListener = null;
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		return downloadUnZipBitmap();
	}

	private Bitmap mBitmap;
	private Handler mHandler = new Handler();
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if(isCancelled()) {
			//bitmap.recycle();
			bitmap = null;
			return;
		}
		
		mBitmap = bitmap;
		if(imageViewReference != null) {
			View view = imageViewReference.get();
			if (view != null) {
	        	new Thread(new Runnable() {
	    			public void run() {
	    				View view = imageViewReference.get();
	    				while (view != null) {
	    					if (!ImageDownloader.mutex) {
	    						ImageDownloader.setMutex(true);
	    						if (mViewId >= 0)
	    							ImageDownloader.mImageCache.put(mEntry, view, mBitmap,ImageDownloader.mutex);
	    						else {
	    							Log.e(TAG,"KDS339_put 1");
	    							ImageDownloader.mImageCache.mThumbCache.put(mPos, mBitmap);
	    							Log.e(TAG,"KDS339_put 2");
	    						}
	    						ImageDownloader.setMutex(false);
	    						break;
	    					}
	    				}
	    			}
	        	}).start();
			}
			
			ImageDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(view);
			if(this == bitmapDownloaderTask) {
				setViewBackground(bitmap,view);
			}
		} 
	}

	private void setViewBackground(Bitmap bitmap, View view) {
		if (view != null) {
			if (bitmap != null) {
				view.setTag(mEntry);
				float scale = getScale(bitmap);
				int w = (int)(bitmap.getWidth() * scale);
				int h = (int)(bitmap.getHeight() * scale);
				if (mViewId >= 0) {
					((PageView)view).setImageSize(w,h);
					view.setBackground(new BitmapDrawable(bitmap));
				} else {
					((ImageView)view).setImageBitmap(bitmap);
				}
				if (mOnImageDownloadCompleteListener != null) {
					mOnImageDownloadCompleteListener.onComplete(bitmap, mViewId, ((PageView)view).mIndex);
				}
			} else {
				view.setBackgroundColor(Color.BLUE);
			}
		}
	}
	
	public static float getScale(Bitmap bitmap) {
		float scale = 1;
		if (bitmap.getWidth() > bitmap.getHeight()) {
			scale = (float)(ImageDownloader.sParentViewSize.Width * 2) / bitmap.getWidth();
		} else {
			scale = (float)(ImageDownloader.sParentViewSize.Width) / bitmap.getWidth();
		}
		return scale;
	}
	
	private ImageDownloaderTask getBitmapDownloaderTask(View view) {
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
	
	private Bitmap downloadUnZipBitmap() {
		if (mViewId >= 0)
			return FileExtract.unzipTargetImage(mZipPath,mEntry);
		else
			return FileExtract.unzipThumbImage(mZipPath,mEntry,mMaxWidth,mMaxHeight);
	}
	
	static Bitmap downloadNetworkBitmap(String url) {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);
	
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
				return null;
			}
		
			final HttpEntity entity = response.getEntity();
			if(entity != null) {
				InputStream inputStream = null;
				//BitmapFactory.Options options = new BitmapFactory.Options();
				//options.inSampleSize = 2;
				
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
					return bitmap;
				} finally {
					if(inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		}
		catch(Exception e)
		{
		getRequest.abort();
		}
		return null;
	}
	static class FlushedInputStream extends FilterInputStream
	{
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while(totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if(bytesSkipped == 0L) {
					int bytes = read();
					if(bytes < 0) {
						break; // we reached EOF
					}
					else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}

