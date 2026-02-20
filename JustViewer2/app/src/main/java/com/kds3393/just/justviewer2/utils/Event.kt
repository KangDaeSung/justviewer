package com.kds3393.just.justviewer2.utils

import com.kds3393.just.justviewer2.music.Mp3Id3Data
import com.kds3393.just.justviewer2.music.PlayerManager

enum class ACTION {
    //Music
    MUSIC_STATE_CHANGE,         //음악 상태 변경 STATE_IDLE(1), STATE_BUFFERING(2), STATE_READY(3), STATE_ENDED(4)
    NONE
}

object Event {
    const val MUSIC_LIST_PREPARE = 10000    //음악 리스트 설정, musicPathList 필수
    const val MUSIC_STATE_PLAY = 10001
    const val MUSIC_STATE_PAUSE = 10002
    const val MUSIC_STATE_END = 10003       //음악을 종료한다.
    /**
     * @param state = ExoPlayer : STATE_IDLE(1), STATE_BUFFERING(2), STATE_READY(3), STATE_ENDED(4)
     *                MUSIC_STATE_PLAY, MUSIC_STATE_PAUSE
     *                MUSIC_LIST_PREPARE
     */
    class Music(var action : ACTION,
                var state : Int,
                var musicPathList:ArrayList<String>? = null,
                var player: PlayerManager? = null,
                var musicInfo: Mp3Id3Data? = null) {
        fun logStateToString() : String {
            return when(state) {
                1 -> "STATE_IDLE"
                2 -> "STATE_BUFFERING"
                3 -> "STATE_READY"
                4 -> "STATE_ENDED"
                10001 -> "MUSIC_STATE_PLAY"
                10002 -> "MUSIC_STATE_PAUSE"
                10003 -> "MUSIC_STATE_END"
                else -> "UNKNOWN"
            }
        }
    }

    class MusicFileRemove(var path:String)
    class FavoriteDir(var path:String)
}