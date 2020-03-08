package cn.vove7.jarvis.receivers

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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

    companion object {
        const val TYPE_GLOBAL = 0
        const val TYPE_LOCAL = 1
    }

    var open = false

    open val receiverType = TYPE_GLOBAL

    /**
     * 注册广播接收器
     */
    fun start() {
        if (!open) {
            onStart()
        }
        open = true
        GlobalApp.APP.apply {
            if (receiverType == TYPE_LOCAL) {
                val intent = registerReceiver(this@DyBCReceiver, intentFilter)
                if (intent != null) onReceive(this, intent)//注册时即通知
            } else {
                LocalBroadcastManager.getInstance(this)
                        .registerReceiver(this@DyBCReceiver, intentFilter)
            }
        }
    }

    open fun onStart() {}

    /**
     * 取消关闭注册
     */
    fun stop() {
        if (open) {
            onStop()
        }
        open = false
        try {
            if (receiverType == TYPE_LOCAL) {
                LocalBroadcastManager.getInstance(GlobalApp.APP).unregisterReceiver(this)
            } else {
                GlobalApp.APP.unregisterReceiver(this)
            }
        } catch (e: Exception) {
        }
    }

    open fun onStop() {}

}