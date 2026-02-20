package common.lib.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.InputStream;

import common.lib.debug.CLog;

public class ImageUtils {
	public static final String TAG = "ImageUtils";
	
	public static Bitmap getImageBitmap(String filepath) {
		Bitmap bm;
		if (TextUtils.isEmpty(filepath) || filepath.lastIndexOf(".") < 0) {
			return null;
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;
			bm = BitmapFactory.decodeFile(filepath, options);
		}
		return bm;
	}
	
	public static Size getSizeOfBitmap(String filepath, float scale) {
		if (TextUtils.isEmpty(filepath)) {
			return null;
		} else {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(filepath, options);
				if (options.outWidth <= 0 || options.outHeight <= 0)
					return null;
				return new Size(Math.round(options.outWidth * scale), Math.round(options.outHeight * scale));
			} catch (Exception e) {
				CLog.e(TAG, e);
				return null;
			}
		}
	}
	
	public static Size getSizeOfBitmap(InputStream is, float scale) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, options);
			if (options.outWidth <= 0 || options.outHeight <= 0)
				return null;
			return new Size((int) (options.outWidth * scale), (int) (options.outHeight * scale));
		} catch (Exception e) {
			CLog.e(TAG, e);
			return null;
		}
	}
	
	public static BitmapFactory.Options getBitmapOption(String filepath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, options);
		int imgWidth = options.outWidth;
		int imgHeight = options.outHeight;
		int scale = 0;
		if (width > 0 && height > 0) {
			if (imgWidth > imgHeight && imgWidth > width) {
				scale = imgWidth / width;
			}
			if (imgWidth <= imgHeight && imgHeight > height) {
				scale = imgHeight / height;
			}
			options.inSampleSize = getSampleSize(scale);
		}
		options.inJustDecodeBounds = false;
		return options;
	}
	
	public static int getSampleSize(int scale) {
		int samplesize = 0;
		if (scale < 4 && scale > 2) {
			samplesize = 2;
		} else if (scale >= 4 && scale <= 8)
			samplesize = 4;
		else if (scale > 8)
			samplesize = 8;
		return samplesize;
	}
	
	public static int exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}
	
	public static ShapeDrawable makeRoundDrawable(int alpha, int round, int color) {
		ShapeDrawable rounddraw = new ShapeDrawable(new RoundRectShape(new float[] { round, round, round, round, round, round, round, round }, null, null));
		rounddraw.getPaint().setColor(color);
		rounddraw.setAlpha(alpha);
		return rounddraw;
	}
}
