package com.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.common.utils.debug.CLog;

import android.graphics.Path;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

public class FileUtils {
	private static final String TAG = "FileUtils";
	
	public static String PATH_SDCARD = Environment.getExternalStorageDirectory().getPath();
	
	/**
	 * 파일을 존재여부를 확인하는 메소드
	 * 
	 * @param filePath : 확인하기 위한 filePath
	 * @return
	 */
	public static Boolean fileIsLive(String filePath) {
		File f1 = new File(filePath);
		
		if (f1.exists() && f1.isFile()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 파일 이름 변경
	 * 
	 * @param oldPath
	 * @param newPath
	 * @return
	 */
	public static boolean rename(String oldPath, String newPath) {
		boolean result;
		File oldFile = new File(oldPath);
		File newFile = new File(newPath);
		if (oldFile != null && oldFile.exists() && oldFile.renameTo(newFile)) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * 파일을 생성하는 매소드
	 * 
	 * @param makeFileName
	 */
	public static void fileMake(String makeFileName) {
		try {
			File f1 = new File(makeFileName);
			f1.createNewFile();
		} catch (Exception e) {
			CLog.e("fileMake", e);
		}
	}
	
	/**
	 * 파일을 삭제하는 메소드
	 * 
	 * @param deleteFileName
	 */
	public static void deleteFile(String deleteFileName) {
		if (deleteFileName != null) {
			File I = new File(deleteFileName);
			if (I.exists())
				I.delete();
		}
	}
	
	/**
	 * 폴더 삭제
	 * 
	 * @param folderPath
	 */
	public static void deleteFolder(String folderPath) {
		File file = new File(folderPath);
		if (!file.exists())
			return;
		File[] childFileList = file.listFiles();
		if (childFileList == null)
			return;
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				deleteFolder(childFile.getAbsolutePath());
			} else {
				childFile.delete();
			}
		}
		
		file.delete();
	}
	
	/**
	 * 파일을 복사하는 메소드
	 * 
	 * @param oriPath 원본 파일 패스
	 * @param targetPath 복사될 파일 패스
	 * @return
	 */
	public static boolean fileCopy(String oriPath, String targetPath) {
		if (oriPath == null || targetPath == null)
			return false;
		File oriFile = new File(oriPath);
		File targetFile = new File(targetPath);
		
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(oriFile);
			FileOutputStream outputStream = new FileOutputStream(targetFile);
			
			FileChannel fcin = inputStream.getChannel();
			FileChannel fcout = outputStream.getChannel();
			
			long size = fcin.size();
			
			fcin.transferTo(0, size, fcout);
			fcout.close();
			fcin.close();
			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			CLog.e(TAG, e);
			return false;
		}
		return true;
	}
	
	/**
	 * 폴더 복사
	 * 
	 * @param oriPath 원본 파일 패스
	 * @param targetPath 복사될 파일 패스
	 */
	public static void copyDirectory(String oriPath, String targetPath) {
		File sourceLocation = new File(oriPath);
		File targetLocation = new File(targetPath);
		copyDirectory(sourceLocation, targetLocation);
	}
	
