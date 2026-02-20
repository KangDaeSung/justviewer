package com.kds3393.just.justviewer2.music

import android.graphics.BitmapFactory
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import common.lib.utils.FileUtils
import common.lib.debug.CLog.e
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.charset.Charset

class Mp3Id3Data {
    var mTitle = "" //노래 재목
    var mArtist = "" //가수 이름
    var mLyrics = "" //가사
}
object Mp3Id3Parser {
    private const val TAG = "Mp3Id3Parser"
    fun mp3HeaderParser(path: String?): Mp3Id3Data {
        val info = Mp3Id3Data()
        val strTemp = StringBuilder() //Version
        val mMajorVersion: Int
        val mRevisionVersion: Int

        //Flag
        var mUnsynchronisation = false //a
        var mExtendedHeader = false //b
        var mExperimentalIndicator = false //c
        var mFooterPresent = false //d
        var mTagSize = 0
        val file = File(path)
        try {
            val raf = RandomAccessFile(file, "r")
            val fileSize = raf.length().toInt()
            val buffer = ByteArray(256)
            raf.read(buffer)
            var point = 3
            if (buffer[0] == 'I'.code.toByte() && buffer[1] == 'D'.code.toByte() && buffer[2] == '3'.code.toByte()) { //id3 포멧 시작
                mMajorVersion = buffer[3].toInt() //Majer Version
                mRevisionVersion = buffer[4].toInt() //Revision Version
                point = 5
                while (buffer[point].toInt() != 0) {
                    if (buffer[point] == 'a'.code.toByte()) mUnsynchronisation = true else if (buffer[point] == 'b'.code.toByte()) mExtendedHeader = true else if (buffer[point] == 'c'.code.toByte()) mExperimentalIndicator = true else if (buffer[point] == 'd'.code.toByte()) mFooterPresent = true
                    point++
                }                //flag 종료코드 00
                point += 1
                mTagSize = getSynchSafeInt(buffer[point + 1], buffer[point + 2], buffer[point + 3]) //태그 size
                point += 4
                var getCount = 0
                val maxCount = 3
                while (point < fileSize) {
                    raf.seek(point.toLong())
                    raf.read(buffer)
                    if (buffer[0] == 'A'.code.toByte() && buffer[1] == 'P'.code.toByte() && buffer[2] == 'I'.code.toByte() && buffer[3] == 'C'.code.toByte()) {        //커버이미지
                        //point = getFrameAPIC(raf, point);
                        point = skipFrame(raf, point)
                    } else if (buffer[0] == 'T'.code.toByte() && buffer[1] == 'I'.code.toByte() && buffer[2] == 'T'.code.toByte() && buffer[3] == '2'.code.toByte()) {        //title
                        point = getFrameString(raf, point, strTemp)
                        info.mTitle = strTemp.toString()
                        getCount++
                        if (getCount >= maxCount) break
                        strTemp.setLength(0)
                    } else if (buffer[0] == 'T'.code.toByte() && buffer[1] == 'P'.code.toByte() && buffer[2] == 'E'.code.toByte() && buffer[3] == '1'.code.toByte()) {        //artist
                        point = getFrameString(raf, point, strTemp)
                        info.mArtist = strTemp.toString()
                        strTemp.setLength(0)
                        getCount++
                        if (getCount >= maxCount) break
                    } else if (buffer[0] == 'T'.code.toByte() && buffer[1] == 'A'.code.toByte() && buffer[2] == 'L'.code.toByte() && buffer[3] == 'B'.code.toByte()) {        //제작사
                        point = skipFrame(raf, point)
                    } else if (buffer[0] == 'U'.code.toByte() && buffer[1] == 'S'.code.toByte() && buffer[2] == 'L'.code.toByte() && buffer[3] == 'T'.code.toByte()) {        //가사
                        point = getFrameUSLT(raf, point, strTemp)
                        info.mLyrics = strTemp.toString()
                        strTemp.setLength(0)
                        getCount++
                        if (getCount >= maxCount) break
                    } else {
                        if (buffer[0].toInt() == 0 && buffer[1].toInt() == 0 && buffer[2].toInt() == 0 && buffer[3].toInt() == 0) break
                        point = skipFrame(raf, point)
                        if (point == -1) {
                            if (TextUtils.isEmpty(info.mTitle) || TextUtils.isEmpty(info.mArtist)) {
                                getLast128InfoMp3(raf, fileSize, info)
                            }
                            break
                        }
                    }
                }
            } else if (buffer[0] == 0xFF.toByte() && buffer[1] == 0xFA.toByte() && buffer[2] == 0xB2.toByte() && buffer[3] == 0x80.toByte() && buffer[4] == 0xED.toByte() && buffer[5] == 0x2B.toByte()) {
                getLast128InfoMp3(raf, fileSize, info)
            }
            if (TextUtils.isEmpty(info.mTitle)) info.mTitle = FileUtils.getFileName(path!!)
            info.mLyrics = info.mLyrics.trim { it <= ' ' }
            raf.close()
        } catch (e: Exception) {
            e(TAG, e)
        }
        return info
    }

