package cn.vove7.jarvis.chat

import androidx.annotation.Keep
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.jarvis.BuildConfig
import cn.vove7.vtp.net.GsonHelper
import com.google.gson.Gson

/**
 * # TulingChatSystem
 * 图灵聊天
 * @author Administrator
 * 2018/10/29
 */
class TulingChatSystem : ChatSystem {

    override suspend fun chatWithText(s: String): ChatResult? {
        val data = RequestData.withText(s)
        val res = HttpBridge.postJson(url, Gson().toJson(data))
        if (res != null) {
            try {
                val obj = GsonHelper.fromJson<ResponseData>(res)
                val err = obj?.parseError()
                if (err != null)
                    return ChatResult(err, arrayListOf())
                if (obj?.results?.isNotEmpty() == true) {
                    return obj.getChatResult()?.let { autoProcess(it) }
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

    private suspend fun autoProcess(result: ChatResult): ChatResult? {
        if (result.word == "请问你想查询哪个城市") {
            val li = SystemBridge.locationInfo()
            if (li != null) {
                return chatWithText(li.city)
            }
        }
        return result
    }

    companion object {
        const val url = "http://openapi.tuling123.com/openapi/api/v2"
    }
}

@Keep
class RequestData {
    /**
     * 输入类型:0-文本(默认)、1-图片、2-音频
     */
    @Keep
    var reqType: Int = 0

    @Keep
    var perception = Perception()

    @Keep
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

@Keep
class UserI {
    @Keep
    val apiKey = {
        AppConfig.chatStr ?: UserInfo.getUserId().let {
            if (BuildConfig.DEBUG || it in 1..999 || UserInfo.isVip()) "0c830192e98e4b29977c281304c6ba3d"
            else "89658c8238384de39cee1ecb529cb81f"
        }
    }.invoke()
    @Keep
    val userId = UserInfo.getEmail()?.let {
        it.substring(0, it.indexOf('@'))
    } ?: "guest"
}

@Keep
class Perception {
    @Keep
    var inputText: InputText? = null

    @Keep
    var inputImage: InputImage? = null

    @Keep
    var selfInfo: SelfInfo? = null
    fun setText(s: String) {
        if (inputText == null)
            inputText = InputText(s)
    }
}

@Keep
class SelfInfo {
    @Keep
    var location: Location? = null
}

@Keep
class Location {
    @Keep
    var city: String? = null

    @Keep
    var province: String? = null

    @Keep
    var street: String? = null
}

@Keep
class InputText(
        @Keep
        var text: String
)

@Keep
class InputImage(
        @Keep
        var url: String
)

@Keep
class ResponseData : ChatResultBuilder {
    @Keep
    var intent: Inten? = null

    @Keep
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
                Pair(8008, "服务器错误"))[intent?.code ?: 0]?.also {
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


@Keep
class Results {
    @Keep
    var groupType: Int = 0

    @Keep
    var resultType: String? = null

    @Keep
    var values: Values? = null

    @Keep
    var emotion: Emotion? = null

    companion object {
        const val RESULT_TYPE_TEXT = "text" //文本
        const val RESULT_TYPE_NEWS = "news" //菜谱/
        const val RESULT_TYPE_URL = "url" //股票 /
    }
}

@Keep
class Emotion {
//robotEmotion
    //userEmotion
}

@Keep
class Values {
    @Keep
    var text: String? = null

    @Keep
    var news: Array<New> = arrayOf()

    @Keep
    var url: String? = null

    @Keep
    class New {
        @Keep
        var name: String = ""

        @Keep
        var icon: String = ""

        @Keep
        var info: String? = null

        @Keep
        var source: String? = null

        @Keep
        var detailurl: String = ""
    }
}

@Keep
class Inten {
    @Keep
    var code: Int = 0

    @Keep
    var intentName: String? = null

    @Keep
    var actionName: String? = null
}