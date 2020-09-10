package cn.vove7.jarvis.tools.timedtask

import cn.daqinjia.android.common.loge
import cn.daqinjia.android.common.logi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import java.io.*

/**
 * # TimedTaskManager
 *
 * @author Vove
 * 2020/9/9
 */
object TimedTaskManager {

    private var tasks = mutableListOf<TimedTask>()
    private const val FILE_NAME = "timed_tasks"

    fun getTasks() = tasks.toList()

    fun removeTask(task: TimedTask) = tasks.remove(task)

    fun addTask(task: TimedTask) {
        if (task !in tasks) {
            tasks.add(task)
            GlobalLog.log("添加任务 $task")
        }
    }

    fun init() {
        restore()
        startup()
    }

    private fun startup() {
        tasks.forEach {
            if (it.enabled) {
                it.sendPendingIntent()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restore() = File(GlobalApp.APP.filesDir, FILE_NAME).also {
        if (it.exists()) {
            kotlin.runCatching {
                tasks = ObjectInputStream(FileInputStream(it)).readObject() as MutableList<TimedTask>
                "定时任务：${tasks.joinToString("\n", "\n")}".logi()
            }.onFailure {
                it.loge()
                GlobalApp.toastError("定时任务恢复失败")
            }
        } else {
            "定时任务无文件".logi()
        }
    }

    fun persistence() {
        val out = ObjectOutputStream(FileOutputStream(File(GlobalApp.APP.filesDir, FILE_NAME)))
        out.writeObject(tasks)
        out.close()
    }

    fun cancelTask(id: String) {
        tasks.find { it.id == id }?.also {
            GlobalLog.err("取消任务: $id")
            it.disable()
        } ?: kotlin.run {
            GlobalLog.err("取消任务失败：未找到任务 $id")
        }
    }


    fun runTask(id: String) {
        tasks.find { it.id == id }?.also {
            it.run()
        } ?: kotlin.run {
            GlobalLog.err("执行任务失败：未找到任务 $id")
        }

    }

}