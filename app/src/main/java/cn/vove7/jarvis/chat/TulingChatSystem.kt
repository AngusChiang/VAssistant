package cn.vove7.jarvis.chat

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.utils.GsonHelper
import com.google.gson.Gson

/**
 * # TulingChatSystem
 *
 * @author Administrator
 * 2018/10/29
 */
class TulingChatSystem : ChatSystem {

    override fun chatWithText(s: String): String? {
        val data = RequestData.withText(s)
        val res = HttpBridge.postJson(url, Gson().toJson(data))
        if (res != null) {
            try {
                val obj = GsonHelper.fromJson<ResponseData>(res)
                if (obj?.parseError() == false)
                    return null
                if (obj?.results?.isNotEmpty() == true) {
                    return obj.results!![0].values?.text
                }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalLog.err(e.message)
                return null
            }


        }
        return null
    }

    companion object {
        const val url = "http://openapi.tuling123.com/openapi/api/v2"
    }
}


class RequestData {
    /**
     * 输入类型:0-文本(默认)、1-图片、2-音频
     */
    var reqType: Int = 0
    var perception = Perception()
    var userInfo = UserI()

    companion object {
        fun withText(s: String): RequestData {
            val r = RequestData()
            r.reqType = 0
            r.perception.setText(s)
            return r
        }
    }
}

class UserI {
    val apiKey = {
        UserInfo.getUserId().let {
            if (it in 1..999) "2a4a7374cf1147759d70432237593c15"
            else "5f9469a7021e463eb098a14026d380ba"
        }
    }.invoke()
    val userId = UserInfo.getEmail()?.let {
        it.substring(0, it.indexOf('@'))
    } ?: "guest"
}

class Perception {
    var inputText: InputText? = null
    var inputImage: InputImage? = null
    var selfInfo: SelfInfo? = null
    fun setText(s: String) {
        if (inputText == null)
            inputText = InputText(s)
    }
}

class SelfInfo {
    var location: Location? = null
}

class Location {
    var city: String? = null
    var province: String? = null
    var street: String? = null
}

class InputText(var text: String)
class InputImage(var url: String)

class ResponseData {
    var intent: Inten? = null
    var results: List<Results>? = null
    fun parseError(): Boolean {
        hashMapOf(
                Pair(5000, "无解析结果"),
                Pair(6000, "暂不支持该功能"),
                Pair(4000, "请求参数格式错误"),
                Pair(4001, "加密方式错误"),
                Pair(4002, "无功能权限"),
                Pair(4003, "该apikey没有可用请求次数"),
                Pair(4005, "无功能权限"),
                Pair(4007, "apikey不合法"),
                Pair(4100, "userid获取失败"),
                Pair(4200, "上传格式错误"),
                Pair(4300, "批量操作超过限制"),
                Pair(4400, "没有上传合法userid"),
                Pair(4500, "userid申请个数超过限制"),
//                Pair(4600, "输入内容为空"),
                Pair(4602, "输入文本内容超长上限150"),
                Pair(7002, "上传信息失败"),
                Pair(8008, "服务器错误"))[intent?.code ?: "0"]?.also {
            GlobalLog.err(it)
            return false
        }
        return true
    }
}


class Results {
    var groupType: Int = 0
    var resultType: String? = null
    var values: Values? = null
    var emotion: Emotion? = null
}

class Emotion {
//robotEmotion
    //userEmotion
}

class Values {
    var text: String? = null
}

class Inten {
    var code: Int = 0
    var intentName: String? = null
    var actionName: String? = null
}