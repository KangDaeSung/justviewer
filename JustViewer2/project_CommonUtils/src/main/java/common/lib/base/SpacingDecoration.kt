package common.lib.base

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpacingDecoration @JvmOverloads constructor(private var spacing:Int = 1, private val orintation: Int = LinearLayoutManager.VERTICAL, color: Int? = null): ItemDecoration() {
    private var mPaint: Paint? = null
    private var mIsShowLastDivider = false

    init {
        if (color != null) {
            mPaint = Paint()
            mPaint!!.color = color
            mPaint!!.strokeWidth = spacing.toFloat()
        }
    }

    fun setSpacing(spacing:Int) {
        this.spacing = spacing
        if (mPaint != null) {
            mPaint?.strokeWidth = spacing.toFloat()
        }
    }

    fun showLastDivider() : SpacingDecoration {
        mIsShowLastDivider = true
        return this
    }

    private var mDrawPaintRuleMap: HashMap<Int, Paint>? = null
    fun addSpacingRule(index: Int, ruleSpacing: Int, color: Int? = null): SpacingDecoration {
        if (mDrawPaintRuleMap == null) {
            mDrawPaintRuleMap = HashMap()
        }
        val p = Paint()
        p.color = color?:mPaint!!.color
        p.strokeWidth = ruleSpacing.toFloat()
        mDrawPaintRuleMap!![index] = p
        return this
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val childPosition = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter!!.itemCount
        if (!mIsShowLastDivider && childPosition >= itemCount - 1) {
            return
        }
        var itemSpacing = spacing
        if (mDrawPaintRuleMap != null) {
            val p = mDrawPaintRuleMap!![childPosition]
            if (p != null) {
                itemSpacing = p.strokeWidth.toInt()
            }
        }

        if (orintation == LinearLayoutManager.VERTICAL) {
            outRect.bottom = itemSpacing
        } else if (orintation == LinearLayoutManager.HORIZONTAL) {
            outRect.right = itemSpacing
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (mPaint != null || mDrawPaintRuleMap != null) {
            val validChildCount = parent.childCount
            for (i in 0 until validChildCount) {
                val child = parent.getChildAt(i)
                val childPosition = parent.getChildAdapterPosition(child)
                val itemCount = parent.adapter!!.itemCount
                if (!mIsShowLastDivider && childPosition >= itemCount - 1) { // Don't draw divider for last line if mShowLastDivider = false
                    continue
                }

                var paint: Paint? = null
                if (mDrawPaintRuleMap != null) {
                    paint = mDrawPaintRuleMap!![i]
                }
                if (paint == null) {
                    paint = mPaint
                }
                if (paint != null) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val heightCenter = child.bottom + params.bottomMargin + paint.strokeWidth / 2
                    c.drawLine(child.left.toFloat(), heightCenter, child.right.toFloat(), heightCenter, paint)
                }
            }
        }
    }

    //현재 position에 대한 col,row index
    private fun getGroupIndex(position: Int, parent: RecyclerView): Int {
        if (parent.layoutManager is GridLayoutManager) {
            val layoutManager = parent.layoutManager as GridLayoutManager?
            val spanSizeLookup = layoutManager!!.spanSizeLookup
            val spanCount = layoutManager.spanCount
            return spanSizeLookup.getSpanGroupIndex(position, spanCount)
        }
        return position
    }

    private fun isReverseLayout(parent: RecyclerView): Boolean {
        val layoutManager = parent.layoutManager
        return if (layoutManager is LinearLayoutManager) layoutManager.reverseLayout else false
    }
}