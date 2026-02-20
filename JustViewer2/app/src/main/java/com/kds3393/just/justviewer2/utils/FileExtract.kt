package com.kds3393.just.justviewer2.utils

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import common.lib.utils.FileUtils
import common.lib.utils.ImageUtils
import common.lib.debug.CLog.e
import common.lib.debug.DevUtils
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipFile
import java.io.*
import java.util.*

class FileExtract(private val mContext: Context) {
    private var mProgress: ProgressDialog? = null
    private var mTail = ""
    private var mIsAsset = false

    interface OnExtractListener {
        fun onStart()
        fun onEnd()
        fun onError(err: Int)
    }

    protected var mOnExtractListener: OnExtractListener? = null
    fun setOnExtractListener(listener: OnExtractListener?) {
        mOnExtractListener = listener
    }

    fun setIsAsset(isAsset: Boolean) {
        mIsAsset = isAsset
    }

    fun extractFile(zipFilePath: String?, filename: String?, unCompressPath: String?): File? {
        var `is`: InputStream? = null
        var entryName: String? = null
        try {
            val file = File(zipFilePath)
            `is` = if (file.exists()) FileInputStream(File(zipFilePath)) else {
                return null
            }
            val zipFile = ZipArchiveInputStream(`is`)
            var zentry: ZipArchiveEntry? = null
            val buffer = ByteArray(BUFFER_SIZE)
            while (zipFile.nextZipEntry.also { zentry = it } != null) {
                if (zentry!!.name.equals(filename, ignoreCase = true)) {
                    entryName = zentry!!.name
                    val targetFile = File(unCompressPath, entryName)
                    if (zentry!!.isDirectory && targetFile.absolutePath != null) {
                        val makeDir = File(targetFile.absolutePath)
                        makeDir.mkdirs()
                    } else {
                        if (targetFile.parent != null) {
                            val makeDir = File(targetFile.parent)
                            makeDir.mkdirs()
                        }
                        var fos: FileOutputStream? = null
                        try {
                            fos = FileOutputStream(targetFile)
                            var len = 0
                            while (zipFile.read(buffer).also { len = it } != -1) {
                                if (len == 0) fos.write(buffer, 0, len)
                            }
                        } catch (e: FileNotFoundException) {
                            fos?.close()
                        } finally {
                            fos?.close()
                        }
                    }
                }
            }
            zipFile.close()
        } catch (e: IOException) {
            e(TAG, e)
        }
        return File(unCompressPath, entryName)
    }

    fun extractFiles(compressedPath: String?, tail: String, progress: ProgressDialog?): Int {
        mTail = tail
        mProgress = progress
        var `is`: InputStream? = null
        try {
            val file = File(compressedPath)
            `is` = if (file.exists()) FileInputStream(File(compressedPath)) else {
                return -1
            }
            val zipFile = ZipArchiveInputStream(`is`)
            val unZipSize = getZipFileSize(zipFile)
            mProgress!!.max = unZipSize.toInt()
            `is`.close()
            `is` = FileInputStream(File(compressedPath))
        } catch (e: IOException) {
            e(TAG, e)
        }
        FileExtractTask().execute(`is`, compressedPath)
        return 1
    }

    @Throws(IOException::class)
    private fun getZipFileSize(file: ZipArchiveInputStream): Long {
        var totalSize: Long = 0
        var zentry: ZipArchiveEntry? = null
        while (file.nextZipEntry.also { zentry = it } != null) {
            totalSize += zentry!!.size
        }
        return totalSize
    }

