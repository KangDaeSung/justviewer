package common.lib.base

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.ceil

private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchIO(block: suspend CoroutineScope.() -> Unit) = ioScope.launch(
    context = CoroutineExceptionHandler { _, e -> Log.e("", "Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Log.e("", "Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun Activity.checkAudioPermission(requestCode: Int): Boolean {
    if (!isGranted(Manifest.permission.RECORD_AUDIO)) {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            requestCode
        )
        return false
    }
    return true
}

fun Activity.isPermission(permission:String, isRequestPermission:Boolean = false): Boolean {
    val isPermission = isGranted(permission)
    if (!isPermission && isRequestPermission) {
        requestPermissions(arrayOf(permission),10901)
    }
    return isPermission
}

fun Context.isGranted(permissionCode: String) = checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED

//-------------------- Int ------------------------------
/** 10000000 -> 1,000,000 */
fun Int.getCommaString(): String = DecimalFormat("#,###,###").format(this.toLong())

/** compareValue에 해당 하는 값과 같으면 lambda 함수에 해당 하는 값을 반환한다. */
fun Int.ifEqual(compareValue: Int, default: () -> Int?): Int? {
    return if (this == compareValue) default() else this
}
//-------------------- Float ------------------------------

fun Float.dp2px(): Int = ceil((this * Resources.getSystem().displayMetrics.density).toDouble()).toInt()
fun Float.sp2px(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f).toInt()

//-------------------- String ------------------------------
/** 숫자인지 체크 */
fun String.isNumber(): Boolean = this.matches("-?[0-9]+(\\.[0-9]+)?".toRegex())

/** 이메일 포맷 체크 */
fun String.isEmail(): Boolean {
    val regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
    val p = Pattern.compile(regex)
    val m = p.matcher(this)
    return m.matches()
}


//--------------------- RESOURCE ---------------------------
/**Resource의 Color id에 해당하는 color값을 가져온다.*/
fun View.getColor(colorId: Int) : Int {
    return this.resources.getColor(colorId, null)
}

//--------------------- Cursor ---------------------------
/** Cursor에서 column 이름의 String 값을 가져온다. */
fun Cursor.extGetString(columnName: String, def:String): String? {
    val columnIndex = this.getColumnIndex(columnName)
    return if (columnIndex >= 0) this.getString(columnIndex) else def
}
/** Cursor에서 column 이름의 Long 값을 가져온다. */
fun Cursor.extGetLong(columnName: String, def:Long?): Long? {
    val columnIndex = this.getColumnIndex(columnName)
    return if (columnIndex >= 0) this.getLong(columnIndex) else def
}

/** Cursor에서 column 이름의 Int 값을 가져온다. */
fun Cursor.extGetInt(columnName: String, def:Int?): Int? {
    val columnIndex = this.getColumnIndex(columnName)
    return if (columnIndex >= 0) this.getInt(columnIndex) else def
}

//--------------------- File ---------------------------
/** 확장자 가져오기 dir/abc.com -> com */
fun String.getFileExt(): String {
    val fileExtension = this.substring(this.lastIndexOf(".") + 1)
    return if (TextUtils.isEmpty(fileExtension)) "" else fileExtension
}

/** 파일 이름만 얻기 */
fun String.getFileName(): String {
    val fileName = this.substring(this.lastIndexOf("/") + 1, this.length)
    return if (fileName.lastIndexOf(".") > 0) fileName.substring(0, fileName.lastIndexOf(".")) else fileName
}

//----------------------- Layout -----------------------------------
/** layoutparams에 width와 height를 재 설정 한다. */
fun View.setSize(w: Int? = null, h: Int? = null) {
    val lp = this.layoutParams
    if (lp != null) {
        lp.width = w?:lp.width
        lp.height = h?:lp.height
        this.layoutParams = lp
    }
}

//----------------------- View -----------------------------------
/** TextView의 font size를 width에 맞춰서 줄여줌 */
fun TextView.autoTextSize(minSize: Int, defaultSize: Int) {
    if (this.width == 0 || TextUtils.isEmpty(this.text)) {
        return
    }
    var maxLine = this.maxLines
    if (maxLine > 5) {
        maxLine = 1
    } else if (maxLine <= 0) {
        maxLine = 1
    }
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSize.toFloat())
    val bounds = Rect()
    for (i in 0..99) {
        this.paint.getTextBounds(this.text.toString(), 0, this.text.length, bounds)
        val viewWidth = this.width * 0.9f * maxLine
        if (viewWidth < bounds.width()) {
            var changeSize = this.textSize * (viewWidth / bounds.width().toFloat())
            if (minSize > changeSize) {
                changeSize = minSize.toFloat()
            }
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, changeSize)
            continue
        }
        break
    }
}

/** INFO : isFade를 true로 실행했다면 @see [show]에서도 isFade를 true로 설정해야 동작함 */
fun View.gone(isFade:Boolean = false) {
    if (visibility != View.GONE) {
        if (isFade) {
            if (visibility == View.VISIBLE) {
                animate().alpha(0f).setListener(object : AniListener() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.GONE
                    }
                }).start()
            } else {
                visibility = View.GONE
            }
        } else {
            visibility = View.GONE
        }
    }
}

/** INFO : isFade를 true로 실행했다면 @see [show]에서도 isFade를 true로 설정해야 동작함 */
fun View.hide(isFade:Boolean = false) {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
        if (isFade) {
            animate().alpha(0f).setListener(null).start()
        }
    }
}

/** INFO : @see [gone] @see [hide]에서 hide에서 isFade를 true로 실행했다면 show에서도 isFade를 true로 설정해야 동작함 */
fun View.show(isFade:Boolean = false) {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        if (isFade) {
            animate().alpha(1f).setListener(null).start()
        }
    }
}

fun View.isShow() : Boolean {
    return visibility == View.VISIBLE
}

/** View click을 연속으로 하지 않도록 ms에 해당 하는 시간 이후 enable시킨다. */
fun View.enableDelayView(ms: Int = 1000) {
    this.isEnabled = false
    if (ms > 0) {
        this.postDelayed({ this.isEnabled = true }, ms.toLong())
    }
}