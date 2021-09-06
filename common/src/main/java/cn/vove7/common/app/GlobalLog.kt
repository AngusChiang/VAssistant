package cn.vove7.common.app

import cn.vove7.android.common.Logger.logi
import cn.vove7.android.common.loge
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.errMessage
import cn.vove7.quantumclock.QuantumClock
import org.greenrobot.eventbus.Subscribe
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * # GlobalLog
 *
 * @author 17719
 * 2018/8/11
 */
object GlobalLog {

    private val logList = mutableListOf<LogInfo>()
    fun log(msg: String?) {
        logi(1) { msg }
        logList.add(LogInfo(LEVEL_INFO, msg ?: ""))
    }

    fun err(e: Throwable) {
        val tmpOutFile: String? = try {
            GlobalApp.APP.cacheDir.absolutePath + "/err"
        } catch (e: Exception) {
            null
        }
        val msg = try {//先写入文件
            if (tmpOutFile == null)
                "cacheDir获取失败" + e.message
            else {
                PrintWriter(BufferedWriter(FileWriter(File(tmpOutFile)))).use {
                    e.printStackTrace(it)
                }
                File(tmpOutFile).readText()
            }
        } catch (e1: Exception) {
            e1.printStackTrace()
            e.message + "\n[[[[[[${e1.message}]]]]]]"
        }
        msg.loge(1)
        logList.add(LogInfo(LEVEL_ERROR, msg))
    }


    fun err(msg: String?) {
        msg.loge(1)
        logList.add(LogInfo(LEVEL_ERROR, msg ?: ""))
    }

    fun clear() {
        synchronized(logList) {
            logList.clear()
        }
    }

    private fun clearIfNeed() {
        if (logList.size > 500)
            clear()
    }

    override fun toString(): String {
        return buildString {
            synchronized(logList) {
                logList.forEach {
                    appendLine(it.toString())
                }
            }
        }
    }

    fun colorHtml(): String {
        return buildString {
            synchronized(logList) {
                logList.forEach {
                    appendLine(it.colorfulHtml())
                }
            }
        }
    }

    const val LEVEL_INFO = 0
    const val LEVEL_ERROR = 1

    class LogInfo(
            val level: Int,
            val msg: String,
            val date: Date = QuantumClock.nowDate
    ) {
        override fun toString(): String {
            val l = when (level) {
                LEVEL_INFO -> "INFO"
                LEVEL_ERROR -> "ERRO"
                else -> ""
            }
            return "${format.format(date)} $l -> $msg\n"
        }

        fun colorfulHtml(): String {
            val l = when (level) {
                LEVEL_INFO -> "info"
                LEVEL_ERROR -> "erro"
                else -> ""
            }
            return "<div class='$l'><xmp>${format.format(date)} -> $msg</xmp></div>\n"
        }

        companion object {
            val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA)
        }
    }


    fun export2Sd() {
        val df = SimpleDateFormat("HH_mm", Locale.getDefault())
        try {
            val p = StorageHelper.logPath
            val f = File(p, "log_${df.format(Date())}.log")
            f.writeText(this.toString())
            GlobalApp.toastInfo("日志已导出至${f.absolutePath}")
            clear()
        } catch (e: Exception) {
            err(e)
            GlobalApp.toastError("导出失败，请检查存储读写权限")
        }
    }

    inline fun logStackTrace() = log(
            Thread.currentThread().stackTrace.joinToString("\n") {
                "${it.methodName}(${it.fileName}:${it.lineNumber})"
            }
    )

    @Subscribe
    fun onPostError(e:Throwable) {
        err(e)
    }

}


fun<T> Result<T>.withFailLog() = onFailure { GlobalLog.err(it) }


fun Throwable.log() {
    GlobalLog.err(this)
}