package cn.vove7.common.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

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
        })
    }

    fun toJson(model: Any): String {
        return builder.create().toJson(model)
    }
}