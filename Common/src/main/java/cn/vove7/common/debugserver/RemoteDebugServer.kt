package cn.vove7.common.debugserver

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cn.vove7.vtp.log.Vog
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

abstract class RemoteDebugServer(private val context: Context) : Thread() {
    var stopped: Boolean = false

    override fun run() {
        stopped = false
        try {
            ServerSocket(LISTEN_PORT).use { server ->
                show("Debug Server started on port $LISTEN_PORT")
                val client = server.accept()
                val inputStream = DataInputStream(BufferedInputStream(client.getInputStream()))
                val outputStream = DataOutputStream(BufferedOutputStream(client.getOutputStream()))

                while (!stopped) {
                    val data = inputStream.readUTF()
                    Vog.d(this, "run  ---->\n $data")
                    val result = onPostAction(data)
                    outputStream.writeUTF(result)
                    outputStream.flush()
                }
                show("RemoteDebug finished！")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            show("${e.message}")
        } finally {
            onFinish()
        }
    }

    open fun onFinish() {

    }

    abstract fun onPostAction(src: String): String

    private fun show(s: String) {
        Vog.d(this, "show  ----> $s")
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private const val LISTEN_PORT = 3333
    }
}