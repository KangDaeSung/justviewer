package common.lib.base

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CoroutineTask(private val onPreExecute:((Array<out Any?>?) -> Unit)? = null,
                    private val doInBackground:(suspend (Array<out Any?>?) -> Any?)? = null,
                    private val onPostExecute:((Any?) -> Unit)? = null) : CoroutineScope {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancel() {
        taskJob?.cancel()
    }

    fun isRunning() : Boolean {
        return taskJob?.isActive == true
    }

    var taskJob : Job? = null
    fun execute(vararg params: Any?) : CoroutineTask {
        onPreExecute?.invoke(params)
        taskJob = launch(coroutineContext) {
            val result = withContext(coroutineContext) {
                doInBackground?.invoke(params)
            }
            launchMain { onPostExecute?.invoke(result) }
        }
        return this
    }
}

