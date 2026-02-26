package com.kds3393.just.justviewer2.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import androidx.core.view.doOnLayout
import com.kds3393.just.justviewer2.config.SettingImageViewer
import com.kds3393.just.justviewer2.config.SharedPrefHelper
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.dialog.BaseDialog
import com.kds3393.just.justviewer2.dialog.DlgAlert
import com.kds3393.just.justviewer2.utils.CToast
import com.kds3393.just.justviewer2.utils.SubStringComparator
import com.kds3393.just.justviewer2.utils.glide.ZipData
import common.lib.base.ActBaseLib
import common.lib.base.getFileName
import common.lib.base.hide
import common.lib.base.show
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import java.io.File
import java.util.ArrayDeque
import java.util.Collections
import kotlin.math.abs
import androidx.core.view.isEmpty
import com.kds3393.just.justviewer2.data.BookmarkData

const val ZOOM_NONE = -1        //최초 미설정
const val ZOOM_HALF_SCREEN = 0  //화면의 2배로 늘려 확대해서 본다. 가로가 긴 이미지를 좌우 스크롤로 볼수 있다.
const val ZOOM_FIT_HEIGHT = 1   //높이에 맞춰서 늘려준다. 화면에 꽉차게 나온다.
const val ZOOM_FIT_SCREEN = 2   //화면에 맞춘다.
const val ZOOM_USER_CUSTOM = 3

class ImageViewer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ImageViewerConfig(context,attrs), GestureDetector.OnGestureListener, OnDoubleTapListener {
    private val mAct: ActBaseLib
    private val linkedViews = ArrayDeque<PageView>()
    private var mIsFirstView = true
    private var mOnPageSelectedListener: OnPageSelectedListener? = null
    fun setOnPageSelectedListener(listener: OnPageSelectedListener?) {
        mOnPageSelectedListener = listener
    }

    interface OnPageSelectedListener {
        fun onPageSelected(CurrentPage: PageView?, index: Int)
        fun onBookSelected(path: String)
        fun onSingleTab()
        fun onLoaded(pageSize: Int)
    }

    private var mGestureDetector: GestureDetector? = null
    private val mScaleGestureDetector: ScaleGestureDetector
    init {
        mAct = ActBaseLib.unwrap(context)
        mGestureDetector = GestureDetector(context, this)
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        for (index in 0 until 3) {
            linkedViews.add(pageViewMaker(getContext(), this, index))
        }
    }

    fun setup(info: BookmarkData) : Boolean{
        bookInfo = info
        CLog.e("KDS3393_TEST_setup zoomType[${bookInfo.zoomType}] bookInfo = $bookInfo")
        doOnLayout {
            setViewSize(width,height)
            linkedViews.forEach {page ->
                page.parentViewSize.Width = width
                page.parentViewSize.Height = height
                bookInfo.zoomStandardHeight.let { page.zoomStandardHeight = it }
                page.zoomType = bookInfo.zoomType
            }
            runInitTask()
        }
        return true
    }

    private fun setViewSize(width: Int, height: Int) {
        CLog.e("KDS3393_TEST_width[$width] height[$height]")
        bookInfo.viewSize.Width = width
        bookInfo.viewSize.Height = height
        movePageHThreshold = (bookInfo.viewSize.Width * 0.1).toInt()
    }

    private fun getPrevPage() : PageView {
        return linkedViews.first()
    }

    private fun getCenterPage() : PageView {
        return linkedViews.elementAt(1)
    }

    private fun getNextPage() : PageView {
        return linkedViews.last()
    }

