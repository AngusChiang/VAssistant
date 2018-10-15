package cn.vove7.common.baiduaip

import com.baidu.aip.nlp.AipNlp
import org.json.JSONArray
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


    fun lexer(text: String):List<String>? {
        val  client = AipNlp(APP_ID, API_KEY, SECRET_KEY)

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(5000)

        // 调用接口
        val res = client.lexer(text, null)

        return if (res.has("error_code")) {
            System.out.println("出错" + res.getString("error_msg"))
            null
        } else {
            val list = mutableListOf<String>()
            val wordArr = res.getJSONArray("items")
            for (i in 0 until  wordArr.length()) {
                (wordArr.get(i) as JSONObject).getString("item").let { w ->
                    if (w != null && w.trim() != "") {
                        list.add(w.replace("\n",""))
                    }
                }
            }
            list
        }
        //System.out.println(res.toString(2));

    }

}