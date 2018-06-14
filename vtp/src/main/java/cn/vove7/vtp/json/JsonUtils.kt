package cn.vove7.vtp.json

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonUtils {
    /**
     * @return json -> List<T>
     */
    fun <T> json2List(json: String): List<T> {
        return Gson().fromJson(json, object : TypeToken<List<T>>() {}.type)
    }

    /**
     * @return json -> Map<K, V>
     */
    fun <K, V> json2Map(json: String): Map<K, V> {
        return Gson().fromJson(json, object : TypeToken<Map<K, V>>() {}.type)
    }

    /**
     * @return Any -> json
     */
    fun toJson(o: Any): String {
        return Gson().toJson(o)
    }
}
