package com.kds3393.just.justviewer2.activity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.dialog.LoadingDialog
import com.kds3393.just.justviewer2.image.ActImageViewerJC
import com.kds3393.just.justviewer2.text.ActTextViewerJC
import common.lib.base.ActBaseLib
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import java.io.File
import androidx.core.net.toUri


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

    fun startViewer(file: File, fileDatas: ArrayList<FileData>?) {
        val array = ArrayList<String>()
        val extension = FileUtils.getExtension(file.name)
        if (fileDatas == null) {
            array.add(file.path)
        } else {
            for (f in fileDatas) {
                array.add(f.mPath)
            }
        }
        if (extension.equals("zip", ignoreCase = true)) {
            val intent = Intent(this, ActImageViewerJC::class.java)
            intent.putExtra(ActMain.EXTRA_BROWSER_PATH, file.absolutePath)
            if (array.isNotEmpty()) {
                intent.putExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY, array)
            }
            startActivity(intent)
        } else if (extension.equals("avi", ignoreCase = true) || extension.equals("mp4", ignoreCase = true) || extension.equals("wmv", ignoreCase = true) || extension.equals("mkv", ignoreCase = true)) {
            val intent = Intent(Intent.ACTION_VIEW, file.path.toUri())
            intent.setDataAndType(file.path.toUri(), "video/mp4")
            startActivity(intent)
        } else if (extension.equals("txt", ignoreCase = true) || extension.equals("smi", ignoreCase = true) || extension.equals("log", ignoreCase = true)) {
            val intent = Intent(this, ActTextViewerJC::class.java)
            intent.putExtra(ActMain.EXTRA_BROWSER_PATH, file.absolutePath)
            startActivity(intent)
        } else if (extension.equals("epub", ignoreCase = true)) {
//            val fileUri = FileProvider.getUriForFile(this, FileUriProvider, file)
//            CLog.e("KDS3393_TEST_file\n" +
//                    "path[${Uri.fromFile(file)}]\n" +
//                    "fileUri[$fileUri]")
//            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(fileUri, "application/epub+zip"))

            CLog.e("KDS3393_TEST_file\n" + "path[${getPath(Uri.fromFile(file))}]")

        }
    }

    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
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