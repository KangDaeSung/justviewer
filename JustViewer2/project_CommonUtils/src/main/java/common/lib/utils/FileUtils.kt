package common.lib.utils

import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import common.lib.debug.CLog.e
import java.io.*
import java.util.*
import java.util.regex.Pattern
import java.util.zip.CRC32
import java.util.zip.Checksum

object FileUtils {
    private const val TAG = "FileUtils"
    var PATH_SDCARD = Environment.getExternalStorageDirectory().path

    /**
     * 파일을 존재여부를 확인하는 메소드
     *
     * @param filePath : 확인하기 위한 filePath
     * @return
     */
    fun fileIsLive(filePath: String?): Boolean {
        val f1 = File(filePath)
        return f1.exists() && f1.isFile
    }

    /**
     * 파일 이름 변경
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    fun rename(oldPath: String?, newPath: String?): Boolean {
        val result: Boolean
        val oldFile = File(oldPath)
        val newFile = File(newPath)
        result = if (oldFile != null && oldFile.exists() && oldFile.renameTo(newFile)) {
            true
        } else {
            false
        }
        return result
    }

    /**
     * 파일을 생성하는 매소드
     *
     * @param makeFileName
     */
    fun fileMake(makeFileName: String?) {
        try {
            val f1 = File(makeFileName)
            f1.createNewFile()
        } catch (e: Exception) {
            e("fileMake", e)
        }
    }

    /**
     * 파일/폴더 삭제
     * @param file : 삭제할 파일 또는 폴더 경로, 폴더는 하위 폴더의 모든 내용을 재귀 호출로 삭제한다.
     */
    fun deleteFile(file: File?) {
        if (file != null && file.exists()) {
            if (file.isDirectory) {
                val childFileList = file.listFiles()
                childFileList?.forEach {
                    if (it.isDirectory) {
                        deleteFile(it)
                    } else {
                        it.delete()
                    }
                }
            }
            file.delete()
        }
    }

    /**
     * 파일을 복사하는 메소드
     *
     * @param oriPath 원본 파일 패스
     * @param targetPath 복사될 파일 패스
     * @return
     */
    fun fileCopy(oriPath: String?, targetPath: String?): Boolean {
        if (oriPath == null || targetPath == null) return false
        val oriFile = File(oriPath)
        val targetFile = File(targetPath)
        val inputStream: FileInputStream
        try {
            inputStream = FileInputStream(oriFile)
            val outputStream = FileOutputStream(targetFile)
            val fcin = inputStream.channel
            val fcout = outputStream.channel
            val size = fcin.size()
            fcin.transferTo(0, size, fcout)
            fcout.close()
            fcin.close()
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e(TAG, e)
            return false
        }
        return true
    }

    /**
     * 폴더 복사
     *
     * @param oriPath 원본 파일 패스
     * @param targetPath 복사될 파일 패스
     */
    fun copyDirectory(oriPath: String?, targetPath: String?) {
        val sourceLocation = File(oriPath)
        val targetLocation = File(targetPath)
        copyDirectory(sourceLocation, targetLocation)
    }

    /**
     * 폴더 복사
     *
     * @param oriDir 원본 파일
     * @param targetDir 복사될 파일
     */
    fun copyDirectory(oriDir: File, targetDir: File) {
        if (oriDir.isDirectory) {
            if (!targetDir.exists()) {
                targetDir.mkdir()
            }
            val children = oriDir.list()
            for (i in children.indices) {
                copyDirectory(File(oriDir, children[i]), File(targetDir, children[i]))
            }
        } else {
            val `in`: InputStream
            try {
                `in` = FileInputStream(oriDir)
                val out: OutputStream = FileOutputStream(targetDir)
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 디렉토리의 파일 리스트를 읽는 메소드
     *
     * @param dirPath
     * @return
     */
    fun getDirFileList(dirPath: String?): ArrayList<File>? {
        if (TextUtils.isEmpty(dirPath)) return null
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            return null
        }
        var dirFileList = ArrayList<File>()
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                dirFileList.addAll(files)
            }
        }
        return dirFileList
    }

