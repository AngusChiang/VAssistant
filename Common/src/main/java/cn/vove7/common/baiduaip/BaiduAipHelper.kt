package cn.vove7.common.baiduaip

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.utils.GsonHelper
import cn.vove7.vtp.log.Vog
import com.baidu.aip.imageclassify.AipImageClassify
import com.baidu.aip.nlp.AipNlp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

/**
 * # BaiduAipHelper
 *
 * @author Administrator
 * 2018/10/14
 */
object BaiduAipHelper {
    private val APP_ID = "14434099"
    private val API_KEY = "mfDGvqViuSk9ZuRyubRwwGhs"
    private val SECRET_KEY = "PsNGEsfU67qVoprKToLVaE1eT4bCbbML"

    /**
     * 分词
     * @param text String
     * @return List<String>?
     */
    fun lexer(text: String): List<String>? {
        val client = AipNlp(APP_ID, API_KEY, SECRET_KEY)

        try {// 可选：设置网络连接参数
            client.setConnectionTimeoutInMillis(5000)
            // 调用接口
            val res = client.lexer(text, null)

            return if (res.has("error_code")) {
                System.out.println("出错" + res.getString("error_msg"))
                null
            } else {
                val list = mutableListOf<String>()
                val wordArr = res.getJSONArray("items")
                for (i in 0 until wordArr.length()) {
                    (wordArr.get(i) as JSONObject).getString("item").let { w ->
                        if (w != null && w.trim() != "") {
                            list.add(w.replace("\n", ""))
                        }
                    }
                }
                list
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            return null
        }
        //System.out.println(res.toString(2));

    }

    fun imageClassify(path: String): ImageClassifyResult? {
        val client = AipImageClassify("10922901", "xwzlOfIIysRN7IDdcjA823ZS",
                "d9ef661698c5d8cd45978aa55e600e03")
        client.setConnectionTimeoutInMillis(5000)
        client.setSocketTimeoutInMillis(6000)
        val result = client.advancedGeneral(path, hashMapOf(Pair("baike_num", "1")))
        Vog.d(this,"imageClassify ---> $result")
        return GsonHelper.fromJson<ImageClassifyResult>(result.toString())
    }
}

/**
 * 图象识别结果
 * @property result Array<Rlt>?
 * @property errorCode String?
 * @property errorMsg String?
 * @property bestResult Rlt?
 * @property resultNum Int
 * @property logId String?
 */
class ImageClassifyResult {
    val result: Array<Rlt>? = null

    @SerializedName("error_code")
    var errorCode: String? = null
    @SerializedName("error_msg")
    var errorMsg: String? = null
    val hasErr: Boolean
        get() {
            val r = errorCode != null
            if (r) {
                GlobalLog.err("图象识别错误信息：$errorMsg")
            }
            return r
        }

    val bestResult: Rlt?
        get() {
            return if (result?.isNotEmpty() == true) {
                result[0]
            } else null
        }

    @SerializedName("result_num")
    val resultNum: Int = 0
    @SerializedName("log_id")
    val logId: String? = null

    data class Rlt(
            var score: Float? = null,
            var root: String? = null,
            var keyword: String? = null,
            @SerializedName("baike_info")
            var baikeInfo: BaiKeInfo? = null
    )

    data class BaiKeInfo(
            @SerializedName("baike_url")
            var baikeUrl: String? = null,
            @SerializedName("image_url")
            var imageUrl: String? = null,
            var description: String? = null,
            var keyword: String? = null
    )
}