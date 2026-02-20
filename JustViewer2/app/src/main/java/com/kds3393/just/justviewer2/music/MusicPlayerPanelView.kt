package com.kds3393.just.justviewer2.music

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.exoplayer2.ExoPlayer
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.SettingActivity
import com.kds3393.just.justviewer2.databinding.MusicPanelBinding
import com.kds3393.just.justviewer2.music.player.MusicService
import com.kds3393.just.justviewer2.utils.Event
import common.lib.base.gone
import common.lib.base.isShow
import common.lib.base.show
import common.lib.utils.SharedBus

class MusicPlayerPanelView : LinearLayout {
    private var mService: MusicService? = null
    var isShow = false
        private set
    private var mShowAnimation: com.kds3393.just.justviewer2.animation.LayoutTranslateAnimation? = null
    private var mHideAnimation: com.kds3393.just.justviewer2.animation.LayoutTranslateAnimation? = null
    private var mMoveDistances = 0 //Open Animation시 이동 거리

    private var mOpenButtonGestureDetector: GestureDetector? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        mShowAnimation = com.kds3393.just.justviewer2.animation.LayoutTranslateAnimation(context, AnticipateInterpolator())
        mHideAnimation = com.kds3393.just.justviewer2.animation.LayoutTranslateAnimation(context, OvershootInterpolator())
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setBackgroundResource(R.drawable.z_music_panel)
        createView()
        SharedBus.register<Event.MusicFileRemove>(context) {
            if (mMusicListPanel != null) {
                mMusicListPanel!!.setMusicListItem(mService!!.mPlayerManager.musicList)
            }
        }
        SharedBus.register<Event.Music>(context) {
            if (it.state == Event.MUSIC_LIST_PREPARE) {  //play list 준비 완료
                if (mMusicListPanel != null && it.musicPathList != null) mMusicListPanel!!.setMusicListItem(it.musicPathList!!)
            } else if (it.state == ExoPlayer.STATE_READY) {     //다음 play할 음악 준비 완료
                binding.panelTitle.text = it.musicInfo?.mTitle
                binding.panelArtist.text = it.musicInfo?.mArtist
                binding.panelSeekbar.progress = 0
                binding.panelSeekbar.max = mService!!.getDuration().toInt()
                binding.panelDurationTime.text = common.lib.utils.Utils.getStringForDate(it.player?.getDuration()?:0L, "mm:ss")
                setPlayButtonImage()
                if (this@MusicPlayerPanelView.visibility != VISIBLE) {
                    this@MusicPlayerPanelView.visibility = VISIBLE
                }
                if (mMusicLyricsPanel != null) {
                    if (isShow) mMusicLyricsPanel!!.show()
                    mMusicLyricsPanel!!.setLyricsText(it.musicInfo?.mLyrics)
                }
                if (mMusicListPanel != null) {
                    if (isShow) mMusicListPanel!!.show()
                    mMusicListPanel!!.onChangePlayMusic()
                }
            } else if (it.state == Event.MUSIC_STATE_PLAY) {   //play
                binding.panelPlay.setBackgroundResource(R.drawable.h_media_pause)
            } else if (it.state == Event.MUSIC_STATE_PAUSE) {   //pause
                binding.panelPlay.setBackgroundResource(R.drawable.h_media_play)
            } else if (it.state == Event.MUSIC_STATE_END) { //모든 음악 play 완료
                if (this@MusicPlayerPanelView.isShow()) {
                    this@MusicPlayerPanelView.gone()
                    if (mMusicLyricsPanel != null) mMusicLyricsPanel!!.startSlideAnimation(isShow = false, isHide = true)
                    if (mMusicListPanel != null) mMusicListPanel!!.startSlideAnimation(isShow = false, isHide = true)
                    setSwitchMusicPanel(false)
                }
            }
        }
    }

    private var mMusicLyricsPanel: MusicLyricsView? = null
    fun setLyricsView(lyrics: MusicLyricsView?) {
        mMusicLyricsPanel = lyrics
    }

    private var mMusicListPanel: MusicListView? = null
    fun setMusicList(l: MusicListView?) {
        mMusicListPanel = l
    }

    lateinit var binding: MusicPanelBinding
    fun setMusicService(service: MusicService?) {
        mService = service
        if (mMusicListPanel != null) {
            mMusicListPanel!!.setMusicService(service)
        }
        mService?.apply {
            if (mPlayerManager.isPlaying() || isPause) {
                binding.panelSeekbar.max = mPlayerManager.getDuration().toInt()
                binding.panelDurationTime.text = common.lib.utils.Utils.getStringForDate(mPlayerManager.getDuration(), "mm:ss")
                val data = mPlayerManager.musicMetaData
                binding.panelTitle.text = data?.mTitle
                binding.panelArtist.text = data?.mArtist
                this@MusicPlayerPanelView.visibility = VISIBLE
                if (mMusicLyricsPanel != null) {
                    if (isShow) mMusicLyricsPanel?.show()
                    mMusicLyricsPanel!!.setLyricsText(data?.mLyrics)
                }
                if (mMusicListPanel != null) {
                    if (isShow) mMusicListPanel?.show()
                    mMusicListPanel!!.setMusicListItem(mService!!.mPlayerManager.musicList)
                }
            }
        }
    }

    fun setSwitchMusicPanel(isShow: Boolean) {
        if (this.isShow == isShow) return
        this.isShow = isShow
        if (isShow) {
            mPanelHandler.sendEmptyMessage(SHOW_MUSIC_PANEL)
            mShowAnimation!!.startUsingDistance(this, true, mMoveDistances, 400, null)
            setPlayButtonImage()
            if (mMusicLyricsPanel != null && mMusicLyricsPanel!!.isLyrics) {
                mMusicLyricsPanel!!.visibility = VISIBLE
            }
            if (mMusicListPanel != null) mMusicListPanel!!.visibility = VISIBLE
        } else {
            if (mMusicLyricsPanel != null) {
                mMusicLyricsPanel!!.startSlideAnimation(isShow = false, isHide = true)
            }
            if (mMusicListPanel != null) mMusicListPanel!!.startSlideAnimation(isShow = false, isHide = true)
            mHideAnimation!!.startUsingDistance(this, true, -mMoveDistances, 400, null)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mMoveDistances == 0) {
            mMoveDistances = t
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createView() {
        binding = MusicPanelBinding.inflate(LayoutInflater.from(context), this)
        binding.panelSeekbar.setOnSeekBarChangeListener(mSeekListener)        // line 0	// open btn
        mOpenButtonGestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(event: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {}
            override fun onShowPress(e: MotionEvent) {}
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                return true
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (!isShow && velocityY < 0) setSwitchMusicPanel(true) else if (isShow && velocityY > 0) setSwitchMusicPanel(false)
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                setSwitchMusicPanel(!isShow)
                return true
            }
        })
        binding.panelOpen.setOnTouchListener { _, event ->
            mOpenButtonGestureDetector!!.onTouchEvent(event)
            true
        }
        binding.panelFavo.setOnClickListener { }
        binding.panelShuffle.setOnClickListener {
            val isMix = !SettingActivity.getMusicListShuffle(context)
            SettingActivity.setMusicListShuffle(context, isMix)
            setSuppleButtonImage()
            if (mService != null) {
                mService!!.setShuffle(isMix)
            }
            if (mMusicListPanel != null) mMusicListPanel!!.setMusicListItem(mService!!.mPlayerManager.musicList)
        }
        binding.panelPrev.setOnClickListener { mService!!.movePrev() }
        binding.panelPlay.setOnClickListener {
            if (mService!!.isPlaying) {
                mService!!.pause()
                binding.panelPlay.setBackgroundResource(R.drawable.h_media_play)
            } else {
                mService!!.play()
                binding.panelPlay.setBackgroundResource(R.drawable.h_media_pause)
            }
        }
        binding.panelNext.setOnClickListener { mService!!.moveNext(false) }
        binding.panelStop.setOnClickListener { mService!!.stop() }
        binding.panelDel.setOnClickListener { mService!!.deleteCurrentMp3() }
    }

    private fun setPlayButtonImage() {
        if (mService != null) {
            if (mService!!.isPlaying) binding.panelPlay.setBackgroundResource(R.drawable.h_media_pause) else binding.panelPlay.setBackgroundResource(R.drawable.h_media_play)
        } else {
            binding.panelPlay.setBackgroundResource(R.drawable.h_media_play)
        }
    }

    private fun setSuppleButtonImage() {
        if (SettingActivity.getMusicListShuffle(context)) {
            binding.panelShuffle.setBackgroundResource(R.drawable.h_media_shuffle_on)
        } else {
            binding.panelShuffle.setBackgroundResource(R.drawable.h_media_shuffle_off)
        }
    }

    private var mDragging = false
    private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStartTrackingTouch(bar: SeekBar) {
            mDragging = true
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
            if (!fromuser) {
                return
            }
            if (mService!!.isPlaying) {
                mService!!.seekTo(progress.toLong())
            }
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            mDragging = false
        }
    }
    private val mPanelHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(handleMsg: Message) {
            var msg = handleMsg
            val pos : Long
            when (msg.what) {
                SHOW_MUSIC_PANEL -> {
                    pos = mService?.getCurrentPosition()?:-1
                    if (pos >= 0) {
                        if (isShow) {
                            if (!mDragging) {
                                binding.panelSeekbar.progress = pos.toInt()
                            }
                            msg = obtainMessage(SHOW_MUSIC_PANEL)
                            sendMessageDelayed(msg, (1000 - pos % 1000))
                        }
                        binding.panelCurrentTime.text = common.lib.utils.Utils.getStringForDate(pos, "mm:ss")
                    } else {
                        sendMessageDelayed(msg, 1000)
                    }
                }
            }
        }
    }

    fun onResume() {
        if (mService != null) {
            setPlayButtonImage()
        }
        setSuppleButtonImage()
    }

    fun clear() {
    }

    companion object {
        private const val SHOW_MUSIC_PANEL = 0
    }
}