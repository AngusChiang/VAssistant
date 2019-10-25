package cn.vove7.jarvis.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import cn.vove7.jarvis.activities.MainActivity

/**
 * # MyReceiver
 *
 * @author Vove
 * 2019/7/21
 */
class JPushReceiver : JPushMessageReceiver() {
    override fun onNotifyMessageArrived(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageArrived(p0, p1)
        Log.d("Debug :", "onNotifyMessageArrived  ----> ${p1}")
    }

    override fun onNotifyMessageOpened(p0: Context?, p1: NotificationMessage?) {
        super.onNotifyMessageOpened(p0, p1)
        p0?.startActivity(Intent(p0, MainActivity::class.java))
    }
}