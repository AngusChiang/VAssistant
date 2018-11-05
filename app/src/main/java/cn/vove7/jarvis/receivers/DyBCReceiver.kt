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

    open fun start() {
        open = true
        GlobalApp.APP.registerReceiver(this, intentFilter)
    }

    fun stop() {
        open = false
        GlobalApp.APP.unregisterReceiver(this)
    }

}