    private fun getLast128InfoMp3(raf: RandomAccessFile, fileSize: Int, info: Mp3Id3Data) { //마지막 128byte에 정보 있는 mp3
        try {
            raf.seek((fileSize - 128).toLong())
            val buffer = ByteArray(128)
            raf.read(buffer)
            if (buffer[0] == 'T'.code.toByte() && buffer[1] == 'A'.code.toByte() && buffer[2] == 'G'.code.toByte()) {
                info.mTitle = String(buffer, 3, 30, Charset.forName("euc-kr")).trim { it <= ' ' }
                info.mArtist = String(buffer, 33, 30, Charset.forName("euc-kr")).trim { it <= ' ' }
                var str = ""
                for (i in 0..9) {
                    str = str + String.format("%02X ", buffer[i])
                }                //Log.e(TAG, "start [" + str + "]");
                str = ""
            }
        } catch (e: IOException) {
            e(TAG, e)
        }
    }

    private fun getFrameString(raf: RandomAccessFile, point: Int, strTmp: StringBuilder): Int {    //title name
        try {
            raf.seek((point + 4).toLong()) // TIT2 태그 건너뜀 4byte
            var buffer: ByteArray? = ByteArray(7)
            raf.read(buffer)            //Log.e(TAG, "[" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
            var skipPoint = 8 //TIT2 Size 4 byte
            val size = String.format("%02X%02X%02X%02X", buffer!![0], buffer[1], buffer[2], buffer[3]).toInt(16)

            //flag 처리 필요
            skipPoint += 2 //APIC flag 2 byte
            raf.seek((point + skipPoint).toLong()) //14byte skip
            buffer = ByteArray(10)
            var valueBuffer: ByteArray? = ByteArray(size)
            var total = 0
            var len = 0
            while (raf.read(buffer).also { len = it } != -1) {
                if (total + len > size) {
                    len = size - total
                }
                System.arraycopy(buffer, 0, valueBuffer, total, len)
                total += len
                if (total >= size) {
                    break
                }
            }
            if (valueBuffer!![0].toInt() == 0) {
                strTmp.append(String(valueBuffer, 1, valueBuffer.size - 1, Charset.forName("euc-kr")))
            } else if (valueBuffer[0].toInt() == 1 && valueBuffer[1].toInt() == -1 && valueBuffer[2].toInt() == -2) {
                strTmp.append(String(valueBuffer, 1, valueBuffer.size - 1, Charset.forName("unicode")).trim { it <= ' ' })
            } else {
                strTmp.append(String(valueBuffer, 1, valueBuffer.size - 1, Charset.forName("utf-8")))
            }
            buffer = null
            valueBuffer = null
            return skipPoint + point + size
        } catch (e: Exception) {
            e(TAG, e)
        }
        return -1
    }

