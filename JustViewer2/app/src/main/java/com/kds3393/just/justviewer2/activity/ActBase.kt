package com.kds3393.just.justviewer2.activity

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kds3393.just.justviewer2.dialog.LoadingDialog
import common.lib.base.ActBaseLib


open class ActBase : ActBaseLib() {
    fun hideSystemUI(root: View, hideType:Int = WindowInsetsCompat.Type.systemBars()) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, root).let { controller ->
            controller.hide(hideType)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemUI(root: View) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, root).show(WindowInsetsCompat.Type.systemBars())
    }

    var loadingDialog: LoadingDialog? = null
    fun showLoadingDialog() {
        if (loadingDialog != null && loadingDialog!!.dialog != null && loadingDialog!!.dialog!!.isShowing) {
            return
        }
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        if (baseContext != null) {
            loadingDialog = LoadingDialog()
            loadingDialog!!.show(ft, "loading")
        }
    }

    fun hideLoadingDialog() {
        try {
            if (baseContext != null && loadingDialog != null && loadingDialog!!.dialog != null && loadingDialog!!.dialog!!.isShowing) {
                loadingDialog!!.dismissAllowingStateLoss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val FileUriProvider = "com.kds3393.just.justviewer2.fileProvider"

        fun unwrap(ctx: Context): ActBase {
            var context = ctx
            while (context !is ActBase && context is ContextWrapper) {
                context = context.baseContext
            }
            return context as ActBase
        }
    }
}