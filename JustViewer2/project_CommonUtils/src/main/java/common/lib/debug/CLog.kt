package common.lib.debug

import android.util.Log
import android.view.View
import java.io.PrintWriter
import java.io.StringWriter

object CLog {
    val TAG = "[PD]"
    var SHOW_LOG = true //false이면 로그 출력 안함
    @JvmStatic
    fun e(obj: Any,log: String) {
        val tag = if (obj::class.simpleName != null) obj::class.simpleName!! else TAG
        e(tag, log)
    }
    @JvmStatic
    fun e(log: String) {
        e(TAG, log)
    }
    fun e(e: Exception) { e(TAG, e) }

    fun e(TAG: String, log: String) {
        if (SHOW_LOG) {
            Log.e(TAG, log)
        }
    }

    fun v(log: String) { v(TAG, log) }
    fun v(TAG: String, log: String) {
        if (SHOW_LOG) {
            Log.v(TAG, log)
        }
    }
    fun d(log: String) { d(TAG, log) }
    fun d(tag: String, log: String) {
        if (SHOW_LOG) {
            Log.d(tag, log)
        }
    }
    fun i(log: String) { i(TAG, log) }
    fun i(tag: String, log: String) {
        if (SHOW_LOG) {
            Log.i(tag, log)
        }
    }
    fun w(log: String) { w(TAG, log) }
    fun w(tag: String, log: String) {
        if (SHOW_LOG) {
            Log.w(tag, log)
        }
    }

    fun SetUseLog(isUseLog: Boolean) {
        SHOW_LOG = isUseLog
    }

    fun getUseLog() : Boolean {
        return SHOW_LOG
    }

    @JvmStatic
    fun e(TAG: String, e: java.lang.Exception) {
        Log.e(TAG, "catch Exception")
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val exceptionAsString = sw.toString()
        Log.e(TAG, exceptionAsString)
    }

    fun getClassAddress(obj: Any): String {
        return obj.toString().substring(obj.toString().lastIndexOf("@") + 5)
    }

    fun layout(log: String, view: View) { layout(TAG, log, view) }
    fun layout(tag: String, desc: String, view: View) {
        e(tag, desc + " l = " + view.left + " r = " + view.right + " t = " + view.top + " b = " + view.bottom)
    }
}