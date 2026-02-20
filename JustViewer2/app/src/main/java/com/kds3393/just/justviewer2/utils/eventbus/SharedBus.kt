package com.kds3393.just.justviewer2.utils.eventbus

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.gson.JsonObject
import com.kds3393.just.justviewer2.activity.ActBase
import common.lib.base.launchMain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ACTION {

    ACTION_NONE
}
/**
 * broadcast : SharedBus.post(SharedBus.MyMedia(ACTION.ACTION_CHAT_UNREAD_BADGE, readMsgNo!!))
 * receive   : SharedBus.register<SharedBus.MyMedia>(lifecycleScope) {}
 */
object SharedBus {
    /*
    replay :
    collector 가 연결됐을 때, 전달받을 이전 데이터의 개수를 지정한다.
    0일 경우, collect 이후의 데이터부터 전달받고,
    1일 경우, collect 직전의 데이터부터 전달받으며 시작한다.
    */
    private val events = MutableSharedFlow<Any>(replay = 0)
    val mutableEvents = events.asSharedFlow()

    inline fun<reified T> register(context: Context, crossinline callback:(T) -> Unit) {
        register(ActBase.unwrap(context).lifecycleScope,callback)
    }

    inline fun<reified T> register(lifecycle: LifecycleCoroutineScope, crossinline callback:(T) -> Unit) {
        lifecycle.launch {
            subscribe<T>().collect { value ->
                callback(value)
            }
        }
    }

    fun post(event: Any) {
        launchMain { emit(event) }
    }

    private suspend fun emit(event: Any) {
        events.emit(event)
    }

    inline fun <reified T> subscribe(): Flow<T> {
        return mutableEvents.filter { it is T }.map { it as T }
    }

    class Login
    class MyMedia(var action: ACTION, var obj: JsonObject?)
    class MediaReply(var action: ACTION, obj: JsonObject, actionObj: JsonObject, act: ActBase? = null) {
        var obj : JsonObject //댓글 json

        var actionObj : JsonObject //삭제된 댓글 no 정보 외 각 action에 필요한 json 정보

        var act: ActBase?

        init {
            this.obj = obj
            this.actionObj = actionObj
            this.act = act
        }
    }
}