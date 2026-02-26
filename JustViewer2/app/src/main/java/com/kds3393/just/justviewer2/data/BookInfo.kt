package com.kds3393.just.justviewer2.data

import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.image.ZOOM_FIT_SCREEN
import com.kds3393.just.justviewer2.utils.CToast
import com.kds3393.just.justviewer2.utils.SubStringComparator
import common.lib.base.CoroutineTask
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import common.lib.utils.Size
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import java.nio.charset.Charset
import java.util.Collections

/**
 * @param targetPath : zip 파일의 path
 * @param isLeft : 페이지 진행 방향 : ImageViewer : 1 - left, 0 - right //MoviePlayer : 1 - portrait, 0 - landscape
 */
data class BookInfo(var targetPath: String, var isLeft:Int = 0) {
    var id: Long = 0        //DB id
    var viewSize = Size()
    var entryArray = ArrayList<FileHeader>()  //Zip file 내부의 image file entry
        private set
    var currentPage = 0     //현재 페이지 number
    var zoomType = ZOOM_FIT_SCREEN       //맞춤 zoom 형태    //TODO 정상 동작 하는지 확인 필요
    //비슷한 이름의 zip 파일 리스트
    var books:ArrayList<String> = ArrayList<String>()
        set(list) {
            field.clear()
            field.addAll(list)
            setTarget(targetPath)
        }
    /**
     * ImageViewer : custom zoom인 경우 기준 height
     * TODO 나중에 Textviewer랑 변수 분리하자
     */
    var zoomStandardHeight = 0
    var bookIndex = 0       /** @params books 의 index */

    override fun toString(): String {
        return "BookInfo:id[$id] currentPage[$currentPage] bookIndex[$bookIndex] zoomType[$zoomType] zoomStandardHeight[$zoomStandardHeight]\n$targetPath"
    }

    /** targetPath를 설정하고 targetPath에 해당하는 bookIndex를 셋팅 */
    private fun setTarget(path:String) {
        targetPath = path
        books.forEachIndexed { index, bookPath ->
            if (targetPath == bookPath) {
                bookIndex = index
                return@forEachIndexed
            }
        }
    }

    /**
     * 파일 삭제시 해당 targetPath를 다음이나 이전 book으로 setting한다.
     * @return 변경할 targetPath가 없으면 false 이때 imageviewer를 종료해준다.
     */
    fun removeCurrentBook() : Boolean {
        books.remove(targetPath)
        if (books.isEmpty()) {
            return false
        }
        val path = if (books.size > bookIndex) books[bookIndex] else books.last()
        setTarget(path)
        return true
    }

    fun isNextBook(): Boolean {
        if (books.isEmpty()) return false
        return books.size - 1 > bookIndex
    }

    fun isPrevBook(): Boolean {
        return bookIndex > 0
    }

    fun isNext(): Boolean {
        return entryArray.size - 1 > currentPage
    }

    fun isPrev(): Boolean {
        return currentPage > 0
    }

    fun setNextBook(): Boolean {
        bookIndex++
        return if (books.isNotEmpty() && books.size > bookIndex) {
            targetPath = books[bookIndex]
            true
        } else {
            false
        }
    }

    fun setPrevBook(): Boolean {
        bookIndex--
        return if (books.isNotEmpty() && bookIndex >= 0) {
            targetPath = books[bookIndex]
            true
        } else {
            false
        }
    }

    var isBookChanging = false
        private set
    fun loadBook(onPostExecute:(() -> Unit)? = null) {
        CoroutineTask(
            onPreExecute = {
                isBookChanging = true
            }, doInBackground = { params ->
                if (params != null) {
                    try {
                        CLog.e("KDS3393_TEST_ZIP targetPath = $targetPath")
                        val zFile = ZipFile(targetPath)
                        zFile.setCharset(Charset.forName("EUC-KR"))
                        val fileHeaders = zFile.fileHeaders

                        entryArray.clear()
                        for (header in fileHeaders) {
                            if (header.isDirectory) continue

                            val extension = FileUtils.getExtension(header.fileName)
                            if (extension.equals("jpg", ignoreCase = true) ||
                                extension.equals("jpeg", ignoreCase = true) ||
                                extension.equals("png", ignoreCase = true) ||
                                extension.equals("bmp", ignoreCase = true) ||
                                extension.equals("webp", ignoreCase = true) ||
                                extension.equals("gif", ignoreCase = true)) {
                                entryArray.add(header)
                            }
                        }
                        CLog.e("KDS3393_TEST_Zip Loaded Size[${entryArray.size}]")
                        Collections.sort(entryArray, SubStringComparator(com.kds3393.just.justviewer2.config.SettingImageViewer.getIsPageRight(com.kds3393.just.justviewer2.CApp.get())))
                    } catch (e: Exception) {
                        CToast.normal("압축파일을 푸는 도중 오류가 발생하였습니다.")
                        CLog.e(e)
                    }

                    //initConfig
                    val book = DBMgr.instance.imageDataLoad(targetPath)
                    if (book == null) {
                        var isLeftView = 1

                        if (!com.kds3393.just.justviewer2.config.SettingImageViewer.getIsPageRight(com.kds3393.just.justviewer2.CApp.get())) isLeftView = 0
                        this@BookInfo.currentPage = 0
                        this@BookInfo.isLeft = isLeftView
                        DBMgr.instance.insertImageData(this@BookInfo)
                    } else {
                        this@BookInfo.id = book.id
                        this@BookInfo.currentPage = book.currentPage
                        this@BookInfo.isLeft = book.isLeft
                        this@BookInfo.zoomType = book.zoomType
                        this@BookInfo.zoomStandardHeight = book.zoomStandardHeight
                    }
                }
            }, onPostExecute = {
                onPostExecute?.invoke()
                isBookChanging = false
            }).execute()
    }
}