package cn.vove7.common.utils

import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.ResponseMessage
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import java.lang.reflect.Type

/**
 * # GsonHelper
 *
 * @author Administrator
 * 2018/9/19
 */
object GsonHelper {
    private val builder = GsonBuilder()

    init {
        builder.serializeSpecialFloatingPointValues()
        builder.addSerializationExclusionStrategy(object : ExclusionStrategy {

            override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                val expose = fieldAttributes.getAnnotation(Expose::class.java)
                return expose != null && !expose.serialize
            }

            override fun shouldSkipClass(aClass: Class<*>): Boolean = false
        }).addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                val expose = fieldAttributes.getAnnotation(Expose::class.java)
                return expose != null && !expose.deserialize
            }

            override fun shouldSkipClass(aClass: Class<*>): Boolean = false
        }).disableHtmlEscaping()
    }

    fun toJson(model: Any?, pretty:Boolean=false): String {
        if (model == null) return ""
        val b= builder
        if(pretty) b.setPrettyPrinting()
        return b.create().toJson(model)
    }

    /**
     * 服务返回Json解析
     * @param s String?
     * @param type Type
     * @return ResponseMessage<T>?
     */
    fun <T> fromJsonObj(s: String?, type: Type): ResponseMessage<T>? {
        val bean = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create().fromJson<ResponseMessage<T>>(s, type)
        return bean
    }

    /**
     * 服务返回Json解析
     * @param s String?
     * @param type Type
     * @return ResponseMessage<T>?
     */
    inline fun <reified T> fromResponseJson(s: String?): ResponseMessage<T>? {
        val type = NetHelper.getType<ResponseMessage<T>>()
        val bean = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create().fromJson<ResponseMessage<T>>(s, type)
        return bean
    }

    inline fun <reified T> fromJson(s: String?): T? {
        val type = NetHelper.getType<T>()
        val bean = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create().fromJson<T>(s, type)
        return bean
    }

}