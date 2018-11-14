package cn.vove7.jarvis.tools.baiduaip.model

import com.google.gson.annotations.SerializedName

/**
 * # TranslateResult
 *
 * @author Administrator
 * 2018/11/14
 */
class TranslateResult {
    val from: String? = null
    val to: String? = null
    @SerializedName("trans_result")
    val results: Array<Result>? = null

    val haveResult get() = results != null && results.isNotEmpty()

    class Result {
        val src: String? = null
        val dst: String? = null
    }
}
