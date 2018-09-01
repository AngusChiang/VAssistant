package cn.vove7.jarvis.utils.debugserver

import cn.vove7.androlua.LuaHelper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionParam
import cn.vove7.common.executor.OnPrint
import cn.vove7.jarvis.R
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.vtp.log.Vog
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread

class RemoteDebugServer : Runnable {

    companion object {
        var server: ServerSocket? = null
        var outputStream: DataOutputStream? = null
        var client: Socket? = null
        var stopped: Boolean = false
        private const val LISTEN_PORT = 3333
        var thread: Thread? = null

        fun start() {
            if (!stopped && thread?.isAlive == true) {
                Vog.d(this, "start ---> thread is Alive")
                return
            }
            thread = thread { RemoteDebugServer().run() }
        }

        fun stop() {
            stopped = true
            server?.close()
            client?.close()
            outputStream?.close()
            thread?.interrupt()

        }
    }

    override fun run() {
        stopped = false
        server = ServerSocket(LISTEN_PORT)
        RhinoApi.regPrint(print)
        LuaHelper.regPrint(print)
        GlobalApp.toastShort("Debug Server started on port $LISTEN_PORT")
        server.use {
            while (!stopped) {
                try {
                    client = server!!.accept()//等待
                    val inputStream = BufferedReader(InputStreamReader(client!!.getInputStream(), "UTF-8"))
                    outputStream = DataOutputStream(BufferedOutputStream(client!!.getOutputStream()))
                    GlobalApp.toastShort(String.format(GlobalApp.getString(R.string.text_establish_connection), client?.inetAddress
                        ?: "none"))
                    print.onPrint(0, "连接成功 ${client!!.inetAddress}")
                    //type -> script -> arg
                    thread {
                        val pThread = Thread.currentThread()
                        try {
                            while (true) {
                                val action = inputStream.readLine()
                                when (action) {
                                    "stop" -> {
                                        AppBus.post("stop execQueue")
                                    }
                                    "exec" -> {
                                        val type = inputStream.readLine()
                                        val arg = inputStream.readLine()
                                        val data = inputStream.readLine()
                                        if (type == null) {
                                            Vog.d(this, "run ---> disconnect: ${client?.inetAddress}")
                                            onFinish()
                                            pThread.interrupt()
                                        } else
                                            onPostAction(type, data.replace("##", "\n"), arg)
                                    }
                                }
                            }
                        } catch (e: Exception) {//client断开连接
                            e.printStackTrace()
                            show("err: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    GlobalApp.toastShort(GlobalApp.getString(R.string.text_disconnect_with_debugger))
                }
            }
        }
        show("RemoteDebug finished!")
// finally {
        stopped = true
        onFinish()
//        }
    }

    private fun onFinish() {
        RhinoApi.unregPrint(print)
        LuaHelper.unRegPrint(print)
    }

    private val print = object : OnPrint {
        override fun onPrint(l: Int, output: String) {
            Vog.d(this, "onPrint ---> $output")

            val end = if (output.endsWith('\n')) "" else "\n"
            try {
                outputStream?.writeUTF("((($output$end)))")//C# 粘包问题 消息格式(((msg)))
                outputStream?.flush()
            } catch (e: Exception) {
                onFinish()
            }
        }
    }

    private fun onPostAction(type: String, src: String, arg: String): String {

        Vog.d(this, "run type ---> $type")
        Vog.d(this, "run arg ---> $arg")
        Vog.d(this, "run src ---->\n $src")

        val ac = Action()
        ac.actionScript = src
        ac.param = ActionParam()
        ac.param.value = arg
        when (type) {
            "Lua" -> {
                ac.scriptType = Action.SCRIPT_TYPE_LUA
            }
            "JavaScript" -> {
                ac.scriptType = Action.SCRIPT_TYPE_JS
            }
        }
        AppBus.post(ac)
        return "ok"
    }

    private fun show(s: String) {

        Vog.d(this, "show  ----> $s")

    }

}