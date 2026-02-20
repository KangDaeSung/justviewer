package common.lib.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class RVListAdapterKt<T> : RecyclerView.Adapter<RVHolderBaseKt<T>>() {
    val items: ArrayList<T> = ArrayList()
    private var mIsEndlessLoop = false
    var onItemClick: OnItemClick<T>? = null

    fun add(item: T) {
        items.add(item)
    }

    fun addDataChange(item: T) {
        items.add(item)
        notifyItemInserted(items.size-1)
    }

    open fun insert(pos: Int, item: T) {
        var insertPos = pos
        if (insertPos == POSITION_LAST) {
            insertPos = items.size
        }
        items.add(insertPos, item)
        notifyItemInserted(insertPos)
    }

    fun change(pos: Int, obj: T) {
        remove(pos)
        insert(pos, obj)
    }

    fun addAll(item: ArrayList<T>) {
        items.addAll(item)
    }

    fun insert(pos: Int, array: ArrayList<T>) {
        var index = pos
        if (pos == POSITION_LAST) {
            index = items.size
        }
        items.addAll(index, array)
        if (pos > POSITION_LAST) {
            notifyItemRangeInserted(index, array.size)
        } else {
            notifyDataSetChanged()
        }
    }

    open fun clear() {
        items.clear()
    }

    fun remove(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun remove(obj: T) {
        val index = items.indexOf(obj)
        if (index >= 0) {
            items.remove(obj)
            notifyItemRemoved(index)
        }
    }

    fun remove(pos: Int, array: ArrayList<T>) {
        items.removeAll(array.toSet())
        notifyItemRangeRemoved(pos, array.size)
    }

    val firstItem: T?
        get() = if (items.size > 0) {
            items[0]
        } else null
    val lastItem: T?
        get() = if (items.size > 0) {
            items[items.size - 1]
        } else null

    fun getItem(pos: Int): T? {
        var itemPos = pos
        if (mIsEndlessLoop) {
            itemPos %= items.size
        }
        return if (items.size > itemPos) {
            items[itemPos]
        } else null
    }

    fun getItemPosition(item: T): Int {
        for (i in items.indices) {
            if (items[i] === item) {
                return i
            }
        }
        return -1
    }

    //item의 중간값을 반환한다. (total / 2)
    open fun getCenterCount(): Int {
        if (items.size == 0) {
            return 0
        }
        return if (mIsEndlessLoop) {
            val realCount: Int = realCount
            Int.MAX_VALUE / realCount / 2 * realCount
        } else {
            realCount / 2
        }
    }

    val realCount: Int
        get() = items.size

    override fun getItemCount(): Int {
        return if (mIsEndlessLoop) {
            if (items.size > 0) {
                Int.MAX_VALUE
            } else {
                0
            }
        } else {
            items.size
        }
    }

    var isEndlessLoop: Boolean
        get() = mIsEndlessLoop
        set(endless) {
            if (mIsEndlessLoop == endless) {
                return
            }
            mIsEndlessLoop = endless
            notifyDataSetChanged()
        }

    companion object {
        const val POSITION_LAST = -1
    }

    override fun onBindViewHolder(holder: RVHolderBaseKt<T>, position: Int) {
        holder.setData(getItem(position)!!)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVHolderBaseKt<T> {
        return super.createViewHolder(parent, viewType)
    }

    //이전 데이터와 검증을 위해 리스트를 Map으로 저장
    private var itemMap : HashMap<Int,T>? = null
    fun setUseItemMap(isUse : Boolean) {
        itemMap = if (isUse) {
            HashMap()
        } else {
            null
        }
    }

    //override하여 adapter에서 맵에 저장해줘야 함
    fun setHashMap(id:Int, item:T) {
        if (itemMap != null) {
            itemMap!![id] = item
        }
    }

    fun hasItem(id:Int) : Boolean {
        return itemMap?.get(id) != null
    }
}