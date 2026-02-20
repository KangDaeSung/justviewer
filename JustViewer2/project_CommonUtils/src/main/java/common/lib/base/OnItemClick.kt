package common.lib.base

import android.view.View

//Holder의 click event를 상위 adapter 또는 Activity에서 제어하기 위한 listener
interface OnItemClick<T> {
    fun onClick(v: View, position: Int, item:T)
}