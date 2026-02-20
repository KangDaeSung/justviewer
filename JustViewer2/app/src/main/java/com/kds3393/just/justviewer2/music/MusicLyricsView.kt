package com.kds3393.just.justviewer2.music

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.core.animation.addListener
import androidx.core.view.doOnLayout
import com.kds3393.just.justviewer2.databinding.VMusicLyricsBinding
import common.lib.base.hide

class MusicLyricsView : LinearLayout {
    val binding = VMusicLyricsBinding.inflate(LayoutInflater.from(context), this)
    var parentWidth = 0
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL

        doOnLayout {
            parentWidth = if (parent is ViewGroup) {
                (parent as ViewGroup).width - binding.root.width
            } else {
                binding.sideLyricsScroll.width
            }
            translationX = binding.sideLyricsScroll.width.toFloat()
        }
        binding.sideOpenBtn.setOnClickListener {
            startSlideAnimation(translationX > 0f)
        }
    }

    fun startSlideAnimation(isShow:Boolean, isHide:Boolean = false) {
        if ((translationX > 0f) == !isShow) {
            if (isHide) {
                this.hide()
            }
            return
        }

        val set = AnimatorSet()
        val transValue = if (isShow) {
            set.interpolator = AnticipateInterpolator()
            parentWidth
        } else {
            set.interpolator = OvershootInterpolator()
            parentWidth + binding.sideLyricsScroll.width
        }
        set.play(ObjectAnimator.ofFloat(this, X, transValue.toFloat()))
        set.duration = 300
        if (isHide) {
            set.addListener(onEnd = {
                this.hide()
            })
        }
        set.start()
    }

    fun setLyricsText(text: String?) {
        if (TextUtils.isEmpty(text)) {
            startSlideAnimation(false,true)
            binding.sideLyricsTxt.text = ""
        } else {
            binding.sideLyricsTxt.text = text
        }
    }

    val isLyrics: Boolean
        get() = !TextUtils.isEmpty(binding.sideLyricsTxt.text)
}