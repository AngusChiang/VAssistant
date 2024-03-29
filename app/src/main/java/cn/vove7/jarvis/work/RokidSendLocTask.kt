package cn.vove7.jarvis.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.SystemClock
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.net.tool.base64
import cn.vove7.common.utils.putJson
import cn.vove7.jarvis.plugins.PluginConfig
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.receivers.UtilEventReceiver.ROKID_SEND_LOC
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.NetHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 *
 * @property configs Function0<Map<String, String>>
 * @property lastTaskDelayTime Long
 * @property EARTH_RADIUS Double
 * @property alarmManager AlarmManager
 * @constructor
 */
class RokidSendLocTask(val configGeter: () -> Map<String, String>) : Runnable {

    private val alarmManager get() = GlobalApp.APP.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    //上次发送间隔 30 min
    var lastTaskDelayTime = 30 * 60 * 1000L

    //上次发送时间
    var lastSendTime = 0L

    private fun calNextDelayTime(longitude: Double, latitude: Double): Long {
        val homeLoc = PluginConfig.rokidHomeLoc ?: return lastTaskDelayTime

        //距离 km
        val disKm = getDistance(longitude to latitude, homeLoc) / 1000

        Vog.d("离家距离：${disKm}Km")

        return when {
            disKm <= 0.1 -> 2.minuteInMillis
            disKm <= 0.3 -> 2.minuteInMillis
            disKm <= 1 -> 3.minuteInMillis
            //2km 2.5min
            //10km 25min
            disKm <= 10.0 -> (disKm * 17.0 / 9 + 1).minuteInMillis
            disKm in 10.0..40.0 -> {
                25.minuteInMillis
            }
            else -> {
                30.minuteInMillis
            }
        }.also {
            Vog.d("下次发送间隔： ${it / 1000 / 60}min")
        }
    }

    private val EARTH_RADIUS = 6378137.0

    //返回单位米
    private fun getDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val lat1 = rad(p1.second)
        val lat2 = rad(p2.second)
        val a = lat1 - lat2
        val b = rad(p1.first) - rad(p2.first)
        var s = 2 * asin(sqrt(sin(a / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(b / 2).pow(2.0)))
        s *= EARTH_RADIUS
        s = (s * 10000).roundToInt() / 10000.toDouble()
        return s
    }

    private fun rad(d: Double): Double {
        return d * Math.PI / 180.0
    }

    private val pendingIntent: PendingIntent
        get() = PendingIntent.getBroadcast(GlobalApp.APP, 0,
                UtilEventReceiver.getIntent(ROKID_SEND_LOC), 0)

    fun nextTask(delay: Long = lastTaskDelayTime) {
        val am = alarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay,
                    pendingIntent)

        } else {
            am.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay,
                    pendingIntent
            )
        }
    }

    fun stop() {
        alarmManager.cancel(pendingIntent)
    }

    private val Int.minuteInMillis: Long get() = this * 60 * 1000L
    private val Double.minuteInMillis: Long get() = (this * 60 * 1000L).toLong()

    override fun run() {
        GlobalScope.launch {
            val configs = configGeter()

            val url = configs["realTimeLocUrl"]
            if (url.isNullOrBlank()) {
                GlobalApp.toastError("若琪实时位置任务无法启动，请先在配置中设置realTimeLocUrl参数", 1)
                GlobalLog.err("若琪实时位置任务无法启动，请先在配置中设置realTimeLocUrl参数")
                PluginConfig.rokidInTimeSendLocation = false
                return@launch
            }

            kotlin.runCatching {
                SystemBridge.location()
            }.onSuccess { loc ->
                if (loc == null) {//位置失败
                    nextTask(5.minuteInMillis)
                    return@launch
                }
                val data = mapOf(
                        "invokeScenes" to true,
                        "name" to "user0gps",
                        "value" to """{"gps":[{"basic":{"lat":"${loc.latitude}","lon":"${loc.longitude}"}}]}"""
                )
                val headers = mapOf(
                        "Authorization" to "Basic " + ("${configs["username"]}:${configs["pass"]}".base64)
                )
                NetHelper.putJson<String>(url, data, headers = headers) {
                    success { _, _ ->
                        lastSendTime = QuantumClock.currentTimeMillis
                        lastTaskDelayTime = calNextDelayTime(loc.longitude, loc.latitude)
                        nextTask(lastTaskDelayTime)
                        GlobalLog.log("Rokid发送位置成功：$loc\n下次发送间隔：${lastTaskDelayTime / 60000}min")
                    }
                    fail { _, e ->
                        nextTask(5.minuteInMillis)
                        GlobalLog.err("Rokid发送位置失败：$e")
                    }
                }
            }.onFailure {
                GlobalLog.err("Rokid获取位置失败 $it")
                nextTask(lastTaskDelayTime / 2)
                return@launch
            }
        }
    }

}