    fun moveRight() {
        if (mFlingRunnable.isFinished) {
            if (!bookInfo.isNext() && getCenterPage().right <= bookInfo.viewSize.Width) {
                moveBook(true) //Next Book
                return
            }

            val distance : Int
            var isViewWidthDistance = false
            if (bookInfo.zoomType == ZOOM_USER_CUSTOM) {
                if (getCenterPage().width > bookInfo.viewSize.Width * 2) isViewWidthDistance = true
            }
            if (bookInfo.zoomType == ZOOM_FIT_HEIGHT || isViewWidthDistance) {                //distance = (views[1].getWidth() / 2) + mSpacing;
                val half = getHalfPage(getCenterPage())
                val skipMargin = (half - bookInfo.viewSize.Width) / 2
                val left = abs(getCenterPage().left)
                val offset : Int
                if (left < getCenterPage().width / 2) {    //오른쪽 페이지로 이동
                    offset = left - skipMargin
                    distance = half - offset
                } else { // 다음 이미지의 왼쪽 페이지로 이동
                    if (bookInfo.isNext()) {
                        val page2Margin = (getHalfPage(getNextPage()) - bookInfo.viewSize.Width) / 2
                        offset = left - half - skipMargin
                        distance = bookInfo.viewSize.Width + skipMargin + mSpacing + page2Margin - offset
                    } else {
                        distance = getCenterPage().width - (left + bookInfo.viewSize.Width)
                    }
                }
            } else if (getCenterPage().right < bookInfo.viewSize.Width) {
                mFlingRunnable.startUsingVelocity(-8000)
                return
            } else if (bookInfo.zoomType == ZOOM_USER_CUSTOM) {
                distance = if (getCenterPage().right <= bookInfo.viewSize.Width) {
                    bookInfo.viewSize.Width + mSpacing
                } else {
                    getCenterPage().right - bookInfo.viewSize.Width
                }
            } else {
                distance = bookInfo.viewSize.Width + mSpacing
            }
            mFlingRunnable.startUsingDistance(-distance)
        }
    }

    fun moveLeft() {
        if (mFlingRunnable.isFinished) {
            if (!bookInfo.isPrev() && getCenterPage().left >= 0) {
                moveBook(false) //Prev Book
                return
            }
            val distance : Int
            var isViewWidthDistance = false
            if (bookInfo.zoomType == ZOOM_USER_CUSTOM) {
                if (getCenterPage().width > bookInfo.viewSize.Width * 2) isViewWidthDistance = true
            }
            if (bookInfo.zoomType == ZOOM_FIT_HEIGHT || isViewWidthDistance) {                //distance = (views[1].getWidth() / 2) + mSpacing;
                val half = getHalfPage(getCenterPage())
                val skipMargin = (half - bookInfo.viewSize.Width) / 2
                val left = abs(getCenterPage().left)
                val offset : Int
                if (left > getHalfPage(getCenterPage())) {    //왼쪽 페이지로 이동
                    offset = left - half - skipMargin
                    distance = half + offset
                } else { // 이전 이미지의 오른쪽 페이지로 이동
                    if (bookInfo.isPrev()) {
                        val page0Margin = (getHalfPage(getPrevPage()) - bookInfo.viewSize.Width) / 2
                        offset = left - skipMargin
                        distance = bookInfo.viewSize.Width + page0Margin + mSpacing + skipMargin + offset
                    } else {
                        distance = left
                    }
                }
            } else if (getCenterPage().left > 0) {
                mFlingRunnable.startUsingVelocity(8000)
                return
            } else if (bookInfo.zoomType == ZOOM_USER_CUSTOM) {
                distance = if (getCenterPage().left < 0) {
                    abs(getCenterPage().left)
                } else {
                    bookInfo.viewSize.Width + mSpacing
                }
            } else {
                distance = bookInfo.viewSize.Width + mSpacing
            }
            mFlingRunnable.startUsingDistance(distance)
        }
    }

    private fun getHalfPage(view: View): Int {
        return if (view.width > view.height) {
            view.width / 2
        } else {
            view.width
        }
    }

    fun gotoPage(index: Int, isForce:Boolean = false) {
        if (!isForce && bookInfo.currentPage == index) return
        bookInfo.currentPage = index
        mDistance = 0
        setView(getCenterPage(), bookInfo.currentPage)
        setSelected(bookInfo.currentPage)
        invalidate()
    }

    val isLeftCurrentPage: Int
        get() {
            val offset = getCenterPage().imageWidth / 4
            return if (abs(getCenterPage().left) <= offset) {
                1
            } else {
                0
            }
        }

    private fun setSelected(index: Int) {
        setView(getPrevPage(), index - 1)
        setView(getNextPage(), index + 1)
        getPrevPage().hide()
        getNextPage().hide()
        if (mOnPageSelectedListener != null) mOnPageSelectedListener!!.onPageSelected(getCenterPage(), bookInfo.currentPage)
    }

