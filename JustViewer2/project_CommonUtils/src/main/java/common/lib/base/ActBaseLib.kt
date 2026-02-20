package common.lib.base

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import common.lib.debug.CLog


open class ActBaseLib : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        setResult(Activity.RESULT_CANCELED, intent)
    }

    override fun onResume() {
        super.onResume()
        if (mRunAfterResume != null) {
            mRunAfterResume!!.run()
            mRunAfterResume = null
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        CLog.i("JV startActivity start = ${this.localClassName}, activity = ${intent?.component?.className}")
    }

    //하위 context를 가지고 있는 View등에서 Activity에 특정 기능을 요청하기 위한 함수
    open fun onFunction(action: String, vararg obj: Any?) {}

    //SDK 33에서 onBackPressed Deprecated에 대한 대체 코드
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (onFinish()) {
                finish()
            }
        }
    }
    fun backPressed() {
        onBackPressedCallback.handleOnBackPressed()
    }
    /**
     * @return true : Activity 종료
     */
    open fun onFinish() : Boolean {
        return true
    }

    /** only used in this class */
    inner class RunResultCallback(val act: ActBaseLib) : ActivityResultCallback<ActivityResult> {
        var callback : ((String?, Int, Intent?) -> Unit)? = null
        override fun onActivityResult(result: ActivityResult) {
            val requestCode = result.data?.getStringExtra(EXTRA_REQUEST_CODE)
            CLog.e("KDS3393_TEST_activitResultLauncher class[${this@ActBaseLib.javaClass.simpleName}] requestCode[$requestCode] resultCode[${result.resultCode}]")
            if (callback != null) {
                callback?.invoke(requestCode, result.resultCode, result.data)
            } else {
                act.onActResult(requestCode, result.resultCode, result.data)
            }
        }
    }
    private val runResultCallback = RunResultCallback(this)

    private val activitResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), activityResultRegistry, runResultCallback)

    open fun onActResult(requestCode:String?, resultCode:Int, intent:Intent?) {}

    /**
     * onActivityResult의 deprecated에 대한 대응
     *
     * @param callback : 만약 camara나 동영상등의 리소스를 많이 사용하는 Activity를 실행시에는 onActResult에서 처리해야 안전함
     * requestCode, resultCode, intent
     */
    fun runActResult(intent: Intent, responseCode: String? = null, option: ActivityOptionsCompat? = null, callback : ((String?, Int, Intent?) -> Unit)? = null) {
        if (responseCode != null) {
            intent.putExtra(EXTRA_REQUEST_CODE, responseCode)
        }
        runResultCallback.callback = callback
        activitResultLauncher.launch(intent, option)
    }

    val isDestory: Boolean
        get() = isDestroyed
    private var mRunAfterResume: Runnable? = null

    //runAfterSaveInstance 내부에서 다시 runAfterSaveInstance를 실행하면 동작하지 않음!! 주의!!!
    fun runAfterSaveInstance(run: Runnable?) {
        if (run != null) {
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                mRunAfterResume = run
            } else {
                runOnUiThread(run)
            }
        } else {
            mRunAfterResume = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Glide.get(this).clearMemory()
    }

    lateinit var mGlideRequestManager: RequestManager
    open fun Glide(): RequestManager {
        if (!::mGlideRequestManager.isInitialized) {
            mGlideRequestManager = Glide.with(this).setDefaultRequestOptions(RequestOptions().timeout(30000))
        }

        return mGlideRequestManager
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Glide.get(this).trimMemory(level)
    }

    //Statusbar와 App UI 겹치기
    fun initIndicatorOverLay() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.parseColor("#77000000")

        val rootView = (this.window.decorView.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            val mlp = view.layoutParams as ViewGroup.MarginLayoutParams
            mlp.bottomMargin = insets.bottom
            view.layoutParams = mlp
            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    //statusbar 숨기기
    fun hideStatusBar() {
        val rootView = (this.window.decorView.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        WindowInsetsControllerCompat(window, rootView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE  //특정 행동을 했을 경우에만 status bar가 나타남
        }
    }

    //statusbar 보이기
    fun showStatusBar() {
        val rootView = (this.window.decorView.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        WindowInsetsControllerCompat(window, rootView).show(WindowInsetsCompat.Type.statusBars())
    }

    // Show the status bar
    @Suppress("DEPRECATION")
    fun statusBar(isShow:Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isShow) {
                window.insetsController?.show(WindowInsets.Type.statusBars())
            } else {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            }
        } else {
            if (isShow) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    companion object {
        const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"   //onActivityResult deprecated로 onActivityResult와 유사하게 동작하게 하기 위한 RequestCode

        private val AVAIL_MINETYPES = arrayOf("image/jpeg", "image/jpg", "image/png", "video/mp4", "audio/mpeg")
        fun checkImageMineType(mineType: String?): String? {
            if (!TextUtils.isEmpty(mineType)) {
                for (m in AVAIL_MINETYPES) {
                    if (m.equals(mineType, ignoreCase = true)) {
                        return mineType
                    }
                }
            }
            return null
        }

        fun unwrap(ctx: Context): ActBaseLib {
            var context = ctx
            while (context !is ActBaseLib && context is ContextWrapper) {
                context = context.baseContext
            }
            return context as ActBaseLib
        }
    }
}