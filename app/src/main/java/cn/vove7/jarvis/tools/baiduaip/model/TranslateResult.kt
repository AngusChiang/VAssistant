package cn.vove7.jarvis.tools.baiduaip.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * # TranslateResult
 *
 * @author Administrator
 * 2018/11/14
 */
class TranslateResult {
    @Keep
    val from: String? = null
    @Keep
    val to: String? = null
    @SerializedName("trans_result")
    @Keep
    val results: Array<Result>? = null
    val haveResult get() = results != null && results.isNotEmpty()

    val transResult by lazy {
        buildString {
            results?.forEach {
                appendLine(it.dst)
            }
        }
    }

    class Result {
        @Keep
        val src: String? = null
        @Keep
        val dst: String? = null
    }
}
