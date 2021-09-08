package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.ViewConfiguration
import cn.vove7.android.common.ext.delayRun
import cn.vove7.android.common.loge
import cn.vove7.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.withFailLog
import cn.vove7.common.interfaces.api.GlobalActionExecutorI
import cn.vove7.common.utils.ScreenAdapter
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
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * # ScrcpyActionExecutor
 *
 * @author Vove
 * @date 2021/8/16
 */
object ScrcpyActionExecutor : GlobalActionExecutorI {

    @JvmStatic
    val availiable
        get() = ShellHelper.hasRootOrAdb()

    @JvmStatic
    val conn = ScrcpyConnection()

    private inline val app get() = GlobalApp.APP

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, dur: Int): Boolean {
        return gesture(dur.toLong(), arrayOf(Pair(x1, y1),
            Pair(x2, y2)))
    }

    override fun press(x: Int, y: Int, delay: Int): Boolean {
        gestureAsync(delay.toLong(), arrayOf(Pair(x, y)))
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
        return performAcsAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
    }

    override fun notificationBar(): Boolean {
        return performAcsAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
    }

    override fun quickSettings(): Boolean {
        return performAcsAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }

    private fun performAcsAction(globalAction: Int): Boolean {
        ControlMessage.performAcsAction(globalAction).also {
            conn.send(it)
        }
        return true
    }

    override fun lockScreen(): Boolean {
        sendKey(KeyEvent.KEYCODE_SLEEP)
        return true
    }

    override fun screenShot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performAcsAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            return false
        }
    }

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

    override fun splitScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            performAcsAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            return false
        }
    }

    private val gestureLock = Object()

    override fun gesture(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        ControlMessage.createSimpleGesture(
            listOf(points.toPointList()),
            duration.toInt()
        ).also {
            conn.send(it)
        }
        synchronized(gestureLock) {
            gestureLock.wait(duration + 800)
        }
        return true
    }

    private fun Array<Pair<Int, Int>>.toPointList() = map {
        Point(ScreenAdapter.scaleX(it.first).toInt(),
            ScreenAdapter.scaleY(it.second).toInt())
    }

    override fun gestures(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        ControlMessage.createSimpleGesture(
            ppss.map { it.toPointList() },
            duration.toInt()
        ).also {
            conn.send(it)
        }
        synchronized(gestureLock) {
            gestureLock.wait(duration + 800)
        }
        return true
    }

    override fun gestureAsync(duration: Long, points: Array<Pair<Int, Int>>): Boolean {
        ControlMessage.createSimpleGesture(
            listOf(points.toPointList()),
            duration.toInt()
        ).also {
            conn.send(it)
        }
        return true
    }

    override fun gesturesAsync(duration: Long, ppss: Array<Array<Pair<Int, Int>>>): Boolean {
        ControlMessage.createSimpleGesture(
            ppss.map { it.toPointList() },
            duration.toInt()
        ).also {
            conn.send(it)
        }
        return true
    }

    override fun release() {
        conn.closeServerDelay()
        conn.close()
    }

    class ScrcpyConnection(val port: Int = 9999) {

        private var sock: Socket? = null
        private var dataInputStream: BufferedReader? = null
        private var dataOutputStream: BufferedWriter? = null
        private var monitorThread: Thread? = null

        private val isConnected get() = sock?.isConnected == true

        private var shellStream: AdbStream? = null

        private var adbClient: AdbClient? = null

        private var delayCloseJob: Job? = null

        private var suProcess: Process? = null

        private fun isScrcpyRunning(): Boolean = try {
            val s = ServerSocket(port, 0, Inet4Address.getByName("localhost"))
            "$port is not in use $s".logi()
            s.close()
            false
        } catch (e: Throwable) {
            "$port is in use".logi()
            true
        }

        fun closeServerDelay() {
            delayCloseJob = delayRun(10000) {
                dataOutputStream?.also {
                    kotlin.runCatching {
                        ControlMessage.exit().post(it)
                    }.withFailLog()
                }
                adbClient?.apply {
                    "destroy adbClient: $adbClient".logi()
                    shellStream?.interrupt()
                    close()
                }
                suProcess?.apply {
                    "destroy suProcess: $this".logi()
                    destroy()
                    waitFor()
                    "destroy suProcess: $this ${this.alive()}".logi()
                }
                sleep(500)
                "Scrcpy close ${!isScrcpyRunning()}".logi()
                adbClient = null
                shellStream = null
                suProcess = null
            }.also {
                it.invokeOnCompletion {
                    delayCloseJob = null
                }
            }
        }

        private fun initScrcpyServer() {
            val scrcpyFileName = "vassist-scrcpy-" + AppConfig.versionCode
            val tmpScrcpyFile = File(GlobalApp.APP.getExternalFilesDir(null), scrcpyFileName)
            if (BuildConfig.DEBUG || !tmpScrcpyFile.exists()) {
                app.assets.open("adb/scrcpy").copyTo(tmpScrcpyFile.outputStream())
            }

            if (!isScrcpyRunning()) {
                "not running start scrcpy server".logi()
                val cmd = "CLASSPATH=${tmpScrcpyFile} app_process / com.vove7.scrcpy.Server" +
                    " VERBOSE SocketServer $port ${GlobalApp.APP.packageName}"
                cmd.logi()
                if (ShellHelper.isRoot()) {
                    initWithRoot(cmd)
                } else {
                    initWithAdb(cmd)
                }
            } else {
                "scrcpy already running...".logi()
            }
        }

        private fun initWithAdb(cmd: String) {
            "initWithAdb".logi()
            val lock = Object()
            adbClient = AdbClient(GlobalApp.APP).also {
                it.connect()
                shellStream = it.shellCommand(cmd) {
                    Log.d("AdbClient", String(it))
                    val s = shellStream ?: return@shellCommand
                    if (String(s.data).contains("waiting client connect")) {
                        synchronized(lock) {
                            lock.notify()
                        }
                        if (!BuildConfig.DEBUG) {
                            s.onData(null)
                            s.noStoreOutput()
                        }
                    }
                }
            }
            synchronized(lock) {
                lock.wait(2000)
            }
        }

        private fun Process.alive(): Boolean {
            return try {
                exitValue()
                false
            } catch (e: IllegalThreadStateException) {
                true
            }
        }

        private fun initWithRoot(cmd: String) {
            "initWithRoot".logi()
            val p = Runtime.getRuntime().exec("su")
            val dos = DataOutputStream(p.outputStream)
            dos.writeBytes(cmd + "\n")
            dos.flush()
            suProcess = p
            sleep(800)
            val lock = Object()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            thread {
                while (p.alive()) try {
                    val s = reader.readLine()
                    if (s.contains("waiting client connect")) {
                        synchronized(lock) {
                            lock.notify()
                        }
                        if (!BuildConfig.DEBUG) {
                            break
                        }
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d("AdbClient", s)
                    }
                } catch (e: Throwable) {
                    break
                }
            }
            synchronized(lock) {
                lock.wait(2000)
            }
        }

        @Throws
        fun requireConnect() {
            delayCloseJob?.cancel()
            if (isConnected) {
                return
            }
            initScrcpyServer()

            val s = Socket()
            s.connect(InetSocketAddress("127.0.0.1", port), 500)
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

        @Synchronized
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
            synchronized(gestureLock) {
                gestureLock.notifyAll()
            }
            monitorThread?.interrupt()
            dataInputStream = null
            dataOutputStream = null
            sock = null
            monitorThread = null
        }
    }
}