package cn.vove7.jarvis.speech.recognition.util

import com.baidu.speech.asr.SpeechConstant

import java.util.HashMap

/**
 * 根据语言， 搜索模型或输入法模型和是否需要语义功能 生成对应PID
 *
 *
 * Created by fujiayi on 2017/8/19.
 */

class PidBuilder {

    private var language = PUDONGHUA

    private var model = SEARCH

    private var supportNlu = false

    private var emptyParams = false

    /**
     * 从 params中 根据_language _model和_nlu_online这三个临时参数，输出PID并加入到params中。
     *
     * @param params
     * @return
     */
    fun addPidInfo(params: MutableMap<String, Any>): Map<String, Any> {

            val lang = params["_language"]
            params.remove("_language")
            val onlineModel = params["_model"]
            params.remove("_model")
            val nlu = params["_nlu_online"]
            params.remove("_nlu_online")
            if (lang == null && onlineModel == null && nlu == null) {
                emptyParams = true
            } else {
                if (lang != null) {
                    language(lang.toString())
                }
                if (onlineModel != null) {
                    model(onlineModel.toString())
                }
                if (nlu != null) {
                    supportNlu(java.lang.Boolean.valueOf(nlu.toString()))
                }
            }
            val pid = toPId()
            if (pid > 0) {
                params[SpeechConstant.PID] = pid
            }


        return params
    }

    /**
     * 返回负数的话，即没有生成PID成功，请不要设置
     *
     * @return -1 没对应pid
     * -2  用于使用 PidBuilder(params), params为空或者没有相关选项用于确定PID
     */
    fun toPId(): Int {
        if (emptyParams) { // 用于 PidBuilder(Map<String,Object> params)
            return -2
        }

        val key = language + "_" + model + "_" + if (supportNlu) 1 else 0
        val result = map!![key]
        return result ?: -1
    }

    /**
     * 语言
     *
     * @param language
     * @return
     */
    fun language(language: String): PidBuilder {
        this.language = language
        emptyParams = false
        return this
    }

    /**
     * 输入法模型或者搜索模型
     *
     * @param model
     * @return
     */
    fun model(model: String): PidBuilder {
        this.model = model
        emptyParams = false
        return this
    }

    /**
     * 是否开启语义识别
     *
     * @param supportNlu
     * @return
     */
    fun supportNlu(supportNlu: Boolean): PidBuilder {
        this.supportNlu = supportNlu
        emptyParams = false
        return this
    }

    companion object {

        val PUDONGHUA = "cmn-Hans-CN"

        val ENGLISH = "en-GB"

        val SICHUAN = "sichuan-Hans-CN"

        val YUEYU = "yue-Hans-CN"

        val SEARCH = "search"

        val INPUT = "input"

        val FAR = "far"

        private var map: MutableMap<String, Int>? = null

        init {
            map = HashMap(16)
            createPid(1536, PUDONGHUA, SEARCH, false)
            createPid(15361, PUDONGHUA, SEARCH, true)
            createPid(1537, PUDONGHUA, INPUT, false)
            createPid(1736, ENGLISH, SEARCH, false)
            createPid(1737, ENGLISH, INPUT, false)
            createPid(1636, YUEYU, SEARCH, false)
            createPid(1637, YUEYU, INPUT, false)
            createPid(1836, SICHUAN, SEARCH, false)
            createPid(1837, SICHUAN, INPUT, false)
            createPid(1936, PUDONGHUA, FAR, false)
            createPid(1936, PUDONGHUA, FAR, true)
        }

        fun create(): PidBuilder {
            return PidBuilder()
        }

        private fun createPid(pid: Int, lang: String, onlineModel: String, nlu: Boolean) {
            val key = lang + "_" + onlineModel + "_" + if (nlu) 1 else 0
            map!![key] = pid
        }
    }
}
