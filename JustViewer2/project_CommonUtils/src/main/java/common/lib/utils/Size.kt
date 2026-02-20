package common.lib.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log

class Size {
    @JvmField
	var Width = 0
    @JvmField
	var Height = 0

    constructor() {
        Width = 0
        Height = 0
    }

    constructor(width: Int, height: Int) {
        Width = width
        Height = height
    }

    constructor(bitmap: Bitmap?) {
        if (bitmap != null) {
            Width = bitmap.width
            Height = bitmap.height
        } else {
            Log.e(TAG, "Error : Bitmap is Null")
            Width = 0
            Height = 0
        }
    }

    override fun toString(): String {
        return "Size[$Width,$Height]"
    }

    companion object {
        private const val TAG = "Size"
        var DisplayWidth = 0
        var DisplayHeight = 0
        var Density = 0f
        fun getDisplaySize(activity: Activity): Size {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

        fun getDensity(activity: Activity): Float {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.density
        }

        fun initDensity(activity: Activity) {
            Density = getDensity(activity)
        }

        fun InitScreenSize(activity: Activity) {
            val size = getDisplaySize(activity)
            initDensity(activity)
            val orientation = activity.requestedOrientation
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                if (size.Width > size.Height) {
                    val temp = size.Width
                    size.Width = size.Height
                    size.Height = temp
                }
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                if (size.Width < size.Height) {
                    val temp = size.Width
                    size.Width = size.Height
                    size.Height = temp
                }
            }
            DisplayWidth = size.Width
            DisplayHeight = size.Height
            Log.d(TAG, "Info : ScreenSize = " + DisplayWidth + " X " + DisplayHeight + " Density = " + Density + " : orientation = " + orientation)
        }
    }
}