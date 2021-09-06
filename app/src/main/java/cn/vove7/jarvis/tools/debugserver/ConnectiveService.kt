package cn.vove7.jarvis.tools.debugserver

import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.helper.ConnectiveNsdHelper
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.jarvis.services.MainService
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.vtp.log.Vog
import fi.iki.elonen.NanoHTTPD
import java.net.URLDecoder
import java.util.*
import kotlin.math.abs

/**
 * # VServer
 *
 * @author Vove
 * 2020/1/7
 */
private const val CONNECTIVE_SERVICE_PORT = 8001

class ConnectiveService : NanoHTTPD(CONNECTIVE_SERVICE_PORT) {

    companion object {
        const val TYPE_COMMAND = "command"
        const val TYPE_SCRIPT = "script"
        const val NSD_TYPE = "_vassistant_cs._tcp"

        private var ins: ConnectiveService? = null

        @Synchronized
        fun start() {
            if (ins == null) {
                ins = ConnectiveService().also {
                    it.start()
                    GlobalLog.log("互联服务启动")
                    GlobalApp.toastInfo("互联服务启动")
                }
            }
        }

        @Synchronized
        fun stop() {
            if (ins != null) {
                GlobalLog.log("互联服务关闭")
                GlobalApp.toastInfo("互联服务关闭")
            }
            MdnsManager.unexportService(CONNECTIVE_SERVICE_PORT)
            ins?.stop()
            ins = null
        }
    }

    override fun start(timeout: Int, daemon: Boolean) {
        super.start(timeout, daemon)
        MdnsManager.exportService(CONNECTIVE_SERVICE_PORT, NSD_TYPE,
                ConnectiveNsdHelper.selfDeviceName)
    }

    override fun serve(session: IHTTPSession?): Response {
        session ?: return super.serve(session)

        //ping from lan device
        if (session.uri == "/") {
            return newFixedLengthResponse("""/|\""" + SystemBridge.deviceName)
        }

        val refuse by lazy { newFixedLengthResponse(Response.Status.BAD_REQUEST, "", "") }

        if (session.uri != "/api") {
            return refuse
        }

        GlobalLog.log(buildString {
            appendLine("远程指令请求：")
            session.headers.forEach {
                appendLine(it)
            }
            appendLine(session.queryParameterString)
        })
        val sign = session.headers["sign"] ?: return refuse
        val ts = session.headers["ts"] ?: return refuse

        //验证时间
        if (abs(ts.toLong() - QuantumClock.currentTimeMillis) > 10000) {
            return refuse
        }

        return if (sign == SecureHelper.signData(session.queryParameterString, ts.toLong(), "cssccssc")) {
            val params = session.queryParameterString.queryString2Map()
            parse(session, params)
            newFixedLengthResponse("OK")
        } else {
            Vog.e("BusServer 参数验证失败")
            refuse
        }
    }

    fun parse(session: IHTTPSession, params: Map<String, String>) {
        val action = params["action"] ?: return
        when (action) {
            TYPE_COMMAND -> {
                val cmd = params["command"] ?: return
                GlobalApp.toastInfo("执行来自：${params["from"]}的指令\n$cmd")
                MainService.parseCommand(cmd)
            }
            TYPE_SCRIPT -> {
                val script = params["script"] ?: return
                val type = params["type"] ?: Action.SCRIPT_TYPE_LUA
                GlobalApp.toastInfo("执行来自：${params["from"]}的脚本")
                GlobalLog.log("执行远程脚本[$type]：${session.remoteIpAddress}\n$script")
                AppBus.post(Action(script, type))
            }
        }
    }

}

fun String.queryString2Map(): Map<String, String> {
    return URLDecoder.decode(this, "utf-8")
            .split('&')
            .let { list ->
                mutableMapOf<String, String>().apply {
                    list.forEach {
                        val (k, v) = it.split('=')
                        put(k, v)
                    }
                }
            }
}