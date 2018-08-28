package cn.vove7.androlua.debugserver

import android.content.Context
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionParam
import cn.vove7.common.debugserver.RemoteDebugServer

/**
 * # LuaDebugServer
 *
 * @author 17719
 * 2018/8/14
 */
class LuaDebugServer(context: Context) : RemoteDebugServer(context) {
    override fun onPostAction(src: String): String {
        val ac = Action(src,"lua")
        ac.param = ActionParam()
        AppBus.post(ac)
        return "已在手机端执行，请查看日志"
    }
}