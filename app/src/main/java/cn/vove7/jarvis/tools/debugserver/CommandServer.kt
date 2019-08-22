package cn.vove7.jarvis.tools.debugserver

import cn.vove7.common.bridges.SystemBridge
import cn.vove7.jarvis.services.MainService
import cn.vove7.vtp.log.Vog
import fi.iki.elonen.NanoHTTPD

/**
 * # CommandServer
 *
 * @author Vove
 * 2019/8/16
 */
class CommandServer : NanoHTTPD(8000) {

    override fun serve(session: IHTTPSession?): Response {
        session ?: return super.serve(session)

        if (!RemoteDebugServer.hasClient) {
            RemoteDebugServer.restartSleepTimer()
        }
        //自动decode
        return session.uri.substring(1).trim().let {
            when {
                it == ("favicon.ico") -> super.serve(session)
                it.isNotEmpty() -> {
                    Vog.d("serve $it")
                    MainService.parseCommand(it, true)
                    newFixedLengthResponse("已执行\n")
                }
                else -> newFixedLengthResponse("请输入指令, 示例：\n${SystemBridge.getLocalIpAddress()}:8000/你好\n")
            }
        }
    }

}