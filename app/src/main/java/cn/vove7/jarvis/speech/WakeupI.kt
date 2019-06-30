package cn.vove7.jarvis.speech

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


    /**
     * 关闭语音唤醒
     */
    fun stop() {
        opened = false
        Vog.d("关闭语音唤醒")
        doStop()
    }

    /**
     * 开启语音唤醒
     */
    fun start() {
        opened = true
        Vog.d("开启语音唤醒")
        doStart()
    }

    abstract fun doStop()

    abstract fun doStart()
    abstract fun release()

}