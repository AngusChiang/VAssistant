package cn.vove7.jarvis.jadb

import android.content.Context
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.Socket
import kotlin.concurrent.thread

@Keep
class JAdb(
        private val host: String = "localhost",
        private val port: Int = 5555
) : AdbBase64 {
    private var ac: AdbConnection? = null
    private var logThread: Thread? = null
    private var shellStream: AdbStream? = null
    var shellHeader: String? = null
        private set

    var onCloseListener: (() -> Unit)? = null
        set(value) {
            ac?.onCloseListener = {
                connected = false
                value?.invoke()
            }
            field = value
        }

    var connected = false
        private set

    fun connect(context: Context): Boolean {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("不可运行于主线程")
        }
        if (connected) {
            return connected
        }

        val (priFile, pubFile) = copyKeyPair(context)
        val ab = AdbCrypto.loadAdbKeyPair(this, priFile, pubFile)
        try {
            ac = AdbConnection.create(TcpChannel(Socket(host, port)), ab)
            ac!!.setOnCloseListener {
                connected = false
                onCloseListener?.invoke()
            }
            ac!!.connect(6000)
            connected = true
        } catch (e: Throwable) {
            if (e !is InterruptedException)
                EventBus.getDefault().post(e)
            ac?.close()
            ac = null
        }

        return connected
    }

    fun execOnShell(cmd: String): AdbStream {
        ensureConnect()
        if (shellStream?.isClosed != false) {
            shellStream = open("shell:")
            shellHeader = String(shellStream!!.read())
            Log.d("JADB", "shellHeader: $shellHeader")
        }
        shellStream!!.write(" $cmd\n")
        return shellStream!!
    }

    fun open(dest: String): AdbStream {
        ensureConnect()
        return ac!!.open(dest)
    }

    private fun copyKeyPair(context: Context): Pair<File, File> {
        val priFile = File(context.filesDir, "pri.txt")
        val pubFile = File(context.filesDir, "pub.txt")
        if (!priFile.exists() || !pubFile.exists()) {
            context.assets.open("adb_key/pri.key").copyTo(priFile.outputStream())
            context.assets.open("adb_key/pub.key").copyTo(pubFile.outputStream())
        }
        return priFile to pubFile
    }

    private fun ensureConnect() {
        if (!connected) {
            throw IllegalStateException("has not connected")
        }
    }

    override fun encodeToString(it: ByteArray?): String = Base64.encodeToString(it, 2)

    fun close() {
        ac?.close()
        shellStream?.close()
        logThread?.interrupt()
        onClose()
    }

    private fun onClose() {
        connected = false
        logThread = null
        shellHeader = null
    }

    fun AdbStream.log() {
        if (!BuildConfig.DEBUG) return
        logThread = thread {
            val t = Thread.currentThread()
            while (!isClosed && !t.isInterrupted) {
                try {
                    val d = String(read())
                    Log.d("ADB", d)
                } catch (e: Throwable) {
                    //Intercept IOException
                    break
                }
            }
            logThread = null
        }
    }
}

