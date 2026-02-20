package common.lib.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

open class RVBase : RecyclerView {
    var emptyView: View? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {}
    open fun loadData(type: Int) {}
    open fun loadData(isNext: Boolean, no: Int) {}

    /**
     * 갱신할때 깜박이는 현상을 방지함
     * 깜빡임 현상이 있을 경우 실행코드 추가해야함
     */
    fun refreshBlinkGuard() {
        val animator = itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        itemAnimator = null
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (layoutManager == null) {
            layoutManager = RVLinearManager(context)
        }
        super.setAdapter(adapter)
    }

    var isShowEmptyView: Boolean
        get() = emptyView != null && emptyView!!.visibility == VISIBLE
        set(isShow) {
            if (emptyView != null) {
                if (isShow) {
                    emptyView!!.visibility = VISIBLE
                    visibility = GONE
                } else {
                    emptyView!!.visibility = GONE
                    visibility = VISIBLE
                }
            }
        }

    var mSnapHelper : LinearSnapHelper? = null
    fun setSnapScroll() {
        mSnapHelper = LinearSnapHelper()
        mSnapHelper!!.attachToRecyclerView(this)
    }
}