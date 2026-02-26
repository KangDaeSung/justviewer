package com.kds3393.just.justviewer2.music.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kds3393.just.justviewer2.activity.SettingActivity.Companion.getMusicListShuffle
import com.kds3393.just.justviewer2.music.PlayerManager
import com.kds3393.just.justviewer2.utils.ACTION
import com.kds3393.just.justviewer2.utils.Event
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import common.lib.utils.SharedBus
import java.io.File

var telephonyListerCount = 0

class MusicService : LifecycleService() {
    var playFolderPath: String? = null
        private set
    lateinit var mPlayerManager: PlayerManager

    private var isForegroundService = false
    private lateinit var notifManager: NotiManager
    lateinit var mediaSession: MediaSessionCompat

    // Warning 해결: 사용하지 않는 title, isPlay 파라미터 제거
    private fun showNotification() {
        notifManager.showNotificationForPlayer(mPlayerManager.exoPlayer)
    }

    private inner class PlayerNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, this@MusicService.javaClass))
                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            // Warning 해결: 최신 stopForeground API 파라미터 적용
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
            stopSelf()
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == MUSIC_NOTIFICATION_DISMISS) {
                stop()
            }
        }
    }

    private var mIsErrorCompletion = false //error -38로 인해 음악이 중도 끝난 이후 onCompletion이 발생하였을 경우 true

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        val sessionActivityPendingIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        notifManager = NotiManager(this, mediaSession.sessionToken, PlayerNotificationListener())

        mPlayerManager = PlayerManager(baseContext)

        notifManager.showNotificationForPlayer(mPlayerManager.exoPlayer)

        SharedBus.register<Event.Music>(lifecycleScope) { event ->
            if (event.action == ACTION.MUSIC_STATE_CHANGE) {
                when (event.state) {
                    ExoPlayer.STATE_BUFFERING -> CLog.d("MusicService STATE_BUFFERING")
                    ExoPlayer.STATE_READY -> {
                        CLog.d("MusicService STATE_READY")
                        if (mPlayerManager.musicList.size <= 0) return@register
                        // Warning 해결: 파라미터가 제거된 showNotification 호출
                        showNotification()
                    }
                    ExoPlayer.STATE_IDLE -> CLog.d("MusicService STATE_IDLE")
                    ExoPlayer.STATE_ENDED -> {
                        CLog.d("STATE_ENDED")
                        if (mIsErrorCompletion) {
                            mPlayerManager.exoPlayer.stop()
                            playMusic(mPlayerManager.mIndex, true)
                            mIsErrorCompletion = false
                        } else {
                            moveNext(true)
                        }
                        notifManager.hideNotification()
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(MUSIC_NOTIFICATION_CLICK)
        intentFilter.addAction(MUSIC_NOTIFICATION_DISMISS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mReceiver, intentFilter)
        }
    }

    //전화 수신 이벤트 리스너 등록
    private fun registerTelephonyListener() {
        telephonyListerCount++
        // Warning 해결: 메인 모듈 API로 Crashlytics 키 설정 방식 최신화
        FirebaseCrashlytics.getInstance().setCustomKey("telephonyListerCount", telephonyListerCount)

        val telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        telMgr.registerTelephonyCallback(baseContext.mainExecutor, object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            private var mIsCallPause = false
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_IDLE -> if (mIsCallPause) {
                        mIsCallPause = false
                        play()
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mPlayerManager.isPlaying()) {
                        mIsCallPause = true
                        pause()
                    }
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent,flags,startId)
        var newList:ArrayList<String>? = null
        if (intent != null) {
            val folderPath = intent.getStringExtra(EXTRA_MUSIC_FOLDER_PATH)
            if (playFolderPath != null && playFolderPath.equals(folderPath, ignoreCase = true)) {
                if (isPause) {
                    play()
                    return START_STICKY
                } else if (mPlayerManager.isPlaying()) {
                    pause()
                    return START_STICKY
                }
            }
            playFolderPath = folderPath
            var beforePath: String? = ""
            if (mPlayerManager.musicList.size == 1) {
                beforePath = mPlayerManager.musicList[0]
            }
            newList = intent.getStringArrayListExtra(EXTRA_MUSIC_FILE_LIST)?:ArrayList()
            if (newList.size == 1 && beforePath.equals(newList[0], ignoreCase = true)) { //동일한 파일을 연속으로 누를 경우 무시
                return START_STICKY
            }

        }
        newList?:return START_STICKY

        mPlayerManager.newMusicList(newList)
        if (getMusicListShuffle(this)) {
            mPlayerManager.shuffleArray()
        }
        mPlayerManager.stop()

        SharedBus.post(Event.Music(ACTION.MUSIC_STATE_CHANGE, Event.MUSIC_LIST_PREPARE, musicPathList = mPlayerManager.musicList))
        registerTelephonyListener()

        playMusic(0, true)
        return START_STICKY
    }

    fun playMusic(index: Int, isPlay: Boolean) {
        mPlayerManager.play(index, isPlay)
    }

    override fun onDestroy() {
        if (mPlayerManager.isPlaying()) {
            mPlayerManager.stop()
        }
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return mBinder
    }

    // Warning 242 해결: 사용하지 않는 isFolderPlaying 제거

    val playIndex: Int
        get() = if (mPlayerManager.musicList.size > 0) mPlayerManager.mIndex else -1

    fun setShuffle(isShuffle: Boolean) {
        if (isShuffle) {
            mPlayerManager.shuffleArray(mPlayerManager.musicList)
        } else {
            mPlayerManager.restoreList()
        }
    }

    fun deleteCurrentMp3() {
        val deletePath = mPlayerManager.getCurrentFile()
        moveNext(false)
        FileUtils.deleteFile(File(deletePath))
        mPlayerManager.removePath(deletePath)
        mPlayerManager.mIndex--
        SharedBus.post(Event.MusicFileRemove(deletePath))
    }

    fun getDuration() : Long {
        if (mPlayerManager.isPlaying()) {
            return mPlayerManager.getDuration()
        }
        return 0
    }

    fun getCurrentPosition() : Long {
        return mPlayerManager.getCurrentPosition()
    }

    fun movePrev() {
        if (mPlayerManager.isPrev()) {
            playMusic(mPlayerManager.mIndex - 1, mPlayerManager.isPlaying())
        } else {
            Toast.makeText(this@MusicService, "처음입니다.", Toast.LENGTH_LONG).show()
        }
    }

    fun moveNext(isForcePlay: Boolean) {
        CLog.e("KDS3393_TEST_MUSIC moveNext")
        if (mPlayerManager.isNext()) {
            var isPlaying = true
            if (!isForcePlay) isPlaying = mPlayerManager.isPlaying()
            playMusic(mPlayerManager.mIndex + 1, isPlaying)
        } else {
            stop()
        }
    }

    val isPause: Boolean
        get() {
            if (mPlayerManager.isPlaying()) return false
            return mPlayerManager.musicList.size > 0
        }

    val isPlaying:Boolean
        get() {
            return mPlayerManager.isPlaying()
        }

    fun pause() {
        if (mPlayerManager.isPlaying()) {
            mPlayerManager.pause()
        }
    }

    fun seekTo(pos: Long) {
        mPlayerManager.seekTo(pos)
    }

    fun play() {
        CLog.e("KDS3393_TEST_MUSIC play")
        if (mPlayerManager.getDuration() > 0) {
            mPlayerManager.play()
        }
    }

    fun stop() {
        CLog.e("KDS3393_TEST_MUSIC stop")
        mPlayerManager.stop()
        mPlayerManager.clear()
        playFolderPath = null
        SharedBus.post(Event.Music(ACTION.MUSIC_STATE_CHANGE, Event.MUSIC_STATE_END))
        stopSelf() //서비스 종료
    }

    companion object {
        // Warning 334 해결: 사용되지 않던 TAG 변수 삭제
        const val EXTRA_MUSIC_FILE_LIST = "just_music_file_list"
        const val EXTRA_MUSIC_FOLDER_PATH = "just_music_folder_path"
        const val MUSIC_NOTIFICATION_CLICK = "music_notifaction_click"
        const val MUSIC_NOTIFICATION_DISMISS = "music_notifaction_dismiss"
    }
}