    //String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
    val externalMounts: String?
        // parse output?
        get() {
            val out = HashSet<String>() //String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
            var pattern = Pattern.compile("(?i).*media_rw.*(storage).*(sdcardfs).*rw.*")
            var s = ""
            try {
                val process = ProcessBuilder().command("mount").redirectErrorStream(true).start()
                process.waitFor()
                val `is` = process.inputStream
                val buffer = ByteArray(1024)
                while (`is`.read(buffer) != -1) {
                    s = s + String(buffer)
                }
                `is`.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // parse output
            val lines = s.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                if (!line.lowercase().contains("asec")) {
                    if (pattern.matcher(line).matches()) {
                        val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (part in parts) {
                            if (part.startsWith("/")) {
                                if (!part.lowercase().contains("vold") && !part.lowercase().contains("/mnt/")) {
                                    out.add(part)
                                }
                            }
                        }
                    }
                }
            }
            for (extSDCardPath in out) {
                return extSDCardPath
            }
            return null
        }

    /**
     * 파일 확장자 얻기
     * @param fileStr
     * @return
     */
	@JvmStatic
	fun getExtension(fileStr: String?): String {
        if (fileStr.isNullOrEmpty()) {
            return ""
        }
        val fileName = fileStr.substring(fileStr.lastIndexOf("/") + 1, fileStr.length)
        return if (fileName.lastIndexOf(".") < 0) {
            ""
        } else fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
    }

    /**
     * 부모 폴더 경로 얻기
     * @param fileStr
     * @return
     */
    fun getParentPath(fileStr: String): String {
        return if (fileStr.lastIndexOf("/") > 0) {
            fileStr.substring(0, fileStr.lastIndexOf("/"))
        } else {
            fileStr
        }
    }

    /**
     * 파일 이름만 얻기
     * @param fileStr
     * @return
     */
    fun getFileName(fileStr: String): String {
        val fileName = fileStr.substring(fileStr.lastIndexOf("/") + 1, fileStr.length)
        return if (fileName.lastIndexOf(".") > 0) fileName.substring(0, fileName.lastIndexOf(".")) else fileName
    }

    /**
     * 파일의 이름과 확장자 얻기
     * @param fileStr
     * @return
     */
    fun getName(fileStr: String): String {
        return fileStr.substring(fileStr.lastIndexOf("/") + 1, fileStr.length)
    }

    /**
     * 해당 경로의 sdcard의 남은 용량을 가져온다.
     * @param path
     * @return
     */
    fun getSDCardStorageByte(path: String?): Long {
        val stat = StatFs(path)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            stat.freeBlocks.toLong() * stat.blockSize.toLong()
        } else stat.freeBlocksLong * stat.blockSizeLong
    }

    /**
     * 해당 경로의 sdcard의 용량을 Mb단위로 가져온다.
     * @param path
     * @return
     */
    fun getSDCardStorageMB(path: String?): Long {
        return getSDCardStorageByte(path) / 1024 / 1024
    }

    /**
     * 파일의 size를 가져온다.
     * @param file
     * @return
     */
    fun getfilesSize(file: File): Long {
        return getfilesSize(0, file)
    }

    fun getfilesSize(strDir: String?): Long {
        val dir = File(strDir)
        return getfilesSize(0, dir)
    }

    fun getfilesSize(size: Long, file: File): Long {
        var totalSize: Long = 0
        if (file.isDirectory) {
            val files = getDirFileList(file.path)
            for (f in files!!) {
                totalSize += getfilesSize(f)
            }
        } else {
            totalSize += file.length()
        }
        return totalSize
    }

    /**
     * 파일의 CRC32값을 가져온다.
     * @param readFile
     * @return CRC32
     */
    fun getCRC32Value(readFile: String): Long {
        val crc: Checksum = CRC32()
        try {
            val raf = RandomAccessFile(readFile, "r")
            raf.seek(0)
            val buffer = ByteArray(readFile.length)
            raf.read(buffer)
            crc.update(buffer, 0, readFile.length)
            raf.close()
        } catch (e: IOException) {
            e(TAG, e)
        }
        return crc.value
    }

    /**
     * 파일의 내용을 Byte로 반환합니다.
     * @param file
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getBytesFromFile(file: File): ByteArray {
        val `is`: InputStream = FileInputStream(file)
        val length = file.length()
        check(length <= Int.MAX_VALUE) { "파일의 크기가 Integer.MAX_VALUE를 넘으면 안됩니다." }
        val bytes = ByteArray(length.toInt())
        var offset = 0
        var numRead = 0
        while (offset < bytes.size && `is`.read(bytes, offset, bytes.size - offset).also { numRead = it } >= 0) {
            offset += numRead
        }
        if (offset < bytes.size) {
            throw IOException("Could not completely read file " + file.name)
        }
        `is`.close()
        return bytes
    }
}