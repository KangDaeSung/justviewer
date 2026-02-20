package com.common.utils.debug;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class DevUtils {
	private static final String TAG = "DevUtils";
	
	private long mStartTime = 0;
	private static int mMidCount = 0;
	public void startTime() {
		mStartTime = System.currentTimeMillis();
	}
	
	public long endTime() {
		long endTime = System.currentTimeMillis();
		return endTime - mStartTime;
	}
	
	private static HashMap<Integer,Long> sHashCode = new HashMap<Integer,Long>();
	public static void sStartTime(int hashcode) {
		mMidCount = 0;
		long time = System.currentTimeMillis();
		sHashCode.put(hashcode,time);
	}
	
	public static long sMidTime(int hashcode, String tag) {
		long endTime = System.currentTimeMillis();
		Long startTime = sHashCode.get(hashcode);
		if (startTime == null)
			return -1;
		mMidCount++;
		Log.e(TAG,"KDS3393_Mid delay " + hashcode + "(" + mMidCount + ") : " + tag + " = " + (endTime - startTime));
		return endTime - startTime;
	}

	public static long sEndTime(int hashcode) {
		long endTime = System.currentTimeMillis();
		Long startTime = sHashCode.get(hashcode);
		if (startTime == null)
			return -1;
		sHashCode.remove(hashcode);
		Log.e(TAG,"KDS3393_delay " + hashcode + " = " + (endTime - startTime));
		return endTime - startTime;
	}
	public static void getCurrentMemory() {
		long m = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		DecimalFormat df = new DecimalFormat("#,##0");

		Log.e(TAG, "using mem : "+df.format(m));
	}
	
	public static int getRandomRGB() {
		Random rnd = new Random();
		return Color.parseColor("#" + String.format("%02X%02X%02X", rnd.nextInt(255),rnd.nextInt(255),rnd.nextInt(255)));
	}
	
	private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");
	public static boolean isDebuggable(Context ctx) {
	    boolean debuggable = false;
	    try
	    {
	        PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),PackageManager.GET_SIGNATURES);
	        Signature signatures[] = pinfo.signatures;

	        CertificateFactory cf = CertificateFactory.getInstance("X.509");

	        for ( int i = 0; i < signatures.length;i++)
	        {   
	            ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
	            X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);       
	            debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
	            if (debuggable)
	                break;
	        }
	    } catch (NameNotFoundException e) {
	        //debuggable variable will remain false
	    } catch (CertificateException e) {
	        //debuggable variable will remain false
	    }
	    return debuggable;
	}
	
	/**
	 * App에 있는 Keystore의 SecretKey값을 반환한다.
	 * 
	 * @return SecretKey
	 */
	public static SecretKey getKeystoreSecretKey(Context context) {
	    SecretKey key = null;
	    try {
	        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
	 
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA-256");
	            md.update(signature.toByteArray());
	            key = new SecretKeySpec(md.digest(), "AES");
	            break;
	        }
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return key;
	}
	
	/**
	 * App에 있는 Keystore의 HashCode를 반환한다.
	 * 
	 * @return hashcode
	 */
	public static int getKeystoreHash(Context context, String packageName) {
		int hash = 0;
		try{
	        PackageManager packageManager = context.getPackageManager();
	        PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
	        Signature[] signs = info.signatures;
	        CertificateFactory cf = CertificateFactory.getInstance("X.509");
	        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(signs[0].toByteArray()));
	        PublicKey key = cert.getPublicKey();
	        hash = ((RSAPublicKey) key).getModulus().hashCode();
	    }catch(Exception e){
	    	CLog.e(TAG,e);
	    }
		return hash;
	}
	
	public static void logChildTree(int depth, View view) {
		String dpStr = "";
		for (int d=0;d<depth;d++) {
			dpStr+="   ";
		}
		String info = "";
		if (view instanceof ViewGroup) {
			info = getParamsInfo(view);
			Log.e(TAG,dpStr + "vg = " + view.toString().substring(view.toString().lastIndexOf(".")+1, view.toString().length()) + info);
			ViewGroup vg = (ViewGroup) view;
			for (int i=0;i<vg.getChildCount();i++) {
				View child = vg.getChildAt(i);
				if (child instanceof ViewGroup) {
					logChildTree(depth + 1,child);
				} else {
					info = getParamsInfo(child);
					Log.e(TAG,dpStr + "   v = " + child.toString().substring(child.toString().lastIndexOf(".")+1, child.toString().length()) + info);
				}
			}
		} else {
			info = getParamsInfo(view);
			Log.e(TAG,dpStr + "v = " + view.toString().substring(view.toString().lastIndexOf(".")+1, view.toString().length()) + info);
		}
	}
	
	private static String getParamsInfo(View view) {
		String info = "";
		if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
			info = " x:" + params.leftMargin + " y:" + params.topMargin + " w:" + params.width + " h:" + params.height;
		} else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
			info = " x:" + params.leftMargin + " y:" + params.topMargin + " w:" + params.width + " h:" + params.height;
		}
		return info;
	}
}
