package com.kds3393.just.justviewer2.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment

open class DialogBase : DialogFragment(), Runnable {
    protected var mOnDialogListener: OnDialogListener? = null
    private val arg: Bundle
        get() {
            var bundle = arguments
            if (bundle == null) {
                bundle = Bundle()
                arguments = bundle
            }
            return bundle
        }
    fun get(key:String,def:String) : String { return requireArguments().getString(key, def) }
    fun set(key:String,value:String?) : String? {
        arg.putString(key, value?:"")
        return value
    }
    fun get(key:String,def:Int) : Int { return requireArguments().getInt(key, def) }
    fun set(key:String,value:Int) : Int {
        arg.putInt(key, value)
        return value
    }
    fun get(key:String,def:Boolean) : Boolean { return requireArguments().getBoolean(key, def) }
    fun set(key:String,value:Boolean) : Boolean {
        arg.putBoolean(key, value)
        return value
    }

    interface OnDialogListener {
        fun onConfirm()
        fun onCancel()
    }

    fun setOnDialogListener(l: OnDialogListener?) {
        mOnDialogListener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dlg = super.onCreateDialog(savedInstanceState)
        dlg.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dlg.setOnCancelListener {
            if (mOnDialogListener != null) {
                mOnDialogListener!!.onConfirm()
            }
        }
        return dlg
    }

    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            _handler.removeCallbacks(this)
            _handler.postDelayed(this, 300)
        } else {
            _handler.removeCallbacks(this)
        }
    }

    fun onKeyDown(keyCode: Int) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            _handler.removeCallbacks(this)
            _handler.postDelayed(this, 500)
        }
    }

    override fun onStop() {
        _handler.removeCallbacks(this)
        super.onStop()
    }

    override fun run() {
        setImmersiveMode()
    }

    @SuppressLint("NewApi")
    fun setImmersiveMode() {
        setImmersiveMode(activity)
    }

    @SuppressLint("NewApi")
    fun setImmersiveMode(activity: Activity?) {
        if (activity == null || activity.window == null) {
            return
        }
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY //                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                //                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                //                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    private val _handler = Handler()

    companion object {
        //---------------------------------------------------------------------------------------------
        val isFragmentAvailable: Boolean
            get() = Build.VERSION.SDK_INT >= 19
    }
}