package com.kds3393.just.justviewer2.music

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.animation.addListener
import androidx.core.view.doOnLayout
import common.lib.utils.FileUtils
import com.kds3393.just.justviewer2.databinding.VMusicListBinding
import com.kds3393.just.justviewer2.databinding.VMusicListItemBinding
import com.kds3393.just.justviewer2.music.player.MusicService
import common.lib.base.hide
import common.lib.base.show

class MusicListView : LinearLayout {
    private var mService: MusicService? = null
    val binding: VMusicListBinding = VMusicListBinding.inflate(LayoutInflater.from(context), this)

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        orientation = HORIZONTAL
        doOnLayout {
            translationX = -binding.musicList.width.toFloat()
        }
        binding.sideOpenBtn.setOnClickListener {
            startSlideAnimation(translationX < 0)
        }

        mAdapter = MP3Adapter(context)
        binding.musicList.adapter = mAdapter
        binding.musicList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> mService!!.playMusic(position, mService!!.isPlaying) }
    }

    fun setMusicService(s: MusicService?) {
        mService = s
    }

    private var mAdapter: MP3Adapter? = null
    fun setMusicListItem(array: ArrayList<String>) {
        mAdapter!!.clear()
        mAdapter!!.addAll(array)
        mAdapter!!.notifyDataSetChanged()
    }

    fun onChangePlayMusic() {
        mAdapter!!.notifyDataSetChanged()
        val first = binding.musicList.firstVisiblePosition
        val last = binding.musicList.lastVisiblePosition
        val visibleCount = last - first
        var index = mService!!.playIndex
        if (index < first || index > last) {
            index -= visibleCount / 2
            if (index < 0) index = 0 else if (index >= mAdapter!!.count) index = mAdapter!!.count - 1
            binding.musicList.setSelection(index)
        }
    }

    fun startSlideAnimation(isShow:Boolean, isHide:Boolean = false) {
        if ((translationX < 0) == !isShow) {
            if (isHide) {
                this.hide()
            }
            return
        }

        val set = AnimatorSet()
        val transValue = if (isShow) {
            set.interpolator = AnticipateInterpolator()
            0
        } else {
            set.interpolator = OvershootInterpolator()
            -binding.musicList.width
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

    inner class MP3Adapter(context: Context?) : ArrayAdapter<String?>(context!!, 0) {
        internal inner class ViewHolder(var itemBinding: VMusicListItemBinding) {
            fun setData(name: String?) {
                itemBinding.musicName.text = name
            }

            fun setPlay(isPlay: Boolean) {
                if (isPlay) {
                    itemBinding.statusIcon.show()
                    itemBinding.musicName.setBackgroundColor(Color.WHITE)
                    itemBinding.musicName.setTextColor(Color.BLACK)
                } else {
                    itemBinding.statusIcon.hide()
                    itemBinding.musicName.background = null
                    itemBinding.musicName.setTextColor(Color.WHITE)
                }
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getContent(position, convertView, parent)
        }

        private fun getContent(position: Int, cView: View?, parent: ViewGroup): View {
            var convertView = cView
            val holder: ViewHolder
            if (convertView == null) {
                val binding = VMusicListItemBinding.inflate(LayoutInflater.from(parent.context))
                convertView = binding.root
                holder = ViewHolder(binding)
                convertView.setTag(holder)
            } else {
                holder = convertView.tag as ViewHolder
            }
            val text = getItem(position)
            holder.setData(FileUtils.getFileName(text!!))
            val index = mService!!.playIndex
            holder.setPlay(index >= 0 && position == index)
            return convertView
        }
    }
}