	/**
	 * 폴더 복사
	 * 
	 * @param oriDir 원본 파일
	 * @param targetDir 복사될 파일
	 */
	public static void copyDirectory(File oriDir, File targetDir) {
		if (oriDir.isDirectory()) {
			if (!targetDir.exists()) {
				targetDir.mkdir();
			}
			
			String[] children = oriDir.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(oriDir, children[i]), new File(targetDir, children[i]));
			}
		} else {
			InputStream in;
			try {
				in = new FileInputStream(oriDir);
				
				OutputStream out = new FileOutputStream(targetDir);
				
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 파일을 이동하는 메소드
	 * 
	 * @param oriPath 원본 파일 패스
	 * @param targetPath 복사될 파일 패스
	 */
	public static void fileMove(String oriPath, String targetPath) {
		fileCopy(oriPath, targetPath);
		// 복사한뒤 원본파일을 삭제함
		deleteFile(oriPath);
		
	}
	
	/**
	 * 디렉토리의 파일 리스트를 읽는 메소드
	 * 
	 * @param dirPath
	 * @return
	 */
	public static List<File> getDirFileList(String dirPath) {
		if (TextUtils.isEmpty(dirPath))
			return null;
		
		File dir = new File(dirPath);
		
		if (!dir.exists() || !dir.isDirectory()) {
			return null;
		}
		
		List<File> dirFileList = null;
		
		if (dir.exists()) {
			File[] files = dir.listFiles();
			
			if (files != null)
				dirFileList = Arrays.asList(files);
		}
		return dirFileList;
	}
	
	/**
	 * 외부 저장소에 대한 mount list를 가져온다.
	 * !확신 없음
	 * @return
	 */
	public static HashSet<String> getExternalMounts() {
		final HashSet<String> out = new HashSet<String>();
		String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
		String s = "";
		try {
			final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
			process.waitFor();
			final InputStream is = process.getInputStream();
			final byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				s = s + new String(buffer);
			}
			is.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		// parse output
		final String[] lines = s.split("\n");
		for (String line : lines) {
			if (!line.toLowerCase(Locale.US).contains("asec")) {
				if (line.matches(reg)) {
					String[] parts = line.split(" ");
					for (String part : parts) {
						if (part.startsWith("/"))
							if (!part.toLowerCase(Locale.US).contains("vold"))
								out.add(part);
					}
				}
			}
		}
		return out;
	}
	
	/**
	 * 파일 확장자 얻기
	 * @param fileStr
	 * @return
	 */
	public static String getExtension(String fileStr) {
		if (fileStr == null || fileStr.lastIndexOf(".") < 0) {
			return "";
		}
		return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
	}
	
	/**
	 * 파일 이름만 얻기
	 * @param fileStr
	 * @return
	 */
	public static String getFileName(String fileStr) {
		String fileName = fileStr.substring(fileStr.lastIndexOf("/") + 1, fileStr.length());
		if (fileName.lastIndexOf(".") > 0)
			return fileName.substring(0, fileName.lastIndexOf("."));
		else
			return fileName;
	}
	
	/**
	 * 파일의 이름과 확장자 얻기
	 * @param fileStr
	 * @return
	 */
	public static String getName(String fileStr) {
		return fileStr.substring(fileStr.lastIndexOf("/") + 1, fileStr.length());
	}
	
	/**
	 * 해당 경로의 sdcard의 남은 용량을 가져온다.
	 * @param path
	 * @return
	 */
	public static long getSDCardStorageByte(String path) {
		StatFs stat = new StatFs(path);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return (long) stat.getFreeBlocks() * (long) stat.getBlockSize();
		}
		return stat.getFreeBlocksLong() * stat.getBlockSizeLong();
	}
	
	/**
	 * 해당 경로의 sdcard의 용량을 Mb단위로 가져온다.
	 * @param path
	 * @return
	 */
	public static long getSDCardStorageMB(String path) {
		return getSDCardStorageByte(path) / 1024 / 1024;
	}
	
	/**
	 * 파일의 size를 가져온다.
	 * @param file
	 * @return
	 */
	public static long getfilesSize(File file) {
		return getfilesSize(0,file);
	}
	
	public static long getfilesSize(String strDir) {
		File dir = new File(strDir);
		return getfilesSize(0,dir);
	}
	
	public static long getfilesSize(long size,File file) {
		long totalSize = 0;
		if (file.isDirectory()) {
			List<File> files = getDirFileList(file.getPath());
			for (File f : files) {
				totalSize += getfilesSize(f);
			}
		} else {
			totalSize += file.length();
		}
		return totalSize;
	}
	
	/**
	 * 파일의 CRC32값을 가져온다.
	 * @param readFile
	 * @return CRC32
	 */
	public static long getCRC32Value(String readFile) {
		Checksum crc = new CRC32();
		
		try {
			RandomAccessFile raf = new RandomAccessFile(readFile, "r");
			raf.seek(0);
			byte buffer[] = new byte[readFile.length()];
			raf.read(buffer);
			crc.update(buffer, 0, readFile.length());
			
			raf.close();
		} catch (IOException e) {
			CLog.e(TAG, e);
		}
		return crc.getValue();
	}
	
	/**
	 * 파일의 내용을 Byte로 반환합니다.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		
		long length = file.length();
		
		if (length > Integer.MAX_VALUE) {
			throw new IllegalStateException("파일의 크기가 Integer.MAX_VALUE를 넘으면 안됩니다.");
		}
		
		byte[] bytes = new byte[(int) length];
		
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		
		is.close();
		return bytes;
	}
}
