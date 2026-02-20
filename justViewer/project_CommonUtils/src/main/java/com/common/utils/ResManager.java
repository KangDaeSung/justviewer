package com.common.utils;

import java.io.InputStream;

import com.common.utils.Size;
import com.common.utils.debug.CLog;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class ResManager {
	private static final String TAG = "ResourceManager";
	
	private static String mDrawablePath = "-mdpi/";
	
	public static void setDrawablePath(String path) {
		mDrawablePath = path;
	}
	
	public static int getResource(String id, String type, Context context) {
		int resourceId = context.getResources().getIdentifier(id, type, context.getPackageName());
		if (resourceId <= 0) {
			resourceId = context.getResources().getIdentifier(id, type, "com.mstory.spsviewer_lib");
		}
		return resourceId;
	}
	
	public static Drawable getAssetDrawable(Context context, String path) {
		AssetManager mngr = context.getResources().getAssets();
		try {
			return Drawable.createFromStream(mngr.open(path), path);
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
		return null;
	}
	
	public static Bitmap getAssetBitmap(Context context, String path) {
		AssetManager mngr = context.getResources().getAssets();
		try {
			return ImageUtils.getImageScaleBitmap(mngr.open(path));
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
		return null;
	}
	
	public static Drawable getResourceDrawable(Context context, String path) {
		InputStream is = null;
		if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".png")) != null) {
			return Drawable.createFromStream(is, path + ".png");
		} else if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".jpg")) != null) {
			return Drawable.createFromStream(is, path + ".jpg");
		}
		return null;
	}
	
	public static Bitmap getResourceBitmap(Context context, String path) {
		InputStream is = null;
		if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".png")) != null) {
			return ImageUtils.getImageScaleBitmap(is);
		} else if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".jpg")) != null) {
			return ImageUtils.getImageScaleBitmap(is);
		}
		return null;
	}
	
	public static Drawable getResourceDensityDrawable(Context context, String path) {
		return getResourceScaleDrawable(context, path, Size.Density);
	}
	
	public static Drawable getResourceScaleDrawable(Context context, String path, float scale) {
		try {
			Bitmap bm = getResourceBitmap(context, path);
			
			int width = Math.round(bm.getWidth() * scale);
			int height = Math.round(bm.getHeight() * scale);
			Bitmap bmp = Bitmap.createScaledBitmap(bm, width, height, true);
			if (bmp != bm) {
				bm.recycle();
			}
			bm = null;
			return ImageUtils.BitmapToDrawable(bmp);
		} catch (Exception e) {
			Log.e(TAG, "Exception path = " + path + " scale = " + scale);
			CLog.e(TAG, e);
			return null;
		}
	}
	
	public static Size getResourceDrawableSize(Context context, String path) {
		InputStream is = null;
		if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".png")) != null) {
			return ImageUtils.getSizeOfBitmap(context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".png"));
		} else if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".jpg")) != null) {
			return ImageUtils.getSizeOfBitmap(context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".jpg"));
		}
		return null;
	}
	
	public static int getResourceViewId(Context context, String strId) {
		return getResource(strId, "id", context);
	}
	
	public static int getResourceLayoutId(Context context, String strId) {
		return getResource(strId, "layout", context);
	}
	
	public static String getResourceString(Context context, String strId) {
		int id = getResourceStringId(context, strId);
		return context.getResources().getString(id);
	}
	
	public static int getResourceStringId(Context context, String strId) {
		return getResource(strId, "string", context);
	}
	
	public static int getResourceDrawableId(Context context, String strId) {
		return getResource(strId, "drawable", context);
	}
	
	public static int getDimen(int res) {
		return (int) getDimen(null,res);
	}
	
	public static int getDimen(Context context, int res) {
		context = Utils.checkContext(context);
		return (int) context.getResources().getDimension(res);
	}
	
	public static int getColor(Context context, int res) {
		return (int) context.getResources().getColor(res);
	}
	
	public static float getTextDimen(Context context, float density, int res) {
		return getTextSize(density, context.getResources().getDimension(res));
	}
	
	public static float getTextSize(float density, float size) {
		return (float) (size / ((0.5 * density) + 0.5));
	}
	
	public static void getMakeSelector(final View image, final String normal, final String press) {
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				final StateListDrawable drawable = ImageUtils.getMakeSelector(image.getContext(), normal, press);
				handler.post(new Runnable() {
					@Override
					public void run() {
						image.setBackgroundDrawable(drawable);
					}
				});
			}
		}).start();
	}
}