    private fun getFrameUSLT(raf: RandomAccessFile, point: Int, strTmp: StringBuilder): Int {    //title name
        try {
            raf.seek((point + 4).toLong()) // TIT2 태그 건너뜀 4byte
            var buffer: ByteArray? = ByteArray(7)
            raf.read(buffer) //			Log.e(TAG, "[" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
            var apicPoint = 8 //TIT2 Size 4 byte
            val size = String.format("%02X%02X%02X%02X", buffer!![0], buffer[1], buffer[2], buffer[3]).toInt(16)

            //flag 처리 필요
            apicPoint += 2 //APIC flag 2 byte

            //이하는 size에 포함된 범위
            var textHeaderPoint = 1 //01로 되어있는 알수 없는 flag


            //apicPoint += 14; //size가 하나 줄어야 하는거 같다
            raf.seek((point + apicPoint).toLong()) //14byte skip
            buffer = ByteArray(10)
            var valueBuffer: ByteArray? = ByteArray(size)
            var total = 0
            var len = 0
            while (raf.read(buffer).also { len = it } != -1) {
                if (total + len > size) {
                    len = size - total                    //Log.e(TAG,"Size = " + size + " total = " + total + " len = " + len);
                }
                System.arraycopy(buffer, 0, valueBuffer, total, len)
                total += len
                if (total >= size) {
                    break
                }
            }
            var encodeType = 0
            if (valueBuffer!![textHeaderPoint] == 'e'.code.toByte() && valueBuffer[textHeaderPoint + 1] == 'n'.code.toByte() && valueBuffer[textHeaderPoint + 2] == 'g'.code.toByte()) {
                encodeType = 0
                textHeaderPoint += 3 // Text Encoding Type
            } else if (valueBuffer[textHeaderPoint] == 'k'.code.toByte() && valueBuffer[textHeaderPoint + 1] == 'o'.code.toByte() && valueBuffer[textHeaderPoint + 2] == 'r'.code.toByte()) {
                encodeType = 1
                textHeaderPoint += 3 // Text Encoding Type
            }


            //textHeaderPoint += 8; // 알 수 없는 테그
            //FF FE 00 00 FF FE FF FE
            //00 00 FF FE
            var checkUniCodePoint = 0
            while (valueBuffer.size > textHeaderPoint + checkUniCodePoint) {                //Log.e(TAG, "[" + String.format("%d %d",valueBuffer[textHeaderPoint+checkUniCodePoint],valueBuffer[textHeaderPoint+checkUniCodePoint+1]) + "]" + " " + (valueBuffer[textHeaderPoint] == 255));
                if (valueBuffer[textHeaderPoint + checkUniCodePoint].toInt() == -1 && valueBuffer[textHeaderPoint + checkUniCodePoint + 1].toInt() == -2 || valueBuffer[textHeaderPoint + checkUniCodePoint].toInt() == 0 && valueBuffer[textHeaderPoint + checkUniCodePoint + 1].toInt() == 0) { //0x00 0x00
                    checkUniCodePoint += 2
                } else {
                    checkUniCodePoint -= 2
                    break
                }
            }
            if (checkUniCodePoint == 0) { // [3 LINES] 가 붙은 가사에 대한 Parser
                var is3LineUnicode = false
                for (i in 0..19) {
                    if (valueBuffer[i] == '['.code.toByte() && valueBuffer[i + 1] == '3'.code.toByte() && valueBuffer[i + 3] == 'L'.code.toByte() && valueBuffer[i + 4] == 'I'.code.toByte()) {
                        is3LineUnicode = true
                    }
                }
                if (is3LineUnicode) {
                    var index = 0
                    var isSkip = true
                    var i = 0
                    while (i < valueBuffer.size) {
                        if (valueBuffer[i] == 0x08.toByte() && valueBuffer[i + 1].toInt() == 0 || valueBuffer[i] == 0x07.toByte() && valueBuffer[i + 1].toInt() == 0) {
                            if (isSkip) {
                                valueBuffer[index] = -1
                                valueBuffer[index + 1] = -2
                                index += 2
                            } else {
                                if (valueBuffer[i] == 0x08.toByte() && valueBuffer[i + 1].toInt() == 0) {
                                    valueBuffer[index] = 0x0A.toByte()
                                    valueBuffer[index + 1] = 0x00.toByte()
                                } else {
                                    valueBuffer[index] = 0x20.toByte()
                                    valueBuffer[index + 1] = 0x00.toByte()
                                }
                                index += 2
                            }
                            isSkip = false
                            i += 5
                        } else if (!isSkip) {
                            valueBuffer[index] = valueBuffer[i]
                            index++
                        }
                        i++
                    }
                    strTmp.append(String(valueBuffer, 0, index, Charset.forName("unicode")))
                } else {
                    strTmp.append(String(valueBuffer, textHeaderPoint, valueBuffer.size - textHeaderPoint, Charset.forName("euc-kr")).trim { it <= ' ' })
                }
            } else if (checkUniCodePoint > 0) {
                textHeaderPoint += checkUniCodePoint
                strTmp.append(String(valueBuffer, textHeaderPoint, valueBuffer.size - textHeaderPoint, Charset.forName("unicode")))
            }
            val logFolder = File(Environment.getExternalStorageDirectory().toString() + "/log")
            if (!logFolder.exists()) {
                logFolder.mkdirs()
            }
            val tmpFolder = File(Environment.getExternalStorageDirectory().toString() + "/log/music.txt")
            val fos: FileOutputStream
            fos = FileOutputStream(tmpFolder)
            fos.write(valueBuffer, textHeaderPoint, valueBuffer.size - textHeaderPoint)
            fos.close()
            buffer = null
            valueBuffer = null
            return apicPoint + point + size
        } catch (e: Exception) {
            e(TAG, e)
        }
        return -1
    }

