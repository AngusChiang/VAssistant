package cn.vove7.common.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * # ThreadPool
 *
 * @author Administrator
 * 2018/11/14
 */
object CoroutineExt {

    fun launch(r: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(block = r)
    }

    fun launchMain(r: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Main, block = r)
    }

    fun launchIo(r: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.IO, block = r)
    }

    fun delayLaunch(millis: Long, context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(context) {
            delay(millis)
            block(this)
        }
    }

    suspend fun <T> withMain(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)

}