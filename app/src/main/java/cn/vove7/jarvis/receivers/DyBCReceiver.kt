package cn.vove7.jarvis.receivers

import android.content.BroadcastReceiver
import android.content.IntentFilter
import cn.vove7.common.app.GlobalApp

/**
 * # DyBCReceiver
 * 动态广播接收器
 * App存活时开启
 * @author Administrator
 * 2018/10/28
 */
abstract class DyBCReceiver : BroadcastReceiver() {
    abstract val intentFilter: IntentFilter

    var open = false

    /**
     * 注册广播接收器
     */
    open fun start() {
        open = true
        GlobalApp.APP.apply {
            val intent = registerReceiver(this@DyBCReceiver, intentFilter)
            if (intent != null) onReceive(this, intent)//注册时即通知
        }
    }

    /**
     * 取消关闭注册
     */
    fun stop() {
        open = false
        try {
            GlobalApp.APP.unregisterReceiver(this)
        } catch (e: Exception) {
        }
    }

}