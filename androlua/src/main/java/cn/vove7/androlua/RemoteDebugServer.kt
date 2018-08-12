package cn.vove7.androlua

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cn.vove7.androlua.luautils.LuaPrinter
import cn.vove7.appbus.AppBus
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.model.Param
import cn.vove7.vtp.log.Vog
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket

internal class RemoteDebugServer(private val context: Context) : Thread() {
    var stopped: Boolean = false


    override fun run() {
        stopped = false
        var print: LuaPrinter.OnPrint? = null
        try {
            ServerSocket(LISTEN_PORT).use { server ->
                show("Debug Server started on port $LISTEN_PORT")
                val client = server.accept()
                val inputStream = DataInputStream(BufferedInputStream(client.getInputStream()))
                val outputStream = DataOutputStream(BufferedOutputStream(client.getOutputStream()))

                val buffer = StringBuffer()
                print = object : LuaPrinter.OnPrint {
                    override fun onPrint(l: Int, output: String) {
                        buffer.append(output)
                    }
                }
                LuaHelper.regPrint(print!!)
                while (!stopped) {
                    val data = inputStream.readUTF()
                    Vog.d(this, "run  ----> $data")
//                    luaHelper.safeEvalLua("require 'bridges'\n$data")
                    post(data)
                    outputStream.writeUTF(buffer.toString())
                    buffer.setLength(0)
                    outputStream.flush()
                }
                show("RemoteDebug finished！")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            show("${e.message}")
        } finally {
            LuaHelper.unRegPrint(print!!)
        }
    }

    fun post(src: String) {
        val ac = Action(src)
        ac.param = Param()
        AppBus.post(ac)
    }

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