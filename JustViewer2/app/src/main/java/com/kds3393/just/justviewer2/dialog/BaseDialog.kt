package com.kds3393.just.justviewer2.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

/**
 * Created by Administrator on 2016-07-24.
 * 기본 다이어로그 보조클래스
 * 모든 다이어로그가 사용하는건 아닙니다
 */
open class BaseDialog(context: Context?) : Dialog(context!!) {
    protected var mOnDialogListener: OnDialogListener? = null

    interface OnDialogListener {
        fun onOk()
        fun onCancel()
    }

    fun setOnDialogListener(listener: OnDialogListener?) {
        mOnDialogListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lpWindow = WindowManager.LayoutParams()
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        lpWindow.dimAmount = 0.8f
        window!!.attributes = lpWindow
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
    }
}