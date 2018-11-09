package cn.vove7.jarvis.tools.baiduaip

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.GsonHelper
import cn.vove7.jarvis.tools.BaiduKey
import cn.vove7.jarvis.tools.baiduaip.model.ImageClassifyResult
import cn.vove7.vtp.log.Vog
import com.baidu.aip.imageclassify.AipImageClassify
import com.baidu.aip.nlp.AipNlp
import org.json.JSONObject

/**
 * # BaiduAipHelper
 *
 * @author Administrator
 * 2018/10/14
 */
object BaiduAipHelper {

    /**
     * 分词
     * @param text String
     * @return List<String>?
     */
    fun lexer(text: String): List<String>? {
        val client = AipNlp(BaiduKey.appId.toString(), BaiduKey.appKey, BaiduKey.sKey)

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

    /**
     * 图像识别
     * @param path String
     * @return ImageClassifyResult?
     */
    fun imageClassify(path: String): ImageClassifyResult? {
        val client = AipImageClassify(BaiduKey.appId.toString(), BaiduKey.appKey, BaiduKey.sKey)
        client.setConnectionTimeoutInMillis(5000)
        client.setSocketTimeoutInMillis(6000)
        val result = client.advancedGeneral(path, hashMapOf(Pair("baike_num", "1")))
        Vog.d(this, "imageClassify ---> $result")
        return GsonHelper.fromJson<ImageClassifyResult>(result.toString())
    }
}
