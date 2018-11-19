package cn.vove7.jarvis.tools.baiduaip.model

import cn.vassistant.plugininterface.app.GlobalLog
import com.google.gson.annotations.SerializedName

/**
 * # ImageClassifyResult
 * 图象识别结果
 * @property result Array<Rlt>?
 * @property errorCode String?
 * @property errorMsg String?
 * @property bestResult Rlt?
 * @property resultNum Int
 * @property logId String?
 *
 * @author Administrator
 * 2018/11/8
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