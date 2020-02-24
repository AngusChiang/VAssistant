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
 * ```
 * override val scopeLazyer: Lazy<out CoroutineScope> = genScopeLazyer()
 * override val scope: CoroutineScope by scopeLazyer
 * override val lc: Lifecycle get() = lifecycle
 * ```
 *
 * Created on 2020/1/13
 * @author Vove
 */

class LifeCycleScope(override val lc: Lifecycle) : LifeCycleScopeDelegate {
    override val scopeLazyer: Lazy<out CoroutineScope> = genScopeLazyer()
    override val scope: CoroutineScope by scopeLazyer
}

interface LifeCycleScopeDelegate {

    val scopeLazyer: Lazy<out CoroutineScope>
    val scope: CoroutineScope
    val lc: Lifecycle

    fun genScopeLazyer(): Lazy<out CoroutineScope> = lazy {
        //初始化时 触发监听
        lc.addObserver(LifecycleScpeObserver(::cancel))
        object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = EmptyCoroutineContext
        }
    }

    private fun cancel() {
        if (scopeLazyer.isInitialized()) {
            kotlin.runCatching {
                scope.cancel()
            }
        }
    }

    fun launch(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
    ): Job = scope.launch(context, start, block)

    fun launchIo(
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(Dispatchers.IO, start, block)
    }

    class LifecycleScpeObserver(val des: () -> Unit) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun cal() = des()
    }
}
