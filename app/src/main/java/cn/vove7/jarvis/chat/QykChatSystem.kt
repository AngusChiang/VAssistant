package cn.vove7.jarvis.chat

//import cn.vove7.common.app.GlobalLog
//import cn.vove7.common.bridges.HttpBridge
//import cn.vove7.vtp.net.GsonHelper

/**
 * # QykChatSystem
 *
 * @author Administrator
 * 2018/10/28
 */
//@Deprecated("服务不可用")
//class QykChatSystem : ChatSystem {
//    override fun chatWithText(s: String): ChatResult? {
//        val data = HttpBridge.get("http://api.qingyunke.com/api.php?key=free&appid=0&msg=$s")
//        return if (data == null) {
////            Vog.d("chatWithText ---> 失败")
//            null
//        } else {
//            try {
//                ChatResult(GsonHelper.fromJson<Map<String, String>>(data)!!["content"]?.replace("{br}", " ")
//                    ?: "", arrayListOf())
//            } catch (e: Exception) {
//                GlobalLog.err(e)
//                null
//            }
//        }
//    }
//}