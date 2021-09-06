package cn.vove7.common.bridges

import android.util.Pair
import android.view.KeyEvent
import android.view.ViewConfiguration
import cn.vove7.android.common.ext.delayRun
import cn.vove7.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.withFailLog
import cn.vove7.common.interfaces.api.GlobalActionExecutorI
import cn.vove7.jadb.AdbClient
import cn.vove7.jadb.AdbStream
import cn.vove7.jadb.BuildConfig
import cn.vove7.scrcpy.common.ControlMessage
import cn.vove7.scrcpy.common.DeviceMessage
import cn.vove7.scrcpy.common.Point
import com.google.gson.Gson
import kotlinx.coroutines.Job
import java.io.*
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

/**
 * # ShellActionExecutor
 *
 * @author Vove
 * @date 2021/8/16
 */
object AdbActionExecutor : GlobalActionExecutorI {

    private val conn = ScrcpyConnection()

    private inline val app get() = GlobalApp.APP

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return gesture(dur.toLong(), arrayOf(Pair(x1, y1),
            Pair(x2, y2)))
    }

    override fun press(x: Int, y: Int, delay: Int): Boolean {
        gesture(delay.toLong(), arrayOf(Pair(x, y)))
        gestureLock.wait()
        return true
    }

    override fun longClick(x: Int, y: Int): Boolean {
        return press(x, y, ViewConfiguration.getLongPressTimeout())
    }

    override fun click(x: Int, y: Int): Boolean {
        return press(x, y, 15)
    }

    override fun back(): Boolean {
        sendKey(KeyEvent.KEYCODE_BACK)
        return true
    }

    override fun home(): Boolean {
        sendKey(KeyEvent.KEYCODE_HOME)
        return true
    }

    override fun powerDialog(): Boolean {
        return false
    }

    override fun notificationBar(): Boolean = false

    override fun quickSettings(): Boolean = false

    override fun lockScreen(): Boolean {
        sendKey(KeyEvent.KEYCODE_SLEEP)
        return true
    }

    override fun screenShot(): Boolean = false

    override fun recents(): Boolean {
        sendKey(KeyEvent.KEYCODE_APP_SWITCH)
        return true
    }

    private fun sendKey(key: Int) {
        ControlMessage.createInjectKeycode(KeyEvent.ACTION_DOWN, key, 0, 0).also {
            conn.send(it)
        }
        ControlMessage.createInjectKeycode(KeyEvent.ACTION_UP, key, 0, 0).also {
            conn.send(it)
        }
    }

    override fun splitScreen(): Boolean = false

    private val gestureLock = Object()

    override fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        ControlMessage.createSimpleGesture(
            listOf(points.map {
                Point(it.first, it.second)
            }),
            duration.toInt()
        )
        synchronized(gestureLock) {
            gestureLock.wait()
        }
        return true
    }

    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        ControlMessage.createSimpleGesture(
            ppss.map {
                it.map { p ->
                    Point(p.first, p.second)
                }
            },
            duration.toInt()
        )
        synchronized(gestureLock) {
            gestureLock.wait()
        }
        return true
    }

    override fun gestureAsync(start: Long, duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        ControlMessage.createSimpleGesture(
            listOf(points.map {
                Point(it.first, it.second)
            }),
            duration.toInt()
        )
        return true
    }

    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        ControlMessage.createSimpleGesture(
            ppss.map {
                it.map { p ->
                    Point(p.first, p.second)
                }
            },
            duration.toInt()
        )
        return true
    }

    override fun release() {
        conn.closeServerDelay()
        conn.close()
    }

    internal class ScrcpyConnection(val port: Int = 9999) {

        private var sock: Socket? = null
        private var dataInputStream: BufferedReader? = null
        private var dataOutputStream: BufferedWriter? = null
        private var monitorThread: Thread? = null

        private val isConnected get() = sock?.isConnected == true

        private var shellStream: AdbStream? = null

        private var adbClient: AdbClient? = null

        private var delayClosrJob : Job?=null

        private fun isScrcpyRunning(): Boolean = try {
            adbClient != null && shellStream != null && run {
                Socket("127.0.0.1", port)
                false
            }
        } catch (e: Throwable) {
            true
        }

        fun closeServerDelay() {
            delayClosrJob = delayRun(10000) {
                shellStream?.interrupt()
                adbClient?.close()
                "Scrcpy close ${!isScrcpyRunning()}".logi()
                adbClient = null
                shellStream = null
            }
        }

        private fun initScrcpyServer() {
            val scrcpyFileName = "vassist-scrcpy-" + AppConfig.versionCode
            val tmpScrcpyFile = File(GlobalApp.APP.getExternalFilesDir(null), scrcpyFileName)
            if (!tmpScrcpyFile.exists()) {
                app.assets.open("adb/scrcpy").copyTo(tmpScrcpyFile.outputStream())
            }

            if (!isScrcpyRunning()) {
                adbClient = AdbClient(GlobalApp.APP).also {
                    it.connect()
                    val cmd = "CLASSPATH=${tmpScrcpyFile} app_process / com.vove7.scrcpy.Server VERBOSE SocketServer $port"
                    cmd.logi()
                    shellStream = it.shellCommand(cmd)
                    if (BuildConfig.DEBUG) {
                        shellStream?.onData {
                            "adb data: ${String(it)}".logi()
                        }
                    } else {
                        shellStream?.noStoreOutput()
                    }
                    sleep(800)
                }
            }
        }

        @Throws
        fun requireConnect() {
            delayClosrJob?.cancel()
            if (isConnected) {
                return
            }
            initScrcpyServer()

            val s = Socket()
            s.connect(InetSocketAddress("127.0.0.1", port))
            sock = s
            dataInputStream = BufferedReader(InputStreamReader(s.inputStream))
            dataOutputStream = BufferedWriter(OutputStreamWriter(s.outputStream))
            monitorCallback(dataInputStream!!)
        }

        private fun monitorCallback(ins: BufferedReader) {
            monitorThread = thread(isDaemon = true) {
                val gson = Gson()
                kotlin.runCatching {
                    while (isConnected) {
                        val newData = ins.readLine()
                        val msg = gson.fromJson(newData, DeviceMessage::class.java)
                        processScrcpyMsg(msg)
                    }
                }.onFailure {
                    close()
                }
            }
        }

        private fun processScrcpyMsg(msg: DeviceMessage) {
            when (msg.type) {
                DeviceMessage.TYPE_ERROR -> {
                    GlobalLog.err("Scrcpy error: ${msg.text}")
                }
                DeviceMessage.TYPE_GESTURE_FINISHED -> {
                    synchronized(gestureLock) {
                        gestureLock.notify()
                    }
                }
            }
        }

        fun send(msg: ControlMessage) {
            requireConnect()
            kotlin.runCatching {
                msg.post(dataOutputStream!!)
            }.onFailure {
                GlobalLog.err(it)
                close()
            }
        }

        fun close() {
            kotlin.runCatching {
                dataInputStream?.close()
            }.withFailLog()
            kotlin.runCatching {
                dataOutputStream?.close()
            }.withFailLog()
            kotlin.runCatching {
                sock?.close()
            }.withFailLog()
            dataInputStream = null
            dataOutputStream = null
            sock = null
        }
    }
}