    private var isBlockTouch = false
    fun setDirection(isRight: Boolean) {
        if (SettingImageViewer.getIsPageRight(context) != isRight) {
            isBlockTouch = true
            Collections.sort(bookInfo.entryArray, SubStringComparator(isRight))
            SharedPrefHelper.setImagePageType(context, isRight)
            SettingImageViewer.setIsPageRight(context, isRight)
            bookInfo.currentPage = pageCount - 1 - bookInfo.currentPage
            bookInfo.isLeft = isLeftCurrentPage
            mIsFirstView = true
            mDistance = 0
            setView(getCenterPage(), bookInfo.currentPage)
            setSelected(bookInfo.currentPage)
            invalidate()
            isBlockTouch = false
        }
    }

    /**
     * 이미지 로딩
     */
    private fun setView(pageView: PageView, pageIndex: Int) {
        if (pageIndex < 0 || pageIndex >= bookInfo.entryArray.size) {
            pageView.clearImage() // 위에서 만든 메모리 해제 함수 호출
            pageView.hide()
            return
        }

        pageView.pageIndex = pageIndex
        val imageZipEntry = bookInfo.entryArray[pageIndex]
        pageView.loadImage(ZipData(bookInfo.targetPath,imageZipEntry,width, height)) {
            if (pageView.imageHeight == 0 || pageView.imageWidth == 0 || bookInfo.viewSize.Width == 0) {
                return@loadImage
            }
            var topMargin = 0
            val width : Int
            val height : Int
            if (bookInfo.zoomType == ZOOM_FIT_HEIGHT) {
                val scale = bookInfo.viewSize.Height.toFloat() / pageView.imageHeight.toFloat()
                width = (pageView.imageWidth.toFloat() * scale).toInt()
                height = bookInfo.viewSize.Height
            } else if (bookInfo.zoomType == ZOOM_FIT_SCREEN) {
                val scale = pageView.imageWidth.toFloat() / bookInfo.viewSize.Width.toFloat()
                height = (pageView.imageHeight.toFloat() / scale).toInt()
                width = bookInfo.viewSize.Width
                topMargin = (bookInfo.viewSize.Height - height) / 2
                if (topMargin < 0) topMargin = 0
            } else if (bookInfo.zoomType == ZOOM_HALF_SCREEN) {
                topMargin = pageView.getTopMargin()
                width = pageView.imageWidth
                height = pageView.imageHeight
            } else {
                val scale = bookInfo.zoomStandardHeight.toFloat() / pageView.imageHeight.toFloat()
                width = (pageView.imageWidth.toFloat() * scale).toInt()
                height = bookInfo.zoomStandardHeight
                topMargin = (bookInfo.viewSize.Height - height) / 2
                if (topMargin < 0) topMargin = 0
            }
            if (mIsFirstView) {
                mIsFirstView = false
                if (bookInfo.isLeft == 0) {
                    mDistance = bookInfo.viewSize.Width - width
                }
            }
            pageView.layout(mDistance, topMargin, mDistance + width, topMargin + height)
            invalidate()
        }
    }

    private fun setZoomType(zoomType:Int, zoomStandardHeight:Int? = null) {
        bookInfo.zoomType = zoomType
        zoomStandardHeight?.let { bookInfo.zoomStandardHeight = it }
        linkedViews.forEach { page ->
            zoomStandardHeight?.let { page.zoomStandardHeight = it }
            page.setupZoomType(zoomType)
        }
    }

