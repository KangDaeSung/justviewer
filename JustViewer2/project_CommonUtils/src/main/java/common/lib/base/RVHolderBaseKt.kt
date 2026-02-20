package common.lib.base

import android.graphics.Point
import android.view.View
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView

abstract class RVHolderBaseKt<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var item : T? = null

    open fun setData(item: T) {
        this.item = item
        clearView()

        if (itemView.width == 0) {
            itemView.run {
                itemView.doOnLayout {
                    OnLayout()
                }
            }
        } else {
            OnLayout()
        }

    }

    
    open fun clearView() {}
    
    //Activity lifecycle에 따른 처리가 필요한 경우
    open fun onDestroy() {}
    
    //가로 swipe 메뉴기능 구현을 위한 swipe할 view 반환
    open fun getSwipeView(): View? {
        return null
    }

    //최대 swipe할수 있는 width 값
    open fun getSwipeClimp(): Float {
        return 0f
    }

    /**
     * Recycler view의 높이에 맞춰 ratio 비율로 item의 size를 고정한다.
     * @param v : RecyclerView
     */
    open fun setHolderRatioSize(rv:RecyclerView, v:View, ratio:Float, padding:Int) {
        val p = getRatioSize(rv,ratio,padding)
        val lp = RecyclerView.LayoutParams(p.x, p.y)
        v.layoutParams = lp
    }

    open fun getRatioSize(v: View, ratio: Float, padding: Int): Point {
        var rvHeight = v.height
        if (rvHeight <= 0) {
            rvHeight = v.layoutParams?.height!!
        }
        if (rvHeight < 0) {
            rvHeight = 0
        }
        val height = rvHeight - padding - padding
        return Point((height / ratio).toInt(), height)
    }

    /**
     * Item을 set할때 layout이 배치된 이후 호출
     */
    open fun OnLayout() {}

}