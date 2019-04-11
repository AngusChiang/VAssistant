package cn.vove7.common.app

import android.util.Log
import cn.vove7.common.utils.StorageHelper
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
        write(LEVEL_INFO, msg)
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
        write(LEVEL_ERROR, msg)
    }


    fun err(msg: String?) {
        write(LEVEL_ERROR, msg)
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

    private fun write(level: Int, msg: String?) {
        synchronized<Unit>(logList) {
            clearIfNeed()
            val pre = findCaller(3)?.let {
                (it.methodName + "(" + it.fileName +
                        ":" + it.lineNumber + ")")

            } ?: "find caller unsuccessfully"
            val text = "$pre  >>  $msg"
            logList.add(LogInfo(level, text))
            try {
                when (level) {
                    LEVEL_INFO -> {
                        Log.i("VOG", text)
                    }
                    LEVEL_ERROR -> {
                        Log.e("VOG", text)
                    }
                }
            } catch (e: Throwable) {
                println(text)
            }
        }
    }

    private fun findCaller(upDepth: Int): StackTraceElement? {
        // 获取堆栈信息
        val callStack = Thread.currentThread().stackTrace
        // 最原始被调用的堆栈信息
        // 日志类名称
        val logClassName = GlobalLog::class.java.name
        // 循环遍历到日志类标识
        var i = 0
        val len = callStack.size
        while (i < len) {
            if (logClassName == callStack[i].className)
                break
            i++
        }
        return try {
            callStack[i + upDepth]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun toString(): String {
        val b = StringBuilder()
        synchronized(logList) {
            logList.forEach {
                b.appendln(it.toString())
            }
        }
        return b.toString()
    }

    const val LEVEL_INFO = 0
    const val LEVEL_ERROR = 1

    class LogInfo(
            val level: Int,
            val msg: String,
            val date: Date = Date()
    ) {
        override fun toString(): String {
            val l = when (level) {
                LEVEL_INFO -> "INFO"
                LEVEL_ERROR -> "ERRO"
                else -> ""
            }
            return "${format.format(date)} $l -> $msg\n"
        }

        companion object {
            val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA)
        }
    }

    val df = SimpleDateFormat("HH_mm", Locale.getDefault())

    fun export2Sd() {
        try {
            val p = StorageHelper.logPath
            val f = File(p, "log_${df.format(Date())}.log")
            f.writeText(this.toString())
            GlobalApp.toastInfo("日志已导出至${f.absolutePath}")
            clear()
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastError("导出失败，请检查存储读写权限")
        }
    }

}


fun Throwable.log() {
    GlobalLog.err(this)
}