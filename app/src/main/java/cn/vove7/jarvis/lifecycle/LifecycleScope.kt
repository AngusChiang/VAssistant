package cn.vove7.jarvis.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * # LifeCycleScope
 * 嵌入lifecycleScope
 *
 * Created on 2020/1/13
 * @author Vove
 */

class LifecycleScope(lifecycle: Lifecycle) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    init {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun destroy() {
                if (coroutineContext[Job] != null) {
                    cancel()
                }
            }
        })
    }
}

interface LifeCycleScopeDelegate {
    val lifecycleScope: LifecycleScope

    fun launch(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
    ): Job = lifecycleScope.launch(context, start, block)

    fun launchIO(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO, start, block)
    }

}
