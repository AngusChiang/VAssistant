package cn.vove7.common.app

import android.os.Environment
import cn.vove7.vtp.log.Vog
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
        Vog.d(this, "log $msg")
        write(LEVEL_INFO, msg)
    }

    fun err(e: Throwable, tag: String = "") {
        val tmpOutFile: String? = try {
            GlobalApp.APP.cacheDir.absolutePath + "/err"
        } catch (e: Exception) {
            null
        }
        val msg = try {
            if (tmpOutFile == null)
                throw Exception("cacheDir获取失败")
            val pw = PrintWriter(BufferedWriter(FileWriter(File(tmpOutFile))))
            e.printStackTrace(pw)
            pw.close()
            File(tmpOutFile).readText()
        } catch (e1: Exception) {//文件读写
            //
            e1.printStackTrace()
            e.message
        }
        Vog.e(this, msg ?: "none")
        write(LEVEL_ERROR, "$tag -- $msg")
    }

    fun err(msg: String?) {
        Vog.e(this, msg ?: "none")
        write(LEVEL_ERROR, msg)
    }

    fun clear() {
        synchronized(logList) {
            logList.clear()
        }
    }

    fun clearIfNeed() {
        if (logList.size > 500)
            clear()
    }

    private fun write(level: Int, msg: String?) {
        synchronized<Unit>(logList) {
            clearIfNeed()
            logList.add(LogInfo(level, msg ?: "null"))
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
            val p = Environment.getExternalStorageDirectory().absolutePath + "/log"
            File(p).let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            val f = File(p, "log_${df.format(Date())}.log")
            f.writeText(this.toString())
            GlobalApp.toastLong("日志已导出至${f.absolutePath}")
            clear()
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastLong("导出失败，可能没有存储读写权限")
        }
    }

}
