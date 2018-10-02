package cn.vove7.common.utils

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

    fun toJson(model: Any?): String {
        if (model == null) return ""
        return builder.create().toJson(model)
    }

    fun <T> fromJsonObj(s: String?, type: Type): ResponseMessage<T>? {
        val bean = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create().fromJson<ResponseMessage<T>>(s, type)
        return bean
    }

}