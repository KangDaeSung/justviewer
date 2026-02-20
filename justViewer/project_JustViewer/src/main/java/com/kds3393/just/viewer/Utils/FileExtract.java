package com.kds3393.just.viewer.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.common.utils.FileUtils;
import com.common.utils.ImageUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;
import com.common.utils.debug.DevUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class FileExtract {
	private static final String TAG = "FileExtract";
	
	private ProgressDialog mProgress;
	private static final int BUFFER_SIZE = 1024 * 16;
	private String mTail = "";
	private Context mContext;
		
	private boolean mIsAsset = false;
	
	public static final int ERR_DECOMPRESSION = -3;
	
	public interface OnExtractListener {
		public void onStart();
		public void onEnd();
		public void onError(int err);
	};
	
	protected OnExtractListener mOnExtractListener;
	public void setOnExtractListener(OnExtractListener listener) {
		mOnExtractListener = listener;
	}
	
	public FileExtract(Context context) {
		mContext = context;
	}
	
	public void setIsAsset(boolean isAsset) {
		mIsAsset = isAsset;
	}
    public File extractFile(String zipFilePath, String filename, String unCompressPath) {
    	InputStream is = null;
    	String entryName = null;
		try {
			File file = new File(zipFilePath);
			if (file.exists())
				is = new FileInputStream(new File(zipFilePath));
			else {
				return null;
			}
			
			ZipArchiveInputStream zipFile = new ZipArchiveInputStream(is);
			ZipArchiveEntry zentry = null;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((zentry = zipFile.getNextZipEntry()) != null) {
				if (zentry.getName().equalsIgnoreCase(filename)) {
					entryName = zentry.getName();
	                File targetFile = new File(unCompressPath, entryName);
	                if (zentry.isDirectory() && targetFile.getAbsolutePath() != null) {
                		File makeDir = new File(targetFile.getAbsolutePath());
                		makeDir.mkdirs();
	                } else {
	                	if (targetFile.getParent() != null) {
	                		File makeDir = new File(targetFile.getParent());
	                		makeDir.mkdirs();
	                	}

	                    FileOutputStream fos = null;
	                    try {
	                        fos = new FileOutputStream(targetFile);
	                        
	                        int len = 0;
	                        while ((len = zipFile.read(buffer)) != -1) {
	                        	if (len == 0)
	                            fos.write(buffer, 0, len);
	                        }
	                    } catch (FileNotFoundException e) {
	                    	if (fos != null) {
	                    		fos.close();
	                    	}
	                    } finally {
	                        if (fos != null) {
	                            fos.close();
	                        }
	                    }
	                }
				}
			}
			
			zipFile.close();
		} catch (IOException e) {
			CLog.e(TAG, e);
		}
		
		return new File(unCompressPath, entryName);
    }
    
    public int extractFiles(String compressedPath, String tail, ProgressDialog progress) {
    	mTail = tail;
    	mProgress = progress;
    	
    	InputStream is = null;
		try {
			File file = new File(compressedPath);
			if (file.exists())
				is = new FileInputStream(new File(compressedPath));
			else {
				return -1;
			}
			
			ZipArchiveInputStream zipFile = new ZipArchiveInputStream(is);
			long unZipSize = getZipFileSize(zipFile);
			
			mProgress.setMax((int) unZipSize);
			is.close();
			is = new FileInputStream(new File(compressedPath));
		} catch (IOException e) {
			CLog.e(TAG, e);
		}
		
		new FileExtractTask().execute(is,compressedPath);
		return 1;
    }
    
	private long getZipFileSize(ZipArchiveInputStream file) throws IOException {
		long totalSize = 0;
		ZipArchiveEntry zentry = null;
		
		while ((zentry = file.getNextZipEntry()) != null) {
			totalSize += zentry.getSize();
		}
		return totalSize;
	}
	
    protected class FileExtractTask extends AsyncTask<Object, Integer, Boolean>{
		private long mCurrentSize = 0;
		@Override
		protected Boolean doInBackground(Object... params) {
			ZipArchiveEntry zentry = null;
			ZipArchiveInputStream zipFile = null;
	        try {
		        zipFile = new ZipArchiveInputStream((InputStream)params[0]);
		        String unCompressedPath = (String)params[1];

				if (mOnExtractListener != null) {
					mOnExtractListener.onStart();
				}
				
				byte[] buffer = new byte[BUFFER_SIZE];
	            while ((zentry = zipFile.getNextZipEntry()) != null) {
	            	String tmpName = zentry.getName();
	                File targetFile = new File(unCompressedPath + "/", tmpName);
	                if (zentry.isDirectory() && targetFile.getAbsolutePath() != null) {
                		File makeDir = new File(targetFile.getAbsolutePath());
                		makeDir.mkdirs();
	                } else {
	                	if (targetFile.getParent() != null) {
	                		File makeDir = new File(targetFile.getParent());
	                		makeDir.mkdirs();
	                	}

	                    FileOutputStream fos = null;
	                    try {
	                        fos = new FileOutputStream(targetFile);
	                        
	                        int len = 0;
	                        while ((len = zipFile.read(buffer)) != -1) {
	                            fos.write(buffer, 0, len);
	                            mCurrentSize += len;
	                            publishProgress((int)mCurrentSize);
	                        }
	                    } catch (FileNotFoundException e) {
	                    	if (fos != null) {
	                    		fos.close();
	                    	}
	                    } finally {
	                        if (fos != null) {
	                            fos.close();
	                        }
	                    }
	                }
	            }
	            zipFile.close();
	            File noMediaFile = new File(unCompressedPath + ".nomedia");
	            if (!noMediaFile.exists())
	            	noMediaFile.mkdir();
	        } catch (IOException e) {
	        	CLog.e(TAG, e);
	            if (zipFile != null) {
	            	try {
						zipFile.close();
					} catch (IOException e1) {
						CLog.e(TAG, e);
					}
	            }
	            return false;
	        } catch (Exception e) {
	        	CLog.e(TAG, e);
	            if (zipFile != null) {
	            	try {
						zipFile.close();
					} catch (IOException e1) {
						CLog.e(TAG, e);
					}
	            }
	            return false;
			} finally {
				
	        }
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... value) {
			mProgress.setProgress(value[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (mProgress != null)
				mProgress.dismiss();
			if (!result) {
				if (mOnExtractListener != null)
					mOnExtractListener.onError(ERR_DECOMPRESSION);
				return;
			}
			if (mOnExtractListener != null) {
				mOnExtractListener.onEnd();
			}
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mProgress != null) {
				mProgress.setProgress(0);
				mProgress.show();
			}
		}
    }
    
    public void compressFile(String path, String zipPath, ProgressDialog progress) {
    	mProgress = progress;
    	ArrayList<File> fileList = new ArrayList<File>();
    	File file = new File(path);
    	fileGather(fileList,file);

    	File zipFile = new File(zipPath);
    	File dir = null;
		dir = new File(zipFile.getParent());  
		if (!dir.exists())
			dir.mkdirs();
		
		new FileCompressTask().execute(zipPath,fileList,path);
    }
    
    private void fileGather(ArrayList<File> fileList, File dir) {
    	File[] listFiles = dir.listFiles();
    	for (File file:listFiles) {
    		if (file.isFile()) {
    			fileList.add(file);
    		} else if(file.isDirectory()) {
    			fileGather(fileList,file);
    		}
    	}
    }
    
    protected class FileCompressTask extends AsyncTask<Object, Integer, String>{
		private long mCurrentSize = 0;
		@Override
		protected String doInBackground(Object... params) {
			
			String zipPath = (String)params[0];
	        ArrayList<File> fileList = (ArrayList<File>)params[1];
	        String rootPath = (String)params[2];
	        
			if (mOnExtractListener != null) {
				mOnExtractListener.onStart();
			}

			try {
				ZipArchiveOutputStream zipFile = new ZipArchiveOutputStream(new FileOutputStream(zipPath));
				byte[] buffer = new byte[BUFFER_SIZE];
				for (File file:fileList) {
					FileInputStream in = new FileInputStream(file);
					String zipFilePath = file.getPath().substring(rootPath.length(), file.getPath().length());
					zipFile.putArchiveEntry(new ZipArchiveEntry(zipFilePath));
					
					int data;
					while ((data = in.read(buffer)) > 0) {
						zipFile.write(buffer, 0, data);
					}
					zipFile.closeArchiveEntry();
					in.close();
				}
				zipFile.close();
			} catch (IOException e) {
				CLog.e(TAG, e);
				return null;
			}
			
			return zipPath;
		}
		
		@Override
		protected void onProgressUpdate(Integer... value) {
			mProgress.setProgress(value[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (mProgress != null) {
				mProgress.dismiss();
			}
			if (result == null) {
				if (mOnExtractListener != null)
					mOnExtractListener.onError(ERR_DECOMPRESSION);
				return;
			}
			if (mOnExtractListener != null) {
				mOnExtractListener.onEnd();
			}
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mProgress != null) {
				mProgress.setProgress(0);
				mProgress.show();
			}
		}
    }
    
    public static ArrayList<ZipEntry> GetZipEntry(String path) {
		DevUtils.sStartTime(1);
		ZipArchiveInputStream zipFile;
		try {
            ZipFile zFile = new ZipFile(path,"EUC-KR");
            Enumeration zipEntries = zFile.getEntries();
            ArrayList<ZipEntry> entryArray = new ArrayList<ZipEntry>();
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipEntries.nextElement();
                String extension = FileUtils.getExtension(entry.getName());
                if (extension.equalsIgnoreCase("jpg") ||
                        extension.equalsIgnoreCase("jpeg") ||
                        extension.equalsIgnoreCase("png") ||
                        extension.equalsIgnoreCase("bmp") ||
                        extension.equalsIgnoreCase("gif")) {
                    entryArray.add(entry);
                }
            }
            zFile.close();
            Collections.sort(entryArray, new SubStringComparator());

			return entryArray;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DevUtils.sEndTime(1);
		return null;
	}
    
    public static byte[] unzipTargetBuffer(String zipPath, ZipEntry entry) {
    	try {
//	    	ZipArchiveInputStream zipFile = new ZipArchiveInputStream(new FileInputStream(zipPath),"euc-kr",false);
			
			int len = 0;
			ZipArchiveEntry zentry = null;
			byte[] buffer = new byte[1024];
			byte[] imagebuffer = new byte[(int) entry.getSize()];
			int total = 0;

            ZipFile zFile = new ZipFile(zipPath,"EUC-KR");
            ZipEntry newEntry = zFile.getEntry(entry.getName());

            InputStream zio = zFile.getInputStream(newEntry);

            while ((len = zio.read(buffer)) != -1) {
                System.arraycopy(buffer, 0, imagebuffer, total, len);
                total+=len;
            }
            zio.close();
			buffer = null;
			return imagebuffer;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
    	return null;
    }
    
    public static void unzipTargetFile(String zipPath, String targetPath, ZipEntry targetEntry) {
    	try {
    		byte[] fileBuffer = unzipTargetBuffer(zipPath,targetEntry);
	    	
			File tmpFolder = new File(targetPath + targetEntry);
			FileOutputStream fos;
			fos = new FileOutputStream(tmpFolder);
			
			fos.write(fileBuffer);
			fileBuffer = null;
			fos.close();
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
    }
    
    public static Bitmap unzipTargetImage(String zipPath, ZipEntry targetEntry) {
    	try {
    		byte[] imagebuffer = unzipTargetBuffer(zipPath,targetEntry);
			Bitmap bmp = BitmapFactory.decodeByteArray(imagebuffer,0,(int) imagebuffer.length, 
					getBitmapOption(imagebuffer,Size.DisplayWidth * 2,Size.DisplayHeight));
			imagebuffer = null;
			return bmp;
    	} catch (Exception e) {
    		CLog.e(TAG, e);
		}
    	return null;
    }
    
    public static Bitmap unzipThumbImage(String zipPath, ZipEntry targetEntry,int maxWidth, int maxHeight) {
    	try {
    		byte[] imagebuffer = unzipTargetBuffer(zipPath,targetEntry);
			Bitmap bmp = BitmapFactory.decodeByteArray(imagebuffer,0,(int) imagebuffer.length, 
					getBitmapOption(imagebuffer,maxWidth,maxHeight));
			imagebuffer = null;
			return bmp;
    	} catch (Exception e) {
    		CLog.e(TAG, e);
		}
    	return null;
    }
    
    public static BitmapFactory.Options getBitmapOption(byte[] buffer,int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(buffer,0,(int) buffer.length, options);
        int imgWidth = options.outWidth;
        int imgHeight = options.outHeight;
        int scale = 1;
        if (imgWidth > 0 && imgHeight > 0) {
            if (imgWidth > imgHeight && imgWidth > width) {
                scale = imgWidth / width; 
            }
            if (imgWidth <= imgHeight && imgHeight > height) {
                scale = imgHeight / height; 
            }
        } else {
        	CLog.e(TAG, "Exception:divide by zero");
        }
        options.inSampleSize = ImageUtils.getSampleSize(scale);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        return options;
    }
}
