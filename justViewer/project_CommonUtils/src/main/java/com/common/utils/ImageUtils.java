package com.common.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.webp.libwebpJNI;
import com.common.utils.Size;
import com.common.utils.Utils;
import com.common.utils.debug.CLog;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class ImageUtils {
	public static final String TAG = "ImageUtils";
	
	public static Bitmap getScaleImageBitmap(String filepath, int maxWidth, int maxHeight) {
		Bitmap bm = null;
		try {
			Bitmap src = BitmapFactory.decodeFile(filepath, getBitmapOption(filepath, maxWidth, maxHeight));
			
			int width = src.getWidth();
			int height = src.getHeight();
			float scale = 1;
			Matrix matrix = null;
			if (maxWidth == 0 && maxHeight == 0) {
				scale = 0;
			} else {
				if (width > height && width > maxWidth) {
					scale = ((float) maxWidth) / width;
				}
				if (width <= height && height > maxHeight) {
					scale = ((float) maxHeight) / height;
				}
				matrix = new Matrix();
				matrix.postScale(scale, scale);
			}
			if (scale == 0) {
				bm = src;
			} else {
				bm = Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
				if (bm != src) {
					src.recycle();
				}
			}
		} catch (Exception e) {
			Log.e("getScaleImageBitmap", "Path = " + filepath);
			CLog.e(TAG, e);
		}
		return bm;
	}
	
	public static Bitmap getImageBitmap(String filepath) {
		Bitmap bm;
		if (TextUtils.isEmpty(filepath) || filepath.lastIndexOf(".") < 0) {
			return null;
		} else if (filepath.substring(filepath.lastIndexOf(".")).equalsIgnoreCase(".webp")) {
			bm = libwebpJNI.getInstance().getWebPLoad(filepath);
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;
			bm = BitmapFactory.decodeFile(filepath, options);
		}
		return bm;
	}
	
	public static Bitmap getImageScaleBitmap(String filepath, float scale) {
		Bitmap bm;
		String ext = FileUtils.getExtension(filepath);
		if (TextUtils.isEmpty(filepath) || filepath.lastIndexOf(".") < 0) {
			return null;
		} else if (ext != null && ext.equalsIgnoreCase(".webp")) {
			bm = libwebpJNI.getInstance().getWebPLoad(filepath);
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;
			// options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bm = BitmapFactory.decodeFile(filepath, options);
		}
		if (bm != null && (scale < 1.0f)) {
			int width = Math.round(bm.getWidth() * scale);
			int height = Math.round(bm.getHeight() * scale);
			Bitmap bmp = Bitmap.createScaledBitmap(bm, width, height, true);
			if (bmp != bm) {
				bm.recycle();
			}
			bm = null;
			return bmp;
		}
		return bm;
	}
	
	public static Bitmap getImageScaleBitmap(InputStream is) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		Bitmap bm = BitmapFactory.decodeStream(is, null, options);
		return bm;
	}
	
	public static Size getSizeOfBitmap(String filepath) {
		return getSizeOfBitmap(filepath, 1.0f);
	}
	
	public static Size getSizeOfBitmap(String filepath, float scale) {
		if (TextUtils.isEmpty(filepath)) {
			return null;
		} else if (filepath.substring(filepath.lastIndexOf(".")).equalsIgnoreCase(".webp")) {
			return libwebpJNI.getInstance().getImageSize(filepath);
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
	
	public static Size getSizeOfBitmap(InputStream is) {
		return getSizeOfBitmap(is, 1.0f);
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
	
	public static boolean saveBitmapImage(String Path, Bitmap bitmap) {
		if (Path == null || bitmap == null)
			return false;
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(Path);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			return true;
		} catch (FileNotFoundException e) {
			CLog.e(TAG, e);
			return false;
		}
	}
	
	/** 이미지 파일 회전 후 파일 덮어쓰기 **/
	public static void saveImageRotate(String path, int maxWidth, int maxHeight) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e) {
			CLog.e(TAG, e);
		}
		if (exif != null) {
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Bitmap bmp = getScaleImageBitmap(path, maxWidth * 2, maxHeight * 2);
			Bitmap rotateBmp = rotate(bmp, exifOrientationToDegrees(exifOrientation));
			if (bmp != rotateBmp)
				bmp.recycle();
			if (rotateBmp == null)
				return;
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(path);
				rotateBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			} catch (FileNotFoundException e) {
				CLog.e(TAG, e);
			}
		}
	}
	
	/** Bitmap 이미지 회전 **/
	public static Bitmap rotate(Bitmap bitmap, int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			
			try {
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != converted) {
					bitmap.recycle();
					bitmap = converted;
				}
			} catch (OutOfMemoryError ex) {
				// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
			}
		}
		return bitmap;
	}
	
	public static String saveScrapBitmapImage(String FolderPath, Bitmap bitmap, String id, int year, int month, int currentPage) {
		if (FolderPath == null)
			return "";
		String today = Utils.getTodayDateFormat();
		String filename = id + "_" + year + "_" + month + "_" + currentPage + "_" + today + ".jpg";
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(FolderPath + filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			return FolderPath + filename;
		} catch (FileNotFoundException e) {
			CLog.e(TAG, e);
			return null;
		}
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
	
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		final int height = bmpOriginal.getHeight();
		final int width = bmpOriginal.getWidth();
		
		final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(bmpGrayscale);
		final Paint paint = new Paint();
		final ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		final ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}
	
	public static ShapeDrawable makeRoundDrawable(int alpha) {
		return makeRoundDrawable(alpha, 7, Color.BLACK);
	}
	
	public static ShapeDrawable makeRoundDrawable(int alpha, int round, int color) {
		ShapeDrawable rounddraw = new ShapeDrawable(new RoundRectShape(new float[] { round, round, round, round, round, round, round, round }, null, null));
		rounddraw.getPaint().setColor(color);
		rounddraw.setAlpha(alpha);
		return rounddraw;
	}
	
	public static ShapeDrawable makeRoundDrawable(int alpha, int round, int color, int strokeWidth) {
		ShapeDrawable rounddraw = new ShapeDrawable(new RoundRectShape(new float[] { round, round, round, round, round, round, round, round }, null, null));
		rounddraw.getPaint().setColor(color);
		rounddraw.getPaint().setStyle(Style.STROKE);
		rounddraw.getPaint().setStrokeWidth(strokeWidth);
		rounddraw.setAlpha(alpha);
		
		return rounddraw;
	}
	
	public static Drawable BitmapToDrawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}
	
	public static Bitmap DrawableToBitmap(Drawable d, Size size) {
		Bitmap bitmap = Bitmap.createBitmap(size.Width, size.Height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		d.setBounds(0, 0, size.Width, size.Height);
		d.draw(canvas);
		return bitmap;
	}
	
	public static String getRealPathFromURI(Activity activity, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	public static Bitmap makeReflectedImages(Bitmap oriBmp) {
		// The gap we want between the reflection and the original image
		final int reflectionGap = 4;
		
		int width = oriBmp.getWidth();
		int height = oriBmp.getHeight();
		Log.e(TAG, "KDS3393_size = " + width + " height = " + height);
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		
		Bitmap reflectionImage = Bitmap.createBitmap(oriBmp, 0, height / 2, width, height / 2, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(oriBmp, 0, 0, null);
		Paint deafaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, oriBmp.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff,
				TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
		oriBmp.recycle();
		return bitmapWithReflection;
	}
	
	public static StateListDrawable getMakeSelector(Context context, String normal, String press) {
		Drawable normalDraw = ResManager.getResourceDrawable(context, normal);
		Drawable pressDraw = ResManager.getResourceDrawable(context, press);
		return getMakeSelector(normalDraw, pressDraw);
	}
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		return getMakeSelector(normalDraw, pressDraw);
	}
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int selected_normal, int selected_press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable selnormalDraw = context.getResources().getDrawable(selected_normal);
		Drawable selpressDraw = context.getResources().getDrawable(selected_press);
		return getMakeSelector(normalDraw, pressDraw, selnormalDraw, selpressDraw);
	}
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
		return getMakeSelector(normalDraw, pressDraw, dimDraw);
	}
	
	public static StateListDrawable getMakeSelector(Context context, int normal, int press, int dim, int selected_normal, int selected_press, int selected_dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
		Drawable selected_normalDraw = context.getResources().getDrawable(selected_normal);
		Drawable selected_pressDraw = context.getResources().getDrawable(selected_press);
		Drawable selected_dimDraw = context.getResources().getDrawable(selected_dim);
		return getMakeSelector(normalDraw, pressDraw, dimDraw, selected_normalDraw, selected_pressDraw, selected_dimDraw);
	}
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int press, int checked_normal, int checked_press) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable checknormalDraw = context.getResources().getDrawable(checked_normal);
		Drawable checkpressDraw = context.getResources().getDrawable(checked_press);
		return getMakeCheckedSelector(normalDraw, pressDraw, checknormalDraw, checkpressDraw);
	}
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int press, int dim, int checked_normal, int checked_press,
			int checked_dim) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable pressDraw = context.getResources().getDrawable(press);
		Drawable dimDraw = context.getResources().getDrawable(dim);
		Drawable checknormalDraw = context.getResources().getDrawable(checked_normal);
		Drawable checkpressDraw = context.getResources().getDrawable(checked_press);
		Drawable checkdimDraw = context.getResources().getDrawable(checked_dim);
		return getMakeCheckedSelector(normalDraw, pressDraw, dimDraw, checknormalDraw, checkpressDraw, checkdimDraw);
	}
	
	public static StateListDrawable getMakeCheckedSelector(Context context, int normal, int checked) {
		Drawable normalDraw = context.getResources().getDrawable(normal);
		Drawable checkedDraw = context.getResources().getDrawable(checked);
		return getMakeCheckedSelector(normalDraw, checkedDraw);
	}
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { android.R.attr.state_pressed }, press);
		imageDraw.addState(new int[] { android.R.attr.state_activated }, press);
		imageDraw.addState(new int[] { -android.R.attr.state_pressed }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press, Drawable dim) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { android.R.attr.state_pressed }, press);
		imageDraw.addState(new int[] { -android.R.attr.state_enabled }, dim);
		imageDraw.addState(new int[] { android.R.attr.state_enabled }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press, Drawable selected_normal, Drawable selected_press) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_selected }, selected_press);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_selected }, press);
		imageDraw.addState(new int[] { android.R.attr.state_selected }, selected_normal);
		imageDraw.addState(new int[] { -android.R.attr.state_selected }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeSelector(Drawable normal, Drawable press, Drawable dim, Drawable selected_n, Drawable selected_p, Drawable selected_d) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { -android.R.attr.state_enabled, android.R.attr.state_selected }, selected_d);
		imageDraw.addState(new int[] { -android.R.attr.state_enabled, -android.R.attr.state_selected }, dim);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_selected }, selected_p);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_selected }, press);
		imageDraw.addState(new int[] { android.R.attr.state_selected }, selected_n);
		imageDraw.addState(new int[] { -android.R.attr.state_selected }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable checked) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { android.R.attr.state_checked }, checked);
		imageDraw.addState(new int[] { -android.R.attr.state_checked }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable press, Drawable checked_n, Drawable checked_p) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_checked }, checked_p);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_checked }, press);
		imageDraw.addState(new int[] { android.R.attr.state_checked }, checked_n);
		imageDraw.addState(new int[] { -android.R.attr.state_checked }, normal);
		return imageDraw;
	}
	
	public static StateListDrawable getMakeCheckedSelector(Drawable normal, Drawable press, Drawable dim, Drawable checked_n, Drawable checked_p,
			Drawable checked_d) {
		StateListDrawable imageDraw = new StateListDrawable();
		imageDraw.addState(new int[] { -android.R.attr.state_enabled, android.R.attr.state_checked }, checked_d);
		imageDraw.addState(new int[] { -android.R.attr.state_enabled, -android.R.attr.state_checked }, dim);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_checked }, checked_p);
		imageDraw.addState(new int[] { android.R.attr.state_pressed, -android.R.attr.state_checked }, press);
		imageDraw.addState(new int[] { android.R.attr.state_checked }, checked_n);
		imageDraw.addState(new int[] { -android.R.attr.state_checked }, normal);
		return imageDraw;
	}
	
	public static ColorStateList getMakeColorSelector(Context context, int normal, int press, int dim) {
		int[][] state = new int[][] { new int[] { android.R.attr.state_pressed }, new int[] { -android.R.attr.state_enabled },
				new int[] { android.R.attr.state_enabled } };
		
		int[] color = new int[] { context.getResources().getColor(press), context.getResources().getColor(dim), context.getResources().getColor(normal) };
		
		return new ColorStateList(state, color);
	}
	
	public static ColorStateList getMakeColorSelector(Context context, int normal, int press) {
		int[][] state = new int[][] { new int[] { android.R.attr.state_pressed }, new int[] { -android.R.attr.state_pressed } };
		
		int[] color = new int[] { context.getResources().getColor(press), context.getResources().getColor(normal) };
		
		return new ColorStateList(state, color);
	}
}
