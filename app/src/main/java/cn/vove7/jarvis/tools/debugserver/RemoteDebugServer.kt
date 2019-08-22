package cn.vove7.jarvis.tools.debugserver

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cn.vove7.androlua.LuaHelper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.startActivityOnNewTask
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.NewInstActivity
import cn.vove7.jarvis.services.MainService
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.vtp.log.Vog
import com.google.gson.Gson
import java.io.*
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


object RemoteDebugServer : Runnable {

    private var server: ServerSocket? = null

    private var clients: HashMap<Socket, PrintWriter>? = null

    val hasClient :Boolean get() = clients?.isNotEmpty() == true

    var stopped: Boolean = true
    private const val LISTEN_PORT = 1527
    private var thread: Thread? = null
    private var commandServer: CommandServer? = null
    var handler: Handler? = null

    @Synchronized
    fun start() {
        commandServer?.stop()
        commandServer = CommandServer().also {
            it.start()
        }
        if (!stopped && thread?.isAlive == true) {
            Vog.d("start ---> thread is Alive")
            return
        }
        thread = thread {
            handler = Handler(Looper.getMainLooper())
            startAutoSleep()
            run()
        }

    }

    @Synchronized
    fun stop() {
        stopped = true
        runOnPool {
            commandServer?.stop()
            commandServer = null
            server?.close()
            clients?.forEach {
                try {
                    it.key.close()
                    it.value.close()
                } catch (e: Exception) {
                }
            }
            clients?.clear()

            thread?.interrupt()

            stopAutoSleep()
            server = null
            thread = null
        }
    }

    override fun run() {
        stopped = false
        clients = hashMapOf()
        try {
            server = ServerSocket(LISTEN_PORT)
        } catch (e: BindException) {//Address already in use
            GlobalApp.toastError("无线调试开启失败：端口被占用")
            stop()
            return
        }
        RhinoApi.regPrint(print)
        LuaHelper.regPrint(print)
        GlobalApp.toastInfo(R.string.text_debug_service_starting, Toast.LENGTH_LONG)
        server?.use {
            while (!stopped) {
                try {
                    val client = it.accept()//等待
                    stopAutoSleep()
                    val inputStream = BufferedReader(InputStreamReader(client.getInputStream(), "UTF-8"))
                    val o = PrintWriter(BufferedWriter(OutputStreamWriter(client.getOutputStream())), true)
                    clients?.put(client, o)
                    GlobalApp.toastInfo(String.format(GlobalApp.getString(R.string.text_establish_connection), client.inetAddress
                        ?: "null"))
                    print.onPrint(0, "与PC[${client.inetAddress}]建立连接   --来自App")
                    //type -> script -> arg
                    runOnPool {
                        try {
                            while (!stopped) {
                                val data = inputStream.readLine()
                                if (data == null) {//断开连接
//                                    onDisConnect(client)
                                    break
                                } else onPostAction(data)
                            }
                        } catch (e: Exception) {//
                            GlobalLog.err(e)
                        }
                        onDisConnect(client, o)
                    }
                } catch (e: Exception) {
                    GlobalApp.toastInfo(R.string.text_disconnect_with_debugger)
                }
            }
        }
        show("RemoteDebug finished!")
// finally {
        stopped = true
        onFinish()
//        }
    }

    private val sleepTime = if (BuildConfig.DEBUG) 100000L else 300000L //5min
    private val sleepRun = Runnable {
        Vog.d("休眠")
        stop()
    }

    fun restartSleepTimer() {
        stopAutoSleep()
        startAutoSleep()
    }

    private fun startAutoSleep() {
        Vog.d("开启自动休眠$sleepTime")
        handler?.postDelayed(sleepRun, sleepTime)
    }

    private fun stopAutoSleep() {
        Vog.d("关闭自动休眠")
        handler?.removeCallbacks(sleepRun)
    }

    private fun onFinish() {
        RhinoApi.unregPrint(print)
        LuaHelper.unRegPrint(print)
    }

    private val print = object : OnPrint {
        override fun onPrint(l: Int, output: String?) {
//            Vog.d("onPrint ---> $output")

            val end = if (output?.endsWith('\n') == true) "" else "\n"
            try {
                clients?.forEach {
                    it.value.print(output + end)
                    it.value.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                onFinish()
            }
        }
    }

    private fun onDisConnect(s: Socket, p: PrintWriter) {
        runInCatch {
            s.use {
                GlobalApp.toastInfo("与${it.inetAddress}断开连接")
            }
            clients?.remove(s)
            if (clients?.isEmpty() == true)
                startAutoSleep()
            p.close()
        }
    }

    /**
     * 解析动作
     * @param actionJson String
     */
    private fun onPostAction(actionJson: String) {
        Vog.d("onPostAction ---> $actionJson")

        runOnPool {
            val action: RemoteAction
            try {
                action = Gson().fromJson<RemoteAction>(actionJson, RemoteAction::class.java)
            } catch (e: Exception) {
                GlobalLog.err(e)
                print.onPrint(0, "发生错误${e.message}")
                return@runOnPool
            }
            when (action.action) {
                "run" -> {
                    val ac = Action()
                    ac.actionScript = action.text
                    when (action.type) {
                        "lua" -> ac.scriptType = Action.SCRIPT_TYPE_LUA
                        "javascript" -> ac.scriptType = Action.SCRIPT_TYPE_JS
                        else -> print.onPrint(0, "不支持的语言${action.type}")
                    }
                    AppBus.post(ac)
                }
                "stop" -> {
                    AppBus.post(AppBus.ACTION_STOP_EXEC)
                }
                "command" -> {//文本指令
                    val cmd = action.text
                    if (cmd == null) {
                        print.onPrint(0, "无动作")
                    } else {
                        print.onPrint(0, "执行：$cmd")
                        MainService.instance?.onParseCommand(cmd)
                    }
                }
                "copyText" -> {
                    SystemBridge.setClipText(action.text)
                    print.onPrint(0, "已复制")
                    GlobalApp.toastInfo("已复制")
                }
                else -> {
                    if (action.action.startsWith("new_inst")) {
                        if (!UserInfo.isLogin()) {
                            GlobalApp.toastError("请登录后操作")
                            return@runOnPool
                        }

                        GlobalApp.APP.apply {
                            val intent = Intent(this, NewInstActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra("remote_script", action.text)
                            intent.putExtra("remote_script_type", if ("lua" == action.type) "lua" else "js")
                            intent.putExtra("type", if (action.action == "new_inst_as_inapp") {
                                ActionNode.NODE_SCOPE_IN_APP
                            } else ActionNode.NODE_SCOPE_GLOBAL)
                            //类型
                            startActivityOnNewTask(intent)
                        }
                    } else {
                        print.onPrint(0, "未知操作${action.action}")
                    }
                }
            }
        }
    }

    private fun show(s: String) {
        Vog.d("show  ----> $s")
    }

}

class RemoteAction(
        val action: String,
        val type: String?,
        val text: String?,
        val extra: String?
)
