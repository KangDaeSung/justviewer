package com.kds3393.just.justviewer2.image

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.kds3393.just.justviewer2.data.BookInfo

open class ImageViewerConfig @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context,attrs) {
    lateinit var bookInfo: BookInfo
    protected var mSpacing = 10
    protected var movePageHThreshold = 0
    protected var mAnimationDuration = 300
    var pageCount = 0
    fun getPageIndex() : Int {
        return if (this::bookInfo.isInitialized) bookInfo.currentPage else -1
    }
}