package cn.vove7.jarvis.tools.debugserver

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
        session.uri.substring(1).also {
            Vog.d("serve $it")
                MainService.parseCommand(it, true)
                return newFixedLengthResponse("已执行")
        }
        return newFixedLengthResponse("请输入指令\nip:8000/你好")
    }

}