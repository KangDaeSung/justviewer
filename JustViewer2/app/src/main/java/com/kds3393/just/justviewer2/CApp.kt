package com.kds3393.just.justviewer2

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager
import common.lib.app.CApplication
import common.lib.debug.CLog

class CApp : CApplication() {
    companion object {
        lateinit var instance: CApplication

        @JvmStatic
        fun get(): CApplication {
            return instance
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        CLog.e("KDS3393_TEST_getScreenSize ${getScreenSize()}")
    }

    private lateinit var mScreenSize: Point
    fun getScreenSize(): Point {
        mScreenSize = Point()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowMetrics = wm.currentWindowMetrics
        val windowInsets: WindowInsets = windowMetrics.windowInsets

        val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom

        val b = windowMetrics.bounds
        mScreenSize.x = b.width() - insetsWidth
        mScreenSize.y = b.height() - insetsHeight
        return mScreenSize
    }
}