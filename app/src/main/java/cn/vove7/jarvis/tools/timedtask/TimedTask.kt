package cn.vove7.jarvis.tools.timedtask

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.utils.format
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.vtp.net.toJson
import cn.vove7.vtp.weaklazy.weakLazy
import java.io.Serializable
import java.util.*

/**
 * # TimedTask
 *
 * @author Vove
 * 2020/9/9
 */
abstract class TimedTask(
        var name: String,
        enabled: Boolean,
        // 类型
        var execType: Int,
        // 执行体
        var execBody: String,
) : Serializable {

    var enabled: Boolean = enabled
        set(value) {
            field = value
            TimedTaskManager.persistence()
        }

    companion object {
        private const val serialVersionUID = 871426140395642268L
        const val TYPE_COMMAND = 0
        const val TYPE_SCRIPT_LUA = 1
        const val TYPE_SCRIPT_JS = 2

        private val alarmManager by weakLazy {
            GlobalApp.APP.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }

    //任务唯一ID
    internal val id = UUID.randomUUID().toString()

    //下次执行时间 ms
    //保证同一时间段内获取的值总是一致
    abstract fun nextTime(): Long

    fun run() {
        if (!enabled) {
            GlobalLog.err("执行未启用任务 $this")
            return
        }
        AppNotification.broadcastNotification(this.hashCode(), "定时任务：$name", "正在执行", null)
        GlobalLog.log("执行任务：$this")
        when (execType) {
            TYPE_COMMAND -> {
                MainService.parseCommand(execBody)
            }
            TYPE_SCRIPT_LUA -> {
                val postAction = Action()
                postAction.actionScript = execBody
                postAction.scriptType = Action.SCRIPT_TYPE_LUA
                AppBus.post(postAction)
            }
            TYPE_SCRIPT_JS -> {
                val postAction = Action()
                postAction.actionScript = execBody
                postAction.scriptType = Action.SCRIPT_TYPE_JS
                AppBus.post(postAction)
            }
            else -> {
                GlobalLog.err("任务执行类型不支持：$this")
            }
        }
        onRunComplete()
        if (enabled) {
            sendPendingIntent()
            //todo add action button disable
            AppNotification.broadcastNotification(
                    this.hashCode(), "定时任务：$name",
                    "执行结束，下次执行时间：${Date(nextTime()).format("MM-dd HH:mm")}", null
            )
        } else {
            AppNotification.broadcastNotification(
                    this.hashCode(), "定时任务：$name", "执行结束",
                    null
            )
        }
    }

    open fun onRunComplete() {}

    @Transient
    private lateinit var pendingIntent: PendingIntent

    private fun getIntent(): PendingIntent {
        if (!::pendingIntent.isInitialized) {
            pendingIntent = PendingIntent.getBroadcast(
                    GlobalApp.APP, hashCode(),
                    UtilEventReceiver.getIntent(UtilEventReceiver.TIMED_TASK).also {
                        it.putExtra("task_id", id)
                    }, PendingIntent.FLAG_CANCEL_CURRENT
            )
        }
        return pendingIntent
    }

    fun disable() {
        enabled = false
    }

    fun cancel() {
        GlobalLog.log("取消任务：$name $id")
        alarmManager.cancel(getIntent())
    }

    fun sendPendingIntent() {
        val nextTime = nextTime()
        if (nextTime < System.currentTimeMillis()) {
            GlobalLog.err("任务时间无效[$id] ${Date(nextTime).format()}")
            enabled = false
            return
        }
        GlobalLog.log("启动任务：$name [$id]  下次执行时间：${Date(nextTime).format()}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTime,
                    getIntent()
            )
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTime,
                    getIntent()
            )
        }
    }

    override fun toString(): String {
        return toJson()
    }
}
typealias HourMinute = Pair<Int, Int>

fun HourMinute.timeString():String = "%02d:%02d".format(first,second)

inline val HourMinute.hour: Int get() = first
inline val HourMinute.minute: Int get() = second

open class EveryDayTimedTask(
        name: String,
        enabled: Boolean,
        execType: Int,
        execBody: String,
        val time: HourMinute
) : TimedTask(name, enabled, execType, execBody) {

    override fun nextTime(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, time.hour)
        cal.set(Calendar.MINUTE, time.minute)
        cal.set(Calendar.SECOND, 0)
        if (cal.timeInMillis < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

}

class OnceTimedTask(
        name: String,
        enabled: Boolean,
        execType: Int,
        execBody: String,
        val timeInMillis: Long
) : TimedTask(name, enabled, execType, execBody) {

    override fun nextTime(): Long {
        return timeInMillis
    }

    override fun onRunComplete() {
        enabled = false
    }
}

class IntervalTimedTask(
        name: String,
        enabled: Boolean,
        execType: Int,
        execBody: String,
        val intervalMinutes: Int //5 - 24 * 60
) : TimedTask(name, enabled, execType, execBody) {

    private var lastExecTime = 0L
    override fun onRunComplete() {
        lastExecTime = System.currentTimeMillis()
    }

    override fun nextTime(): Long {
        val expectLastTime = lastExecTime + intervalMinutes * 60000
        return if (lastExecTime == 0L) {
            System.currentTimeMillis() + 3000
        } else if (expectLastTime <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 3000
        } else {
            expectLastTime
        }
    }

}