    private var mDistance = 0
    private val mFlingRunnable: FlingRunnable = FlingRunnable()
    private val mBlockLayoutRequests = false
    private var mIsScaling = false
    private var mOldWidth = 0
    private var mOldHeight = 0
    private var mOldLeft = 0
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isBlockTouch) return true

        // 1. ScaleGestureDetector에 이벤트 위임 (멀티 터치 줌 처리)
        mScaleGestureDetector.onTouchEvent(event)

        val action = event.action

        // 2. 멀티 터치 중(pointerCount > 1)일 때는 일반 스크롤/플링(GestureDetector) 처리 무시
        if (event.pointerCount > 1) {
            return true
        } else {
            // 3. 싱글 터치에 대한 제스처 처리
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!mFlingRunnable.isFinished) {
                        onFinishedMovement()
                        mFlingRunnable.stop(false)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val fling = mGestureDetector!!.onTouchEvent(event)
                    if (!fling) onUp()
                    return true
                }
            }
            mGestureDetector!!.onTouchEvent(event)
        }
        return true
    }

    private fun setInitLayout() {
        setPrevLayout()
        setNextLayout()
    }

    private fun setPrevLayout() {
        val viewLeft = getCenterPage().left - getPrevPage().width
        val viewRight = getCenterPage().left
        getPrevPage().layout(viewLeft, getPrevPage().top, viewRight, getPrevPage().bottom)
    }

    private fun setNextLayout() {
        val viewLeft = getCenterPage().right
        val viewRight = getCenterPage().right + getNextPage().width
        getNextPage().layout(viewLeft, getNextPage().top, viewRight, getNextPage().bottom)
    }

    private fun scrollIntoSlots() {
        if (isEmpty()) return
        val rightPos = bookInfo.viewSize.Width - getCenterPage().right - mSpacing
        if (getCenterPage().right > 0 && bookInfo.viewSize.Width != rightPos && rightPos > -mSpacing) { // next View와 화면 공유 중
            val center = rightPos - movePageHThreshold
            if (center < 0) { //원래 위치
                if (abs(rightPos + mSpacing) == 1) {
                    onFinishedMovement()
                } else {
                    mFlingRunnable.startUsingDistance(rightPos + mSpacing)
                }
            } else { //next로 이동
                mFlingRunnable.startUsingDistance(-getNextPage().left)
            }
            return
        } else if (getCenterPage().left > 0 && getPrevPage().right < bookInfo.viewSize.Width) { //prev View와 화면 공유 중
            if (getCenterPage().left < movePageHThreshold) { //원 위치
                if (Math.abs(getCenterPage().left) == 1) {
                    onFinishedMovement()
                } else {
                    mFlingRunnable.startUsingDistance(-getCenterPage().left)
                }
            } else { //prev로 이동
                mFlingRunnable.startUsingDistance(bookInfo.viewSize.Width - getPrevPage().right)
            }
            return
        } else {
            onFinishedMovement() // 이동 종료
        }
    }

    private fun onFinishedMovement() {
        if (getCenterPage().right >= bookInfo.viewSize.Width && getCenterPage().left <= 0) {
            return
        }

        var newCenterId = -1
        var newCenterOffset = Int.MAX_VALUE
        //Center Page 찾기
        linkedViews.forEach { page ->
            if (page.imageWidth == 0) return@forEach    //continue
            val offset = page.right - bookInfo.viewSize.Width
            if (newCenterOffset > abs(offset)) {
                newCenterId = page.viewId
                newCenterOffset = abs(offset)
            }
        }

        if (newCenterId == 0) {
            val last = linkedViews.removeLast()
            linkedViews.addFirst(last)
        } else if (newCenterId == 2) {
            val first = linkedViews.removeFirst()
            linkedViews.addLast(first)
        }
        linkedViews.forEachIndexed { index, pageView ->
            pageView.viewId = index
        }

        getCenterPage().apply {
            bookInfo.currentPage = pageIndex
            mDistance = left
            setSelected(bookInfo.currentPage)
        }
    }

    private fun onUp() {
        if (mFlingRunnable.isFinished) {
            scrollIntoSlots()
        }
    }

    override fun onDown(arg0: MotionEvent): Boolean {
        setInitLayout()
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//        return mFlingRunnable.startUsingVelocity((velocityX * 0.7).toInt())
        return mFlingRunnable.startUsingVelocity(velocityX.toInt())
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        doScroll(-distanceX)
        return false
    }

    override fun onLongPress(arg0: MotionEvent) { }

    private fun doScroll(distanceX: Float): Boolean {
        getCenterPage().offsetLeftAndRight(distanceX.toInt())

        if (distanceX > 0 && !bookInfo.isPrev() && getCenterPage().left > 0) {
            getCenterPage().apply { layout(0, top, width, bottom) }
            if (mFlingRunnable.isFinished) return moveBook(false, isShowDialog = true) //Prev Book
        } else if (distanceX < 0 && !bookInfo.isNext() && getCenterPage().right < bookInfo.viewSize.Width) {
            getCenterPage().apply { layout(parentViewSize.Width - width, top, parentViewSize.Width, bottom) }
            if (mFlingRunnable.isFinished) return moveBook(true, isShowDialog = true) //Next Book
        }

        mDistance = getCenterPage().left
        setPrevLayout()
        setNextLayout()

        if (getPrevPage().right > 0) {
            getPrevPage().show()
        } else {
            getPrevPage().hide()
        }
        if (getNextPage().left < bookInfo.viewSize.Width) {
            getNextPage().show()
        } else {
            getNextPage().hide()
        }
        return false
    }

    override fun onShowPress(arg0: MotionEvent) {

    }

    override fun onSingleTapUp(arg0: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        setZoomType((bookInfo.zoomType + 1) % 3)
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (mOnPageSelectedListener != null) mOnPageSelectedListener!!.onSingleTab()
        return false
    }

    override fun requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout()
        }
    }
    var oldRect: Rect = Rect()
    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (bookInfo.entryArray.isNotEmpty()) {
            val newRect = Rect(l,t,r,b)
            if (oldRect == newRect) {
                return
            }
            oldRect = newRect
            CLog.e("KDS3393_TEST_onLayout [$l,$t,$r,$b][${r-l},${b-t}]")
            linkedViews.forEach { page ->
                page.setLayout()
            }
        }
    }

    private inner class FlingRunnable : Runnable {
        var isFinished = true
            private set
        private val mScroller = OverScroller(context)
        private var mLastFlingX = 0

        private fun startCommon() {
            removeCallbacks(this)
        }

        fun startUsingVelocity(initialVelocity: Int): Boolean {
            isFinished = false
            if (initialVelocity == 0) {
                isFinished = true
                return false
            }
            startCommon()
            val min = getCenterPage().left
            var max = bookInfo.viewSize.Width
            val limitMax = getCenterPage().right - bookInfo.viewSize.Width
            if (limitMax < max) max = limitMax
            mLastFlingX = 0

            val flingThreshold = 800

            if (initialVelocity < -flingThreshold && max <= 0) { // 페이지 이동 우측 (다음 페이지)
                startUsingDistance(-(bookInfo.viewSize.Width + max + mSpacing))
                return true
            } else if (initialVelocity > flingThreshold && min >= 0) { // 페이지 이동 좌측 (이전 페이지)
                startUsingDistance(bookInfo.viewSize.Width - min + mSpacing)
                return true
            }

            mScroller.fling(0, 0, -initialVelocity, 0, min, max, 0, 0)
//            mScroller.extendDuration(mAnimationDuration)
            postOnAnimation(this)
            return true
        }

        fun startUsingDistance(distance: Int) {
            isFinished = false
            if (distance == 0) {
                isFinished = true
                return
            }
            startCommon()
            mLastFlingX = 0
            mScroller.startScroll(0, 0, -distance, 0, mAnimationDuration)
            postOnAnimation(this)
        }

        fun stop(scrollIntoSlots: Boolean) {
            removeCallbacks(this)
            endFling(scrollIntoSlots)
        }

        private fun endFling(scrollIntoSlots: Boolean) {
            mScroller.forceFinished(true)
            if (scrollIntoSlots) scrollIntoSlots()
            isFinished = true
        }

        override fun run() {
            val scroller = mScroller
            var more = scroller.computeScrollOffset()
            val x = scroller.currX

            if (mLastFlingX != x) {
                val offset = mLastFlingX - x
                if (doScroll(offset.toFloat())) { // 책 이동으로 인해 Fling 중단
                    removeCallbacks(this)
                    mScroller.forceFinished(true)
                    return
                } else if (abs(offset) == 1) {
                    more = false
                }
            }
            if (more) {
                mLastFlingX = x
                postOnAnimation(this)
            } else {
                endFling(true)
            }
        }
    }

    //-------------------------------------------- Book Manage ------------------------------------------
    fun runInitTask() {
        bookInfo.loadBook { //onPostExecute
            pageCount = bookInfo.entryArray.size
            var index = bookInfo.currentPage
            if (mOnPageSelectedListener != null) {
                mOnPageSelectedListener!!.onLoaded(pageCount)
                mOnPageSelectedListener!!.onPageSelected(getCenterPage(), index)
                mOnPageSelectedListener!!.onBookSelected(bookInfo.targetPath)
            }
            if (!SettingImageViewer.getIsPageRight(context)) index = pageCount -  1 - index
            gotoPage(index, true)
            isBlockTouch = false
        }
    }

    fun moveBook(isNextArg: Boolean, isShowMsg : Boolean = true, isShowDialog : Boolean = false): Boolean {
        var isNext = isNextArg
        if (context == null) {
            return false
        }
        if (!SettingImageViewer.getIsPageRight(context)) {
            isNext = !isNext
        }
        if (bookInfo.books.isEmpty()) return false
        if (bookInfo.isBookChanging) return false

        if (isNext) {
            if (!bookInfo.isNextBook()) {
                if (isShowMsg) CToast.normal(context, "마지막 책입니다.\n다음 권으로 넘어갈 수 없습니다.")
                return false
            }
        } else {
            if (!bookInfo.isPrevBook()) {
                if (isShowMsg) CToast.normal(context, "첫번째 책입니다.\n이전 권으로 넘어갈 수 없습니다.")
                return false
            }
        }

        isBlockTouch = true
        if (isShowDialog) {
            val targetPath = if (isNext && bookInfo.books.isNotEmpty() && bookInfo.books.size > bookInfo.bookIndex + 1) {
                bookInfo.books[bookInfo.bookIndex + 1]
            } else if (bookInfo.books.isNotEmpty() && bookInfo.bookIndex - 1 >= 0) {
                bookInfo.books[bookInfo.bookIndex - 1]
            } else {
                return false
            }
            val dialog = DlgAlert(context, "Move", "${targetPath.getFileName()}이로 이동하시겠습니까?", object : BaseDialog.OnDialogListener {
                override fun onOk() {
                    DBMgr.instance.updateBookmark(bookInfo)

                    if (isNext) {
                        bookInfo.setNextBook()
                    } else {
                        bookInfo.setPrevBook()
                    }
                    //다음 책으로 이동
                    val file = File(bookInfo.targetPath)
                    if (file.exists() && file.isFile && FileUtils.getExtension(file.path).equals("zip", ignoreCase = true)) {
                        linkedViews.forEach { it.background = null }
                        runInitTask()
                    }
                }

                override fun onCancel() {
                    isBlockTouch = false
                }
            })
            dialog.show()
        } else {
            linkedViews.forEach { it.background = null }
            runInitTask()
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mIsScaling = true
            mOldWidth = getCenterPage().width
            mOldHeight = getCenterPage().height
            mOldLeft = getCenterPage().left
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (mOldHeight == 0 || mOldWidth == 0) return false

            // 누적 비율(scaleFactor)을 이전 크기에 곱하여 새 크기 계산
            val scaleFactor = detector.scaleFactor
            var height = (mOldHeight * scaleFactor).toInt()

            // 최대 확대 제한
            if (height > bookInfo.viewSize.Height) return true

            var scale = height.toFloat() / mOldHeight.toFloat()
            var width = (mOldWidth * scale).toInt()

            // 최소 축소 제한 (화면 너비보다 작아지지 않도록)
            if (width < bookInfo.viewSize.Width) {
                scale = bookInfo.viewSize.Width.toFloat() / mOldWidth.toFloat()
                width = bookInfo.viewSize.Width
                height = (mOldHeight * scale).toInt()
            }

            // 포커스 X 좌표를 기준으로 확대 중심점 설정 (자연스러운 줌 처리)
            val diffWidth = width - mOldWidth
            val focusRatio = if (mOldWidth > 0) (detector.focusX - mOldLeft) / mOldWidth else 0.5f
            var left = (mOldLeft - (diffWidth * focusRatio)).toInt()

            // 좌우 여백 보정
            if (left > 0) left = 0
            var right = left + width
            if (right < bookInfo.viewSize.Width) {
                right = bookInfo.viewSize.Width
                left = right - width
            }

            // 상하 중앙 정렬
            var topMargin = (bookInfo.viewSize.Height - height) / 2
            if (topMargin < 0) topMargin = 0

            getCenterPage().layout(left, topMargin, right, topMargin + height)
            invalidate()
            setZoomType(ZOOM_USER_CUSTOM, height)

            // 다음 이벤트를 위해 현재 상태 갱신
            mOldWidth = width
            mOldHeight = height
            mOldLeft = left

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mIsScaling = false
        }
    }
}