package cn.vove7.jarvis.tools.baiduaip

import cn.vove7.common.app.GlobalLog
import cn.vove7.common.bridges.HttpBridge
import cn.vove7.common.netacc.tool.SecureHelper
import cn.vove7.common.utils.GsonHelper
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.jarvis.tools.BaiduKey
import cn.vove7.jarvis.tools.baiduaip.model.ImageClassifyResult
import cn.vove7.jarvis.tools.baiduaip.model.Point
import cn.vove7.jarvis.tools.baiduaip.model.TextOcrItem
import cn.vove7.jarvis.tools.baiduaip.model.TranslateResult
import cn.vove7.vtp.log.Vog
import com.baidu.aip.imageclassify.AipImageClassify
import com.baidu.aip.nlp.AipNlp
import com.baidu.aip.ocr.AipOcr
import org.json.JSONObject
import java.util.*

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

    /**
     * aid:20180807000192209
     * sk:O5HfygUvr_56vS6lwuo4
     * @param text String
     * @return String?
     */
    fun translate(text: String, from: String = "auto", to: String = "auto"): TranslateResult? {
        val appid = "20180807000192209"
        val sk = "O5HfygUvr_56vS6lwuo4"
        val salt = Random().nextInt()

        val jData = HttpBridge.post("https://fanyi-api.baidu.com/api/trans/vip/translate", hashMapOf(
                Pair("q", text)
                , Pair("from", from)
                , Pair("to", to)
                , Pair("appid", appid)
                , Pair("salt", salt)
                , Pair("sign", SecureHelper.MD5(appid + text + salt + sk).toLowerCase())
        ))
        println(jData)
        return try {
            GsonHelper.fromJson<TranslateResult>(jData)
        } catch (e: Exception) {
            GlobalLog.err(e.message, "bah97")
            null
        }
    }

    /**
     * 图片ocr
     */
    @Throws
    fun ocr(imgPath: String): ArrayList<TextOcrItem> {
        val ocrStr = AppConfig.textOcrStr
        val baiduOcr = if (ocrStr?.isBlank() != false) {//null or true
            AipOcr(BaiduKey.appId.toString(), BaiduKey.appKey, BaiduKey.sKey)
        } else {
            try {
                val kkk = ocrStr.split("#")
                AipOcr(kkk[0], kkk[1], kkk[2])
            } catch (e: Exception) {
                GlobalLog.err(e)
                throw Exception("文字识别参数设置错误")
            }
        }


        val options = HashMap<String, String>()
        options["vertexes_location"] = "true"
        options["recognize_granularity"] = "big"

        val obj = baiduOcr.general(imgPath, options)

        if (obj.has("error_code")) {//出错
            val errMsg = "文字OCR错误：" + obj.getString("error_code") + obj.getString("error_msg")
            GlobalLog.err(errMsg)
            throw Exception(errMsg)
        }
        val ocrResult = arrayListOf<TextOcrItem>()

        val words = obj.getJSONArray("words_result")
        for (i in 0 until words.length()) {
            val wordObj = words.getJSONObject(i)

            val text = wordObj.getString("words")
            val list = mutableListOf<Point>()
            wordObj.getJSONArray("vertexes_location").apply {
                for (j in 0 until length()) {
                    getJSONObject(j).also {
                        list.add(Point(it.getInt("x"), it.getInt("y")))
                    }
                }
            }
            ocrResult.add(TextOcrItem(text, list))
        }
        return ocrResult
    }

}
