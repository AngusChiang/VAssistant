package cn.vove7.jarvis.jadb.debug

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.jarvis.jadb.JAdb
import cn.vove7.jarvis.jadb.R
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * # AdbActivity
 *
 * @author Vove
 * @date 2021/8/12
 */
class AdbActivity : AppCompatActivity() {

    private val logView: TextView by lazy {
        findViewById(R.id.log_view)
    }

    lateinit var adb: JAdb

    private var connecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adb)

        val p = Runtime.getRuntime().exec("getprop service.adb.tcp.port")
        logv("service.adb.tcp.port: " + String(p.inputStream.readBytes()))

        findViewById<View>(R.id.connect_btn).setOnClickListener {
            if (connecting) {
                return@setOnClickListener
            }
            if (::adb.isInitialized && adb.connected) {
                adb.close()
                logv("closed success")
                return@setOnClickListener
            }
            connecting = true
            val host = findViewById<EditText>(R.id.host_et).text.toString()
            adb = JAdb(host)
            adb.onCloseListener = {
                logv("closed")
            }
            logv("connect...")
            thread {
                if (adb.connect(this)) {
                    logv("connect success")
                } else {
                    logv("connect failed")
                }
                connecting = false
            }
        }
        findViewById<View>(R.id.run_btn).setOnClickListener {
            if (::adb.isInitialized && adb.connected) {
                val cmd = findViewById<EditText>(R.id.cmd_et).text.toString()
                thread {
                    val s = execWithAdb(cmd)
                    Log.d("ADBActivity", s)
                    logv("out: $s")
                }
            } else {
                logv("has not connected")
            }
        }

        findViewById<View>(R.id.open_btn).setOnClickListener {
            if (::adb.isInitialized) {
                thread {
                    adb.open(findViewById<TextView>(R.id.open_et).text.toString())
                }
            } else {
                logv("has not connected")
            }
        }
    }


    fun execWithAdb(cmd: String?): String {
        val lock = CountDownLatch(1)
        var ret = ""
        fun String.isEnd(): Boolean {
            if (adb.shellHeader == null) {
                if (this.matches(".*:/.*[$#][ ]*".toRegex())) {
                    return true
                }
            } else {
                if (this.matches(adb.shellHeader!!
                                .replace("/ $", ".*\\$")
                                .replace("/ #", ".*#")
                                .toRegex())) {
                    return true
                }
            }
            return false
        }

        val t = thread {//异步读取 防止异常超时
            val tt = Thread.currentThread()
            val stream = adb.execOnShell(cmd ?: " ")
            val sb = StringBuilder()
            var f = true
            while (!tt.isInterrupted) {
                try {
                    val s = stream.read()
                    if (f && !String(s).isEnd()) {
                        f = false
                        continue
                    }
                    sb.append(String(s))
                } catch (e: Throwable) {
                    ret = sb.toString()
                    lock.countDown()
                    break
                }
                val lastLine = sb.toString().lines().last()
                if (lastLine.isEnd()) {
                    Log.d("ADB", "raw out: $sb")
                    sb.deleteRange((sb.length - lastLine.length - 1).coerceAtLeast(0), sb.length)
                    ret = sb.toString()
                    lock.countDown()
                    break
                }
            }
        }
        lock.await(30, TimeUnit.SECONDS)
        t.interrupt()
        return ret
    }

    override fun onDestroy() {
        if (::adb.isInitialized) {
            adb.close()
        }
        super.onDestroy()
    }

    private fun logv(s: Any?) {
        runOnUiThread {
            logView.append(s.toString() + "\n")
        }
    }

}