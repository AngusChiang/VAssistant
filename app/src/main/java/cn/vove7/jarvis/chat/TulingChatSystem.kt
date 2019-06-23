package cn.vove7.jarvis.chat

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.vtp.net.GsonHelper
import cn.vove7.jarvis.BuildConfig
import cn.vove7.common.app.AppConfig
import com.google.gson.Gson

/**
 * # TulingChatSystem
 * 图灵聊天
 * @author Administrator
 * 2018/10/29
 */
class TulingChatSystem : ChatSystem {

    override fun chatWithText(s: String): ChatResult? {
        val data = RequestData.withText(s)
        val res = HttpBridge.postJson(url, Gson().toJson(data))
        if (res != null) {
            try {
                val obj = GsonHelper.fromJson<ResponseData>(res)
                val err = obj?.parseError()
                if (err != null)
                    return ChatResult(err, arrayListOf())
                if (obj?.results?.isNotEmpty() == true) {
                    return obj.getChatResult()
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
        AppConfig.chatStr ?: UserInfo.getUserId().let {
            if (BuildConfig.DEBUG || it in 1..999 || UserInfo.isVip()) "2a4a7374cf1147759d70432237593c15"
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

class ResponseData : ChatResultBuilder {
    var intent: Inten? = null
    var results: List<Results>? = null
    fun parseError(): String? {
        hashMapOf(
                Pair(5000, "无解析结果"),
                Pair(6000, "暂不支持该功能"),
                Pair(4000, "请求参数格式错误"),
                Pair(4001, "加密方式错误"),
                Pair(4002, "无功能权限"),
                Pair(4003, "该apikey没有可用请求次数，请查看[帮助>常见问题]"),
                Pair(4005, "无功能权限"),
                Pair(4007, "apikey不合法"),
                Pair(4100, "userid获取失败"),
                Pair(4200, "上传格式错误"),
                Pair(4300, "批量操作超过限制"),
                Pair(4400, "没有上传合法userid"),
                Pair(4500, "申请个数超过限制"),
//                Pair(4600, "输入内容为空"),
                Pair(4602, "输入文本内容超长上限150"),
                Pair(7002, "上传信息失败"),
                Pair(8008, "服务器错误"))[intent?.code ?: "0"]?.also {
            GlobalLog.err(it)
            return it
        }
        return null
    }

    override fun getWord(): String? {
        return results?.find {
            it.resultType == Results.RESULT_TYPE_TEXT
        }?.values?.text
    }

    override fun getResultUrls(): ArrayList<UrlItem> {
        val arr = arrayOf(Results.RESULT_TYPE_NEWS, Results.RESULT_TYPE_URL)
        results?.find { it.resultType in arr }?.apply {
            when (resultType) {
                Results.RESULT_TYPE_NEWS -> {
                    val list = arrayListOf<UrlItem>()
                    values?.news?.forEach {
                        list.add(UrlItem(it.name, it.icon, it.detailurl, it.info, it.source))
                    }
                    return list
                }
                Results.RESULT_TYPE_URL -> {
                    return arrayListOf(UrlItem("", "", values?.url ?: ""))
                }

            }
        }
        return arrayListOf()//null
    }

}


class Results {
    var groupType: Int = 0
    var resultType: String? = null
    var values: Values? = null
    var emotion: Emotion? = null

    companion object {
        const val RESULT_TYPE_TEXT = "text" //文本
        const val RESULT_TYPE_NEWS = "news" //菜谱/
        const val RESULT_TYPE_URL = "url" //股票 /
    }
}

class Emotion {
//robotEmotion
    //userEmotion
}

class Values {
    var text: String? = null
    var news: Array<New> = arrayOf()
    var url: String? = null

    class New {
        var name: String = ""
        var icon: String = ""
        var info: String? = null
        var source: String? = null
        var detailurl: String = ""
    }
}

class Inten {
    var code: Int = 0
    var intentName: String? = null
    var actionName: String? = null
}