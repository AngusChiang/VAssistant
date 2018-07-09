package cn.vove7.vtp.log

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import java.io.File
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
/**
 * #Vog
 *
 */
object Vog {
    private var output_level = Log.VERBOSE

    /**
     * 网络日志
     */
    private var sendToServer = false
    private var logServer: String = ""
    /**
     * 本地日志
     */
    private var log2Local: Boolean = false
    private var localLogLevel = Log.DEBUG
    private var logPath = ""
    private lateinit var logFle: File
    var MAX_LOG_FILE_SIZE = (1 shl 20).toLong()//1M

    fun send2Server(msg: String) {

    }

    /**
     * @param outputLevel Log.***
     */
    fun init(context: Context, outputLevel: Int, sendToServer: Boolean = false): Vog {
        output_level = outputLevel
        logPath = context.externalCacheDir.parent
        this.sendToServer = sendToServer
        return this
    }

    /**
     * 开启日志本地保存
     */
    fun log2Local(localLogLevel: Int) {
        this.localLogLevel = localLogLevel
        this.log2Local = true
        createLogFile()
    }

    private fun createLogFile() {
        logFle = File(logPath, getLogName())
    }

    fun cancelLog2Local() {
        this.log2Local = false
    }

    fun d(o: Any, msg: String) {
        println(Log.DEBUG, o.javaClass.simpleName, msg.toString())
    }

    fun wtf(o: Any, msg: Any) {
        println(Log.ERROR, o.javaClass.simpleName, msg.toString())
    }

    fun v(o: Any, msg: Any) {
        println(Log.VERBOSE, o.javaClass.simpleName, msg.toString())
    }

    fun i(o: Any, msg: Any) {
        println(Log.INFO, o.javaClass.simpleName, msg.toString())
    }

    fun w(o: Any, msg: Any) {
        println(Log.WARN, o.javaClass.simpleName, msg.toString())
    }

    fun e(o: Any, msg: Any) {
        println(Log.ERROR, o.javaClass.simpleName, msg.toString())
    }

    fun a(o: Any, msg: Any) {
        println(Log.ASSERT, o.javaClass.simpleName, msg.toString())
    }

    val dateFormat = SimpleDateFormat("MM-dd hh-mm-ss", Locale.CHINA)
    private fun println(priority: Int, tag: String, msg: String) {
        if (output_level <= priority) {
            Log.println(priority,"Vog: $tag" , msg + '\n')
        }
        if (localLogLevel <= priority) {
            val date = dateFormat.format(Date(System.currentTimeMillis()))
            log2File("$date: $tag: $msg")
        }
    }


    private fun log2File(s: String) {
        if (checkLogFileSize(logFle.length())) {
            createLogFile()
        }
        logFle.appendText(s, Charset.forName("UTF-8"))
    }

    private fun getLogName(): String {
        var latestName = "app.log"
        var latestTime = 0L
        var size = 0L
        File(logPath)
                .walk()
                .filter { it.extension == "log" }
                .filter { it.isFile }
                .filter { it.name.startsWith("app") }
                .forEach {
                    if (it.lastModified() > latestTime) {
                        latestName = it.name
                        latestTime = it.lastModified()
                        size = it.length()
                    }
                }
        return if (latestTime != 0L && checkLogFileSize(size)) {
            latestName
        } else {//create new
            "app${System.currentTimeMillis() % 10000}.log"
        }
    }

    private fun checkLogFileSize(s: Long): Boolean {
        return s <= MAX_LOG_FILE_SIZE
    }
}
