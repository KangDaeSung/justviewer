package common.lib.debug

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import common.lib.utils.FileUtils
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

object DevUtils {
    private var mStartTime: Long = 0
    fun startTime() {
        mStartTime = System.currentTimeMillis()
    }

    fun endTime(): Long {
        val endTime = System.currentTimeMillis()
        return endTime - mStartTime
    }

    private const val TAG = "DevUtils"
    private val sHashCode = HashMap<Int, Long>()
    fun sStartTime(hashcode: Int) {
        val time = System.currentTimeMillis()
        sHashCode[hashcode] = time
    }

    fun sEndTime(hashcode: Int): Long {
        val endTime = System.currentTimeMillis()
        val startTime = sHashCode[hashcode] ?: return -1
        sHashCode.remove(hashcode)
        Log.e(TAG, "delay " + hashcode + " = " + (endTime - startTime))
        return endTime - startTime
    }

    fun randomRGB(): Int {
        val rnd = Random()
        return Color.parseColor("#" + String.format("77%02X%02X%02X", rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)))
    }

    private val DEBUG_DN = X500Principal("CN=Android Debug,O=Android,C=US")
    fun isDebuggable(ctx: Context): Boolean {
        var debuggable = false
        try {
            val pinfo = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES)
            val signatures = pinfo.signatures
            val cf = CertificateFactory.getInstance("X.509")
            for (i in signatures.indices) {
                val stream = ByteArrayInputStream(signatures[i].toByteArray())
                val cert = cf.generateCertificate(stream) as X509Certificate
                debuggable = cert.subjectX500Principal == DEBUG_DN
                if (debuggable) break
            }
        } catch (e: PackageManager.NameNotFoundException) {
            CLog.e(TAG, e)
        } catch (e: CertificateException) {
            CLog.e(TAG, e)
        }
        return debuggable
    }

    /** App에 있는 Keystore의 SecretKey값을 반환한다. */
    fun getKeystoreSecretKey(context: Context): SecretKey? {
        var key: SecretKey? = null
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                key = SecretKeySpec(md.digest(), "AES")
                break
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return key
    }

    /** App에 있는 Keystore의 HashCode를 반환한다. */
    fun getKeystoreHash(context: Context, packageName: String?): Int {
        var hash = 0
        try {
            val packageManager = context.packageManager
            val info = packageManager.getPackageInfo(packageName!!, PackageManager.GET_SIGNATURES)
            val signs = info.signatures
            val cf = CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(ByteArrayInputStream(signs[0].toByteArray())) as X509Certificate
            val key = cert.publicKey
            hash = (key as RSAPublicKey).modulus.hashCode()
        } catch (e: Exception) {
            CLog.e(TAG, e)
        }
        return hash
    }

    /** Layout 안의 View들에 대한 tree 구조 로그 */
    fun logChildTree(depth: Int, view: View, limitDepth:Int = 1000, postLog:String = "", ) {
        if (depth >= limitDepth) return
        var dpStr = ""
        for (d in 0 until depth) {
            dpStr += "   "
        }
        var info = ""
        if (view is ViewGroup) {
            info = getParamsInfo(view)
            Log.e(TAG, postLog + dpStr + "vg = " + logView(view) + info)
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is ViewGroup) {
                    logChildTree(depth + 1, child, limitDepth, postLog)
                } else {
                    info = getParamsInfo(child)
                    Log.e(TAG, postLog + dpStr + "   v = " + logView(child) + info)
                }
            }
        } else {
            info = getParamsInfo(view)
            Log.e(TAG, postLog + dpStr + "v = " + logView(view) + info)
        }
    }

    /**
     * 기준 String : com.kds.just.enhancedview.view.EnhancedTextView{5ee4f27 V.ED..... ......ID 0,0-0,0 #7f0b06f5 app:id/vFieldItemTitleTv} x:0 y:0 w:0 h:-2
     *               ID 0,0-0,0 #7f0b06f5 app:id/vFieldItemTitleTv} x:0 y:0 w:0 h:-2
     */
    fun logView(v:View) : String {
        val className = v.javaClass.simpleName
        val idName = getIdString(v.context,v.id)
        val size = "{${v.left},${v.top}-${v.right},${v.bottom}},{${v.width},${v.height}}"
        val instanceCode = Integer.toHexString(System.identityHashCode(v))
        return "$className[$idName,$size,$instanceCode]"
    }

    /** View가 클릭이 안되는 경우 해당 View영역 안에 클릭 이벤트가 선언되어 있는지 로그로 확인*/
    fun showClickListener(depth: Int, view: View) {
        var dpStr = ""
        for (d in 0 until depth) {
            dpStr += "   "
        }
        var info = ""
        if (view is ViewGroup) {
            if (view.hasOnClickListeners()) {
                view.setBackgroundColor(randomRGB())
                info = getParamsInfo(view)
                Log.e(TAG, dpStr + "v = " + view.toString().substring(view.toString().lastIndexOf(".") + 1, view.toString().length) + info)
            }
            val vg = view
            for (i in 0 until vg.childCount) {
                val child = vg.getChildAt(i)
                if (child is ViewGroup) {
                    showClickListener(depth + 1, child)
                } else {
                    if (child.hasOnClickListeners()) {
                        child.setBackgroundColor(randomRGB())
                        info = getParamsInfo(child)
                        Log.e(TAG, dpStr + "   v = " + child.toString().substring(child.toString().lastIndexOf(".") + 1, child.toString().length) + info)
                    }
                }
            }
        } else {
            if (view.hasOnClickListeners()) {
                view.setBackgroundColor(randomRGB())
                info = getParamsInfo(view)
                Log.e(TAG, dpStr + "v = " + view.toString().substring(view.toString().lastIndexOf(".") + 1, view.toString().length) + info)
            }
        }
    }

    /** View의 속성(x, y, width, height, visibility) 정보를 string으로 반환한다.*/
    private fun getParamsInfo(view: View): String {
        var info = ""
        if (view.layoutParams is FrameLayout.LayoutParams) {
            val params = view.layoutParams as FrameLayout.LayoutParams
            info = " x:" + params.leftMargin + " y:" + params.topMargin + " w:" + params.width + " h:" + params.height
        } else if (view.layoutParams is ConstraintLayout.LayoutParams) {
            val params = view.layoutParams as ConstraintLayout.LayoutParams
            info = " x:" + params.leftMargin + " y:" + params.topMargin + " w:" + params.width + " h:" + params.height
        }
        if (view.visibility == View.VISIBLE) {
            info += " [VISIBLE]"
        } else if (view.visibility == View.INVISIBLE) {
            info += " [INVISIBLE]"
        } else if (view.visibility == View.GONE) {
            info += " [GONE]"
        }
        return info
    }

    /** cache 폴더를 삭제한다. */
    fun deleteCache(context: Context) {
        try {
            FileUtils.deleteFile(context.cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**Int형의 id로 정의된 id name을 가져온다. ex) -134820 => R.id.layoutid*/
    fun getIdString(context: Context, id: Int): String {
        if (id < 10) return Integer.toHexString(id)
        try {
            var name = context.resources.getResourceName(id)
            if (!TextUtils.isEmpty(name) && name.lastIndexOf("/") > 0) {
                name = name.substring(name.lastIndexOf("/") + 1, name.length)
            }
            return name
        } catch (e: Exception) {
        }
        return Integer.toHexString(id)
    }

    //---------------------------------- Memory -----------------------------------
    /**현재 가용 가능한 메모리, 사용중 메모리등의 정보를 로그로 보여준다.*/
    fun memoryLog(context :Context) {
        val mi = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(mi)
        val availableMegs = mi.availMem / 1048576L
        val totalMem = mi.totalMem / 1048576L
        val percentAvail = (mi.availMem / mi.totalMem.toDouble() * 100.0).toInt()
        Log.e(TAG, "MEMORY = " + availableMegs + " totalMem = " + totalMem + " usedMem = " + (totalMem - availableMegs) + " percent = " + percentAvail)
    }
}