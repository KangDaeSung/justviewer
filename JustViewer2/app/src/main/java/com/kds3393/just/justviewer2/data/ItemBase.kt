package com.kds3393.just.justviewer2.data

import java.io.Serializable

open class ItemBase(var mPath: String = "") : Serializable {
    companion object {
        private const val TAG = "ItemBase"
    }
}