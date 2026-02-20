package com.kds3393.just.justviewer2.image

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.utils.glide.ZipData
import com.kds3393.just.justviewer2.utils.glide.ZipImageLoader
import common.lib.base.launchMain
import common.lib.debug.CLog
import common.lib.utils.LayoutUtils
import common.lib.utils.Size
import androidx.core.view.isVisible
import androidx.core.view.isGone

fun pageViewMaker(context: Context, parentView: ViewGroup, viewId: Int): PageView {
    val view = PageView(context)
    view.viewId = viewId
    parentView.addView(view)
    LayoutUtils.setLayoutParams(parentView, view, 0, 0, 0, 0, 0, 0)
    return view
}

class PageView(context: Context) : AppCompatImageView(context) {
    var viewId = -1
    var pageIndex = -1
    val parentViewSize = Size()
    var zoomType = ZOOM_NONE
    var zoomStandardHeight = 0
    var imageWidth = 0
        private set
    var imageHeight = 0
        private set

    fun setLayout(width: Int = -1, height: Int = -1) {
        var layoutWidth = width
        var layoutHeight = height
        if (width >= 0) this.imageWidth = width
        if (height >= 0) this.imageHeight = height
        if (this.imageWidth == 0 && this.imageHeight == 0) {
            return
        }
        if (zoomType == ZOOM_FIT_HEIGHT) {
            setZoomFitHeight()
        } else if (zoomType == ZOOM_FIT_SCREEN) {
            setZoomFitScreen()
        } else if (zoomType == ZOOM_HALF_SCREEN) {
            setZoomHalfScreen()
        } else {
            if (zoomStandardHeight > 0) {
                val scale: Float = zoomStandardHeight.toFloat() / layoutHeight.toFloat()
                layoutWidth = (layoutWidth.toFloat() * scale).toInt()
                layoutHeight = zoomStandardHeight
                var topMargin = (parentViewSize.Height - layoutHeight) / 2
                if (topMargin < 0) {
                    topMargin = 0
                }
                LayoutUtils.setFrameLayoutParams(this, layoutWidth, layoutHeight, 0, topMargin)
            } else {
                setZoomFitScreen()
            }
        }
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        if (pageIndex >= 0) {
            super.layout(l, t, r, b)
            layoutParams.width = r - l
            layoutParams.height = b - t
        }
//        CLog.e("KDS3393_TEST_pageview viewId[$viewId] pageIndex[$pageIndex] layout [$l,$t,$r,$b]")
    }

    //---------------------------------  Zoom Type Layout Change  -----------------------------------
    fun setupZoomType(type:Int) {
        if (zoomType == type) return
        zoomType = type
        if (imageWidth != 0 && imageHeight != 0) {
            when (type) {
                ZOOM_FIT_HEIGHT -> setZoomFitHeight()
                ZOOM_HALF_SCREEN -> setZoomHalfScreen()
                ZOOM_FIT_SCREEN -> setZoomFitScreen()
            }
        }
    }

    //ZOOM_FIT_HEIGHT   //높이에 맞춰서 늘려준다. 화면에 꽉차게 나온다.
    private fun setZoomFitHeight() {
        if (height > 0 && parentViewSize.Height > height) {
            val scale = parentViewSize.Height.toFloat() / height.toFloat()
            val l = (left.toFloat() * scale).toInt()
            val r = (right.toFloat() * scale).toInt()
            layout(l, 0, r, Size.DisplayHeight)
        }
    }

    //ZOOM_HALF_SCREEN  //화면의 2배로 늘려 확대해서 본다. 가로가 긴 이미지를 좌우 스크롤로 볼수 있다.
    private fun setZoomHalfScreen() {
        val scale = imageHeight.toFloat() / height.toFloat()
        val l = (left.toFloat() * scale).toInt()
        val r = (right.toFloat() * scale).toInt()
        val top = getTopMargin()
        layout(l, top, r, top + imageHeight)
    }

    //ZOOM_FIT_SCREEN = 2   //화면에 맞춘다.
    private fun setZoomFitScreen() {
        val scale = imageWidth.toFloat() / parentViewSize.Width.toFloat()
        val h = (imageHeight.toFloat() / scale).toInt()
        var top = (parentViewSize.Height - h) / 2
        if (top < 0) top = 0
        layout(0, top, parentViewSize.Width, top + h)
    }

    //topMargin값을 계산한다.
    fun getTopMargin(): Int {
        var topMargin = (parentViewSize.Height - imageHeight) / 2
        if (topMargin < 0) topMargin = 0
        return topMargin
    }

    //-------------------------------------  image loading  -----------------------------------------
    fun getScale(bitmap: Bitmap): Float {
        return if (bitmap.width > bitmap.height) {
            (parentViewSize.Width * 2).toFloat() / bitmap.width
        } else {
            parentViewSize.Width.toFloat() / bitmap.width
        }
    }

    private var imageZipData : ZipData? = null
    private var onLoadingComplete:(() -> Unit)? = null
    fun loadImage(zipData: ZipData, onLoadingComplete:(() -> Unit)? = null) {
        if (imageZipData == zipData) {  //동일한 이미지 entry이면 로딩하지 않는다.
            CLog.e("KDS3393_TEST_loadImage pageIndex[$pageIndex] SAME return")
            return
        }
        this.onLoadingComplete = onLoadingComplete
        Glide.with(context).asBitmap()
            .override(parentViewSize.Width*2,parentViewSize.Height)
            .placeholder(R.drawable.file)
            .load(zipData)
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                    return true
                }

                override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    launchMain {
                        val imageScale: Float = getScale(resource)
                        setLayout((resource.width * imageScale).toInt(), (resource.height * imageScale).toInt())
                        setImageBitmap(resource)
                        onLoadingComplete?.invoke()
                    }
                    return true
                }
            }).submit()
    }

    override fun toString(): String {
        val size = "{${left},${top}-${right},${bottom}},{${width},${height}}"
        val zipName = imageZipData?.entry?.name
        val showType = if (isVisible) {
            "show"
        } else if (isGone) {
            "gone"
        } else {
            "hide"
        }
        return "PageView[$viewId] mPageIndex[$pageIndex] size[$size] zipName[$zipName] $showType"
    }

    companion object {
        fun getZoomTypeStr(type:Int) : String {
            return when(type) {
                ZOOM_HALF_SCREEN -> "ZOOM_HALF_SCREEN"
                ZOOM_FIT_HEIGHT -> "ZOOM_FIT_HEIGHT"
                ZOOM_FIT_SCREEN -> "ZOOM_FIT_SCREEN"
                ZOOM_USER_CUSTOM -> "ZOOM_USER_CUSTOM"
                ZOOM_NONE -> "ZOOM_NONE"
                else -> type.toString()
            }
        }
    }
}