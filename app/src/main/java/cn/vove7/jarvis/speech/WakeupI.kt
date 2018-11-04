package cn.vove7.jarvis.speech

import android.support.annotation.CallSuper

/**
 * # WakeupI
 *
 * @author Administrator
 * 2018/11/4
 */
abstract class WakeupI {
    abstract var opened: Boolean

    init {
        instance = this
    }

    @CallSuper
    open fun stop() {
        opened = false
    }

    @CallSuper
    open fun start() {
        opened = true
    }

    abstract fun release()

    companion object {
        var instance: WakeupI? = null
    }
}