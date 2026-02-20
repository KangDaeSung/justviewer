package com.kds3393.just.justviewer2.utils

import android.content.Context
import android.widget.Toast
import com.kds3393.just.justviewer2.CApp
import common.lib.debug.CLog

object CToast {
    private var toast:Toast? = null
    fun unimplemented(ctx: Context?) {
        Toast.makeText(ctx, "미구현 항목", Toast.LENGTH_SHORT).show()
    }

    fun normal(text: String) {
        normal(CApp.get(),text)
    }

    fun normal(ctx: Context, text: String) {
        if (text.isNotEmpty()) {
            Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun normal(ctx: Context? = null, res: Int) {
        Toast.makeText(ctx?: CApp.get(), (ctx?: CApp.get()).getString(res), Toast.LENGTH_SHORT).show()
    }
}