package com.kds3393.just.viewer.Music;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.common.utils.FileUtils;
import com.common.utils.debug.CLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Mp3Id3Parser {
	private static final String TAG = "Mp3Id3Parser";
	
	public String mTitle = "";		//노래 재목
	public String mArtist = "";	//가수 이름
	public String mLyrics = "";	//가사
    
    
    public static Mp3Id3Parser mp3HeaderParser(String path) {
    	Mp3Id3Parser info = new Mp3Id3Parser();
    	StringBuilder strTemp = new StringBuilder();
        //Version
        int mMajorVersion;
        int mRevisionVersion;
        
        //Flag
        boolean mUnsynchronisation = false;		//a
        boolean mExtendedHeader = false;			//b
        boolean mExperimentalIndicator = false;	//c
        boolean mFooterPresent = false;			//d
        
        int mTagSize = 0;
        
        File file = new File(path);
        try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			int fileSize = (int) raf.length();
			
			byte[] buffer = new byte[256];
			raf.read(buffer);
			
			int point = 3;
			if (buffer[0] == 'I' && buffer[1] == 'D' && buffer[2] == '3') { //id3 포멧 시작
				mMajorVersion = buffer[3];		//Majer Version
				mRevisionVersion = buffer[4];	//Revision Version
				point = 5;
				while(buffer[point] != 0) {
					if (buffer[point] == 'a')
						mUnsynchronisation = true;
					else if (buffer[point] == 'b')
						mExtendedHeader = true;
					else if (buffer[point] == 'c')
						mExperimentalIndicator = true;
					else if (buffer[point] == 'd')
						mFooterPresent = true;
					point++;
				}
				//flag 종료코드 00
				point+=1;
				
				mTagSize = getSynchSafeInt(buffer[point+1],buffer[point+2],buffer[point+3]); //태그 size
				point+=4;

				int getCount = 0;
				final int maxCount = 3;
				while(point < fileSize) {
					raf.seek(point);
					raf.read(buffer);
					if (buffer[0] == 'A' && buffer[1] == 'P' && 
							buffer[2] == 'I' && buffer[3] == 'C') {		//커버이미지
						//point = getFrameAPIC(raf, point);
						point = skipFrame(raf, point);
					} else if (buffer[0] == 'T' && buffer[1] == 'I' && 
							buffer[2] == 'T' && buffer[3] == '2') {		//title
						point = getFrameString(raf, point,strTemp);
						info.mTitle = strTemp.toString();
						getCount++;
						if (getCount >= maxCount)
							break;
						strTemp.setLength(0);
					} else if (buffer[0] == 'T' && buffer[1] == 'P' && 
							buffer[2] == 'E' && buffer[3] == '1') {		//artist
						point = getFrameString(raf, point,strTemp);
						info.mArtist = strTemp.toString();
						strTemp.setLength(0);
						getCount++;
						if (getCount >= maxCount)
							break;
					} else if (buffer[0] == 'T' && buffer[1] == 'A' && 
							buffer[2] == 'L' && buffer[3] == 'B') {		//제작사
						point = skipFrame(raf, point);
					} else if (buffer[0] == 'U' && buffer[1] == 'S' && 
							buffer[2] == 'L' && buffer[3] == 'T') {		//가사
						point = getFrameUSLT(raf, point,strTemp);
						info.mLyrics = strTemp.toString();
						strTemp.setLength(0);
						getCount++;
						if (getCount >= maxCount)
							break;
					} else {
						if (buffer[0] == 0 && buffer[1] == 0 && 
							buffer[2] == 0 && buffer[3] == 0)
							break;
						point = skipFrame(raf, point);
						if (point == -1) {
							if (TextUtils.isEmpty(info.mTitle) || TextUtils.isEmpty(info.mArtist)) {
								getLast128InfoMp3(raf,fileSize,info);
							}
							break;
						}
					}
				}
			} else if (buffer[0] == (byte)0xFF && buffer[1] == (byte)0xFA && buffer[2] == (byte)0xB2 &&
					buffer[3] == (byte)0x80 && buffer[4] == (byte)0xED && buffer[5] == (byte)0x2B){ 
				getLast128InfoMp3(raf,fileSize,info);
			}
			if (TextUtils.isEmpty(info.mTitle))
				info.mTitle = FileUtils.getFileName(path);
			info.mLyrics = info.mLyrics.trim();
			raf.close();
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
        return info;
    }
    
    private static void getLast128InfoMp3(RandomAccessFile raf, int fileSize, Mp3Id3Parser info) {//마지막 128byte에 정보 있는 mp3
		try {
			raf.seek(fileSize - 128);
			byte[] buffer = new byte[128];
			raf.read(buffer);
			if (buffer[0] == 'T' && buffer[1] == 'A' && buffer[2] == 'G') {
				info.mTitle = new String(buffer,3,30,"euc-kr").trim();
				info.mArtist = new String(buffer,33,30,"euc-kr").trim();
				
				String str = "";
				for (int i=0;i<10;i++) {
					str = str + String.format("%02X ",buffer[i]);
				}
				//Log.e(TAG, "start [" + str + "]");
				str = "";
			}
		} catch (IOException e) {
			CLog.e(TAG, e);
		}
    }
    
    private static int getFrameString(RandomAccessFile raf, int point, StringBuilder strTmp) {	//title name
    	try {
			raf.seek(point + 4); // TIT2 태그 건너뜀 4byte

			byte[] buffer = new byte[7];
			raf.read(buffer);
			//Log.e(TAG, "[" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
			
			int skipPoint = 8; //TIT2 Size 4 byte
			int size = Integer.parseInt(String.format("%02X%02X%02X%02X",buffer[0],buffer[1],buffer[2],buffer[3]), 16);
			
			//flag 처리 필요
			skipPoint += 2; //APIC flag 2 byte
			raf.seek(point + skipPoint); //14byte skip
			buffer = new byte[10];
			byte[] valueBuffer = new byte[size];
			int total = 0;
			int len = 0;
			while ((len = raf.read(buffer)) != -1) {
				if ((total + len) > size) {
					len = size - total;
				}
            	System.arraycopy(buffer, 0, valueBuffer, total, len);
            	total+=len;
            	if (total >= size) {
            		break;
            	}
            }
			if (valueBuffer[0] == 0) {
				strTmp.append(new String(valueBuffer,1,valueBuffer.length-1,"euc-kr"));
			} else if (valueBuffer[0] == 1 && valueBuffer[1] == -1 && valueBuffer[2] == -2){
				strTmp.append(new String(valueBuffer,1,valueBuffer.length-1,"unicode").trim());
			} else {
				strTmp.append(new String(valueBuffer,1,valueBuffer.length-1,"utf-8"));
			}
			
			buffer = null;
			valueBuffer = null;
			return skipPoint + point + size;
    	} catch (Exception e) {
    		CLog.e(TAG, e);
		}
    	return -1;
    }
    
    private static int getFrameUSLT(RandomAccessFile raf, int point, StringBuilder strTmp) {	//title name
    	try {
			raf.seek(point + 4); // TIT2 태그 건너뜀 4byte

			byte[] buffer = new byte[7];
			raf.read(buffer);
//			Log.e(TAG, "[" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
			
			int apicPoint = 8; //TIT2 Size 4 byte
			int size = Integer.parseInt(String.format("%02X%02X%02X%02X",buffer[0],buffer[1],buffer[2],buffer[3]), 16) ;
			
			//flag 처리 필요
			apicPoint += 2; //APIC flag 2 byte
			
			//이하는 size에 포함된 범위
			int textHeaderPoint = 1; //01로 되어있는 알수 없는 flag
			
			
			//apicPoint += 14; //size가 하나 줄어야 하는거 같다
			raf.seek(point + apicPoint); //14byte skip
			buffer = new byte[10];
			byte[] valueBuffer = new byte[size];
			int total = 0;
			int len = 0;
			while ((len = raf.read(buffer)) != -1) {
				if ((total + len) > size) {
					len = size - total;
					//Log.e(TAG,"Size = " + size + " total = " + total + " len = " + len);
				}
            	System.arraycopy(buffer, 0, valueBuffer, total, len);
            	total+=len;
            	if (total >= size) {
            		break;
            	}
            }
			
			int encodeType = 0;
			if (valueBuffer[textHeaderPoint] == 'e' &&
					valueBuffer[textHeaderPoint + 1] == 'n' &&
					valueBuffer[textHeaderPoint + 2] == 'g') {
				encodeType = 0;
				textHeaderPoint += 3; // Text Encoding Type
			} else if (valueBuffer[textHeaderPoint] == 'k' &&
					valueBuffer[textHeaderPoint + 1] == 'o' &&
					valueBuffer[textHeaderPoint + 2] == 'r') {
				encodeType = 1;
				textHeaderPoint += 3; // Text Encoding Type
			}
			
			
			//textHeaderPoint += 8; // 알 수 없는 테그
			//FF FE 00 00 FF FE FF FE
			//00 00 FF FE
			int checkUniCodePoint = 0;
			while(valueBuffer.length > (textHeaderPoint + checkUniCodePoint)) {
				//Log.e(TAG, "[" + String.format("%d %d",valueBuffer[textHeaderPoint+checkUniCodePoint],valueBuffer[textHeaderPoint+checkUniCodePoint+1]) + "]" + " " + (valueBuffer[textHeaderPoint] == 255));
				if ((valueBuffer[textHeaderPoint+checkUniCodePoint] == -1 && valueBuffer[textHeaderPoint+checkUniCodePoint + 1] == -2) || //0xFF 0xFE
						(valueBuffer[textHeaderPoint+checkUniCodePoint] == 0 && valueBuffer[textHeaderPoint+checkUniCodePoint + 1] == 0)) { //0x00 0x00
					checkUniCodePoint+=2;
				} else {
					checkUniCodePoint-=2;
					break;
				}
			}
			
			if (checkUniCodePoint == 0) {// [3 LINES] 가 붙은 가사에 대한 Parser
				boolean is3LineUnicode = false;
				for(int i=0;i<20;i++) {
					if (valueBuffer[i] == '[' && valueBuffer[i+1] == '3' && valueBuffer[i+3] == 'L' && valueBuffer[i+4] == 'I') {
						is3LineUnicode = true;
					}
				}
				if (is3LineUnicode) {
					int index = 0;
					boolean isSkip = true;
					for (int i=0;i<valueBuffer.length;i++) {
						
						if ((valueBuffer[i] == (byte)0x08 && valueBuffer[i+1] == 0) || 
								(valueBuffer[i] == (byte)0x07 && valueBuffer[i+1] == 0)) {
							if (isSkip) {
								valueBuffer[index] = -1;
								valueBuffer[index+1] = -2;
								index+=2;
							} else {
								if ((valueBuffer[i] == (byte)0x08 && valueBuffer[i+1] == 0)) {
									valueBuffer[index] = (byte)0x0A;
									valueBuffer[index+1] = (byte)0x00;
								} else {
									valueBuffer[index] = (byte)0x20;
									valueBuffer[index+1] = (byte)0x00;
								}
								
								index+=2;
							}
							isSkip = false;
							i+=5;
						} else if (!isSkip) {
							valueBuffer[index] = valueBuffer[i];
							index++;
						}
					}
					strTmp.append(new String(valueBuffer,0,index,"unicode"));
				} else {
					strTmp.append(new String(valueBuffer,textHeaderPoint,valueBuffer.length - textHeaderPoint,"euc-kr").trim());
				}
			} else if (checkUniCodePoint > 0){
				textHeaderPoint+=checkUniCodePoint;
				strTmp.append(new String(valueBuffer,textHeaderPoint,valueBuffer.length - textHeaderPoint,"unicode"));
			}

			File logFolder = new File(Environment.getExternalStorageDirectory() + "/log");
			if (!logFolder.exists()) {
				logFolder.mkdirs();
			}
			File tmpFolder = new File(Environment.getExternalStorageDirectory() + "/log/music.txt");
			FileOutputStream fos;
			fos = new FileOutputStream(tmpFolder);
			
			fos.write(valueBuffer,textHeaderPoint,valueBuffer.length - textHeaderPoint);
			fos.close();
			
			buffer = null;
			valueBuffer = null;
			return apicPoint + point + size;
    	} catch (Exception e) {
    		CLog.e(TAG, e);
		}
    	return -1;
    }
    
    private static int skipFrame(RandomAccessFile raf, int point) {
    	try {
			raf.seek(point + 4); // TIT2 태그 건너뜀 4byte

			byte[] buffer = new byte[7];
			raf.read(buffer);
			//Log.e(TAG, "1 [" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
			
			int apicPoint = 10; //TIT2 Size 4 byte
			int size = Integer.parseInt(String.format("%02X%02X%02X%02X",buffer[0],buffer[1],buffer[2],buffer[3]), 16);
			
			buffer = null;
			return apicPoint + point + size;
    	} catch (NumberFormatException e) {
			return -1;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
    	
    	return -1;
    }
    
    private static int getFrameAPIC(RandomAccessFile raf, int point) {	//커버 이미지
    	try {
			raf.seek(point + 4); // APIC 태그 건너뜀 4byte

			byte[] buffer = new byte[7];
			raf.read(buffer);
			
			
			int apicPoint = 8; //APIC Size 4 byte
			
			int size = Integer.parseInt(String.format("%02X%02X%02X%02X",buffer[0],buffer[1],buffer[2],buffer[3]), 16);
			
			//flag 처리 필요
			apicPoint += 2; //APIC flag 2 byte
			
			size-=14; // 알수 없는 14byte skip
			apicPoint+=14; // 알수 없는 14byte skip
			
			Log.e(TAG,"getAPIC 1 = " + size + " point = " + point + " apicPoint = " + apicPoint);
			
			raf.seek(point + apicPoint); //14byte skip
			buffer = new byte[1024];
			byte[] imgBuffer = new byte[size];
			int total = 0;
			int len = 0;
			while ((len = raf.read(buffer)) != -1) {
				if ((total + len) > size) {
					len = size - total;
					Log.e(TAG,"Size = " + size + " total = " + total + " len = " + len);
				}
            	System.arraycopy(buffer, 0, imgBuffer, total, len);
            	total+=len;
            	if (total >= size) {
            		break;
            	}
            }
			
			//debug ----------------------------------
			String str = "";
			for (int i=0;i<10;i++) {
				str = str + String.format("%02X ",imgBuffer[i]);
			}
			//Log.e(TAG, "start [" + str + "]" + len);
			str = "";
			for (int i=imgBuffer.length-10;i<imgBuffer.length;i++) {
				str = str + String.format("%02X ",imgBuffer[i]);
			}
			Log.e(TAG, "end [" + str + "]");
			//debug ----------------------------------
			
			Bitmap bmp = BitmapFactory.decodeByteArray(imgBuffer,0,(int) imgBuffer.length);
			Log.e(TAG,"Bitmap = " + bmp);
			//mImgView.setImageBitmap(bmp);
			
			buffer = null;
			imgBuffer = null;
			return apicPoint + point + size;
		} catch (Exception e) {
			CLog.e(TAG, e);
		}
    	return -1;
    }
    
    private static int getSynchSafeInt(byte v1, byte v2, byte v3) {
		String zeroTag = "0000000";
		String bit1 = Integer.toBinaryString(v1);

		String bit2 = Integer.toBinaryString(v2);
		if (bit2.length() < 7) {
			bit2 = zeroTag.substring(8 - bit2.length()) + bit2;
		}
		String bit3 = Integer.toBinaryString(v3);
		if (bit3.length() < 7) {
			bit3 = zeroTag.substring(8 - bit3.length()) + bit3;
		}				
		
		String sizeStr = bit1+bit2+bit3;
		int size = Integer.parseInt(sizeStr, 2);
		return size;
    }
    
}
