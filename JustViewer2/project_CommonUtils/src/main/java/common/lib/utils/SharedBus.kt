package common.lib.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import common.lib.base.ActBaseLib
import common.lib.base.launchMain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
        register(ActBaseLib.unwrap(context).lifecycleScope,callback)
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
}