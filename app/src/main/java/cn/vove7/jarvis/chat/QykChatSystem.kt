package cn.vove7.jarvis.chat

import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.netacc.NetHelper
import cn.vove7.vtp.log.Vog
import com.google.gson.Gson

/**
 * # QykChatSystem
 *
 * @author Administrator
 * 2018/10/28
 */
class QykChatSystem : ChatSystem {
    override fun chatWithText(s: String): String? {
        val data = HttpBridge.get("http://api.qingyunke.com/api.php?key=free&appid=0&msg=$s")
        return if (data == null) {
            Vog.d(this, "chatWithText ---> 失败")
            null
        } else {
            Gson().fromJson<Map<String, String>>(data, NetHelper.MapType)["content"]?.replace("{br}"," ")
        }
    }
}