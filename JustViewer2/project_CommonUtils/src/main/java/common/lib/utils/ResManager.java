package common.lib.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.InputStream;

public class ResManager {
	private static final String TAG = "ResourceManager";
	
	private static String mDrawablePath = "-mdpi/";

	public static Drawable getResourceDrawable(Context context, String path) {
		InputStream is = null;
		if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".png")) != null) {
			return Drawable.createFromStream(is, path + ".png");
		} else if ((is = context.getClass().getResourceAsStream("/res/drawable" + mDrawablePath + path + ".jpg")) != null) {
			return Drawable.createFromStream(is, path + ".jpg");
		}
		return null;
	}
}
