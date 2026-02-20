package common.lib.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import common.lib.view.ProgressCircle
import java.text.NumberFormat

/**
 *
 * A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.
 *
 * The dialog can be made cancelable on back key press.
 *
 * The progress range is 0..10000.
 */
class ProgressCircleDialog(context: Context?) : AlertDialog(context) {
    private var mProgress: ProgressCircle? = null
    private var mMessageView: TextView? = null
    private var mProgressPercentFormat: NumberFormat? = null
    private var mMax = 0
    private var mProgressVal = 0
    private var mMessage: CharSequence? = null
    private var mHasStarted = false

    private fun initFormats() {
        mProgressPercentFormat = NumberFormat.getPercentInstance()
        mProgressPercentFormat!!.maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle) {
        val displayMetrics = DisplayMetrics()
        window!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        var size = 0
        size = if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            displayMetrics.heightPixels
        } else {
            displayMetrics.widthPixels
        }
        size = (size * 0.2).toInt()
        val layout = LinearLayout(context)
        layout.setBackgroundColor(Color.BLACK)
        setView(layout)
        ProgressCircle.make(context, layout, size, size, 0, 0).apply {
            mProgress = this
            setBGCircleColor(Color.GRAY)
            setPersentColor(Color.GRAY)
            setStrokeWidth(4)
            isInfinity = mIsInfinity
            setShowPersent(mIsShowPersent)
        }
        common.lib.utils.ViewMaker.TextViewMaker(context, layout, "", (size * 2f).toInt(), size, 0, 0).apply {
            mMessageView = this
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.14f)
        }

        if (mMax > 0) {
            setMax(mMax)
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal)
        }
        if (mMessage != null) {
            setMessage(mMessage!!)
        }
        super.onCreate(savedInstanceState)
    }

    public override fun onStart() {
        super.onStart()
        mHasStarted = true
    }

    override fun onStop() {
        super.onStop()
        mHasStarted = false
    }

    fun setProgress(value: Int) {
        mProgressVal = value
        if (mHasStarted) {
            mProgress!!.setProgress(mProgressVal)
        }
    }

    val progress: Float
        get() = if (mProgress != null) {
            mProgress!!.progress
        } else mProgressVal.toFloat()
    val max: Float
        get() = if (mProgress != null) {
            mProgress!!.max
        } else mMax.toFloat()

    fun setMax(max: Int) {
        mMax = max
        if (mProgress != null) {
            mProgress!!.max = mMax.toFloat()
        }
    }

    private var mIsInfinity = true
    private var mIsShowPersent = true
    var isInfinity: Boolean
        get() = if (mProgress != null) mProgress!!.isInfinity else mIsInfinity
        set(isInfinity) {
            mIsInfinity = isInfinity
            if (mProgress != null) {
                mProgress!!.isInfinity = mIsInfinity
            }
        }

    fun setShowPersent(isShow: Boolean) {
        mIsShowPersent = isShow
        if (mProgress != null) mProgress!!.setShowPersent(mIsShowPersent)
    }

    override fun setMessage(message: CharSequence) {
        mMessage = message
        if (mProgress != null) {
            mMessageView!!.text = mMessage
        }
    }

    companion object {
        @JvmOverloads
        fun show(context: Context?, title: CharSequence?, message: CharSequence, indeterminate: Boolean = false, cancelable: Boolean = false, cancelListener: DialogInterface.OnCancelListener? = null): ProgressCircleDialog {
            val dialog = ProgressCircleDialog(context)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.setCancelable(cancelable)
            dialog.setOnCancelListener(cancelListener)
            dialog.show()
            return dialog
        }
    }

    init {
        initFormats()
    }
}