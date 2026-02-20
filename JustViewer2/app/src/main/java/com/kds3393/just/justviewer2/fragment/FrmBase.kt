package com.kds3393.just.justviewer2.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.kds3393.just.justviewer2.activity.ActBase

/**
 * Created by android on 2016-10-12.
 */
open class FrmBase : Fragment() {
    private val arg: Bundle
        get() {
            var bundle = arguments
            if (bundle == null) {
                bundle = Bundle()
                arguments = bundle
            }
            return bundle
        }
    fun getArg(key:String,def:String?) : String { return requireArguments().getString(key, def) }
    fun setArg(key:String,value:String?) : String {
        arg.putString(key, value?:"")
        return value?:""
    }
    fun getArg(key:String,def:Int) : Int { return requireArguments().getInt(key, def) }
    fun setArg(key:String,value:Int) : Int {
        arg.putInt(key, value)
        return value
    }
    fun getArg(key:String,def:Boolean) : Boolean { return requireArguments().getBoolean(key, def) }
    fun setArg(key:String,value:Boolean) : Boolean {
        arg.putBoolean(key, value)
        return value
    }

    open fun onBackPressed(): Boolean {
        return true
    }

    val isFragmentUIActive: Boolean
        get() = isAdded && !isDetached && isVisible

    private fun getActBase() : ActBase? {
        return if (activity != null && activity is ActBase) {
            activity as ActBase
        } else {
            null
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    open fun setEditMode(isEdit: Boolean) {}

    fun runActResult(intent: Intent, responseCode: String? = null, option: ActivityOptionsCompat? = null, callback : ((String?, Int, Intent?) -> Unit)? = null) {
        getActBase()?.runActResult(intent,responseCode,option,callback)
    }
}