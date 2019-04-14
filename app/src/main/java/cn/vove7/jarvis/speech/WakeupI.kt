package cn.vove7.jarvis.speech

import android.support.annotation.CallSuper
import cn.vove7.vtp.log.Vog

/**
 * # WakeupI
 *
 * @author Administrator
 * 2018/11/4
 */
abstract class WakeupI {
    /**
     * 唤醒开启状态
     */
    var opened: Boolean = false
        set(v) {
            Vog.d("语音唤醒开启状态：$v")
            field = v
        }


    @CallSuper
    open fun stop() {
        opened = false
        Vog.d("关闭语音唤醒")
    }

    @CallSuper
    open fun start() {
        opened = true
        Vog.d("开启语音唤醒")
    }

    abstract fun release()

}