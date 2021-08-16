package cn.vove7.jarvis.view.statusbar

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.jarvis.R
import cn.vove7.jarvis.receivers.UtilEventReceiver

/**
 * # ExecuteAnimation
 *
 * @author 17719247306
 * 2018/9/4
 */
class ExecuteAnimation : StatusAnimation() {
    override var title: String = GlobalApp.getString(R.string.text_executing)

    override var beginAniId: Int = R.drawable.voice_animation

    override var successId: Int = R.drawable.ic_big_smile

    private var failed = false

    override fun onFailed() {
        failed = true
        super.onFailed()
    }

    override fun finish() {
        if (!failed)
            super.finish()
    }

    override fun onShowNtf(c: String, builder: NotificationCompat.Builder) {
        val pi = PendingIntent.getBroadcast(GlobalApp.APP, 0, UtilEventReceiver.getIntent(AppBus.ACTION_STOP_EXEC),
                PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pi)
        builder.addAction(NotificationCompat.Action(null, "STOP", pi))
    }
}