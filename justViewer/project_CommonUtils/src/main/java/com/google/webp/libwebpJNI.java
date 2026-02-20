package com.google.webp;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import com.common.utils.Size;

import android.graphics.Bitmap;
import android.util.Log;

public class libwebpJNI {
	private static final String TAG = "libwebpJNI";
	private static final String LIB_NAME = "webp";
	
	public native byte[] WebPDecodeARGB(byte[] jarg1, int jarg2, int[] jarg3, int[] jarg4);
	public native int WebPGetInfo(byte[] jarg1, int jarg2, int[] jarg3, int[] jarg4);

	private static final libwebpJNI instance = new libwebpJNI();
	
	public static libwebpJNI getInstance() {
	    return instance;
	}
	
	static{
		System.loadLibrary( LIB_NAME );
	}
	
	public Size getImageSize(String path) {
		return getImageSize(path,1.0f);
	}
	
	public Size getImageSize(String path, float scale) {
    	File readFile = new File(path);
        int readcount=0;
        if(readFile!=null&&readFile.exists()){
            try {
                FileInputStream fis = new FileInputStream(readFile);
                readcount = (int)readFile.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                fis.close();
                int[] width = new int[] { 0 };
                int[] height = new int[] { 0 };
                WebPGetInfo(buffer,buffer.length,width,height);
                return new Size(Math.round(width[0] * scale),Math.round(height[0] * scale));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
	}
	
    public Bitmap getWebPLoad(String path) {
    	File readFile = new File(path);
        int readcount=0;
        if(readFile!=null&&readFile.exists()){
            try {
                FileInputStream fis = new FileInputStream(readFile);
                readcount = (int)readFile.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                fis.close();
                Log.e(TAG,"KDS3393 path = " + path);
                return webpToBitmap(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public Bitmap webpToBitmap(byte[] encoded) {
        int[] width = new int[] { 0 };
        int[] height = new int[] { 0 };
        byte[] decoded = WebPDecodeARGB(encoded, encoded.length, width, height);

        int[] pixels = new int[decoded.length / 4];
        ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);

        return Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
    }
}
