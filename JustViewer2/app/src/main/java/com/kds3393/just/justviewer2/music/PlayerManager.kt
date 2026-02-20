package com.kds3393.just.justviewer2.music

import android.content.Context
import android.net.Uri
import common.lib.utils.SharedBus
import common.lib.debug.CLog
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.kds3393.just.justviewer2.utils.ACTION
import com.kds3393.just.justviewer2.utils.Event
import java.io.File
import java.util.*

class PlayerManager(private val context: Context) {
    val baseMusicList = ArrayList<String>()     //원본 music file path, musicList가 변경된 경우 원복하기 위한 데이터
    var musicList = ArrayList<String>()         //play하고 있는 music file path list shuffle이나 다른 이유로 변경될수 있다.
    var mIndex = 0                              //musicList에서 현재 play중인 file path의 index
    var musicMetaData: Mp3Id3Data? = null       //musicList에서 현재 play중인 music의 제목, 가사등의 정보

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(), true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            CLog.e("KDS3393_TEST_MUSIC playerListener onPlaybackStateChanged playbackState = $playbackState")
            if (playbackState == ExoPlayer.STATE_READY && musicList.size > mIndex) {
                musicMetaData = Mp3Id3Parser.mp3HeaderParser(musicList[mIndex])
            }
            SharedBus.post(Event.Music(ACTION.MUSIC_STATE_CHANGE, playbackState,player = this@PlayerManager, musicInfo = musicMetaData))
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            CLog.e("KDS3393_TEST_MUSIC playerListener onIsPlayingChanged isPlaying = $isPlaying")
            SharedBus.post(Event.Music(ACTION.MUSIC_STATE_CHANGE, if (isPlaying) Event.MUSIC_STATE_PLAY else Event.MUSIC_STATE_PAUSE))
        }

        override fun onPlayerError(error: PlaybackException) {
            CLog.e(error)
        }
    }

    //새로운 폴더나 파일을 선택한 경우
    fun newMusicList(list : ArrayList<String>) {
        musicList = list
        baseMusicList.clear()
        baseMusicList.addAll(musicList)
        CLog.e("KDS3393_TEST_MUSIC newMusicList [${baseMusicList}]")
    }

    //shuffle 했던 순서를 원복
    fun restoreList() {
        val curPath = getCurrentFile()
        musicList.clear()
        musicList.addAll(baseMusicList)
        mIndex = musicList.indexOf(curPath)
    }

    fun clear() {
        musicList.clear()
        baseMusicList.clear()
    }

    //파일을 삭제했거나 목록에서 특정 파일을 지울경우
    fun removePath(path:String) {
        musicList.remove(path)
        baseMusicList.remove(path)
    }

    fun getCurrentFile() : String {
        return musicList[mIndex]
    }
    fun seekTo(pos: Long) {
        exoPlayer.seekTo(pos)
    }

    fun play(index:Int, isPlay: Boolean) {
        if (musicList.size <= index) //때때로 mMp3PathList의 size가 0인경우 있음
            return
        if (File(musicList[index]).exists()) {
            play(musicList[index],isPlay)
        } else {
            CLog.e("KDS3393_TEST_MUSIC play Failed index[$index] musicList[$musicList]")
        }
    }

    fun play(newMusicPath:String? = null, isPlay : Boolean = true) {
        CLog.e("KDS3393_TEST_MUSIC playerManager newMusicPath = $newMusicPath")
        if (newMusicPath.isNullOrEmpty()) {
            if (!exoPlayer.isPlaying) {
                exoPlayer.play()
            }
        } else {
            mIndex = musicList.indexOf(newMusicPath)
            exoPlayer.setMediaSource(createMediaSource(newMusicPath))
            exoPlayer.playWhenReady = isPlay
            exoPlayer.prepare()
        }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
    }

    fun isPlaying() : Boolean {
        return exoPlayer.isPlaying
    }

    fun getDuration() : Long {
        return exoPlayer.duration
    }

    fun getCurrentPosition() : Long {
        return exoPlayer.currentPosition
    }

    fun isPrev() : Boolean {
        return mIndex > 0
    }

    fun isNext() : Boolean {
        return musicList.size > mIndex + 1
    }

    fun release() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    private fun createMediaSource(url: String): MediaSource {
        val targetUrl = Uri.fromFile(File(url))
        CLog.e("KDS3393_TEST_MUSIC createMediaSource scheme[${targetUrl.scheme}] url = $targetUrl")
        val videoSource = if (targetUrl.scheme == "file") {
            ProgressiveMediaSource.Factory(FileDataSource.Factory()).createMediaSource(MediaItem.Builder().setUri(targetUrl).build())
        } else {
            ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(MediaItem.Builder().setUri(targetUrl).build())
        }

        return ConcatenatingMediaSource(videoSource)
    }

    fun shuffleArray(array: ArrayList<String> = musicList) {
        var curPath: String? = null
        if (isPlaying()) {
            curPath = array.removeAt(mIndex)
        }
        array.shuffle()
        if (isPlaying() && curPath != null) {   //현재 play중인 경우 play중인 file path를 index 0으로 고정하고 나머지만 shuffle 한다.
            array.add(0,curPath)
            CLog.e("curPath = $curPath")
        }
        mIndex = 0
    }
}