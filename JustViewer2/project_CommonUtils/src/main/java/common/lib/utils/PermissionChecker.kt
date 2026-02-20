package common.lib.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import common.lib.base.ActBaseLib
import common.lib.debug.CLog

open class PermissionChecker(val act: ActBaseLib? = null, val frm:Fragment? = null) {
    val permissions = ArrayList<String>()
    private val actPermissionResult = getActResult { result  ->
        var isDeny = false
        for (item in result.entries) {
            if (!item.value) {
                isDeny = true
                break
            }
        }
        CLog.e("KDS3393_TEST_actPermissionResult $result")
        if (isDeny) { //거부된 권한이 있는 경우
            denyListener?.invoke(result.toMutableMap())
        } else {
            grantListener?.invoke()
        }
    }

    fun clearPermission() {
        permissions.clear()
    }

    fun addPermissions(vararg p:String) : PermissionChecker {
        p.forEach {
            permissions.add(it)
        }
        return this
    }

    fun addPermission(p:String) : PermissionChecker {
        permissions.add(p)
        return this
    }

    //거부된 권한이 있을 경우 호출 처리할 lambda callback (승인된 권한까지 인자로 넘겨줌)
    private var denyListener : ((MutableMap<String,Boolean>) -> Unit)? = null
    private var grantListener : (() -> Unit)? = null
    fun setDenyListener(listener : (MutableMap<String,Boolean>) -> Unit) : PermissionChecker {
        this.denyListener = listener
        return this
    }
    fun setGrantListener(listener : () -> Unit) : PermissionChecker {
        this.grantListener = listener
        return this
    }

    fun check() {
        actPermissionResult.launch(permissions.toTypedArray())
    }

    companion object {
        fun isCheck(act: AppCompatActivity, permission:String) : Boolean {
            return ContextCompat.checkSelfPermission(act,permission) == PackageManager.PERMISSION_GRANTED
        }

        //다른 앱 위에 그리기 권한 요청
        fun checkDrawOverlays(act:ActBaseLib,msg:String,positive:String,callback:(Boolean) -> Unit) {
            if (Settings.canDrawOverlays(act)) {
                callback(true)
            } else {
                val builder = AlertDialog.Builder(act, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                builder.setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton(positive) { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${act.packageName}"))
                        act.runActResult(intent, "OVERLAY_PERMISSION") { _, _, _ ->
                            callback(Settings.canDrawOverlays(act))
                        }
                    }
                    .setNegativeButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    open fun showDenyDialog(permission:MutableMap<String,Boolean>, denyTitle:Int?, denyMessage:Int, callback:(() -> Unit)? = null) {
        val context = if (getAct() != null) {
            getAct()
        } else {
            callback?.invoke()
            return
        }
        var isDeny = false
        for (item in permission.entries) {
            if (item.value) {
                isDeny = true
                break
            }
        }
        if (isDeny) {
            val title = if (denyTitle != null) {
                context!!.getString(denyTitle)
            } else {
                null
            }
            showDenyDialog(permission, title, context!!.getString(denyMessage), callback)
        }
    }

    open fun showDenyDialog(permission:MutableMap<String,Boolean>, denyTitle:String?, denyMessage:String, callback:(() -> Unit)? = null) {
        val context = getAct()?:return
        val builder = AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
        if (denyTitle != null) {
            builder.setTitle(denyTitle)
        }
        builder.setMessage(denyMessage)
            .setCancelable(false)
            .setPositiveButton("Close") { _, _ ->
                callback?.invoke()
            }.setOnCancelListener {
                callback?.invoke()
            }
            .show()
    }

    /**
     * SDK 34에서 미디어 권한 체크
     * 미디어 권한은 SDK 34버전에서는 READ_MEDIA_VISUAL_USER_SELECTED가 필수로 포함되어야 함
     * READ_MEDIA_VISUAL_USER_SELECTED이 true인 경우 추가 팝업을 제거하기 위해 거부된 Media 권한을 제거한다.
     * 사진 선택 : READ_MEDIA_IMAGES=false, READ_MEDIA_VIDEO=false, READ_MEDIA_VISUAL_USER_SELECTED=true
     * 모두 허용 : READ_MEDIA_IMAGES=true, READ_MEDIA_VIDEO=true, READ_MEDIA_VISUAL_USER_SELECTED=true
     * 자동화 하지 않은 이유 : 모두 허용이 꼭 필요할 경우 그에 대한 처리를 하기 위함
     */
    fun isMediaPermission(permission:MutableMap<String,Boolean>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {    //아래의 권한 중 하나라도 권한이
            if (permission[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true) {
                permission.remove(Manifest.permission.READ_MEDIA_IMAGES)
                permission.remove(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }
    }

    fun getAct() : FragmentActivity? {
        return act?: frm?.requireActivity()
    }

    private fun getActResult(callback: ActivityResultCallback<Map<String, Boolean>>) : ActivityResultLauncher<Array<String>> {
        return act?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),callback)
            ?: (frm?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),callback)
            ?: throw NullPointerException("ActivityResultLauncher"))
    }
}