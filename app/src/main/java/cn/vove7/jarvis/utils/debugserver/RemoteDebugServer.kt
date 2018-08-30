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
import cn.vove7.vtp.toast.Voast
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class RemoteDebugServer : Runnable {
    lateinit var server: ServerSocket
    lateinit var client: Socket
    var outputStream: DataOutputStream? = null

    companion object {
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
            thread?.interrupt()
        }

    }

    override fun run() {
        stopped = false
        server = ServerSocket(LISTEN_PORT)
        RhinoApi.regPrint(print)
        LuaHelper.regPrint(print)
        show("Debug Server started on port $LISTEN_PORT")
        stopped = false
        server.use {
            while (!stopped) {
                client = server.accept()//等待
                val inputStream = DataInputStream(BufferedInputStream(client.getInputStream()))
                outputStream = DataOutputStream(BufferedOutputStream(client.getOutputStream()))
                Voast.with(GlobalApp.APP).top().show( String.format(GlobalApp.getString(R.string.text_establish_connection),client.inetAddress))
                print.onPrint(0, "连接成功 ${client.inetAddress}")
                //type -> script -> arg
                thread {
                    try {
                        val pThread = Thread.currentThread()
                        while (!pThread.isInterrupted) {
                            val type = inputStream.readLine()
//                            Vog.d(this, "run ---> $type")
                            val arg = inputStream.readLine()
//                            Vog.d(this, "run ---> $arg")
                            val data = inputStream.readLine()
//                            Vog.d(this, "run ---> $data")
                            if (type == null) {
                                Vog.d(this,"run ---> disconnect: ${client.inetAddress}")
                                pThread.interrupt()
                            } else
                                onPostAction(type, data.replace("##", "\n"), arg)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        show("err: ${e.message}")
                    }
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

            val end=if(output.endsWith('\n')) "" else "\n"
            outputStream?.writeUTF("((($output$end)))")//C# 粘包问题 消息格式(((msg)))
            outputStream?.flush()
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