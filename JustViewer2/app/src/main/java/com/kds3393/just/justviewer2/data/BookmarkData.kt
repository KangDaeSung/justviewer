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
import java.io.Serializable
import java.nio.charset.Charset
import java.util.Collections

const val BOOKMARK_TYPE_IMAGE = 0
const val BOOKMARK_TYPE_TEXT = 1
/**
 * @param targetPath : 파일의 경로
 * @param bookType : 뷰어 타입 (DBInfo.TYPE_IMAGE 또는 DBInfo.TYPE_TEXT)
 * @param isLeft : 페이지 진행 방향 (1 - left, 0 - right)
 */
data class BookmarkData(
    var targetPath: String,
    var bookType: Int = BOOKMARK_TYPE_IMAGE,
    var isLeft: Int = 0
) : Serializable {

    // 공통 필드
    var id: Long = -1L
    var currentPage = 0

    // ---------------- ImageViewer 전용 필드 ----------------
    var viewSize = Size()
    var entryArray = ArrayList<FileHeader>()
        private set
    var zoomType = ZOOM_FIT_SCREEN
    var zoomStandardHeight = 0
    var books = ArrayList<String>()
        set(list) {
            field.clear()
            field.addAll(list)
            setTarget(targetPath)
        }
    var bookIndex = 0
    var isBookChanging = false
        private set

    // ---------------- TextViewer 전용 필드 ----------------
    var hideLine = 0

    override fun toString(): String {
        return "BookmarkData: id[$id] type[$bookType] page[$currentPage] bookIndex[$bookIndex] zoomType[$zoomType] hideLine[$hideLine]\n$targetPath"
    }

    // ---------------- 공통 및 이미지 뷰어 로직 ----------------
    private fun setTarget(path: String) {
        targetPath = path
        books.forEachIndexed { index, bookPath ->
            if (targetPath == bookPath) {
                bookIndex = index
                return@forEachIndexed
            }
        }
    }

    fun removeCurrentBook(): Boolean {
        books.remove(targetPath)
        if (books.isEmpty()) return false

        val path = if (books.size > bookIndex) books[bookIndex] else books.last()
        setTarget(path)
        return true
    }

    fun isNextBook(): Boolean = books.isNotEmpty() && books.size - 1 > bookIndex
    fun isPrevBook(): Boolean = bookIndex > 0
    fun isNext(): Boolean = entryArray.size - 1 > currentPage
    fun isPrev(): Boolean = currentPage > 0

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

    fun loadBook(onPostExecute: (() -> Unit)? = null) {
        // 텍스트 뷰어일 경우 압축 해제 로직을 건너뜁니다.
        if (bookType == BOOKMARK_TYPE_TEXT) {
            onPostExecute?.invoke()
            return
        }

        CoroutineTask(
            onPreExecute = {
                isBookChanging = true
            },
            doInBackground = { params ->
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
                            if (listOf("jpg", "jpeg", "png", "bmp", "webp", "gif").contains(extension.lowercase())) {
                                entryArray.add(header)
                            }
                        }
                        CLog.e("KDS3393_TEST_Zip Loaded Size[${entryArray.size}]")
                        Collections.sort(entryArray, SubStringComparator(com.kds3393.just.justviewer2.config.SettingImageViewer.getIsPageRight(com.kds3393.just.justviewer2.CApp.get())))
                    } catch (e: Exception) {
                        CToast.normal("압축파일을 푸는 도중 오류가 발생하였습니다.")
                        CLog.e(e)
                    }

                    // 통합 DB에서 데이터 조회 (수정됨)
                    val book = DBMgr.instance.loadBookmark(targetPath, BOOKMARK_TYPE_TEXT)
                    if (book == null) {
                        var isLeftView = 1
                        if (!com.kds3393.just.justviewer2.config.SettingImageViewer.getIsPageRight(com.kds3393.just.justviewer2.CApp.get())) isLeftView = 0

                        this@BookmarkData.currentPage = 0
                        this@BookmarkData.isLeft = isLeftView
                        this@BookmarkData.bookType = BOOKMARK_TYPE_TEXT

                        // DB 데이터 삽입 (수정됨)
                        DBMgr.instance.insertBookmark(this@BookmarkData)
                    } else {
                        // DB에 데이터가 존재하면 기존 데이터 로드
                        this@BookmarkData.id = book.id
                        this@BookmarkData.currentPage = book.currentPage
                        this@BookmarkData.isLeft = book.isLeft
                        this@BookmarkData.zoomType = book.zoomType
                        this@BookmarkData.zoomStandardHeight = book.zoomStandardHeight
                        this@BookmarkData.hideLine = book.hideLine
                    }
                }
            },
            onPostExecute = {
                onPostExecute?.invoke()
                isBookChanging = false
            }
        ).execute()
    }
}