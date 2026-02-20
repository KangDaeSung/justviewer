package common.lib.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import common.lib.debug.CLog
import java.io.*

var APP_VERSION_CODE = 0L            //버전 코드
var APP_VERSION_NAME = "0.0.0"      // 버전 이름
var APP_PACK_NAME : String? = null  // 패키지명
var APP_NAME : String? = null       // 앱 이름
@SuppressLint("Registered")
open class CApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }

    protected open fun init() {
        try {
            val pInfo = packageManager.getPackageInfoCompat(packageName)
            APP_VERSION_NAME = pInfo.versionName
            APP_NAME = pInfo.applicationInfo.name
            APP_VERSION_CODE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                pInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            CLog.e(e)
        }
        APP_PACK_NAME = packageName
    }
}