    protected inner class FileExtractTask : AsyncTask<Any?, Int, Boolean>() {
        private var mCurrentSize: Long = 0
        protected override fun doInBackground(vararg params: Any?): Boolean {
            var zentry: ZipArchiveEntry? = null
            var zipFile: ZipArchiveInputStream? = null
            try {
                zipFile = ZipArchiveInputStream(params[0] as InputStream)
                val unCompressedPath = params[1] as String
                if (mOnExtractListener != null) {
                    mOnExtractListener!!.onStart()
                }
                val buffer = ByteArray(BUFFER_SIZE)
                while (zipFile.nextZipEntry.also { zentry = it } != null) {
                    val tmpName = zentry!!.name
                    val targetFile = File("$unCompressedPath/", tmpName)
                    if (zentry!!.isDirectory && targetFile.absolutePath != null) {
                        val makeDir = File(targetFile.absolutePath)
                        makeDir.mkdirs()
                    } else {
                        if (targetFile.parent != null) {
                            val makeDir = File(targetFile.parent)
                            makeDir.mkdirs()
                        }
                        var fos: FileOutputStream? = null
                        try {
                            fos = FileOutputStream(targetFile)
                            var len = 0
                            while (zipFile.read(buffer).also { len = it } != -1) {
                                fos.write(buffer, 0, len)
                                mCurrentSize += len.toLong()
                                publishProgress(mCurrentSize.toInt())
                            }
                        } catch (e: FileNotFoundException) {
                            fos?.close()
                        } finally {
                            fos?.close()
                        }
                    }
                }
                zipFile.close()
                val noMediaFile = File("$unCompressedPath.nomedia")
                if (!noMediaFile.exists()) noMediaFile.mkdir()
            } catch (e: IOException) {
                e(TAG, e)
                if (zipFile != null) {
                    try {
                        zipFile.close()
                    } catch (e1: IOException) {
                        e(TAG, e)
                    }
                }
                return false
            } catch (e: Exception) {
                e(TAG, e)
                if (zipFile != null) {
                    try {
                        zipFile.close()
                    } catch (e1: IOException) {
                        e(TAG, e)
                    }
                }
                return false
            } finally {
            }
            return true
        }

        override fun onProgressUpdate(vararg values: Int?) {
            mProgress!!.progress = values[0]!!
        }

        override fun onPostExecute(result: Boolean) {
            if (mProgress != null) mProgress!!.dismiss()
            if (!result) {
                if (mOnExtractListener != null) mOnExtractListener!!.onError(ERR_DECOMPRESSION)
                return
            }
            if (mOnExtractListener != null) {
                mOnExtractListener!!.onEnd()
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (mProgress != null) {
                mProgress!!.progress = 0
                mProgress!!.show()
            }
        }
    }

    fun compressFile(path: String?, zipPath: String?, progress: ProgressDialog?) {
        mProgress = progress
        val fileList = ArrayList<File>()
        val file = File(path)
        fileGather(fileList, file)
        val zipFile = File(zipPath)
        var dir: File? = null
        dir = File(zipFile.parent)
        if (!dir.exists()) dir.mkdirs()
        FileCompressTask().execute(zipPath, fileList, path)
    }

    private fun fileGather(fileList: ArrayList<File>, dir: File) {
        val listFiles = dir.listFiles()
        for (file in listFiles) {
            if (file.isFile) {
                fileList.add(file)
            } else if (file.isDirectory) {
                fileGather(fileList, file)
            }
        }
    }

    protected inner class FileCompressTask : AsyncTask<Any, Int, String?>() {
        private val mCurrentSize: Long = 0
        protected override fun doInBackground(vararg params: Any): String? {
            val zipPath = params[0] as String
            val fileList = params[1] as ArrayList<File>
            val rootPath = params[2] as String
            if (mOnExtractListener != null) {
                mOnExtractListener!!.onStart()
            }
            try {
                val zipFile = ZipArchiveOutputStream(FileOutputStream(zipPath))
                val buffer = ByteArray(BUFFER_SIZE)
                for (file in fileList) {
                    val `in` = FileInputStream(file)
                    val zipFilePath = file.path.substring(rootPath.length, file.path.length)
                    zipFile.putArchiveEntry(ZipArchiveEntry(zipFilePath))
                    var data: Int
                    while (`in`.read(buffer).also { data = it } > 0) {
                        zipFile.write(buffer, 0, data)
                    }
                    zipFile.closeArchiveEntry()
                    `in`.close()
                }
                zipFile.close()
            } catch (e: IOException) {
                e(TAG, e)
                return null
            }
            return zipPath
        }

        protected override fun onProgressUpdate(vararg value: Int?) {
            mProgress!!.progress = value[0]!!
        }

        override fun onPostExecute(result: String?) {
            if (mProgress != null) {
                mProgress!!.dismiss()
            }
            if (result == null) {
                if (mOnExtractListener != null) mOnExtractListener!!.onError(ERR_DECOMPRESSION)
                return
            }
            if (mOnExtractListener != null) {
                mOnExtractListener!!.onEnd()
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (mProgress != null) {
                mProgress!!.progress = 0
                mProgress!!.show()
            }
        }
    }

    companion object {
        private const val TAG = "FileExtract"
        private const val BUFFER_SIZE = 1024 * 16
        const val ERR_DECOMPRESSION = -3
        fun GetZipEntry(path: String?): ArrayList<ZipEntry>? {
            DevUtils.sStartTime(1)
            var zipFile: ZipArchiveInputStream
            try {
                val zFile = ZipFile(path, "EUC-KR")
                val zipEntries = zFile.entries
                val entryArray = ArrayList<ZipEntry>()
                while (zipEntries.hasMoreElements()) {
                    val entry = zipEntries.nextElement() as ZipEntry
                    val extension = FileUtils.getExtension(entry.name)
                    if (extension.equals("jpg", ignoreCase = true) || extension.equals("jpeg", ignoreCase = true) || extension.equals("png", ignoreCase = true) || extension.equals("bmp", ignoreCase = true) || extension.equals("gif", ignoreCase = true)) {
                        entryArray.add(entry)
                    }
                }
                zFile.close()
                Collections.sort(entryArray, SubStringComparator())
                return entryArray
            } catch (e: IOException) {
                e.printStackTrace()
            }
            DevUtils.sEndTime(1)
            return null
        }

        fun unzipTargetBuffer(zipPath: String?, entry: ZipEntry): ByteArray? {
            try {
                //	    	ZipArchiveInputStream zipFile = new ZipArchiveInputStream(new FileInputStream(zipPath),"euc-kr",false);
                var len = 0
                val zentry: ZipArchiveEntry? = null
                var buffer: ByteArray? = ByteArray(1024)
                val imagebuffer = ByteArray(entry.size.toInt())
                var total = 0
                val zFile = ZipFile(zipPath, "EUC-KR")
                val newEntry = zFile.getEntry(entry.name)
                val zio = zFile.getInputStream(newEntry)
                while (zio.read(buffer).also { len = it } != -1) {
                    System.arraycopy(buffer, 0, imagebuffer, total, len)
                    total += len
                }
                zio.close()
                buffer = null
                return imagebuffer
            } catch (e: Exception) {
                e(TAG, e)
            }
            return null
        }

        fun unzipTargetFile(zipPath: String?, targetPath: String, targetEntry: ZipEntry) {
            try {
                var fileBuffer = unzipTargetBuffer(zipPath, targetEntry)
                val tmpFolder = File(targetPath + targetEntry)
                val fos: FileOutputStream
                fos = FileOutputStream(tmpFolder)
                fos.write(fileBuffer)
                fileBuffer = null
                fos.close()
            } catch (e: Exception) {
                e(TAG, e)
            }
        }

        fun unzipTargetImage(zipPath: String?, targetEntry: ZipEntry, maxWidth: Int, maxHeight: Int): Bitmap? {
            try {
                var imagebuffer = unzipTargetBuffer(zipPath, targetEntry)
                val bmp = BitmapFactory.decodeByteArray(imagebuffer, 0, imagebuffer!!.size, getBitmapOption(imagebuffer, maxWidth, maxHeight))
                imagebuffer = null
                return bmp
            } catch (e: Exception) {
                e(TAG, e)
            }
            return null
        }

        fun unzipThumbImage(zipPath: String?, targetEntry: ZipEntry, maxWidth: Int, maxHeight: Int): Bitmap? {
            try {
                var imagebuffer = unzipTargetBuffer(zipPath, targetEntry)
                val bmp = BitmapFactory.decodeByteArray(imagebuffer, 0, imagebuffer!!.size, getBitmapOption(imagebuffer, maxWidth, maxHeight))
                imagebuffer = null
                return bmp
            } catch (e: Exception) {
                e(TAG, e)
            }
            return null
        }

        fun getBitmapOption(buffer: ByteArray?, width: Int, height: Int): BitmapFactory.Options {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(buffer, 0, buffer!!.size, options)
            val imgWidth = options.outWidth
            val imgHeight = options.outHeight
            var scale = 1
            if (imgWidth > 0 && imgHeight > 0) {
                if (imgWidth > imgHeight && imgWidth > width) {
                    scale = imgWidth / width
                }
                if (imgWidth <= imgHeight && imgHeight > height) {
                    scale = imgHeight / height
                }
            } else {
                e(TAG, "Exception:divide by zero")
            }
            options.inSampleSize = common.lib.utils.ImageUtils.getSampleSize(scale)
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inJustDecodeBounds = false
            return options
        }
    }
}