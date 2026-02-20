package common.lib.utils;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.text.TextUtils;
import android.util.Base64;

import common.lib.debug.CLog;

public class CipherUtils {
	private static final String TAG = "CipherUtils";
	
	public static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
	
	private String mKey;
	private SecretKeySpec keySpec;
	private Cipher cipher;
	
	public void setSecretKey(String key) {
		this.mKey = key;
	}
	
	public void setAlgorithm(String algorithm) {
		byte[] raw = stringToBytes(mKey);
		keySpec = new SecretKeySpec(raw, algorithm);
		try {
			cipher = Cipher.getInstance(algorithm);
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
	}
	
	public byte[] encrypt(byte[] abyte) {
		try {
			byte[] encrypted = null;
			synchronized (cipher) {
				byte[] textBytes = abyte;
				AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
				     SecretKeySpec newKey = new SecretKeySpec(mKey.getBytes(), "AES");
				     Cipher cipher = null;
				cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
				encrypted = cipher.doFinal(textBytes);
			}
			
			return encrypted;
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
		return null;
	}
	
	public String encrypt(String str) {
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		byte[] encrypted = encrypt(str.getBytes());
		return Base64.encodeToString(encrypted, 0);
	}
	
	public byte[] decrypt(byte[] abyte) {
		try {
			byte[] decrypted = null;
			synchronized (cipher) {
				//byte[] textBytes = str.getBytes("UTF-8");
				AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
				SecretKeySpec newKey = new SecretKeySpec(mKey.getBytes(), "AES");
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
				decrypted = cipher.doFinal(abyte);
			}
			return decrypted;
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
		return null;
	}
	
	public String decrypt(String str) {
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		try {
			byte[] decrypted = decrypt(Base64.decode(str,0));
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			CLog.e(TAG,e);
		}
		return str;
	}
	
	public static String generateRandomSecretKey(String algorithm) throws Exception {
		KeyGenerator KeyGen = KeyGenerator.getInstance(algorithm);
		KeyGen.init(128);
		SecretKey key = KeyGen.generateKey();
		byte[] raw = key.getEncoded();
		return bytesToString(raw);
	}
	
	private static String bytesToString(byte[] bytes) {
		byte[] b2 = new byte[bytes.length + 1];
		b2[0] = 1;
		System.arraycopy(bytes, 0, b2, 1, bytes.length);
		return new BigInteger(b2).toString(Character.MAX_RADIX);
	}
	
	private static byte[] stringToBytes(String str) {
		byte[] bytes = new BigInteger(str, Character.MAX_RADIX).toByteArray();
		return Arrays.copyOfRange(bytes, 1, bytes.length);
	}
	
	public static void main(String args[]) throws Exception {
		String key = generateRandomSecretKey("AES");
		System.out.println(key);
		CipherUtils cs = new CipherUtils();
		cs.setSecretKey(key);
		cs.setAlgorithm("AES");
		String encStr = cs.encrypt("This is Java Class");
		System.out.println(encStr);
		String decStr = cs.decrypt(encStr);
		System.out.println(decStr);
	}
	
	public static String byteArrayToHex(byte[] ba) {
		if (ba == null || ba.length == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer(ba.length * 2);
		String hexNumber;
		
		int idx = 1;
		for (int x = 0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xFF & ba[x]);
			sb.append(hexNumber.substring(hexNumber.length() - 2));
			sb.append(" ");
			
			if (idx % 20 == 0) {
				sb.append("\n");
			}
			idx++;
		}
		return sb.toString();
	}
	
	// String에 대해 Hash값 변환
	public static String hashSignature(String data, String key) throws SignatureException {
		String result;
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = org.apache.commons.net.util.Base64.encodeBase64URLSafeString(rawHmac);
		} catch (NoSuchAlgorithmException e) {
			throw new SignatureException("Failed to generate Hmac : " + e.getMessage());
		} catch (InvalidKeyException e) {
			throw new SignatureException("Failed to generate Hmac : " + e.getMessage());
		}
		return result;
	}
}