    private fun skipFrame(raf: RandomAccessFile, point: Int): Int {
        try {
            raf.seek((point + 4).toLong()) // TIT2 태그 건너뜀 4byte
            var buffer: ByteArray? = ByteArray(7)
            raf.read(buffer)            //Log.e(TAG, "1 [" + String.format("%02X %02X %02X %02X ",buffer[0],buffer[1],buffer[2],buffer[3]) + "]");
            val apicPoint = 10 //TIT2 Size 4 byte
            val size = String.format("%02X%02X%02X%02X", buffer!![0], buffer[1], buffer[2], buffer[3]).toInt(16)
            buffer = null
            return apicPoint + point + size
        } catch (e: NumberFormatException) {
            return -1
        } catch (e: Exception) {
            e(TAG, e)
        }
        return -1
    }

    private fun getFrameAPIC(raf: RandomAccessFile, point: Int): Int {    //커버 이미지
        try {
            raf.seek((point + 4).toLong()) // APIC 태그 건너뜀 4byte
            var buffer: ByteArray? = ByteArray(7)
            raf.read(buffer)
            var apicPoint = 8 //APIC Size 4 byte
            var size = String.format("%02X%02X%02X%02X", buffer!![0], buffer[1], buffer[2], buffer[3]).toInt(16)

            //flag 처리 필요
            apicPoint += 2 //APIC flag 2 byte
            size -= 14 // 알수 없는 14byte skip
            apicPoint += 14 // 알수 없는 14byte skip
            Log.e(TAG, "getAPIC 1 = $size point = $point apicPoint = $apicPoint")
            raf.seek((point + apicPoint).toLong()) //14byte skip
            buffer = ByteArray(1024)
            var imgBuffer: ByteArray? = ByteArray(size)
            var total = 0
            var len = 0
            while (raf.read(buffer).also { len = it } != -1) {
                if (total + len > size) {
                    len = size - total
                    Log.e(TAG, "Size = $size total = $total len = $len")
                }
                System.arraycopy(buffer, 0, imgBuffer, total, len)
                total += len
                if (total >= size) {
                    break
                }
            }

            //debug ----------------------------------
            var str = ""
            for (i in 0..9) {
                str = str + String.format("%02X ", imgBuffer!![i])
            }            //Log.e(TAG, "start [" + str + "]" + len);
            str = ""
            for (i in imgBuffer!!.size - 10 until imgBuffer.size) {
                str = str + String.format("%02X ", imgBuffer[i])
            }
            Log.e(TAG, "end [$str]")            //debug ----------------------------------
            val bmp = BitmapFactory.decodeByteArray(imgBuffer, 0, imgBuffer.size)
            Log.e(TAG, "Bitmap = $bmp")            //mImgView.setImageBitmap(bmp);
            buffer = null
            imgBuffer = null
            return apicPoint + point + size
        } catch (e: Exception) {
            e(TAG, e)
        }
        return -1
    }

    private fun getSynchSafeInt(v1: Byte, v2: Byte, v3: Byte): Int {
        val zeroTag = "0000000"
        val bit1 = Integer.toBinaryString(v1.toInt())
        var bit2 = Integer.toBinaryString(v2.toInt())
        if (bit2.length < 7) {
            bit2 = zeroTag.substring(8 - bit2.length) + bit2
        }
        var bit3 = Integer.toBinaryString(v3.toInt())
        if (bit3.length < 7) {
            bit3 = zeroTag.substring(8 - bit3.length) + bit3
        }
        val sizeStr = bit1 + bit2 + bit3
        return sizeStr.toInt(2)
    }
}