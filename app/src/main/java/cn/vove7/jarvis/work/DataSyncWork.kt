package cn.vove7.jarvis.work

import android.content.Context
import androidx.work.*
import cn.vove7.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.receivers.UtilEventReceiver
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.tools.AppNotification
import cn.vove7.jarvis.tools.DataUpdator
import cn.vove7.quantumclock.QuantumClock
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * # DataSyncWork
 * 数据同步
 * @author Vove
 * 2020/2/29
 */

class DataSyncWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        fun getRequest(): WorkRequest = PeriodicWorkRequest
                .Builder(DataSyncWork::class.java,
                        AppConfig.netConfig("dataSyncInterval", 60L), TimeUnit.MINUTES)
                .setConstraints(
                        Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                )
                .addTag("DataSyncWork")
                .build()

        fun startOnce() {
            WorkManager.getInstance(GlobalApp.APP)
                    .enqueue(OneTimeWorkRequest.Builder(DataSyncWork::class.java)
                            .build()
                    )
        }
    }

    override fun doWork(): Result {
        val time = Calendar.getInstance(TimeZone.getDefault())
        val h = time.get(Calendar.HOUR_OF_DAY)
        //夜间休息
        if (h in 1..6) {
            return Result.success()
        }

        ("数据同步").logi()
        AppConfig.fetchNetConfig()
        AppConfig.checkAppUpdate(GlobalApp.APP, false, onUpdate)

        if (!AppConfig.FIRST_IN) {
            DataUpdator.checkUpdate { result ->
                if (result.hasUpdate) {
                    //更新成功
                    GlobalLog.log("数据同步完成")
                    AppNotification.broadcastNotification(1234, "指令数据已更新",
                            "点击查看更新内容",
                            UtilEventReceiver.getIntent(UtilEventReceiver.INST_DATA_SYNC_FINISH).apply {
                                putExtra("content", result.result)
                            }
                    )
                } else {
                    ("暂未更新").logi()
                }
            }
        }
        runBlocking {
            val job = QuantumClock.sync()
            job.invokeOnCompletion { e ->
                if (e != null) {
                    GlobalLog.err(e)
                }
                ("时间同步完成 ${QuantumClock.nowDate} $e").logi()
            }
            job.join()
        }
        // 某些定时操作检查
        MainService.homeControlSystem?.onDataSync()

        return Result.success()
    }

    private val onUpdate: (Pair<String, String>?) -> Unit
        get() = a@{ hasUpdate ->
            hasUpdate ?: return@a
            AppNotification.broadcastNotification(
                    123, "发现新版本 ${hasUpdate.first}",
                    "查看更新日志",
                    (UtilEventReceiver.getIntent(UtilEventReceiver.APP_HAS_UPDATE).apply {
                        putExtra("version", hasUpdate.first)
                        putExtra("log", hasUpdate.second)
                    })
            )
        }
}